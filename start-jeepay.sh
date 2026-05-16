#!/bin/bash
set -e

echo "=========================================="
echo "  Jeepay 快速启动脚本"
echo "=========================================="
echo ""

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 错误：Docker未安装"
    exit 1
fi

# 检查docker-compose是否安装
if ! docker compose version &> /dev/null; then
    echo "❌ 错误：Docker Compose未安装"
    exit 1
fi

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "📦 [1/5] 启动基础设施服务（MySQL、Redis、RocketMQ）..."
docker compose up -d mysql redis rocketmq-namesrv rocketmq-broker

echo ""
echo "⏳ [2/5] 等待基础设施服务就绪（60秒）..."
echo "   正在检查服务健康状态..."
sleep 30

# 等待MySQL就绪
echo "   等待 MySQL 就绪..."
until docker compose exec -T mysql mysqladmin ping -h127.0.0.1 -uroot -prootroot --silent 2>/dev/null; do
    echo "   等待 MySQL..."
    sleep 5
done

# 等待Redis就绪
echo "   等待 Redis 就绪..."
until docker compose exec -T redis redis-cli ping 2>/dev/null | grep -q PONG; do
    echo "   等待 Redis..."
    sleep 2
done

# 等待RocketMQ就绪
echo "   等待 RocketMQ 就绪..."
sleep 10

echo ""
echo "🔨 [3/5] 构建Jeepay应用服务（manager、payment、merchant）..."
docker compose build --no-cache manager payment merchant

echo ""
echo "🚀 [4/5] 启动Jeepay应用服务..."
docker compose up -d manager payment merchant

echo ""
echo "⏳ [5/5] 等待服务启动..."
sleep 20

echo ""
echo "=========================================="
echo "  服务状态"
echo "=========================================="
docker compose ps

echo ""
echo "✅ 启动完成！"
echo ""
echo "📍 服务地址："
echo "   • Manager管理平台: http://localhost:9217"
echo "   • Payment支付网关: http://localhost:9216"
echo "   • Merchant商户平台: http://localhost:9218"
echo ""
echo "🔍 查看日志："
echo "   • docker compose logs -f manager"
echo "   • docker compose logs -f payment"
echo "   • docker compose logs -f merchant"
echo ""
echo "🛑 停止服务："
echo "   • docker compose down"
echo ""
echo "=========================================="
