#!/bin/bash

echo "=========================================="
echo "  Jeepay Manager - Railway 部署脚本"
echo "=========================================="
echo ""

# 检查是否安装了railway CLI
if ! command -v railway &> /dev/null; then
    echo "❌ 错误：Railway CLI未安装"
    echo ""
    echo "请先安装Railway CLI："
    echo "  npm install -g @railway/cli"
    echo ""
    echo "安装后登录："
    echo "  railway login"
    exit 1
fi

# 检查是否已登录
if ! railway whoami &> /dev/null; then
    echo "❌ 错误：未登录Railway"
    echo ""
    echo "请先登录："
    echo "  railway login"
    exit 1
fi

echo "✅ Railway CLI已就绪"

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo "📋 Railway 部署选项："
echo ""
echo "  1. 部署 Jeepay Manager（运营平台）"
echo "  2. 部署 Jeepay Payment（支付网关）"
echo "  3. 部署 Jeepay Merchant（商户平台）"
echo "  4. 部署所有服务"
echo "  5. 查看日志"
echo "  6. 退出"
echo ""

read -p "请选择 [1-6]: " choice

case $choice in
    1)
        SERVICE="manager"
        SERVICE_NAME="Jeepay Manager"
        SERVICE_DIR="jeepay-manager"
        PORT=9217
        ;;
    2)
        SERVICE="payment"
        SERVICE_NAME="Jeepay Payment"
        SERVICE_DIR="jeepay-payment"
        PORT=9216
        ;;
    3)
        SERVICE="merchant"
        SERVICE_NAME="Jeepay Merchant"
        SERVICE_DIR="jeepay-merchant"
        PORT=9218
        ;;
    4)
        echo ""
        echo "📦 部署所有Jeepay服务..."
        echo ""
        
        # 部署Manager
        echo "🔨 部署 Jeepay Manager..."
        cd jeepay-manager
        railway up
        cd ..
        
        # 部署Payment
        echo "🔨 部署 Jeepay Payment..."
        cd jeepay-payment
        railway up
        cd ..
        
        # 部署Merchant
        echo "🔨 部署 Jeepay Merchant..."
        cd jeepay-merchant
        railway up
        cd ..
        
        echo ""
        echo "✅ 所有服务部署完成！"
        echo ""
        echo "📍 服务地址："
        echo "   Manager: http://localhost:$PORT"
        echo "   Payment: http://localhost:9216"
        echo "   Merchant: http://localhost:9218"
        echo ""
        exit 0
        ;;
    5)
        echo ""
        echo "🔍 查看日志选项："
        echo ""
        echo "  1. Jeepay Manager"
        echo "  2. Jeepay Payment"
        echo "  3. Jeepay Merchant"
        echo "  4. 所有服务"
        echo ""
        read -p "请选择 [1-4]: " log_choice
        
        case $log_choice in
            1)
                railway logs jeepay-manager
                ;;
            2)
                railway logs jeepay-payment
                ;;
            3)
                railway logs jeepay-merchant
                ;;
            4)
                railway logs jeepay-manager
                railway logs jeepay-payment
                railway logs jeepay-merchant
                ;;
            *)
                echo "无效选择"
                exit 1
                ;;
        esac
        exit 0
        ;;
    6)
        echo "退出"
        exit 0
        ;;
    *)
        echo "无效选择"
        exit 1
        ;;
esac

echo ""
echo "🚀 开始部署 $SERVICE_NAME..."

# 进入服务目录
cd $SERVICE_DIR

# 检查Dockerfile是否存在
if [ ! -f "Dockerfile.railway" ]; then
    echo "❌ 错误：Dockerfile.railway 不存在"
    echo ""
    echo "创建Dockerfile.railway..."
    
    # 创建Dockerfile
    cat > Dockerfile.railway << 'EOF'
