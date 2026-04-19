#!/bin/sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)

DOCKERHUB_NAMESPACE=${DOCKERHUB_NAMESPACE:-}
IMAGE_TAG=${IMAGE_TAG:-latest}
PUSH_LATEST=${PUSH_LATEST:-false}
SKIP_MAVEN=${SKIP_MAVEN:-false}

if [ -z "$DOCKERHUB_NAMESPACE" ]; then
    echo "ERROR: 请先设置 DOCKERHUB_NAMESPACE，例如：DOCKERHUB_NAMESPACE=myname"
    exit 1
fi

if [ "$SKIP_MAVEN" != "true" ]; then
    echo "[1/3] 打包后端 JAR ..."
    (
        cd "$ROOT_DIR"
        mvn clean package -DskipTests
    )
else
    echo "[1/3] 跳过 Maven 打包（SKIP_MAVEN=true）"
fi

build_and_push() {
    serviceName=$1
    moduleDir=$2
    localImage="jeepay/$serviceName:$IMAGE_TAG"
    remoteImage="$DOCKERHUB_NAMESPACE/$serviceName:$IMAGE_TAG"

    echo "[2/3] 构建镜像: $remoteImage"
    docker build -t "$localImage" -t "$remoteImage" "$ROOT_DIR/$moduleDir"

    echo "[3/3] 推送镜像: $remoteImage"
    docker push "$remoteImage"

    if [ "$PUSH_LATEST" = "true" ] && [ "$IMAGE_TAG" != "latest" ]; then
        latestLocalImage="jeepay/$serviceName:latest"
        latestRemoteImage="$DOCKERHUB_NAMESPACE/$serviceName:latest"
        echo "[3/3] 追加 latest 标签: $latestRemoteImage"
        docker tag "$localImage" "$latestLocalImage"
        docker tag "$localImage" "$latestRemoteImage"
        docker push "$latestRemoteImage"
    fi
}

build_and_push jeepay-manager jeepay-manager
build_and_push jeepay-merchant jeepay-merchant
build_and_push jeepay-payment jeepay-payment

echo "完成。"
