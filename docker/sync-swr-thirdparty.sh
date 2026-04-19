#!/bin/sh

set -eu

SWR_REGISTRY=${SWR_REGISTRY:-swr.cn-south-1.myhuaweicloud.com}
SWR_NAMESPACE=${SWR_NAMESPACE:-}
DOCKER_IO_MIRROR=${DOCKER_IO_MIRROR:-swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io}

MYSQL_8_SOURCE=${MYSQL_8_SOURCE:-$DOCKER_IO_MIRROR/mysql:8}
MYSQL_8025_SOURCE=${MYSQL_8025_SOURCE:-$DOCKER_IO_MIRROR/mysql:8.0.25}
REDIS_6214_SOURCE=${REDIS_6214_SOURCE:-$DOCKER_IO_MIRROR/redis:6.2.14}
ROCKETMQ_531_SOURCE=${ROCKETMQ_531_SOURCE:-$DOCKER_IO_MIRROR/apache/rocketmq:5.3.1}
NGINX_1180_SOURCE=${NGINX_1180_SOURCE:-$DOCKER_IO_MIRROR/nginx:1.18.0}
TEMURIN_17_JRE_SOURCE=${TEMURIN_17_JRE_SOURCE:-$DOCKER_IO_MIRROR/eclipse-temurin:17-jre}

if [ -z "$SWR_NAMESPACE" ]; then
    echo "ERROR: 请先设置 SWR_NAMESPACE，例如：SWR_NAMESPACE=jeepay"
    exit 1
fi

run_with_retry() {
    attempt=1
    max_attempts=3

    while true; do
        if "$@"; then
            return 0
        fi

        if [ "$attempt" -ge "$max_attempts" ]; then
            return 1
        fi

        echo "RETRY [$attempt/$max_attempts] $*"
        attempt=$((attempt + 1))
        sleep 3
    done
}

sync_platform_image() {
    sourceImage=$1
    targetRepo=$2
    targetTag=$3
    platform=$4

    archSuffix=$(echo "$platform" | awk -F/ '{print $2}')
    remoteImage="$SWR_REGISTRY/$SWR_NAMESPACE/$targetRepo:${targetTag}-${archSuffix}"

    echo "SYNC [$platform] $sourceImage -> $remoteImage"
    run_with_retry docker buildx imagetools create --platform "$platform" -t "$remoteImage" "$sourceImage"
}

publish_manifest() {
    targetRepo=$1
    targetTag=$2
    manifestRef="$SWR_REGISTRY/$SWR_NAMESPACE/$targetRepo:$targetTag"
    shift 2

    docker manifest rm "$manifestRef" >/dev/null 2>&1 || true
    docker manifest create "$manifestRef" "$@"

    for imageRef in "$@"; do
        arch=$(echo "$imageRef" | awk -F- '{print $NF}')
        docker manifest annotate "$manifestRef" "$imageRef" --os linux --arch "$arch"
    done

    docker manifest push "$manifestRef"
}

sync_multiarch_image() {
    sourceImage=$1
    targetRepo=$2
    targetTag=$3
    amd64Image="$SWR_REGISTRY/$SWR_NAMESPACE/$targetRepo:${targetTag}-amd64"
    arm64Image="$SWR_REGISTRY/$SWR_NAMESPACE/$targetRepo:${targetTag}-arm64"

    sync_platform_image "$sourceImage" "$targetRepo" "$targetTag" linux/amd64
    sync_platform_image "$sourceImage" "$targetRepo" "$targetTag" linux/arm64
    publish_manifest "$targetRepo" "$targetTag" "$amd64Image" "$arm64Image"
}

sync_amd64_only_image() {
    sourceImage=$1
    targetRepo=$2
    targetTag=$3
    remoteImage="$SWR_REGISTRY/$SWR_NAMESPACE/$targetRepo:$targetTag"

    echo "SYNC [linux/amd64] $sourceImage -> $remoteImage"
    run_with_retry docker buildx imagetools create --platform linux/amd64 -t "$remoteImage" "$sourceImage"
}

sync_multiarch_image "$MYSQL_8_SOURCE" mysql 8
sync_amd64_only_image "$MYSQL_8025_SOURCE" mysql 8.0.25
sync_multiarch_image "$REDIS_6214_SOURCE" redis 6.2.14
sync_amd64_only_image "$ROCKETMQ_531_SOURCE" rocketmq 5.3.1
sync_multiarch_image "$NGINX_1180_SOURCE" nginx 1.18.0
sync_multiarch_image "$TEMURIN_17_JRE_SOURCE" eclipse-temurin 17-jre

echo "完成。"
