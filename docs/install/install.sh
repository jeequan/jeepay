#! /bin/sh
#exec 2>>build.log  ##编译过程打印到日志文件中
## 一键启动jeepay服务，包含mysqlDB/RocketMQ/redis/javaservice/nginx   .Power by terrfly


if [ $UID != '0' ]; then
    echo 'ERROR： 请使用root用户安装（Please install using root user）！'
    exit 0
fi

# 第0步：提示信息
echo "请确认当前是全新服务器安装,  是否继续？"
echo "(Please confirm if it is a brand new server installation, do you want to continue?)"
echo " [yes/no] ?"
read useryes
if [ -z "$useryes" ] || [ $useryes != 'yes' ]
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

# 第0步：提示信息
echo "检查配置信息是否正确（配置内容在 config.sh文件）："
echo "【项目根目录的地址】： $rootDir"
echo "【mysql root密码】： $mysql_pwd"
echo " [yes/no] ?"
read useryes
if [ -z "$useryes" ] || [ $useryes != 'yes' ]
then
    echo 'good bye'
    exit 0
fi

# 检查 git
if ! [ -x "$(command -v git)" ]; then
    echo 'install git...'
    yum install -y git
fi

# 检查 docker环境
if ! [ -x "$(command -v docker)" ]; then
    echo 'install docker...'
    yum install  -y yum-utils && yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
    yum makecache && yum install  -y docker-ce
    systemctl restart docker && systemctl enable docker
fi


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

# 第2步：拉取项目源代码  || 拉取脚本文件
echo "[2] 拉取项目源代码文件.... "
cd $rootDir/sources
git clone https://gitee.com/jeequan/jeepay.git
# cd jeepay && git checkout -b dev origin/dev # 切换到dev分支。
echo "[2] Done. "

#源码中install.sh文件目录
sourcesInstallPath=$rootDir/sources/jeepay/docs/install

# 创建一个 bridge网络
docker network create jeepay-net

# 第3步：下载mysql官方镜像 & 启动
echo "[3] 下载并启动mysql容器.... "
echo "提示：  如下载进度缓慢，建议配置阿里云或其他镜像加速服务。  "

# 将Mysql的配置文件复制到对应的映射目录下
cd $sourcesInstallPath && cp ./include/my.cnf $rootDir/mysql/config/my.cnf

# 镜像启动
docker run -p 3306:3306 --name mysql8 --network=jeepay-net  \
--platform=linux/amd64 \
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
docker exec -i mysql8 sh -c "mysql -uroot -p$mysql_pwd --default-character-set=utf8mb4  jeepaydb" < $rootDir/sources/jeepay/docs/sql/init.sql

echo "[3] Done. "

# 第4步：下载redis官方镜像 & 启动
echo "[4] 下载并启动redis容器.... "

# 将配置文件复制到对应的映射目录下
cd $sourcesInstallPath && cp ./include/redis.conf $rootDir/redis/config/redis.conf
chmod 644 $rootDir/redis/config/redis.conf

# 镜像启动
docker run -p 6379:6379 --name redis6 --network=jeepay-net  \
--platform=linux/amd64 \
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

brokerHostIp=$(hostname -I 2>/dev/null | awk '{print $1}')
if [ -z "$brokerHostIp" ]; then
  brokerHostIp=$(ip route get 1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if ($i=="src") {print $(i+1); exit}}')
fi

if [ -z "$brokerHostIp" ]; then
  echo "[5] ERROR: 无法自动识别当前服务器 IP，无法生成 RocketMQ broker.conf"
  exit 1
fi

sed "s/%BROKER_IP%/$brokerHostIp/g" \
  $rootDir/rocketmq/broker/conf/broker.conf.template > $rootDir/rocketmq/broker/conf/broker.conf

echo "[5] RocketMQ brokerIP1 使用当前服务器IP: $brokerHostIp"

# 启动 NameServer
docker run -d --name rocketmq-namesrv --network=jeepay-net \
--platform=linux/amd64 \
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
--platform=linux/amd64 \
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
    sleep 5
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

cd $rootDir/nginx/html
wget https://gitee.com/jeequan/jeepay-ui/releases/download/v1.10.0/html.tar.gz
tar -vxf html.tar.gz

# 将配置文件复制到对应的映射目录下
cd $sourcesInstallPath && cp ./include/nginx.conf $rootDir/nginx/conf/nginx.conf


docker run --name nginx118  \
--platform=linux/amd64 \
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

echo "[8] Done. "

echo ">>>>>>> "
echo ">>>>>>> "
echo ">>>>>>>安装完成， 所有的配置文件和项目文件都在：$rootDir 文件夹中。 "
echo ">>>>>>>项目访问地址 （注意开通端口防火墙）：   "
echo ">>>>>>>运营平台： http://外网IP:19217   账号密码： jeepay/jeepay123   "
echo ">>>>>>>商户平台： http://外网IP:19218   账号密码： 需要登录运营平台手动创建。    "
echo ">>>>>>>支付网关： http://外网IP:19216   "
echo ">>>>>>>若配置域名请更改 $rootDir/nginx/conf/nginx.conf 配置文件。 "
echo ""
echo "Complete."
