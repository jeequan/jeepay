#!/bin/bash

################################################################################
# macOS 快速安装脚本
# 功能：一键安装 Jeepay 部署所需的所有工具
# 
# 使用方法：
#   chmod +x install-macos.sh
#   ./install-macos.sh
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

# Banner
echo -e "${BLUE}"
cat << "EOF"
    ____             __        ____          _ __          ______           
   / __ \_________  / /_____  / __ )___     (_) /_____    / ____/___  _____
  / /_/ / ___/ _ \/ __/ __ \/ __ \/ _ \   / / __/ __ \  / /   / __ \/ ___/
 / ____/ /  /  __/ /_/ /_/ / /_/ /  __/  / / /_/ /_/ / / /___/ /_/ / /    
/_/   /_/   \___/\__/\____/_____/\___/  /_/\__/\____/  \____/\____/_/     
                                                                           
    macOS 工具安装脚本 v1.0
EOF
echo -e "${NC}"

# 检查 Homebrew
check_homebrew() {
    print_info "检查 Homebrew..."
    if ! command -v brew &> /dev/null; then
        print_warning "Homebrew 未安装，正在安装..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        if [ $? -ne 0 ]; then
            print_error "Homebrew 安装失败"
            exit 1
        fi
    fi
    print_success "Homebrew 已准备就绪"
}

# 安装工具
install_tools() {
    print_info "更新 Homebrew..."
    brew update
    echo ""
    
    # 安装 Railway CLI
    print_info "安装 Railway CLI..."
    if ! command -v railway &> /dev/null; then
        if command -v npm &> /dev/null; then
            npm install -g @railway/cli
        else
            brew tap railwayapp/tap
            brew install railway
        fi
    else
        print_warning "Railway CLI 已安装"
    fi
    print_success "Railway CLI 安装完成"
    echo ""
    
    # 安装 Docker Desktop
    print_info "安装 Docker Desktop..."
    if ! command -v docker &> /dev/null; then
        brew install --cask docker
    else
        print_warning "Docker Desktop 已安装"
    fi
    print_success "Docker Desktop 安装完成"
    echo ""
    
    # 安装 Maven
    print_info "安装 Maven..."
    if ! command -v mvn &> /dev/null; then
        brew install maven
    else
        print_warning "Maven 已安装"
    fi
    print_success "Maven 安装完成"
    echo ""
    
    # 安装 JDK 17
    print_info "安装 JDK 17..."
    if ! java -version 2>&1 | grep -q "17"; then
        brew install --cask temurin17
    else
        print_warning "JDK 17 已安装"
    fi
    print_success "JDK 17 安装完成"
    echo ""
    
    # 安装 Git
    print_info "安装 Git..."
    if ! command -v git &> /dev/null; then
        brew install git
    else
        print_warning "Git 已安装"
    fi
    print_success "Git 安装完成"
    echo ""
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
    echo "1. 启动 Docker Desktop（从应用程序文件夹）"
    echo "2. 运行验证脚本："
    echo "   ./verify-tools.sh"
    echo ""
    echo "3. 登录 Railway："
    echo "   railway login"
    echo ""
}

# 主函数
main() {
    check_homebrew
    install_tools
    show_result
}

# 运行主函数
main "$@"
