#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
UNINSTALL_SCRIPT="$SCRIPT_DIR/uninstall.sh"
PUBLISH_SCRIPT="$SCRIPT_DIR/../../docker/publish-dockerhub.sh"
PUBLISH_SWR_SCRIPT="$SCRIPT_DIR/../../docker/publish-swr.sh"

if ! grep -q 'docker rm -f' "$UNINSTALL_SCRIPT"; then
  echo "FAIL: uninstall.sh does not force-remove containers"
  exit 1
fi

if ! grep -q 'docker network rm' "$UNINSTALL_SCRIPT"; then
  echo "FAIL: uninstall.sh does not remove the Docker network"
  exit 1
fi

if ! grep -q 'DOCKERHUB_NAMESPACE' "$PUBLISH_SCRIPT" 2>/dev/null; then
  echo "FAIL: publish-dockerhub.sh is missing or does not require DOCKERHUB_NAMESPACE"
  exit 1
fi

if ! grep -q 'docker push' "$PUBLISH_SCRIPT" 2>/dev/null; then
  echo "FAIL: publish-dockerhub.sh does not push images"
  exit 1
fi

if ! grep -q 'SWR_NAMESPACE' "$PUBLISH_SWR_SCRIPT" 2>/dev/null; then
  echo "FAIL: publish-swr.sh is missing or does not require SWR_NAMESPACE"
  exit 1
fi

if ! grep -q 'docker manifest create' "$PUBLISH_SWR_SCRIPT" 2>/dev/null; then
  echo "FAIL: publish-swr.sh does not create a multi-arch manifest"
  exit 1
fi

if ! grep -q 'docker push --platform' "$PUBLISH_SWR_SCRIPT" 2>/dev/null; then
  echo "FAIL: publish-swr.sh does not push platform-specific images for SWR"
  exit 1
fi

echo "PASS"
