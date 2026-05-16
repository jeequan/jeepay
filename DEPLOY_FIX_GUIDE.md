# Jeepay Manager 部署错误修复指南

## 问题诊断

错误 `Error: Unable to access jarfile target/*jar` 表示 **Docker容器尝试运行jar文件时找不到它**。

### 根本原因

1. **Maven构建未完成** - 项目从未被编译打包
2. **Dockerfile配置不当** - 使用了需要预构建jar的Dockerfile
3. **构建顺序错误** - docker-compose构建时没有先生成jar

## 解决方案

### 方案一：使用 docker-compose 本地部署（推荐）

这是最简单的部署方式，Dockerfile已经配置好会自动构建。

#### 步骤1：确保MySQL、Redis、RocketMQ已启动

```bash
cd /workspace
docker compose up -d mysql redis rocketmq-namesrv rocketmq-broker
```

#### 步骤2：等待依赖服务健康

```bash
# 检查服务状态
docker compose ps

# 等待所有服务健康（大约需要1-2分钟）
docker compose ps --format "table {{.Name}}\t{{.Status}}"
```

#### 步骤3：构建并启动Jeepay服务

**重要**：必须先构建jar文件，再启动服务！

```bash
# 方式A：一次性构建并启动（推荐）
cd /workspace
docker compose up -d --build manager payment merchant

# 方式B：分步操作
# 1. 先构建所有服务
docker compose build manager payment merchant

# 2. 再启动
docker compose up -d manager payment merchant
```

#### 步骤4：验证服务状态

```bash
# 查看日志
docker compose logs -f manager

# 检查健康状态
docker compose ps
```

### 方案二：手动Maven构建 + Docker部署

如果需要本地开发或自定义构建：

#### 步骤1：安装Maven和Java 17

```bash
# 检查Java版本
java -version

# 如果没有Java 17，安装它
# Ubuntu/Debian:
sudo apt update && sudo apt install openjdk-17-jdk maven

# macOS:
brew install openjdk@17 maven
```

#### 步骤2：构建项目

```bash
cd /workspace

# 构建所有模块（跳过测试以加快速度）
mvn clean package -DskipTests

# 或者只构建manager模块
cd jeepay-manager
mvn clean package -DskipTests
```

#### 步骤3：确认jar文件生成

```bash
ls -lh jeepay-manager/target/jeepay-manager.jar
```

#### 步骤4：启动服务

```bash
cd /workspace
docker compose up -d manager payment merchant
```

### 方案三：Railway部署

#### 关键配置

在Railway Dashboard中配置：

1. **Dockerfile Path**：
   ```
   jeepay-manager/Dockerfile.railway
   ```

2. **环境变量**（参考 `conf/devCommons/config/application.yml`）：
   ```
   # 数据库配置
   SPRING_DATASOURCE_URL=jdbc:mysql://mysql.railway.internal:3306/jeepaydb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
   SPRING_DATASOURCE_USERNAME=root
   SPRING_DATASOURCE_PASSWORD=你的密码

   # Redis配置
   SPRING_DATA_REDIS_HOST=redis.railway.internal
   SPRING_DATA_REDIS_PORT=6379

   # RocketMQ配置
   ROCKETMQ_NAMESRV_ADDR=rocketmq-namesrv:9876
   ```

3. **端口**：9217

#### 验证Railway部署

```bash
# 查看构建日志
railway logs jeepay-manager --verbose

# 查看运行时日志
railway logs jeepay-manager
```

## 常见问题排查

### 问题1：构建时内存不足

**症状**：Maven构建失败，OOM错误

**解决方案**：
```bash
# 设置Maven内存限制
export MAVEN_OPTS="-Xmx1024m -Xms512m"
mvn clean package -DskipTests
```

### 问题2：下载依赖超时

**症状**：`Could not resolve dependencies` 错误

**解决方案**：
```bash
# 使用阿里云镜像（如果在中国）
mvn clean package -DskipTests -s settings.xml
```

创建 `settings.xml`：
```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

### 问题3：端口冲突

**症状**：`Bind for 0.0.0.0:9217 failed: port is already allocated`

**解决方案**：
```bash
# 查找占用端口的进程
lsof -i :9217

# 或修改docker-compose.yml中的端口映射
```

### 问题4：数据库连接失败

**症状**：`Connection refused` 或 `Unknown database`

**解决方案**：
1. 确认MySQL容器正在运行
2. 检查数据库是否已初始化
3. 验证环境变量配置正确

```bash
# 检查MySQL日志
docker compose logs mysql

# 进入MySQL容器检查
docker compose exec mysql mysql -uroot -prootroot -e "SHOW DATABASES;"
```

## 快速启动脚本

创建一个一键启动脚本 `start-jeepay.sh`：

```bash
#!/bin/bash
set -e

echo "=== Jeepay 全量启动脚本 ==="

# 1. 启动基础设施
echo "[1/4] 启动MySQL、Redis、RocketMQ..."
docker compose up -d mysql redis rocketmq-namesrv rocketmq-broker

# 2. 等待依赖服务就绪
echo "[2/4] 等待依赖服务健康（60秒）..."
sleep 60

# 3. 构建并启动应用
echo "[3/4] 构建并启动Jeepay服务..."
docker compose up -d --build manager payment merchant

# 4. 显示状态
echo "[4/4] 检查服务状态..."
docker compose ps

echo ""
echo "✅ 启动完成！"
echo "   Manager管理平台: http://localhost:9217"
echo "   Payment支付网关: http://localhost:9216"
echo "   Merchant商户平台: http://localhost:9218"
```

使用方式：
```bash
chmod +x start-jeepay.sh
./start-jeepay.sh
```

## 环境要求

- Docker 20.10+
- Docker Compose v2+
- 内存建议 4GB+
- Java 17（仅本地开发需要）

## 服务依赖关系

```
manager ──┬──> mysql
          ├──> redis
          └──> rocketmq

payment ──┬──> mysql
          ├──> redis
          └──> rocketmq

merchant ─┬──> mysql
         ├──> redis
         └──> rocketmq
```

## 验证清单

部署完成后，确认以下几点：

- [ ] `docker compose ps` 显示所有服务为 `Up` 状态
- [ ] MySQL健康检查通过
- [ ] Redis健康检查通过
- [ ] RocketMQ健康检查通过
- [ ] Manager、Payment、Merchant服务健康检查通过
- [ ] 访问 `http://localhost:9217` 可以打开管理界面

## 获取帮助

如果问题仍未解决：

1. 查看完整日志：
   ```bash
   docker compose logs --tail=100 manager
   ```

2. 检查容器内部：
   ```bash
   docker compose exec manager ls -la /jeepayhomes/service/app/
   ```

3. 在GitHub提交Issue并附上：
   - 完整的错误日志
   - 操作系统和Docker版本
   - 部署方式（docker-compose/Railway/其他）