# Jeepay Manager Dockerfile for Railway
# 自动构建jar文件

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制pom文件
COPY pom.xml /build/
COPY ../jeepay-z-codegen/pom.xml /build/jeepay-z-codegen/
COPY ../jeepay-core/pom.xml /build/jeepay-core/
COPY ../jeepay-service/pom.xml /build/jeepay-service/
COPY ../jeepay-components/pom.xml /build/jeepay-components/
COPY ../jeepay-components/jeepay-components-mq/pom.xml /build/jeepay-components/jeepay-components-mq/
COPY ../jeepay-components/jeepay-components-oss/pom.xml /build/jeepay-components/jeepay-components-oss/
COPY pom.xml /build/jeepay-manager/

# 复制源码
COPY ../jeepay-z-codegen/src /build/jeepay-z-codegen/src
COPY ../jeepay-core/src /build/jeepay-core/src
COPY ../jeepay-service/src /build/jeepay-service/src
COPY ../jeepay-components/src /build/jeepay-components/src
COPY src /build/jeepay-manager/src

# 复制配置文件
COPY ../conf/devCommons /build/conf/devCommons
COPY src/main/resources /build/jeepay-manager/src/main/resources

# 构建项目
RUN mvn clean package -DskipTests -q

# 第二阶段
FROM eclipse-temurin:17-jre

WORKDIR /jeepayhomes/service/app

RUN mkdir -p /jeepayhomes/service/logs \
    /jeepayhomes/service/uploads

COPY --from=builder /build/jeepay-manager/target/jeepay-manager.jar /jeepayhomes/service/app/jeepay-manager.jar

EXPOSE 9217

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

CMD ["sh", "-c", "java $JAVA_OPTS -jar jeepay-manager.jar"]
EOF

    echo "✅ Dockerfile.railway 已创建"
fi

# 部署到Railway
echo "📤 上传到Railway..."
railway up

# 设置端口
echo "⚙️  设置端口..."
railway variables set SERVER_PORT=$PORT

# 设置默认环境变量（如果尚未设置）
echo "⚙️  配置环境变量..."
read -p "是否配置MySQL连接？[y/N]: " configure_mysql
if [ "$configure_mysql" = "y" ]; then
    read -p "MySQL Host: " mysql_host
    read -p "MySQL Port [3306]: " mysql_port
    mysql_port=${mysql_port:-3306}
    read -p "MySQL Database [jeepaydb]: " mysql_db
    mysql_db=${mysql_db:-jeepaydb}
    read -p "MySQL Username: " mysql_user
    read -s -p "MySQL Password: " mysql_pass
    echo ""
    
    railway variables set SPRING_DATASOURCE_URL="jdbc:mysql://${mysql_host}:${mysql_port}/${mysql_db}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
    railway variables set SPRING_DATASOURCE_USERNAME="$mysql_user"
    railway variables set SPRING_DATASOURCE_PASSWORD="$mysql_pass"
fi

read -p "是否配置Redis连接？[y/N]: " configure_redis
if [ "$configure_redis" = "y" ]; then
    read -p "Redis Host: " redis_host
    read -p "Redis Port [6379]: " redis_port
    redis_port=${redis_port:-6379}
    read -s -p "Redis Password (可选): " redis_pass
    echo ""
    
    railway variables set SPRING_DATA_REDIS_HOST="$redis_host"
    railway variables set SPRING_DATA_REDIS_PORT="$redis_port"
    if [ -n "$redis_pass" ]; then
        railway variables set SPRING_DATA_REDIS_PASSWORD="$redis_pass"
    fi
fi

read -p "是否配置RocketMQ连接？[y/N]: " configure_rocketmq
if [ "$configure_rocketmq" = "y" ]; then
    read -p "RocketMQ Nameserver Address: " rocketmq_addr
    railway variables set ROCKETMQ_NAMESRV_ADDR="$rocketmq_addr"
fi

echo ""
echo "✅ 部署完成！"
echo ""
echo "📍 $SERVICE_NAME 信息："
echo "   端口: $PORT"
echo ""
echo "🔍 查看日志："
echo "   railway logs jeepay-$SERVICE"
echo ""
echo "🌐 打开Dashboard："
echo "   railway open"
