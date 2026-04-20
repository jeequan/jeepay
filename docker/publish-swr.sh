#!/bin/sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)

SWR_REGISTRY=${SWR_REGISTRY:-swr.cn-south-1.myhuaweicloud.com}
SWR_NAMESPACE=${SWR_NAMESPACE:-}
IMAGE_TAG=${IMAGE_TAG:-latest}
PUSH_LATEST=${PUSH_LATEST:-false}
SKIP_MAVEN=${SKIP_MAVEN:-false}
PLATFORMS=${PLATFORMS:-linux/amd64 linux/arm64}

if [ -z "$SWR_NAMESPACE" ]; then
    echo "ERROR: 请先设置 SWR_NAMESPACE，例如：SWR_NAMESPACE=jeepay"
    exit 1
fi

if [ "$SKIP_MAVEN" != "true" ]; then
    echo "[1/4] 打包后端 JAR ..."
    (
        cd "$ROOT_DIR"
        mvn clean package -DskipTests
    )
else
    echo "[1/4] 跳过 Maven 打包（SKIP_MAVEN=true）"
fi

build_and_push_platform_image() {
    serviceName=$1
    moduleDir=$2
    platform=$3
    archSuffix=$(echo "$platform" | awk -F/ '{print $2}')
    localImage="jeepay/$serviceName:${IMAGE_TAG}-${archSuffix}"
    remoteImage="$SWR_REGISTRY/$SWR_NAMESPACE/$serviceName:${IMAGE_TAG}-${archSuffix}"

    echo "[2/4] 构建镜像: $localImage ($platform)"
    docker build --platform "$platform" --provenance=false \
        -t "$localImage" "$ROOT_DIR/$moduleDir"

    echo "[3/4] 推送镜像: $remoteImage"
    docker tag "$localImage" "$remoteImage"
    docker push --platform "$platform" "$remoteImage"
}

publish_manifest() {
    serviceName=$1
    targetTag=$2
    manifestRef="$SWR_REGISTRY/$SWR_NAMESPACE/$serviceName:$targetTag"

    echo "[4/4] 创建并推送多架构清单: $manifestRef"
    docker manifest rm "$manifestRef" >/dev/null 2>&1 || true

    manifestImages=""
    for platform in $PLATFORMS; do
        archSuffix=$(echo "$platform" | awk -F/ '{print $2}')
        platformImage="$SWR_REGISTRY/$SWR_NAMESPACE/$serviceName:${IMAGE_TAG}-${archSuffix}"
        manifestImages="$manifestImages $platformImage"
    done

    # shellcheck disable=SC2086
    docker manifest create "$manifestRef" $manifestImages

    for platform in $PLATFORMS; do
        arch=$(echo "$platform" | awk -F/ '{print $2}')
        os=$(echo "$platform" | awk -F/ '{print $1}')
        platformImage="$SWR_REGISTRY/$SWR_NAMESPACE/$serviceName:${IMAGE_TAG}-${arch}"
        docker manifest annotate "$manifestRef" "$platformImage" --os "$os" --arch "$arch"
    done

    docker manifest push "$manifestRef"
}

build_and_publish_service() {
    serviceName=$1
    moduleDir=$2

    for platform in $PLATFORMS; do
        build_and_push_platform_image "$serviceName" "$moduleDir" "$platform"
    done

    publish_manifest "$serviceName" "$IMAGE_TAG"

    if [ "$PUSH_LATEST" = "true" ] && [ "$IMAGE_TAG" != "latest" ]; then
        publish_manifest "$serviceName" "latest"
    fi
}

build_and_publish_service jeepay-manager jeepay-manager
build_and_publish_service jeepay-merchant jeepay-merchant
build_and_publish_service jeepay-payment jeepay-payment

echo "完成。"
