#!/bin/sh

set -eu

HUB_PAT_TOKEN=$(
  printf %s https://index.docker.io/v1/ \
    | docker-credential-osxkeychain get \
    | /usr/bin/python3 -c 'import json,sys; print(json.load(sys.stdin)["Secret"])'
)

exec docker run -i --rm \
  -e HUB_PAT_TOKEN="$HUB_PAT_TOKEN" \
  mcp/dockerhub \
  --transport=stdio \
  --username=jeepay
