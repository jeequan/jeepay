#!/bin/bash

################################################################################
# Jeepay Railway 部署脚本
# 功能：一键部署 Jeepay 到 Railway
# 
# 使用方法：
#   chmod +x deploy-to-railway.sh
#   ./deploy-to-railway.sh
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 输出函数
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Banner
echo -e "${BLUE}"
cat << "EOF"
    ____             __        ____          _ __          ______           
   / __ \_________  / /_____  / __ )___     (_) /_____    / ____/___  _____
  / /_/ / ___/ _ \/ __/ __ \/ __ \/ _ \   / / __/ __ \  / /   / __ \/ ___/
 / ____/ /  /  __/ /_/ /_/ / /_/ /  __/  / / /_/ /_/ / / /___/ /_/ / /    
/_/   /_/   \___/\__/\____/_____/\___/  /_/\__/\____/  \____/\____/_/     
                                                                             
    Railway Deployment Script v1.0
EOF
echo -e "${NC}"

# 检查 Railway CLI
check_railway_cli() {
    print_info "检查 Railway CLI..."
    if ! command -v railway &> /dev/null; then
        print_warning "Railway CLI 未安装，正在安装..."
        npm install -g @railway/cli
        if [ $? -ne 0 ]; then
            print_error "Railway CLI 安装失败，请手动安装：npm install -g @railway/cli"
            exit 1
        fi
    fi
    print_success "Railway CLI 已安装"
}

# 检查 Docker
check_docker() {
    print_info "检查 Docker..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    print_success "Docker 已安装"
}

# 登录 Railway
railway_login() {
    print_info "登录 Railway..."
    railway login
    if [ $? -ne 0 ]; then
        print_error "Railway 登录失败"
        exit 1
    fi
    print_success "Railway 登录成功"
}

# 创建项目
create_project() {
    print_info "创建 Railway 项目..."
    
    # 列出所有项目
    print_info "当前项目列表："
    railway list
    
    echo ""
    read -p "请输入项目名称（或输入 'new' 创建新项目）: " project_name
    
    if [ "$project_name" = "new" ]; then
        read -p "请输入新项目名称: " new_project_name
        railway init --name "$new_project_name"
        print_success "项目 '$new_project_name' 创建成功"
    else
        railway link "$project_name"
        print_success "已切换到项目: $project_name"
    fi
}

# 配置环境变量
config_env_vars() {
    print_info "配置环境变量..."
    
    # MySQL 配置
    read -p "MySQL Root 密码 [默认: root123456]: " mysql_root_password
    mysql_root_password=${mysql_root_password:-root123456}
    railway variables set MYSQL_ROOT_PASSWORD "$mysql_root_password"
    
    read -p "MySQL 数据库名 [默认: jeepaydb]: " mysql_database
    mysql_database=${mysql_database:-jeepaydb}
    railway variables set MYSQL_DATABASE "$mysql_database"
    
    # RabbitMQ 配置
    read -p "RabbitMQ 用户名 [默认: admin]: " rabbitmq_user
    rabbitmq_user=${rabbitmq_user:-admin}
    railway variables set RABBITMQ_DEFAULT_USER "$rabbitmq_user"
    
    read -p "RabbitMQ 密码 [默认: admin123]: " rabbitmq_password
    rabbitmq_password=${rabbitmq_password:-admin123}
    railway variables set RABBITMQ_DEFAULT_PASSWORD "$rabbitmq_password"
    
    railway variables set RABBITMQ_DEFAULT_VHOST "/jeepay"
    
    print_success "环境变量配置完成"
}

# 部署服务
deploy_services() {
    print_info "开始部署服务..."
    
    # 部署 MySQL
    print_info "1. 部署 MySQL..."
    railway up --service mysql --select
    railway deploy
    
    # 部署 Redis
    print_info "2. 部署 Redis..."
    railway up --service redis --select
    railway deploy
    
    # 部署 RabbitMQ
    print_info "3. 部署 RabbitMQ..."
    railway up --service rabbitmq --select
    railway deploy
    
    # 等待 MySQL 初始化
    print_warning "等待 MySQL 初始化完成（约 30 秒）..."
    sleep 30
    
    # 部署 Payment 服务
    print_info "4. 部署 Jeepay Payment..."
    cd jeepay-payment
    railway up --service jeepay-payment --select
    railway deploy
    cd ..
    
    # 部署 Manager 服务
    print_info "5. 部署 Jeepay Manager..."
    cd jeepay-manager
    railway up --service jeepay-manager --select
    railway deploy
    cd ..
    
    # 部署 Merchant 服务
    print_info "6. 部署 Jeepay Merchant..."
    cd jeepay-merchant
    railway up --service jeepay-merchant --select
    railway deploy
    cd ..
    
    print_success "所有服务部署完成"
}

# 显示部署结果
show_result() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Jeepay 部署完成！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "${BLUE}服务地址：${NC}"
    echo "  - 运营平台: http://localhost:9217"
    echo "  - 商户平台: http://localhost:9218"
    echo "  - 支付网关: http://localhost:9216"
    echo "  - RabbitMQ: http://localhost:15672"
    echo ""
    echo -e "${BLUE}默认账号：${NC}"
    echo "  - 运营平台: jeepay / jeepay123"
    echo "  - 商户平台: 需在运营平台创建"
    echo "  - RabbitMQ: admin / admin123"
    echo ""
    echo -e "${BLUE}查看日志：${NC}"
    echo "  railway logs -f"
    echo ""
    echo -e "${BLUE}查看状态：${NC}"
    echo "  railway status"
    echo ""
}

# 主函数
main() {
    check_docker
    check_railway_cli
    railway_login
    create_project
    config_env_vars
    deploy_services
    show_result
}

# 运行主函数
main "$@"
