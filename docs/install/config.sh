#! /bin/sh
#exec 2>>build.log  ##编译过程打印到日志文件中
## 配置文件   .Power by terrfly

# 【项目根目录的地址】 该地址下会包含： nginx/mysql/mq/redis等文件
rootDir="/jeepayhomes"

# 【mysql密码】建议更改
mysql_pwd="jeepaydb123456"


# ----------------------------------------------------------------------------
# 【高级】可选覆盖项：默认值已在 install.sh 中设置，以下行默认注释掉。
# 只有以下场景才需要打开对应行并修改：
#   - 企业内部镜像仓库代理；
#   - 固定到特定版本或自行构建的镜像；
#   - 强制使用 Docker Hub 上游镜像（国内网络不推荐）。
# 默认均指向华为云 SWR 公开仓库（swr.cn-south-1.myhuaweicloud.com/jeepay/*），
# 由计全官方维护，公网可匿名拉取，不依赖 Docker Hub。
# ----------------------------------------------------------------------------

# mysqlImage="swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8.0.25"
# redisImage="swr.cn-south-1.myhuaweicloud.com/jeepay/redis:6.2.14"
# rocketmqImage="swr.cn-south-1.myhuaweicloud.com/jeepay/rocketmq:5.3.1"
# nginxImage="swr.cn-south-1.myhuaweicloud.com/jeepay/nginx:1.18.0"
# managerImage="swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-manager:3.2.0"
# merchantImage="swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-merchant:3.2.0"
# paymentImage="swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-payment:3.2.0"

# RocketMQ 上游仅发布 linux/amd64 镜像，ARM64 宿主需提前注册 qemu/binfmt 仿真。
# 若使用自行构建的 arm64 RocketMQ 镜像，请同时覆盖 rocketmqImage 并将下行改为 linux/arm64：
# rocketmqPlatform="linux/amd64"


#当前路径， 不要更改参数。
currentPath=`pwd`
