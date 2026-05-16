# Jeepay 部署工具安装指南

## 目录

- [概述](#概述)
- [工具清单](#工具清单)
- [Railway CLI 安装](#railway-cli-安装)
- [Docker 安装](#docker-安装)
- [Docker Compose 安装](#docker-compose-安装)
- [Maven 安装](#maven-安装)
- [JDK 17 安装](#jdk-17-安装)
- [Git 安装](#git-安装)
- [环境验证](#环境验证)
- [快速安装脚本](#快速安装脚本)

---

## 概述

本指南将帮助您在不同操作系统上安装 Jeepay 部署所需的所有工具。

---

## 工具清单

| 工具 | 用途 | 最低版本 |
|------|------|----------|
| Railway CLI | Railway 部署工具 | 最新 |
| Docker | 容器化平台 | 20.10+ |
| Docker Compose | 容器编排 | 2.0+ |
| Maven | Java 项目构建 | 3.6+ |
| JDK | Java 开发工具包 | 17+ |
| Git | 版本控制 | 最新 |

---

## Railway CLI 安装

### 方法一：使用 npm（推荐）

```bash
# 全局安装 Railway CLI
npm install -g @railway/cli

# 验证安装
railway --version
```

### 方法二：使用 Homebrew (macOS)

```bash
brew tap railwayapp/tap
brew install railway
```

### 方法三：使用 curl (Linux/macOS)

```bash
curl -fsSL https://railway.app/install.sh | bash

# 或使用 wget
wget -qO- https://railway.app/install.sh | bash
```

### 方法四：手动下载

访问 [Railway CLI GitHub 发布页面](https://github.com/railwayapp/cli/releases)，下载对应您操作系统的二进制文件并添加到 PATH。

### 验证安装

```bash
# 检查版本
railway --version

# 查看帮助
railway --help
```

---

## Docker 安装

### macOS

#### 使用 Docker Desktop（推荐）

1. 下载：https://www.docker.com/get-started
2. 安装下载的 `.dmg` 文件
3. 启动 Docker Desktop
4. 验证安装

```bash
docker --version
```

#### 使用 Homebrew

```bash
# 安装 Docker Desktop
brew install --cask docker

# 或安装 Docker CLI
brew install docker
```

### Linux

#### Ubuntu/Debian

```bash
# 1. 更新包索引
sudo apt-get update

# 2. 安装依赖
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# 3. 添加 Docker 官方 GPG 密钥
sudo mkdir -m 0755 -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 4. 设置仓库
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. 安装 Docker
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 6. 验证安装
sudo docker run hello-world
```

#### CentOS/RHEL

```bash
# 1. 安装依赖
sudo yum install -y yum-utils

# 2. 添加仓库
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 3. 安装 Docker
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 4. 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 5. 验证安装
sudo docker run hello-world
```

#### Arch Linux

```bash
sudo pacman -S docker

# 启动服务
sudo systemctl start docker
sudo systemctl enable docker
```

#### Fedora

```bash
sudo dnf install docker

# 启动服务
sudo systemctl start docker
sudo systemctl enable docker
```

### Windows

#### 方式一：Docker Desktop（推荐）

1. 下载：https://www.docker.com/get-started
2. 下载 Windows 版本并安装
3. 重启电脑
4. 启动 Docker Desktop
5. 在 PowerShell 中验证：

```powershell
docker --version
```

#### 方式二：使用 Chocolatey

```powershell
# 使用管理员权限打开 PowerShell
choco install docker-desktop
```

### 安装后配置（Linux）

将当前用户添加到 docker 组，避免使用 sudo：

```bash
# 创建 docker 组（如果不存在）
sudo groupadd docker

# 将当前用户添加到 docker 组
sudo usermod -aG docker $USER

# 重新加载组成员资格
newgrp docker

# 验证不需要 sudo
docker run hello-world
```

---

## Docker Compose 安装

### Docker Compose v2（推荐，Docker Desktop 已包含）

如果您安装了 Docker Desktop，Docker Compose v2 已经内置。

```bash
# 验证安装
docker compose version
```

### Linux 独立安装

```bash
# 下载 Docker Compose v2
DOCKER_COMPOSE_VERSION="v2.24.0"
curl -SL https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose

# 添加执行权限
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker-compose --version
```

### 或者使用包管理器

```bash
# Ubuntu/Debian
sudo apt-get install docker-compose-plugin

# CentOS/RHEL
sudo yum install docker-compose-plugin
```

---

## Maven 安装

### macOS

#### 使用 Homebrew（推荐）

```bash
brew install maven
```

#### 手动安装

```bash
# 1. 下载 Maven
# 访问：https://maven.apache.org/download.cgi

# 2. 解压
tar -xzf apache-maven-3.9.x-bin.tar.gz
sudo mv apache-maven-3.9.x /usr/local/

# 3. 添加到 PATH（在 ~/.zshrc 或 ~/.bashrc）
export M2_HOME=/usr/local/apache-maven-3.9.x
export PATH=$M2_HOME/bin:$PATH

# 4. 重新加载配置
source ~/.zshrc

# 5. 验证安装
mvn -version
```

### Linux

#### Ubuntu/Debian

```bash
sudo apt-get update
sudo apt-get install -y maven

# 验证
mvn -version
```

#### CentOS/RHEL

```bash
sudo yum install -y maven
```

#### 手动安装（所有 Linux 发行版）

```bash
# 下载并安装 Maven 3.9.x
MAVEN_VERSION="3.9.6"
cd /tmp
wget https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
tar xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz
sudo mv apache-maven-${MAVEN_VERSION} /opt/maven

# 配置环境变量
echo 'export M2_HOME=/opt/maven' >> ~/.bashrc
echo 'export PATH=$M2_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 验证
mvn -version
```

### Windows

#### 使用 Chocolatey

```powershell
choco install maven
```

#### 手动安装

1. 下载 Maven：https://maven.apache.org/download.cgi
2. 解压到 `C:\Program Files\Apache\maven`
3. 添加环境变量：
   - `MAVEN_HOME=C:\Program Files\Apache\maven`
   - `PATH=%MAVEN_HOME%\bin;%PATH%`
4. 重启 PowerShell/命令提示符

```powershell
mvn -version
```

---

## JDK 17 安装

### macOS

#### 使用 Homebrew（推荐）

```bash
# 安装 Eclipse Temurin (AdoptOpenJDK)
brew install --cask temurin17

# 或者安装 Oracle JDK
brew install --cask oracle-jdk17

# 验证安装
java -version
```

#### 手动安装

1. 下载 JDK：https://adoptium.net/
2. 下载 macOS 版本并安装
3. 验证：

```bash
java -version
```

### Linux

#### Ubuntu/Debian

```bash
# 安装 OpenJDK 17
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

# 验证
java -version

# 设置 JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

#### CentOS/RHEL

```bash
# 安装 OpenJDK 17
sudo yum install -y java-17-openjdk-devel

# 验证
java -version
```

#### 手动安装（所有 Linux 发行版）

```bash
# 下载 Eclipse Temurin
JDK_VERSION="17.0.10"
cd /tmp
wget https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JDK_VERSION}%2B7/OpenJDK17U-jdk_x64_linux_hotspot_${JDK_VERSION}_7.tar.gz

# 解压
tar xzf OpenJDK17U-jdk_x64_linux_hotspot_*.tar.gz
sudo mv jdk-17* /opt/jdk-17

# 配置环境变量
echo 'export JAVA_HOME=/opt/jdk-17' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 验证
java -version
```

### Windows

#### 使用 Chocolatey

```powershell
choco install temurin17
```

#### 手动安装

1. 下载 JDK 17：https://adoptium.net/
2. 下载 Windows 版本并安装
3. 配置环境变量：
   - `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x`
   - `PATH=%JAVA_HOME%\bin;%PATH%`
4. 验证：

```powershell
java -version
```

---

## Git 安装

### macOS

```bash
# 使用 Homebrew
brew install git

# 验证
git --version
```

### Linux

```bash
# Ubuntu/Debian
sudo apt-get install -y git

# CentOS/RHEL
sudo yum install -y git

# 验证
git --version
```

### Windows

```powershell
# 使用 Chocolatey
choco install git

# 或下载安装：https://git-scm.com/download/win
```

---

## 环境验证

### 验证所有工具

```bash
# 检查版本
echo "=== 检查工具版本 ==="
echo "Railway CLI:"
railway --version

echo -e "\nDocker:"
docker --version

echo -e "\nDocker Compose:"
docker compose version

echo -e "\nMaven:"
mvn -version

echo -e "\nJava:"
java -version

echo -e "\nGit:"
git --version
```

### 输出示例

```
=== 检查工具版本 ===
Railway CLI:
v2.24.0

Docker:
Docker version 24.0.6

Docker Compose:
Docker Compose version v2.21.0

Maven:
Apache Maven 3.9.6

Java:
openjdk version "17.0.10" 2024-01-16

Git:
git version 2.43.0
```

---

## 快速安装脚本

### macOS 一键安装（Homebrew）

创建 `install-tools-mac.sh`：

```bash
#!/bin/bash
set -e

echo "=== 开始安装部署工具 ==="

# 检查 Homebrew
if ! command -v brew &> /dev/null; then
    echo "安装 Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
fi

# 更新 Homebrew
brew update

# 安装工具
echo "安装 Railway CLI..."
npm install -g @railway/cli || brew tap railwayapp/tap && brew install railway

echo "安装 Docker Desktop..."
brew install --cask docker

echo "安装 Maven..."
brew install maven

echo "安装 JDK 17..."
brew install --cask temurin17

echo "安装 Git..."
brew install git

echo "=== 安装完成！ ==="
echo "请手动启动 Docker Desktop，然后运行验证脚本："
echo "./verify-tools.sh"
```

使用：

```bash
chmod +x install-tools-mac.sh
./install-tools-mac.sh
```

### Ubuntu/Debian 一键安装

创建 `install-tools-ubuntu.sh`：

```bash
#!/bin/bash
set -e

echo "=== 开始安装部署工具 ==="

# 更新包索引
sudo apt-get update

# 安装基础工具
sudo apt-get install -y ca-certificates curl gnupg lsb-release git

# 安装 Node.js (for npm)
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

# 安装 Railway CLI
echo "安装 Railway CLI..."
sudo npm install -g @railway/cli

# 安装 Docker
echo "安装 Docker..."
sudo mkdir -m 0755 -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 安装 Maven 和 JDK
echo "安装 Maven 和 JDK..."
sudo apt-get install -y maven openjdk-17-jdk

# 将当前用户添加到 docker 组
sudo usermod -aG docker $USER

echo "=== 安装完成！ ==="
echo "请重新登录以应用 docker 组权限，然后运行验证脚本："
echo "newgrp docker"
echo "./verify-tools.sh"
```

### 验证脚本

创建 `verify-tools.sh`：

```bash
#!/bin/bash
set -e

echo "=== 验证工具安装 ==="
echo ""

echo "1. 检查 Railway CLI..."
if command -v railway &> /dev/null; then
    railway --version
    echo "✅ Railway CLI: 正常"
else
    echo "❌ Railway CLI: 未安装"
fi
echo ""

echo "2. 检查 Docker..."
if command -v docker &> /dev/null; then
    docker --version
    if docker ps &> /dev/null; then
        echo "✅ Docker: 正常（服务运行中）"
    else
        echo "⚠️  Docker: 已安装，但服务未运行"
    fi
else
    echo "❌ Docker: 未安装"
fi
echo ""

echo "3. 检查 Docker Compose..."
if command -v docker &> /dev/null && docker compose version &> /dev/null; then
    docker compose version
    echo "✅ Docker Compose: 正常"
else
    echo "❌ Docker Compose: 未安装"
fi
echo ""

echo "4. 检查 Maven..."
if command -v mvn &> /dev/null; then
    mvn -version | head -n 1
    echo "✅ Maven: 正常"
else
    echo "❌ Maven: 未安装"
fi
echo ""

echo "5. 检查 Java..."
if command -v java &> /dev/null; then
    java -version 2>&1 | head -n 1
    echo "✅ Java: 正常"
else
    echo "❌ Java: 未安装"
fi
echo ""

echo "6. 检查 Git..."
if command -v git &> /dev/null; then
    git --version
    echo "✅ Git: 正常"
else
    echo "❌ Git: 未安装"
fi
echo ""

echo "=== 验证完成 ==="
```

使用：

```bash
chmod +x verify-tools.sh
./verify-tools.sh
```

---

## 常见问题

### Docker 启动失败

```bash
# macOS
open -a Docker

# Linux
sudo systemctl start docker

# Windows
# 启动 Docker Desktop
```

### 权限问题 (Linux)

```bash
# 将用户添加到 docker 组
sudo usermod -aG docker $USER

# 重新登录
newgrp docker

# 或临时使用 sudo
sudo docker ps
```

### 网络问题

```bash
# 配置 Docker 镜像加速器
# 编辑 /etc/docker/daemon.json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn"
  ]
}

# 重启 Docker
sudo systemctl restart docker
```

---

**文档版本**: v1.0
**最后更新**: 2026-05-16
