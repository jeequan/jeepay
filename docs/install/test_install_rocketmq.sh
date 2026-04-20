#!/bin/sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
INSTALL_SCRIPT="$SCRIPT_DIR/install.sh"
BROKER_TEMPLATE="$SCRIPT_DIR/../../docker/rocketmq/broker/conf/broker.conf.template"

if grep -q 'brokerIP1=172\.20\.0\.13' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh still contains a hard-coded RocketMQ broker IP"
  exit 1
fi

if grep -q 'brokerIP1=172\.20\.0\.13' "$BROKER_TEMPLATE"; then
  echo "FAIL: broker.conf template still contains a hard-coded RocketMQ broker IP"
  exit 1
fi

if ! grep -q 'brokerIP1=%BROKER_IP%' "$BROKER_TEMPLATE"; then
  echo "FAIL: broker.conf template does not contain the runtime brokerIP1 placeholder"
  exit 1
fi

if ! grep -q 'brokerIP1=\${brokerIP1:-rocketmq-broker}' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not default brokerIP1 to the broker container name (rocketmq-broker)"
  exit 1
fi

if ! grep -q 'sed "s/%BROKER_IP%/' "$INSTALL_SCRIPT"; then
  echo "FAIL: install.sh does not render the RocketMQ broker.conf template"
  exit 1
fi

echo "PASS"
