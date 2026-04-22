#! /bin/sh
## 一键启动jeepay服务，包含mysqlDB/RocketMQ/redis/javaservice/nginx   .Power by terrfly
## 本脚本顶层严格遵循 POSIX sh，保证 sh install.sh / bash install.sh / dash install.sh
## 都能跑通。内部个别依赖 bash 的 TCP 探针（/dev/tcp）通过 `docker exec <ctr> bash -c`
## 显式调用容器内的 bash，与外层 shell 无关。

# ---------------------------------------------------------------------------
# 命令行参数解析（放在 root 校验之前，便于非 root 也能 --help）
# ---------------------------------------------------------------------------
AUTO_YES=0
show_usage() {
    cat <<EOF
用法: bash install.sh [选项]

选项:
  -y, --yes    自动确认所有 yes/no 提示，适合自动化 / CI 场景。
  -h, --help   打印本帮助并退出。

环境变量（可选，见 docs/deploy/shell.md "高级覆盖项"）：
  rootDir / mysql_pwd                        根目录与 MySQL 密码
  mysqlHostPort / redisHostPort              宿主端口覆盖
  mysqlImage / redisImage / rocketmqImage    镜像覆盖
  nginxImage / managerImage / merchantImage  镜像覆盖
  paymentImage                               镜像覆盖
  rocketmqPlatform                           RocketMQ 平台（默认 linux/amd64）
  jeepayRef                                  源码 tag，默认 V3.2.8
  uiRelease                                  前端静态资源 release（默认 V3.0.0）
EOF
}
while [ $# -gt 0 ]; do
    case "$1" in
        -y|--yes) AUTO_YES=1; shift ;;
        -h|--help) show_usage; exit 0 ;;
        *) echo "未知参数：$1"; show_usage; exit 1 ;;
    esac
done

if [ "$(id -u)" != '0' ]; then
    echo 'ERROR： 请使用root用户安装（Please install using root user）！'
    exit 1
fi

# 把全部输出同时落盘到日志文件，失败时可回看完整上下文。
# 选 /tmp 是因为此时 rootDir 还没解析，/tmp 几乎肯定可写。
# 用 POSIX 的 "mkfifo + tee 后台" 代替 bash 的 `exec > >(tee ...)` 进程替换，
# 保证本脚本可被 dash / bash / bash-as-sh 直接执行，不挑 /bin/sh。
INSTALL_LOG_FILE="/tmp/jeepay-install-$(date +%Y%m%d-%H%M%S)-$$.log"
INSTALL_LOG_FIFO="$INSTALL_LOG_FILE.pipe"
mkfifo "$INSTALL_LOG_FIFO"
tee -a "$INSTALL_LOG_FILE" < "$INSTALL_LOG_FIFO" &
exec > "$INSTALL_LOG_FIFO" 2>&1
rm -f "$INSTALL_LOG_FIFO"  # 已打开的 FD 继续使用，名字可以直接删除
echo "安装日志将同时写入：$INSTALL_LOG_FILE"
[ "$AUTO_YES" = "1" ] && echo "已启用 --yes 自动确认模式"

# 统一的 yes/no 交互封装：--yes 模式下跳过并打印提示，保持日志上下文完整
confirm_yes() {
    promptMsg=$1
    if [ -n "$promptMsg" ]; then
        echo "$promptMsg"
    fi
    echo " [yes/no] ?"
    if [ "$AUTO_YES" = "1" ]; then
        echo "(auto-yes 自动确认)"
        useryes=yes
    else
        read useryes
    fi
}

# ---------------------------------------------------------------------------
# 跨发行版的依赖安装辅助：检测包管理器（apt/dnf/yum/apk），对 wget/curl/git/
# docker 做自动安装并校验，无法自动安装时给出明确错误。
# ---------------------------------------------------------------------------
detect_pkg_mgr() {
    if command -v apt-get >/dev/null 2>&1; then
        echo "apt"
    elif command -v dnf >/dev/null 2>&1; then
        echo "dnf"
    elif command -v yum >/dev/null 2>&1; then
        echo "yum"
    elif command -v apk >/dev/null 2>&1; then
        echo "apk"
    else
        echo "unknown"
    fi
}

PKG_MGR=$(detect_pkg_mgr)
APT_UPDATED=0

pkg_install() {
    case "$PKG_MGR" in
        apt)
            if [ "$APT_UPDATED" -eq 0 ]; then
                apt-get update -y >/dev/null 2>&1 || true
                APT_UPDATED=1
            fi
            DEBIAN_FRONTEND=noninteractive apt-get install -y "$@"
            ;;
        dnf) dnf install -y "$@" ;;
        yum) yum install -y "$@" ;;
        apk) apk add --no-cache "$@" ;;
        *)
            echo "ERROR: 未识别的包管理器（非 apt/dnf/yum/apk），请手动安装：$*"
            return 1
            ;;
    esac
}

