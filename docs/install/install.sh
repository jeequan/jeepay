#! /bin/sh
#exec 2>>build.log  ##编译过程打印到日志文件中
## 一键启动jeepay服务，包含mysqlDB/MQ/redis/javaservice/nginx   .Power by terrfly


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

# mkdir $rootDir/activemq -p

mkdir $rootDir/redis -p
mkdir $rootDir/redis/config -p
mkdir $rootDir/redis/data -p

mkdir $rootDir/service/configs -p
mkdir $rootDir/service/uploads -p
mkdir $rootDir/service/logs -p

mkdir $rootDir/sources -p
echo "[1] Done. "

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
--restart=always --privileged=true \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/mysql/log:/var/log/mysql  \
-v $rootDir/mysql/data:/var/lib/mysql  \
-v $rootDir/mysql/mysql-files:/var/lib/mysql-files \
-v $rootDir/mysql/config:/etc/mysql/conf.d  \
-e MYSQL_ROOT_PASSWORD=$mysql_pwd \
-id mysql:8.0.25

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

# 镜像启动
docker run -p 6379:6379 --name redis6 --network=jeepay-net  \
--restart=always --privileged=true \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/redis/config/redis.conf:/etc/redis/redis.conf \
-v $rootDir/redis/data:/data \
-d redis:6.2.14 redis-server /etc/redis/redis.conf


echo "[4] Done. "


# 第5步：下载并启动activemq容器
echo "[5] 下载并启动activemq容器.... "

docker run -p 8161:8161 -p 61616:61616 --name activemq5 --network=jeepay-net \
--restart=always \
-v /etc/localtime:/etc/localtime:ro \
-d jeepay/activemq:5.15.16


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
-d jeepay/jeepay-manager

echo "[6.2] 下载并启动 java 项目 [ jeepaymerchant  ] .... "
# 运行 java项目
docker run -itd --name jeepaymerchant --restart=always --network=jeepay-net \
-p 9218:9218 \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/service/logs:/jeepayhomes/service/logs \
-v $rootDir/service/uploads:/jeepayhomes/service/uploads \
-v $rootDir/service/configs/merchant/application.yml:/jeepayhomes/service/app/application.yml \
-d jeepay/jeepay-merchant

echo "[6.3] 下载并启动 java 项目 [ jeepaypayment  ] .... "
# 运行 java项目
docker run -itd --name jeepaypayment --restart=always --network=jeepay-net \
-p 9216:9216 \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/service/logs:/jeepayhomes/service/logs \
-v $rootDir/service/uploads:/jeepayhomes/service/uploads \
-v $rootDir/service/configs/payment/application.yml:/jeepayhomes/service/app/application.yml \
-d jeepay/jeepay-payment

echo "[6] Done. "


echo "[7] 下载并启动 nginx .... "

cd $rootDir/nginx/html
wget https://gitee.com/jeequan/jeepay-ui/releases/download/v1.10.0/html.tar.gz
tar -vxf html.tar.gz

# 将配置文件复制到对应的映射目录下
cd $sourcesInstallPath && cp ./include/nginx.conf $rootDir/nginx/conf/nginx.conf


docker run --name nginx118  \
--restart=always --privileged=true --net=host \
-v /etc/localtime:/etc/localtime:ro \
-v $rootDir/nginx/conf/nginx.conf:/etc/nginx/nginx.conf \
-v $rootDir/nginx/conf/conf.d:/etc/nginx/conf.d \
-v $rootDir/nginx/logs:/var/log/nginx \
-v $rootDir/nginx/html:/usr/share/nginx/html \
-d nginx:1.18.0

echo "[7] Done. "

docker logs jeepaypayment

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


