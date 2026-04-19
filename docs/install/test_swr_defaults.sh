#!/bin/sh

set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname "$0")/../.." && pwd)
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
INSTALL_SCRIPT="$ROOT_DIR/docs/install/install.sh"
PAYMENT_DOCKERFILE="$ROOT_DIR/jeepay-payment/Dockerfile"
MANAGER_DOCKERFILE="$ROOT_DIR/jeepay-manager/Dockerfile"
MERCHANT_DOCKERFILE="$ROOT_DIR/jeepay-merchant/Dockerfile"
SYNC_SCRIPT="$ROOT_DIR/docker/sync-swr-thirdparty.sh"

if ! grep -q 'swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8' "$COMPOSE_FILE"; then
  echo "FAIL: docker-compose.yml does not default MySQL to SWR"
  exit 1
fi

if ! grep -q 'swr.cn-south-1.myhuaweicloud.com/jeepay/rocketmq:5.3.1' "$COMPOSE_FILE"; then
  echo "FAIL: docker-compose.yml does not default RocketMQ to SWR"
  exit 1
fi

if ! grep -q 'swr.cn-south-1.myhuaweicloud.com/jeepay/redis:6.2.14' "$COMPOSE_FILE"; then
  echo "FAIL: docker-compose.yml does not default Redis to SWR"
  exit 1
fi

for service in payment manager merchant; do
  if ! awk "
    \$1 == \"$service:\" { in_service = 1; next }
    in_service && /^[^[:space:]]/ { in_service = 0 }
    in_service && /healthcheck:/ { found = 1 }
    END { exit found ? 0 : 1 }
  " "$COMPOSE_FILE"; then
    echo "FAIL: docker-compose.yml service '$service' is missing a healthcheck"
    exit 1
  fi
done

if ! grep -q 'swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8.0.25' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not default MySQL to SWR"
  exit 1
fi

if ! grep -q 'mysqlImage=\${mysqlImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/mysql:8.0.25}' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not allow overriding mysqlImage"
  exit 1
fi

if ! grep -q 'swr.cn-south-1.myhuaweicloud.com/jeepay/redis:6.2.14' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not default Redis to SWR"
  exit 1
fi

if ! grep -q 'chmod 644 \$rootDir/redis/config/redis.conf' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not fix redis.conf permissions for container startup"
  exit 1
fi

if ! grep -q 'swr.cn-south-1.myhuaweicloud.com/jeepay/rocketmq:5.3.1' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not default RocketMQ to SWR"
  exit 1
fi

if ! grep -q 'nginxImage=\${nginxImage:-swr.cn-south-1.myhuaweicloud.com/jeepay/nginx:1.18.0}' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not allow overriding nginxImage"
  exit 1
fi

for app in manager merchant payment; do
  if ! grep -q "swr.cn-south-1.myhuaweicloud.com/jeepay/jeepay-$app:3.2.0" "$INSTALL_SCRIPT"; then
    echo "FAIL: install.sh does not default jeepay-$app to SWR 3.2.0"
    exit 1
  fi
done

if ! grep -q 'swr.cn-south-1.myhuaweicloud.com/jeepay/nginx:1.18.0' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not default Nginx to SWR"
  exit 1
fi

if ! grep -q 'rocketmqPlatform=${rocketmqPlatform:-linux/amd64}' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not expose overridable rocketmqPlatform default"
  exit 1
fi

if [ "$(grep -c -- '--platform=$rocketmqPlatform' "$INSTALL_SCRIPT")" -lt 2 ]; then
  echo "FAIL: install.sh does not apply rocketmqPlatform to both namesrv and broker docker run"
  exit 1
fi

if grep -qE "^--platform=linux/amd64 " "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh still hard-codes --platform=linux/amd64 for images with multi-arch manifests"
  exit 1
fi

for dockerfile in "$PAYMENT_DOCKERFILE" "$MANAGER_DOCKERFILE" "$MERCHANT_DOCKERFILE"; do
  if ! grep -q 'swr.cn-south-1.myhuaweicloud.com/jeepay/eclipse-temurin:17-jre' "$dockerfile"; then
    echo "FAIL: $(basename "$dockerfile") does not default Java base image to SWR"
    exit 1
  fi
done

if ! grep -q 'docker buildx imagetools create' "$SYNC_SCRIPT" 2>/dev/null; then
  echo "FAIL: sync-swr-thirdparty.sh does not use direct registry mirroring"
  exit 1
fi

if ! grep -q 'docker manifest create' "$SYNC_SCRIPT" 2>/dev/null; then
  echo "FAIL: sync-swr-thirdparty.sh does not publish multi-arch manifests where available"
  exit 1
fi

if ! grep -q 'docker buildx imagetools create --platform linux/amd64' "$SYNC_SCRIPT" 2>/dev/null; then
  echo "FAIL: sync-swr-thirdparty.sh does not handle amd64-only images explicitly"
  exit 1
fi

echo "PASS"