ensure_cmd() {
    cmdName=$1
    pkgName=${2:-$cmdName}
    if command -v "$cmdName" >/dev/null 2>&1; then
        return 0
    fi
    echo "install $pkgName..."
    if ! pkg_install "$pkgName"; then
        echo "ERROR: 自动安装 $pkgName 失败（包管理器：$PKG_MGR），请手动安装后重试。"
        exit 1
    fi
    if ! command -v "$cmdName" >/dev/null 2>&1; then
        echo "ERROR: 已尝试安装 $pkgName 但仍找不到 $cmdName，请排查后重试。"
        exit 1
    fi
}

# 拷贝 / 写入目标文件后的校验：
#   - require_ok $? "描述"                        检查上一条命令的返回值
#   - assert_regular_file "$path" "描述"          校验路径确为普通文件（不是目录 / 不存在）
# 避免某些 cp 失败后脚本继续运行，再后面的 docker run -v 把不存在的路径
# 自动创建为空目录，容器拿到空配置静默错乱。
require_ok() {
    rc=$1
    shift
    if [ "$rc" -ne 0 ]; then
        echo "ERROR: 步骤失败（exit=$rc）：$*"
        exit 1
    fi
}
assert_regular_file() {
    filePath=$1
    fileLabel=$2
    if [ -f "$filePath" ]; then
        return 0
    fi
    if [ -d "$filePath" ]; then
        echo "ERROR: $fileLabel 现在是目录而不是文件：$filePath"
        echo "       典型原因：之前一次 docker run 在该路径不存在时，把它自动创建为了目录。"
        echo "       请执行：rm -rf \"$filePath\"，然后重跑 install.sh。"
    else
        echo "ERROR: $fileLabel 不存在：$filePath"
        echo "       检查源码目录 \$rootDir/sources/jeepay 是否完整（比如 git clone 是否完成、分支是否正确）。"
    fi
    exit 1
}

ensure_docker() {
    if command -v docker >/dev/null 2>&1; then
        return 0
    fi
    echo "install docker..."
    case "$PKG_MGR" in
        apt)
            pkg_install docker.io
            ;;
        dnf|yum)
            # 添加阿里云的 docker-ce 源（仅对 RHEL 系）
            $PKG_MGR install -y yum-utils >/dev/null 2>&1 || true
            if command -v yum-config-manager >/dev/null 2>&1; then
                yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo >/dev/null 2>&1 || true
            fi
            $PKG_MGR makecache >/dev/null 2>&1 || true
            pkg_install docker-ce
            ;;
        apk)
            pkg_install docker
            command -v rc-update >/dev/null 2>&1 && rc-update add docker default 2>/dev/null || true
            command -v service >/dev/null 2>&1 && service docker start 2>/dev/null || true
            ;;
        *)
            echo "ERROR: 未识别的系统，无法自动安装 docker。"
            echo "       请参考 https://docs.docker.com/engine/install/ 手动安装后重试。"
            exit 1
            ;;
    esac
    if ! command -v docker >/dev/null 2>&1; then
        echo "ERROR: docker 自动安装失败，请手动安装后重试。"
        exit 1
    fi
    # 启动 docker daemon（systemd 系统）
    if command -v systemctl >/dev/null 2>&1; then
        systemctl start docker 2>/dev/null || true
        systemctl enable docker 2>/dev/null || true
    fi
    # 校验 daemon 可用
    if ! docker info >/dev/null 2>&1; then
        echo "ERROR: docker 已安装但 daemon 未能启动。请执行以下命令手动启动后重试："
        echo "       systemctl start docker   # systemd 系统"
        echo "       service docker start     # 非 systemd 系统"
        exit 1
    fi
}

if [ "$PKG_MGR" = "unknown" ]; then
    echo "WARN: 未识别到支持的包管理器（apt/dnf/yum/apk），将跳过依赖自动安装；"
    echo "      请确保 wget / curl / git / docker 已手动安装。"
fi

# 预置 wget / curl，用于下载 config.sh / html.tar.gz 与部署自检
ensure_cmd wget
ensure_cmd curl

# 第0步：提示信息
confirm_yes "请确认当前是全新服务器安装,  是否继续？
(Please confirm if it is a brand new server installation, do you want to continue?)"
if [ -z "$useryes" ] || [ "$useryes" != 'yes' ]
then
	echo 'good bye'
	exit 0
fi

# 检查 配置文件是否存在
if ! [ -f "./config.sh" ]; then
    echo '下载默认配置文件。'
    wget -O config.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/config.sh
fi

#引入config
chmod 777 ./config.sh
. ./config.sh

if [ -d $rootDir ]; then
    echo "ERROR： $rootDir 根文件夹存在，请手动删除后重试！ "
    exit 0
fi

