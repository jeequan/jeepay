#!/bin/bash

################################################################################
# Ubuntu/Debian 快速安装脚本
# 功能：一键安装 Jeepay 部署所需的所有工具
# 
# 使用方法：
#   chmod +x install-ubuntu.sh
#   sudo ./install-ubuntu.sh
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# 检查 root 权限
check_root() {
    if [ "$EUID" -ne 0 ]; then
        print_error "请使用 sudo 运行此脚本"
        echo "使用方法: sudo $0"
        exit 1
    fi
}

# Banner
echo -e "${BLUE}"
cat << "EOF"
    ____             __        ____          _ __          ______           
   / __ \_________  / /_____  / __ )___     (_) /_____    / ____/___  _____
  / /_/ / ___/ _ \/ __/ __ \/ __ \/ _ \   / / __/ __ \  / /   / __ \/ ___/
 / ____/ /  /  __/ /_/ /_/ / /_/ /  __/  / / /_/ /_/ / / /___/ /_/ / /    
/_/   /_/   \___/\__/\____/_____/\___/  /_/\__/\____/  \____/\____/_/     
                                                                           
    Ubuntu/Debian 工具安装脚本 v1.0
EOF
echo -e "${NC}"

# 检查操作系统
check_os() {
    print_info "检查操作系统..."
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        if [ "$ID" != "ubuntu" ] && [ "$ID" != "debian" ]; then
            print_warning "检测到的操作系统不是 Ubuntu 或 Debian ($ID)"
            read -p "是否继续？(y/n): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                exit 1
            fi
        fi
    fi
    print_success "操作系统检查通过"
}

# 更新系统
update_system() {
    print_info "更新系统包索引..."
    apt-get update -y
    print_success "系统更新完成"
}

# 安装基础工具
install_basics() {
    print_info "安装基础工具..."
    apt-get install -y ca-certificates curl gnupg lsb-release git
    print_success "基础工具安装完成"
}

# 安装 Node.js 和 npm
install_nodejs() {
    print_info "安装 Node.js..."
    if ! command -v node &> /dev/null; then
        curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
        apt-get install -y nodejs
    else
        print_warning "Node.js 已安装"
    fi
    print_success "Node.js 安装完成"
}

# 安装 Railway CLI
install_railway() {
    print_info "安装 Railway CLI..."
    if ! command -v railway &> /dev/null; then
        npm install -g @railway/cli
    else
        print_warning "Railway CLI 已安装"
    fi
    print_success "Railway CLI 安装完成"
}

# 安装 Docker
install_docker() {
    print_info "安装 Docker..."
    if ! command -v docker &> /dev/null; then
        # 添加 Docker 官方 GPG 密钥
        mkdir -m 0755 -p /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/${ID}/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        
        # 设置仓库
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/${ID} $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
        
        # 更新并安装
        apt-get update
        apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        
        # 启动并启用 Docker 服务
        systemctl start docker
        systemctl enable docker
    else
        print_warning "Docker 已安装"
    fi
    print_success "Docker 安装完成"
}

# 安装 Maven 和 JDK
install_maven_jdk() {
    print_info "安装 Maven 和 JDK..."
    if ! command -v mvn &> /dev/null || ! java -version 2>&1 | grep -q "17"; then
        apt-get install -y maven openjdk-17-jdk
    else
        print_warning "Maven 和 JDK 已安装"
    fi
    print_success "Maven 和 JDK 安装完成"
}

# 配置用户权限
configure_permissions() {
    print_info "配置 Docker 权限..."
    if [ "$SUDO_USER" ]; then
        usermod -aG docker "$SUDO_USER"
        print_success "已将用户 $SUDO_USER 添加到 docker 组"
    fi
}

# 显示安装结果
show_result() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  工具安装完成！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "${BLUE}下一步操作：${NC}"
    echo ""
    echo "1. 重新登录以应用 Docker 权限（重要）："
    if [ "$SUDO_USER" ]; then
        echo "   su - $SUDO_USER"
        echo "   newgrp docker"
    fi
    echo ""
    echo "2. 验证安装："
    echo "   ./verify-tools.sh"
    echo ""
    echo "3. 登录 Railway："
    echo "   railway login"
    echo ""
    echo -e "${YELLOW}注意：${NC}"
    echo "请使用非 root 用户运行后续命令"
    echo ""
}

# 主函数
main() {
    check_root
    check_os
    update_system
    install_basics
    install_nodejs
    install_railway
    install_docker
    install_maven_jdk
    configure_permissions
    show_result
}

# 运行主函数
main "$@"
