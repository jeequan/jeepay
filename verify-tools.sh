#!/bin/bash

################################################################################
# 工具安装验证脚本
# 功能：验证所有部署所需的工具是否正确安装
# 
# 使用方法：
#   chmod +x verify-tools.sh
#   ./verify-tools.sh
################################################################################

set +e

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
                                                                           
    工具安装验证脚本 v1.0
EOF
echo -e "${NC}"

# 统计
total=0
passed=0
warnings=0

# 检查函数
check_tool() {
    local name=$1
    local check_cmd=$2
    local version_cmd=$3
    
    total=$((total + 1))
    
    echo -e "${BLUE}检查 $name...${NC}"
    
    if $check_cmd &> /dev/null; then
        if [ -n "$version_cmd" ]; then
            echo -n "  "
            $version_cmd
        fi
        print_success "$name: 正常"
        passed=$((passed + 1))
    else
        print_error "$name: 未安装"
    fi
    echo ""
}

# 检查 Railway CLI
check_railway() {
    check_tool "Railway CLI" "command -v railway" "railway --version"
}

# 检查 Docker
check_docker() {
    echo -e "${BLUE}检查 Docker...${NC}"
    total=$((total + 1))
    
    if command -v docker &> /dev/null; then
        echo -n "  "
        docker --version
        
        if docker ps &> /dev/null; then
            print_success "Docker: 正常（服务运行中）"
            passed=$((passed + 1))
        else
            print_warning "Docker: 已安装，但服务未运行"
            warnings=$((warnings + 1))
        fi
    else
        print_error "Docker: 未安装"
    fi
    echo ""
}

# 检查 Docker Compose
check_docker_compose() {
    echo -e "${BLUE}检查 Docker Compose...${NC}"
    total=$((total + 1))
    
    if command -v docker &> /dev/null; then
        if docker compose version &> /dev/null; then
            echo -n "  "
            docker compose version
            print_success "Docker Compose: 正常"
            passed=$((passed + 1))
        else
            print_warning "Docker Compose: Docker 已安装但 Compose 不可用"
            warnings=$((warnings + 1))
        fi
    else
        print_error "Docker Compose: Docker 未安装"
    fi
    echo ""
}

# 检查 Maven
check_maven() {
    check_tool "Maven" "command -v mvn" "mvn -version | head -n 1"
}

# 检查 Java
check_java() {
    echo -e "${BLUE}检查 Java...${NC}"
    total=$((total + 1))
    
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n 1)
        echo "  $java_version"
        
        if echo "$java_version" | grep -q "17"; then
            print_success "Java: 正常（版本 17）"
            passed=$((passed + 1))
        else
            print_warning "Java: 已安装，但版本不是 17"
            warnings=$((warnings + 1))
        fi
    else
        print_error "Java: 未安装"
    fi
    echo ""
}

# 检查 Git
check_git() {
    check_tool "Git" "command -v git" "git --version"
}

# 显示总结
show_summary() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  验证总结${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "  总检查数: $total"
    echo "  通过: $passed"
    echo "  警告: $warnings"
    echo "  失败: $((total - passed - warnings))"
    echo ""
    
    if [ $passed -eq $total ]; then
        echo -e "${GREEN}✅ 所有工具均已正确安装！${NC}"
        echo ""
        echo "下一步操作："
        echo "  1. 登录 Railway: railway login"
        echo "  2. 开始部署: ./deploy-to-railway.sh"
    elif [ $warnings -gt 0 ] && [ $((passed + warnings)) -eq $total ]; then
        echo -e "${YELLOW}⚠️  所有工具已安装，但有警告${NC}"
        echo ""
        echo "请检查上面的警告信息"
    else
        echo -e "${RED}❌ 部分工具未安装${NC}"
        echo ""
        echo "请运行安装脚本："
        echo "  macOS: ./install-macos.sh"
        echo "  Ubuntu: sudo ./install-ubuntu.sh"
    fi
    echo ""
}

# 主函数
main() {
    check_railway
    check_docker
    check_docker_compose
    check_maven
    check_java
    check_git
    show_summary
}

# 运行主函数
main "$@"
