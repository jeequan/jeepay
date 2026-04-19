#! /bin/sh
#exec 2>>build.log  ##编译过程打印到日志文件中
## 项目卸载   .Power by terrfly

#引入config
. ./config.sh

remove_container_if_exists() {
    containerName=$1
    if docker ps -a --format '{{.Names}}' | grep -qx "$containerName"; then
        docker rm -f "$containerName"
    else
        echo "skip container: $containerName"
    fi
}

remove_network_if_exists() {
    networkName=$1
    if docker network ls --format '{{.Name}}' | grep -qx "$networkName"; then
        docker network rm "$networkName"
    else
        echo "skip network: $networkName"
    fi
}

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


remove_container_if_exists nginx118
remove_container_if_exists jeepaymanager
remove_container_if_exists jeepaymerchant
remove_container_if_exists jeepaypayment
remove_container_if_exists mysql8
remove_container_if_exists redis6
remove_container_if_exists rocketmq-broker
remove_container_if_exists rocketmq-namesrv

remove_network_if_exists jeepay-net

rm -rf "$rootDir"