# ---------------------------------------------------------------------------
# 宿主端口占用预检
#   - MySQL / Redis：被占时脚本自动选空闲端口，打印 INFO 提示；用户显式
#     指定的 mysqlHostPort / redisHostPort 被占则直接报错（尊重用户）。
#   - RocketMQ / Nginx：与容器内通信耦合，不支持自动换端口，被占时退出。
# jeepay 各服务通过 jeepay-net 内部的 mysql:3306 / redis:6379 通信，
# 宿主 host port 变化不影响业务。
# ---------------------------------------------------------------------------
port_in_use() {
    portNum=$1
    if command -v ss >/dev/null 2>&1; then
        ss -lnt 2>/dev/null | awk '{print $4}' | grep -qE "[:.]${portNum}$"
    elif command -v netstat >/dev/null 2>&1; then
        netstat -lnt 2>/dev/null | awk '{print $4}' | grep -qE "[:.]${portNum}$"
    else
        return 1
    fi
}

print_port_owner() {
    portNum=$1
    if command -v ss >/dev/null 2>&1; then
        ss -lntp 2>/dev/null | grep -E "[:.]${portNum}[[:space:]]" | head -3
    elif command -v lsof >/dev/null 2>&1; then
        lsof -i :"$portNum" -sTCP:LISTEN 2>/dev/null | tail -n +2 | head -3
    elif command -v netstat >/dev/null 2>&1; then
        netstat -lntp 2>/dev/null | grep -E "[:.]${portNum}[[:space:]]" | head -3
    fi
}

# 依次尝试：基准端口 → 基准 + 10000 → 再逐个 +1；返回首个空闲端口，找不到返回空串。
pick_host_port() {
    basePort=$1
    if ! port_in_use "$basePort"; then
        echo "$basePort"
        return 0
    fi
    candidate=$((basePort + 10000))
    while [ "$candidate" -le 65000 ]; do
        if ! port_in_use "$candidate"; then
            echo "$candidate"
            return 0
        fi
        candidate=$((candidate + 1))
    done
    echo ""
    return 1
}

# 先识别 mysqlHostPort / redisHostPort 是否由用户显式指定，再决定是否自动换
mysqlHostPortSource="auto"
redisHostPortSource="auto"
[ -n "$mysqlHostPort" ] && mysqlHostPortSource="user"
[ -n "$redisHostPort" ] && redisHostPortSource="user"
mysqlHostPort=${mysqlHostPort:-3306}
redisHostPort=${redisHostPort:-6379}

resolve_host_port() {
    portLabel=$1
    portVar=$2       # 形如 "mysqlHostPort"
    portSource=$3    # "auto" 或 "user"
    portValue=$4     # 当前候选端口
    if ! port_in_use "$portValue"; then
        return 0
    fi
    if [ "$portSource" = "user" ]; then
        echo "ERROR: 您在 config.sh / 环境变量中指定的 $portVar=$portValue ($portLabel) 已被占用："
        print_port_owner "$portValue" | sed 's/^/      /'
        exit 1
    fi
    # 自动选
    newPort=$(pick_host_port "$portValue")
    if [ -z "$newPort" ]; then
        echo "ERROR: 无法为 $portLabel 找到空闲的宿主端口，请手动设置 $portVar。"
        exit 1
    fi
    echo "INFO: 宿主端口 $portValue 已被占，$portLabel 自动改用 $newPort（仅影响宿主机外部访问，不影响 jeepay 内部通信）。"
    eval "$portVar=$newPort"
}

echo "检查宿主端口占用情况..."
resolve_host_port "MySQL" mysqlHostPort "$mysqlHostPortSource" "$mysqlHostPort"
resolve_host_port "Redis" redisHostPort "$redisHostPortSource" "$redisHostPort"

# RocketMQ / Nginx 的端口不支持自动换，被占就退出
portConflict=0
check_port() {
    portLabel=$1
    portNum=$2
    if port_in_use "$portNum"; then
        echo "  - 端口 $portNum ($portLabel) 已被占用："
        print_port_owner "$portNum" | sed 's/^/      /'
        portConflict=1
    fi
}
check_port "RocketMQ NameServer"          9876
check_port "RocketMQ Broker 10909"        10909
check_port "RocketMQ Broker 10911"        10911
check_port "RocketMQ Broker 10912"        10912
check_port "Nginx -> payment"             19216
check_port "Nginx -> manager"             19217
check_port "Nginx -> merchant"            19218

if [ "$portConflict" -eq 1 ]; then
    echo
    echo "ERROR： 上述端口与容器内通信耦合，暂不支持自动换端口。请先释放占用进程后重跑脚本。"
    exit 1
fi

# 第0步：提示信息
confirm_yes "检查配置信息是否正确（配置内容在 config.sh文件）：
【项目根目录的地址】： $rootDir
【mysql root密码】： $mysql_pwd"
if [ -z "$useryes" ] || [ "$useryes" != 'yes' ]
then
    echo 'good bye'
    exit 0
fi

# 检查 git、docker（基于 detect_pkg_mgr 结果自动适配 apt/dnf/yum/apk）
ensure_cmd git
ensure_docker


