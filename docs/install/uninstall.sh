#! /bin/sh
#exec 2>>build.log  ##编译过程打印到日志文件中
## 项目卸载   .Power by terrfly

#引入config
. ./config.sh

# 第0步：提示信息
echo "请确认全部卸载,  是否继续？"
echo "(Confirm uninstallation, do you want to continue?)"
echo " [yes/no] ?"
read useryes
if [ -z "$useryes" ] || [ $useryes != 'yes' ]
then
	echo 'good bye'
	exit 0
fi


docker stop nginx118 && docker rm nginx118
docker stop jeepaymanager && docker rm jeepaymanager
docker stop jeepaymerchant && docker rm jeepaymerchant
docker stop jeepaypayment && docker rm jeepaypayment

docker stop mysql8 && docker rm mysql8

docker stop redis6 && docker rm redis6

docker stop activemq5 && docker rm activemq5

docker network rm jeepay-net

rm -rf $rootDir
