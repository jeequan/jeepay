# 镜像发布（维护者流程）

终端用户**不需要**看本文。仓库默认镜像已指向华为云 SWR 公开仓库，装即用。

本文给计全团队 / 二开团队说明如何把自己构建的 jeepay 镜像推到镜像仓库。

## 发布到 Docker Hub

```bash
docker login

DOCKERHUB_NAMESPACE=<你的 Docker Hub 用户名或组织> \
IMAGE_TAG=3.2.0 \
PUSH_LATEST=true \
bash docker/publish-dockerhub.sh
```

参数：

- `DOCKERHUB_NAMESPACE`：必填。
- `IMAGE_TAG`：版本号，默认 `latest`。
- `PUSH_LATEST=true`：非 latest tag 时额外补推 latest。
- `SKIP_MAVEN=true`：JAR 已提前打包时跳过 `mvn package`。

推送产物：

- `<namespace>/jeepay-manager:<tag>`
- `<namespace>/jeepay-merchant:<tag>`
- `<namespace>/jeepay-payment:<tag>`

## 发布到华为云 SWR（amd64 + arm64 多架构）

```bash
docker login swr.cn-south-1.myhuaweicloud.com

SWR_NAMESPACE=jeepay \
IMAGE_TAG=3.2.0 \
PUSH_LATEST=true \
bash docker/publish-swr.sh
```

- 分别构建并推 `amd64` / `arm64` 临时 tag，再合成统一多架构 tag。
- 用户最终使用：`swr.cn-south-1.myhuaweicloud.com/<namespace>/jeepay-manager:3.2.0`。
- 改区域：`SWR_REGISTRY=swr.cn-north-4.myhuaweicloud.com`。

## 同步第三方基础镜像到 SWR

让国内用户完全免 Docker Hub 访问：

```bash
SWR_NAMESPACE=jeepay bash docker/sync-swr-thirdparty.sh
```

默认同步：

- `mysql:8` / `mysql:8.0.25`
- `redis:6.2.14`
- `apache/rocketmq:5.3.1`
- `nginx:1.18.0`
- `eclipse-temurin:17-jre`

同步完成后仓库默认部署配置会优先使用 `swr.cn-south-1.myhuaweicloud.com/jeepay/*`。
