#!/bin/bash
set -e

echo "=========================================="
echo "  Jeepay Manager 独立启动脚本"
echo "=========================================="
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 检查target目录是否存在jar文件
if [ ! -f "jeepay-manager/target/jeepay-manager.jar" ]; then
    echo "⚠️  未找到构建好的jar文件"
    echo "📦 正在构建 Jeepay Manager..."
    
    # 检查Maven是否安装
    if ! command -v mvn &> /dev/null; then
        echo "❌ 错误：Maven未安装"
        echo "   请安装Maven："
        echo "   Ubuntu/Debian: sudo apt install maven"
        echo "   macOS: brew install maven"
        exit 1
    fi
    
    # 检查Java是否安装
    if ! command -v java &> /dev/null; then
        echo "❌ 错误：Java未安装"
        echo "   请安装 Java 17："
        echo "   Ubuntu/Debian: sudo apt install openjdk-17-jdk"
        echo "   macOS: brew install openjdk@17"
        exit 1
    fi
    
    echo "🔨 使用Maven构建项目..."
    mvn clean package -DskipTests
    
    if [ ! -f "jeepay-manager/target/jeepay-manager.jar" ]; then
        echo "❌ 构建失败：未生成jar文件"
        exit 1
    fi
    
    echo "✅ 构建成功！"
fi

echo "🚀 启动 Jeepay Manager..."
echo "   jar文件: jeepay-manager/target/jeepay-manager.jar"
echo ""

# 使用docker-compose启动依赖服务并运行manager
docker compose up -d --build manager

echo ""
echo "✅ 启动完成！"
echo "📍 Manager管理平台: http://localhost:9217"
echo ""
echo "🔍 查看日志："
echo "   docker compose logs -f manager"
