#! /bin/sh
#exec 2>>build.log  ##编译过程打印到日志文件中
## 项目卸载   .Power by terrfly

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

# rootDir 获取优先级：
#   1) 环境变量（export rootDir=/your/path）
#   2) 当前目录 ./config.sh（按 README 老流程从 $rootDir/sources/jeepay/docs/install 执行时）
#   3) 从跑着的 mysql8 容器数据卷反推（install.sh 把 $rootDir/mysql/data 挂到容器 /var/lib/mysql）
# 都拿不到就给明确错误，不会误删。

if [ -z "$rootDir" ] && [ -f "./config.sh" ]; then
    . ./config.sh
fi

if [ -z "$rootDir" ]; then
    if command -v docker >/dev/null 2>&1; then
        mysqlDataMount=$(docker inspect mysql8 --format '{{range .Mounts}}{{if eq .Destination "/var/lib/mysql"}}{{.Source}}{{end}}{{end}}' 2>/dev/null)
        if [ -n "$mysqlDataMount" ]; then
            # /home/jeepay/mysql/data → /home/jeepay
            candidate=$(echo "$mysqlDataMount" | sed 's|/mysql/data/*$||')
            if [ -n "$candidate" ] && [ -d "$candidate" ]; then
                rootDir="$candidate"
                echo "从 mysql8 容器卷自动识别到 rootDir=$rootDir"
            fi
        fi
    fi
fi

if [ -z "$rootDir" ]; then
    echo "ERROR： 无法自动识别 jeepay 的 rootDir。"
    echo "       可能原因：jeepay 容器已全部删除 & 当前目录没有 config.sh。"
    echo "       请手动指定后重试，例如："
    echo "         rootDir=/jeepayhomes sh uninstall.sh"
    exit 1
fi

# 第0步：提示信息
echo "======================================"
echo "即将卸载 jeepay 部署："
echo "  - 删除容器：nginx118 / jeepaymanager / jeepaymerchant / jeepaypayment /"
echo "              mysql8 / redis6 / rocketmq-broker / rocketmq-namesrv"
echo "  - 删除网络：jeepay-net"
echo "  - 递归删除目录： $rootDir"
echo "======================================"
echo "请确认全部卸载,  是否继续？ (Confirm uninstallation, do you want to continue?)"
echo " [yes/no] ?"
read useryes
if [ -z "$useryes" ] || [ "$useryes" != 'yes' ]
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