# 第1步：创建基本目录
echo "[1] 创建项目根目录($rootDir).... "
mkdir $rootDir/nginx -p
mkdir $rootDir/nginx/conf -p
mkdir $rootDir/nginx/conf.d -p
mkdir $rootDir/nginx/html -p
mkdir $rootDir/nginx/logs -p

mkdir $rootDir/mysql -p
mkdir $rootDir/mysql/config -p
mkdir $rootDir/mysql/log -p
mkdir $rootDir/mysql/data -p
mkdir $rootDir/mysql/mysql-files -p

mkdir $rootDir/rocketmq -p
mkdir $rootDir/rocketmq/namesrv/logs -p
mkdir $rootDir/rocketmq/broker/logs -p
mkdir $rootDir/rocketmq/broker/store -p
mkdir $rootDir/rocketmq/broker/conf -p

mkdir $rootDir/redis -p
mkdir $rootDir/redis/config -p
mkdir $rootDir/redis/data -p

mkdir $rootDir/service/configs -p
mkdir $rootDir/service/uploads -p
mkdir $rootDir/service/logs -p

mkdir $rootDir/sources -p
echo "[1] Done. "

mysqlImage=${mysqlImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8.0.25}
redisImage=${redisImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/redis:6.2.14}
rocketmqImage=${rocketmqImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/rocketmq:5.3.1}
nginxImage=${nginxImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/nginx:1.18.0}
managerImage=${managerImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-manager:3.2.0}
merchantImage=${merchantImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-merchant:3.2.0}
paymentImage=${paymentImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-payment:3.2.0}

# rocketmq 上游仅发布 linux/amd64 镜像，ARM64 宿主需借助 qemu/binfmt 仿真。
# 其他镜像（mysql/redis/nginx）已具备多架构 manifest，不再强制 platform。
rocketmqPlatform=${rocketmqPlatform:-linux/amd64}

# 源码 ref：默认锁到 V3.2.8 release tag，保证 git clone 下来的 SQL / broker.conf.template /
# nginx.conf / conf/* 与业务镜像（3.2.0 业务镜像与 V3.2.x 配置兼容）契合，不受 master 后续演进影响。
# 如需用最新 master 或其他 tag，安装前导出 jeepayRef=xxx 覆盖即可（透传给 git clone --branch）。
jeepayRef=${jeepayRef:-V3.2.8}

# 第2步：拉取项目源代码  || 拉取脚本文件
echo "[2] 拉取项目源代码文件 (ref=$jeepayRef).... "
cd $rootDir/sources
git clone --branch "$jeepayRef" --depth 1 https://gitee.com/jeequan/jeepay.git
echo "[2] Done. "

#源码中install.sh文件目录
sourcesInstallPath=$rootDir/sources/jeepay/docs/install

# 将本次安装的"生效配置"快照写回 sources 目录下的 config.sh，
# 使 uninstall.sh（按文档从该目录运行）读到的是用户实际使用的 rootDir，
# 而不是仓库里 rootDir="/jeepayhomes" 的默认模板。
cat > "$sourcesInstallPath/config.sh" <<EOF
#! /bin/sh
# 由 install.sh 在安装阶段自动生成，供 uninstall.sh 读取。请勿手工编辑。
rootDir="$rootDir"
mysql_pwd="$mysql_pwd"
mysqlImage="$mysqlImage"
redisImage="$redisImage"
rocketmqImage="$rocketmqImage"
rocketmqPlatform="$rocketmqPlatform"
nginxImage="$nginxImage"
managerImage="$managerImage"
merchantImage="$merchantImage"
paymentImage="$paymentImage"
jeepayRef="$jeepayRef"
mysqlHostPort="$mysqlHostPort"
redisHostPort="$redisHostPort"
currentPath=\`pwd\`
EOF

# 创建一个 bridge网络
docker network create jeepay-net

# 第3步：下载mysql官方镜像 & 启动
echo "[3] 下载并启动mysql容器.... "
echo "提示：  如下载进度缓慢，建议配置阿里云或其他镜像加速服务。  "

# 将Mysql的配置文件复制到对应的映射目录下
cd $sourcesInstallPath && cp ./include/my.cnf $rootDir/mysql/config/my.cnf
require_ok $? "复制 MySQL 配置 my.cnf"
assert_regular_file "$rootDir/mysql/config/my.cnf" "MySQL 配置 my.cnf"

# 镜像启动
docker run -p $mysqlHostPort:3306 --name mysql8 --network=jeepay-net  \
--network-alias mysql \
--restart=always --privileged=true \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/mysql/log:/var/log/mysql  \
-v $rootDir/mysql/data:/var/lib/mysql  \
-v $rootDir/mysql/mysql-files:/var/lib/mysql-files \
-v $rootDir/mysql/config:/etc/mysql/conf.d  \
-e MYSQL_ROOT_PASSWORD=$mysql_pwd \
-id $mysqlImage

# 避免未启动完成或出现错误： ERROR 2002 (HY000): Can't connect to local MySQL server through socket '/var/run/mysqld/mysqld.sock'
# echo "等待重启mysql容器....... "

while true
do
 # docker exec mysql8 mysql | grep '(using password: NO)'  使用这个判断不行， 若没有启动会报错
 docker logs mysql8 > /tmp/installmysql.log
 logContent=$(cat /tmp/installmysql.log | grep 'MySQL init process done')
 if [ ! -n "$logContent" ];then
    docker logs mysql8
    echo "[3] 等待启动mysql容器....... "
    sleep 30
  else
    echo "[3] mysql启动完成 $logContent"
    sleep 10
    break
  fi
done

echo "[3] 初始化数据导入 ...... "
# 创建数据库  && 导入数据
echo "CREATE DATABASE jeepaydb DEFAULT CHARACTER SET utf8mb4" | docker exec -i mysql8 mysql -uroot -p$mysql_pwd
assert_regular_file "$rootDir/sources/jeepay/docs/sql/init.sql" "MySQL 初始化脚本 init.sql"
docker exec -i mysql8 sh -c "mysql -uroot -p$mysql_pwd --default-character-set=utf8mb4  jeepaydb" < $rootDir/sources/jeepay/docs/sql/init.sql
require_ok $? "导入 MySQL 初始化脚本 init.sql"

echo "[3] Done. "

# 第4步：下载redis官方镜像 & 启动
echo "[4] 下载并启动redis容器.... "

# 将配置文件复制到对应的映射目录下
cd $sourcesInstallPath && cp ./include/redis.conf $rootDir/redis/config/redis.conf
require_ok $? "复制 Redis 配置 redis.conf"
assert_regular_file "$rootDir/redis/config/redis.conf" "Redis 配置 redis.conf"
chmod 644 $rootDir/redis/config/redis.conf

# 镜像启动
docker run -p $redisHostPort:6379 --name redis6 --network=jeepay-net  \
--network-alias redis \
--restart=always --privileged=true \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/redis/config/redis.conf:/etc/redis/redis.conf \
-v $rootDir/redis/data:/data \
-d $redisImage redis-server /etc/redis/redis.conf


echo "[4] Done. "


# 第5步：下载并启动 RocketMQ 容器
echo "[5] 下载并启动 RocketMQ 容器.... "

# 拷贝 broker 配置文件
cd $sourcesInstallPath && cp ./../../docker/rocketmq/broker/conf/broker.conf.template $rootDir/rocketmq/broker/conf/broker.conf.template
require_ok $? "复制 RocketMQ broker 配置模板 broker.conf.template"
assert_regular_file "$rootDir/rocketmq/broker/conf/broker.conf.template" "RocketMQ broker 配置模板"

# brokerIP1 默认写容器名 rocketmq-broker：jeepay 所有业务（manager / merchant /
# payment）都在 jeepay-net 内，通过 Docker 内置 DNS 按容器名解析最稳，不受宿主
# 多网卡 / 多 Docker bridge 影响（老版本用 hostname -I 首项在某些宿主上会挑到
# 172.16.0.x 这种仅 Docker 内部可达的地址，导致业务容器连 broker 失败）。
# 如有外部 RocketMQ 客户端需求（非 jeepay-net 内），安装前 export brokerIP1=真实IP。
brokerIP1=${brokerIP1:-rocketmq-broker}

sed "s/%BROKER_IP%/$brokerIP1/g" \
  $rootDir/rocketmq/broker/conf/broker.conf.template > $rootDir/rocketmq/broker/conf/broker.conf

echo "[5] RocketMQ brokerIP1 设为: $brokerIP1"

# 启动 NameServer
docker run -d --name rocketmq-namesrv --network=jeepay-net \
--platform=$rocketmqPlatform \
-p 9876:9876 \
--restart=always \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/rocketmq/namesrv/logs:/home/rocketmq/logs \
-e JAVA_OPT_EXT="-Xms256m -Xmx256m -Xmn128m" \
$rocketmqImage sh mqnamesrv

# 启动 Broker
mkdir -p $rootDir/rocketmq/broker/store/config
cat > $rootDir/rocketmq/broker/store/config/topics.json <<'EOF'
{"dataVersion":{"counter":0,"timestamp":0},"topicConfigTable":{}}
EOF
cat > $rootDir/rocketmq/broker/store/config/topicQueueMapping.json <<'EOF'
{"dataVersion":{"counter":0,"timestamp":0},"topicQueueMappingInfoMap":{}}
EOF

docker run -d --name rocketmq-broker --network=jeepay-net \
--platform=$rocketmqPlatform \
-p 10909:10909 -p 10911:10911 -p 10912:10912 \
--restart=always \
-u 0:0 \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/rocketmq/broker/logs:/home/rocketmq/logs \
-v $rootDir/rocketmq/broker/store:/home/rocketmq/store \
-v $rootDir/rocketmq/broker/conf/broker.conf:/home/rocketmq/rocketmq-5.3.1/conf/broker.conf:ro \
-e JAVA_OPT_EXT="-Xms512m -Xmx512m -Xmn256m" \
-e NAMESRV_ADDR="rocketmq-namesrv:9876" \
$rocketmqImage sh mqbroker -n rocketmq-namesrv:9876 -c /home/rocketmq/rocketmq-5.3.1/conf/broker.conf

rocketmqWaitCount=0
while true
do
  docker logs rocketmq-broker > /tmp/installrocketmq.log 2>&1
  logContent=$(cat /tmp/installrocketmq.log | grep 'boot success')
  failContent=$(cat /tmp/installrocketmq.log | grep -E 'NullPointerException|ERROR|Exception')

  if [ -n "$logContent" ]; then
    echo "[5] RocketMQ 启动完成 $logContent"
    # boot success 只代表进程起来了；还要等 broker 向 nameserver 完成注册 + TCP 端口真正
    # 开始 accept 连接后才算"可用"。老版本 sleep 5 有时会让 [6] 启动的 Java 应用第一次
    # send 撞上尚未稳定的状态，客户端缓存坏连接，直到业务重启才恢复。这里轮询 broker
    # 容器自身的 localhost:10911，确认 TCP 就绪。
    # 在 broker 容器内用 bash /dev/tcp 探 localhost:10911。
    # 容器里 /bin/sh 可能是 dash，不支持 /dev/tcp，所以直接用 bash。
    brokerTcpWait=0
    brokerTcpReady=0
    while [ $brokerTcpWait -lt 30 ]; do
        if docker exec rocketmq-broker timeout 2 bash -c "echo > /dev/tcp/localhost/10911" >/dev/null 2>&1; then
            echo "[5] RocketMQ Broker TCP 10911 已就绪（等待 ${brokerTcpWait}s）"
            brokerTcpReady=1
            break
        fi
        sleep 2
        brokerTcpWait=$((brokerTcpWait + 2))
    done
    if [ $brokerTcpReady -eq 0 ]; then
        echo "[5] WARN: 30s 后 TCP 10911 仍未就绪，可能导致业务容器第一次 send 失败，"
        echo "         必要时安装结束后 docker restart jeepaymanager jeepaymerchant jeepaypayment 即可。"
    fi
    break
  fi

  rocketmqWaitCount=$((rocketmqWaitCount + 1))

  if [ -n "$failContent" ] && [ $rocketmqWaitCount -ge 3 ]; then
    echo "[5] ERROR: RocketMQ Broker 启动失败，请检查最近日志："
    docker logs --tail 100 rocketmq-broker
    echo "[5] 常见原因："
    echo "    1. RocketMQ 镜像架构与服务器不兼容（当前使用 $rocketmqImage）"
    echo "    2. Broker 存储目录权限异常：$rootDir/rocketmq/broker/store"
    echo "    3. broker.conf 配置或挂载失败：$rootDir/rocketmq/broker/conf/broker.conf"
    echo "    4. NameServer 未正常启动，可执行：docker logs --tail 50 rocketmq-namesrv"
    exit 1
  fi

  if [ $rocketmqWaitCount -ge 20 ]; then
    echo "[5] ERROR: RocketMQ Broker 启动超时，请检查日志："
    docker logs --tail 100 rocketmq-broker
    echo "[5] 可继续排查：docker logs rocketmq-namesrv && docker logs rocketmq-broker"
    exit 1
  fi

  docker logs --tail 20 rocketmq-broker
  echo "[5] 等待启动 RocketMQ Broker....... ($rocketmqWaitCount/20)"
  sleep 15
done

echo "[5] Done. "


# 第6步：下载并启动 java 项目

# 复制java配置文件
cd $rootDir/service/configs/ && cp -r $rootDir/sources/jeepay/conf/* .
require_ok $? "复制 service/configs/*（来源：\$rootDir/sources/jeepay/conf）"

# 把 conf 模板里的 Docker Compose 默认密码 rootroot 替换为本次安装实际使用的
# mysql_pwd，保证 manager / merchant / payment 能连上脚本初始化的 MySQL。
# 仅替换 datasource 段的密码（Redis password 为空，activemq 密码 "manager"，不受影响）。
# 同时校验 yml 为真实文件，拦截"历史遗留空目录"导致 docker run -v 挂载异常。
for svc in manager merchant payment; do
    svcYml="$rootDir/service/configs/$svc/application.yml"
    assert_regular_file "$svcYml" "jeepay-$svc 配置 application.yml"
    sed -i "s|password: rootroot|password: $mysql_pwd|g" "$svcYml"
done


echo "[6.1] 下载并启动 java 项目 [ jeepaymanager  ] .... "
# 运行 java项目
docker run -itd --name jeepaymanager --restart=always --network=jeepay-net \
-p 9217:9217 \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/service/logs:/jeepayhomes/service/logs \
-v $rootDir/service/uploads:/jeepayhomes/service/uploads \
-v $rootDir/service/configs/manager/application.yml:/jeepayhomes/service/app/application.yml \
-d $managerImage

echo "[6.2] 下载并启动 java 项目 [ jeepaymerchant  ] .... "
# 运行 java项目
docker run -itd --name jeepaymerchant --restart=always --network=jeepay-net \
-p 9218:9218 \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/service/logs:/jeepayhomes/service/logs \
-v $rootDir/service/uploads:/jeepayhomes/service/uploads \
-v $rootDir/service/configs/merchant/application.yml:/jeepayhomes/service/app/application.yml \
-d $merchantImage

echo "[6.3] 下载并启动 java 项目 [ jeepaypayment  ] .... "
# 运行 java项目
docker run -itd --name jeepaypayment --restart=always --network=jeepay-net \
-p 9216:9216 \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/service/logs:/jeepayhomes/service/logs \
-v $rootDir/service/uploads:/jeepayhomes/service/uploads \
-v $rootDir/service/configs/payment/application.yml:/jeepayhomes/service/app/application.yml \
-d $paymentImage

echo "[6] Done. "


echo "[7] 下载并启动 nginx .... "

# 前端静态资源压缩包由 jeepay-ui 项目按 release 发布；可通过 uiRelease 覆盖。
uiRelease=${uiRelease:-V3.0.0}
cd $rootDir/nginx/html
wget "https://gitee.com/jeequan/jeepay-ui/releases/download/${uiRelease}/html.tar.gz"
require_ok $? "下载前端静态资源 html.tar.gz (release=${uiRelease})"
tar -vxf html.tar.gz

# 将配置文件复制到对应的映射目录下
cd $sourcesInstallPath && cp ./include/nginx.conf $rootDir/nginx/conf/nginx.conf
require_ok $? "复制 Nginx 配置 nginx.conf"
assert_regular_file "$rootDir/nginx/conf/nginx.conf" "Nginx 配置 nginx.conf"


docker run --name nginx118  \
--restart=always --privileged=true --net=host \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/nginx/conf/nginx.conf:/etc/nginx/nginx.conf \
-v $rootDir/nginx/conf/conf.d:/etc/nginx/conf.d \
-v $rootDir/nginx/logs:/var/log/nginx \
-v $rootDir/nginx/html:/usr/share/nginx/html \
-d $nginxImage

echo "[7] Done. "

# 第8步：部署后自检
echo "[8] 部署后自检.... "

# 8.1 等待三个 jeepay 应用 healthcheck 转为 healthy，最长 3 分钟
healthTimeout=180
healthInterval=5
healthElapsed=0
allHealthy=0
while [ $healthElapsed -lt $healthTimeout ]
do
    managerStat=$(docker inspect --format '{{.State.Health.Status}}' jeepaymanager 2>/dev/null)
    merchantStat=$(docker inspect --format '{{.State.Health.Status}}' jeepaymerchant 2>/dev/null)
    paymentStat=$(docker inspect --format '{{.State.Health.Status}}' jeepaypayment 2>/dev/null)
    if [ "$managerStat" = "healthy" ] && [ "$merchantStat" = "healthy" ] && [ "$paymentStat" = "healthy" ]; then
        allHealthy=1
        break
    fi
    echo "[8] 等待 jeepay 服务就绪... manager=$managerStat merchant=$merchantStat payment=$paymentStat (${healthElapsed}/${healthTimeout}s)"
    sleep $healthInterval
    healthElapsed=$((healthElapsed + healthInterval))
done

if [ $allHealthy -eq 1 ]; then
    echo "[8] jeepay 应用容器全部 healthy"
else
    echo "[8] WARN: 超过 ${healthTimeout}s 仍有容器未进入 healthy，可执行 docker logs jeepaymanager/jeepaymerchant/jeepaypayment 查看详情"
fi

# 8.2 探测对外暴露的三个 HTTP 端口；未返回状态码即视为未响应
probeEndpoint() {
    endpointLabel=$1
    endpointPort=$2
    endpointCode=$(curl -s -o /dev/null -w '%{http_code}' -m 5 http://127.0.0.1:$endpointPort/ 2>/dev/null)
    if [ -n "$endpointCode" ] && [ "$endpointCode" != "000" ]; then
        echo "[8] $endpointLabel (http://127.0.0.1:$endpointPort) -> HTTP $endpointCode"
    else
        echo "[8] WARN: $endpointLabel (http://127.0.0.1:$endpointPort) 未响应"
    fi
}
probeEndpoint "支付网关" 19216
probeEndpoint "运营平台" 19217
probeEndpoint "商户平台" 19218

# 8.3 DB + Redis 连通性探测：调用运营平台图形验证码接口，后端会向 Redis 写入
# 验证码 token。如果 MySQL / Redis 容器别名解析失败或密码不对，这里会 5xx。
vercodeCode=$(curl -s -o /dev/null -w '%{http_code}' -m 10 "http://127.0.0.1:19217/api/anon/auth/vercode?t=$(date +%s)" 2>/dev/null)
if [ "$vercodeCode" = "200" ]; then
    echo "[8] 运营平台图形验证码接口 OK（MySQL / Redis 连通正常）"
else
    echo "[8] WARN: 图形验证码接口 HTTP $vercodeCode，登录页可能拿不到验证码。"
    echo "[8]        典型原因："
    echo "[8]         1) 容器别名解析：application.yml 中 host 写的是 mysql / redis，"
    echo "[8]            若对应容器未设置 --network-alias，会报 UnknownHostException。"
    echo "[8]         2) 密码错配：application.yml 里 password 与 MySQL 实际密码不一致。"
    echo "[8]        查看日志：docker logs --tail 50 jeepaymanager"
fi

# 8.4 RocketMQ 连通性探针：从 jeepaymanager 容器直接 TCP 探 rocketmq-broker:10911。
# Spring Boot RocketMQ producer 是懒加载 + 连接级缓存，如果业务首次 send 撞上 broker
# 尚未完全稳定，会缓存坏连接直到业务容器重启。这里主动探一次，不通就提示用户操作。
if docker exec jeepaymanager sh -c "timeout 5 bash -c 'echo > /dev/tcp/rocketmq-broker/10911'" >/dev/null 2>&1; then
    echo "[8] RocketMQ Broker TCP 连通 OK（rocketmq-broker:10911）"
else
    echo "[8] WARN: jeepaymanager 连不上 rocketmq-broker:10911。"
    echo "[8]        建议执行（清掉客户端路由缓存）："
    echo "[8]          docker restart jeepaymanager jeepaymerchant jeepaypayment"
    echo "[8]        仍不通则查："
    echo "[8]          docker logs --tail 100 rocketmq-broker | grep -i boot"
    echo "[8]          docker exec rocketmq-broker sh mqadmin clusterList -n rocketmq-namesrv:9876"
fi

echo "[8] Done. "

# 识别宿主机的内网 IP（路由到公网时使用的 source IP）与外网 IP（通过公网服务反查）
# 用于在 summary box 里同时展示"内网可访问""外网可访问"两类地址，客户复制即用。
detect_internal_ip() {
    candidateIP=$(ip route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i=="src") {print $(i+1); exit}}')
    if [ -z "$candidateIP" ]; then
        candidateIP=$(hostname -I 2>/dev/null | awk '{print $1}')
    fi
    echo "$candidateIP"
}

detect_external_ip() {
    for svc in \
        "https://ipv4.icanhazip.com" \
        "https://api.ipify.org" \
        "https://ifconfig.me" \
        "https://ipinfo.io/ip"; do
        result=$(curl -s --max-time 5 "$svc" 2>/dev/null | tr -d '\n\r ')
        if echo "$result" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$'; then
            echo "$result"
            return 0
        fi
    done
    echo ""
    return 1
}

internalIP=$(detect_internal_ip)
echo "探测外网 IP（最长 20 秒，失败自动跳过）..."
externalIP=$(detect_external_ip)

internalIPLine=${internalIP:-未识别}
if [ -n "$externalIP" ]; then
    externalNote="外网 IP ： $externalIP（公网可达，前提是 19216/19217/19218 端口已开通防火墙）"
else
    externalNote="外网 IP ： 未能自动识别（可能无公网 / curl 被限制），请根据实际情况替换下方 URL 的 IP 段"
fi

cat <<SUMMARY

============================================================
                 jeepay 安装完成
============================================================
 安装目录    ： $rootDir
 nginx 配置  ： $rootDir/nginx/conf/nginx.conf
 安装日志    ： $INSTALL_LOG_FILE

 内网 IP ： $internalIPLine
 $externalNote

 访问地址：
   运营平台 （内网）： http://$internalIPLine:19217
SUMMARY

if [ -n "$externalIP" ]; then
    echo "            （外网）： http://$externalIP:19217"
fi

cat <<SUMMARY
            账号 / 密码   ： jeepay / jeepay123

   商户平台 （内网）： http://$internalIPLine:19218
SUMMARY

if [ -n "$externalIP" ]; then
    echo "            （外网）： http://$externalIP:19218"
fi

cat <<SUMMARY
            账号        ： 登录运营平台创建，默认密码 jeepay666

   支付网关 （内网）： http://$internalIPLine:19216/cashier/index.html
SUMMARY

if [ -n "$externalIP" ]; then
    echo "            （外网）： http://$externalIP:19216/cashier/index.html"
fi

cat <<SUMMARY

 常用命令：
   docker ps                                查看 8 个容器状态
   docker logs jeepaymanager --tail 100     查看某个服务日志
   wget -O uninstall.sh https://gitee.com/jeequan/jeepay/raw/master/docs/install/uninstall.sh \\
     && bash uninstall.sh                   一键卸载（自动识别 rootDir）
============================================================
SUMMARY

echo ""
echo "Complete."
