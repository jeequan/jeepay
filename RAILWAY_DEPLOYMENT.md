# Jeepay Manager Railway 部署详细指南

## 目录

- [环境要求](#环境要求)
- [部署方案选择](#部署方案选择)
- [方案A：使用优化后的Dockerfile（推荐）](#方案a使用优化后的dockerfile推荐)
- [方案B：使用Railway构建命令](#方案b使用railway构建命令)
- [环境变量配置](#环境变量配置)
- [数据库配置](#数据库配置)
- [常见问题排查](#常见问题排查)
- [验证部署](#验证部署)

---

## 环境要求

在开始Railway部署之前，确保你已准备好：

1. **Railway账户** - [官网注册](https://railway.app)
2. **Railway CLI** - 安装方法：
   ```bash
   # 安装Railway CLI
   npm install -g @railway/cli
   
   # 登录
   railway login
   ```

3. **MySQL数据库** - Railway提供的或外部MySQL
4. **Redis缓存** - Railway提供的或外部Redis
5. **RocketMQ消息队列** - 需要自建或使用云服务

---

## 部署方案选择

### 方案A：使用优化后的Dockerfile（推荐）✅

这个方案使用多阶段构建，Dockerfile会自动完成Maven构建过程。

#### 步骤1：检查Dockerfile配置

确保 `/workspace/jeepay-manager/Dockerfile.railway` 文件存在并包含以下关键配置：

```dockerfile
# 第一阶段：使用Maven构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制所有必要的pom文件
COPY pom.xml /build/
COPY jeepay-z-codegen/pom.xml /build/jeepay-z-codegen/
COPY jeepay-core/pom.xml /build/jeepay-core/
COPY jeepay-service/pom.xml /build/jeepay-service/
COPY jeepay-components/pom.xml /build/jeepay-components/
COPY jeepay-components/jeepay-components-mq/pom.xml /build/jeepay-components/jeepay-components-mq/
COPY jeepay-components/jeepay-components-oss/pom.xml /build/jeepay-components/jeepay-components-oss/
COPY jeepay-manager/pom.xml /build/jeepay-manager/

# 复制源码
COPY jeepay-z-codegen/src /build/jeepay-z-codegen/src
COPY jeepay-core/src /build/jeepay-core/src
COPY jeepay-service/src /build/jeepay-service/src
COPY jeepay-components/src /build/jeepay-components/src
COPY jeepay-manager/src /build/jeepay-manager/src

# 复制配置文件
COPY conf/devCommons /build/conf/devCommons
COPY jeepay-manager/src/main/resources /build/jeepay-manager/src/main/resources

# 执行Maven构建
RUN mvn clean package -DskipTests -q

# 第二阶段：运行时镜像
FROM eclipse-temurin:17-jre

WORKDIR /jeepayhomes/service/app

# 复制构建好的jar文件
COPY --from=builder /build/jeepay-manager/target/jeepay-manager.jar /jeepayhomes/service/app/jeepay-manager.jar

EXPOSE 9217

CMD ["sh", "-c", "java $JAVA_OPTS -jar jeepay-manager.jar"]
```

#### 步骤2：在Railway Dashboard配置

1. **连接到GitHub仓库**
   - 进入 Railway Dashboard
   - 点击 "New Project" → "Deploy from GitHub repo"
   - 选择 `jeepay` 仓库

2. **配置Dockerfile路径**
   - 在 "Settings" → "Build" 中设置：
     ```
     Dockerfile Path: jeepay-manager/Dockerfile.railway
     ```

3. **设置环境变量**（详见下文）

4. **配置端口**
   - 在 "Settings" → "Networking" 中：
     ```
     Port: 9217
     ```

#### 步骤3：部署

Railway会自动检测Dockerfile并执行构建部署。

---

## 方案B：使用Railway构建命令

如果你想在Railway中使用自定义构建命令：

### 配置

在Railway Dashboard的 "Settings" → "Build" 中设置：

#### 构建命令

```bash
cd jeepay-manager && mvn clean package -DskipTests
```

#### Dockerfile路径

```
jeepay-manager/Dockerfile.railway.simple
```

#### 启动命令

```bash
java -jar jeepay-manager/target/jeepay-manager.jar
```

### 重要说明

⚠️ **方案B的局限性**：
- Railway的构建命令在部署环境中执行
- Maven构建可能需要较长时间
- 需要确保构建完成后再启动应用

✅ **推荐使用方案A**，因为Dockerfile中已经包含了完整的构建流程。

---

## 环境变量配置

Jeepay Manager需要配置多个环境变量才能正常运行。

### 必需的环境变量

#### 1. 数据库配置

```bash
# 数据库连接URL
SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-host:3306/jeepaydb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true

# 数据库用户名
SPRING_DATASOURCE_USERNAME=root

# 数据库密码
SPRING_DATASOURCE_PASSWORD=your_mysql_password
```

#### 2. Redis配置

```bash
# Redis主机地址
SPRING_DATA_REDIS_HOST=your-redis-host

# Redis端口
SPRING_DATA_REDIS_PORT=6379

# Redis密码（如果有）
SPRING_DATA_REDIS_PASSWORD=
```

#### 3. RocketMQ配置

```bash
# RocketMQ NameServer地址
ROCKETMQ_NAMESRV_ADDR=your-rocketmq-host:9876
```

#### 4. 应用配置

```bash
# 应用端口
SERVER_PORT=9217

# 日志级别（可选）
LOGGING_LEVEL_ROOT=INFO
```

### 在Railway中配置环境变量

#### 方法1：通过Railway Dashboard

1. 进入你的Jeepay Manager项目
2. 点击 "Variables" 标签
3. 点击 "New Variable"
4. 添加键值对：
   - Key: `SPRING_DATASOURCE_URL`
   - Value: `jdbc:mysql://mysql.railway.internal:3306/jeepaydb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true`

#### 方法2：通过Railway CLI

```bash
# 设置环境变量
railway variables set SPRING_DATASOURCE_URL="jdbc:mysql://mysql.railway.internal:3306/jeepaydb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"

railway variables set SPRING_DATASOURCE_USERNAME="root"
railway variables set SPRING_DATASOURCE_PASSWORD="your_password"
railway variables set SPRING_DATA_REDIS_HOST="redis.railway.internal"
railway variables set SPRING_DATA_REDIS_PORT="6379"
railway variables set ROCKETMQ_NAMESRV_ADDR="rocketmq.railway.internal:9876"
```

---

## 数据库配置

### 方案1：使用Railway MySQL插件

#### 创建MySQL数据库

1. 在Railway Dashboard中，点击 "New Project"
2. 选择 "MySQL" → "Empty Database"
3. Railway会自动创建MySQL实例并提供连接信息

#### 获取连接信息

1. 点击MySQL服务
2. 在 "Connect" 标签中查看：
   - Hostname
   - Port
   - Username
   - Password
   - Database

#### 配置Jeepay Manager

在Jeepay Manager的 "Variables" 中设置：

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://mysql.railway.internal:3306/jeepaydb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=你的MySQL密码
```

#### 初始化数据库

Jeepay需要初始化数据库表结构：

1. 在MySQL服务中，点击 " Executions"
2. 点击 "New Execution"
3. 选择 `/workspace/docs/sql/init.sql` 文件执行
4. 再执行 `/workspace/docs/sql/patch.sql` 文件

或者通过命令行：

```bash
# 连接到Railway MySQL
mysql -h hostname -P 3306 -u root -p jeepaydb < docs/sql/init.sql
mysql -h hostname -P 3306 -u root -p jeepaydb < docs/sql/patch.sql
```

### 方案2：使用外部MySQL

如果你已有外部MySQL服务（阿里云、腾讯云等）：

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://rm-xxxx.mysql.rds.aliyuncs.com:3306/jeepaydb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
```

---

## Redis配置

### 方案1：使用Railway Redis插件

#### 创建Redis

1. 在Railway Dashboard中，点击 "New Project"
2. 选择 "Redis" → "Empty Redis"
3. Railway会自动创建Redis实例

#### 获取连接信息

1. 点击Redis服务
2. 在 "Connect" 标签中查看连接信息

#### 配置Jeepay Manager

```bash
SPRING_DATA_REDIS_HOST=redis.railway.internal
SPRING_DATA_REDIS_PORT=6379
# 如果Redis设置了密码，取消下面这行的注释
# SPRING_DATA_REDIS_PASSWORD=your_redis_password
```

### 方案2：使用外部Redis

```bash
SPRING_DATA_REDIS_HOST=your-redis-host
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=your_redis_password
```

---

## RocketMQ配置

Railway不提供RocketMQ插件，需要使用外部服务。

### 方案1：使用阿里云MQ

```bash
ROCKETMQ_NAMESRV_ADDR=rmq-cn-xxxx.rocketmq.aliyun.com:8080
# 或者使用TCP端口
# ROCKETMQ_NAMESRV_ADDR=rmq-cn-xxxx.rocketmq.aliyun.com:10911
```

### 方案2：自建RocketMQ

如果你有其他云服务或自建RocketMQ：

```bash
ROCKETMQ_NAMESRV_ADDR=your-rocketmq-host:9876
```

### 方案3：使用Docker Compose本地测试

⚠️ 注意：Railway不支持docker-compose，只支持Docker部署。

如果需要完整的RocketMQ支持，建议：
1. 使用Railway部署Jeepay Manager
2. 使用其他平台部署RocketMQ（如阿里云、腾讯云）
3. 或者使用轻量级MQ替代方案

---

## 常见问题排查

### 问题1：构建失败 - Unable to access jarfile

**症状**：
```
Error: Unable to access jarfile target/*jar
```

**原因**：
1. Maven构建未完成
2. Dockerfile配置错误
3. 构建命令不正确

**解决方案**：

1. **使用方案A（推荐）**：确保Dockerfile路径正确
   ```
   jeepay-manager/Dockerfile.railway
   ```

2. **检查Dockerfile内容**：确保包含Maven构建步骤

3. **验证构建日志**：在Railway Dashboard查看构建日志

### 问题2：数据库连接失败

**症状**：
```
Connection refused or Unknown database
```

**解决方案**：

1. 检查MySQL服务是否正常运行
2. 验证环境变量配置正确
3. 确认数据库已创建
4. 检查数据库初始化脚本是否已执行

```bash
# 测试数据库连接
mysql -h hostname -P 3306 -u root -p -e "SHOW DATABASES;"
```

### 问题3：Redis连接失败

**症状**：
```
Cannot connect to Redis
```

**解决方案**：

1. 检查Redis服务是否正常运行
2. 验证主机地址和端口
3. 如果有密码，确保配置了密码

```bash
# 测试Redis连接
redis-cli -h hostname -p 6379 ping
```

### 问题4：RocketMQ连接失败

**症状**：
```
RocketMQ connection failed
```

**解决方案**：

1. 确认RocketMQ服务地址正确
2. 检查NameServer是否可访问
3. 确保防火墙开放9876端口

### 问题5：内存不足

**症状**：
```
Java heap space error or OOMKilled
```

**解决方案**：

在Railway Dashboard设置环境变量：

```bash
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
```

或在Dockerfile中修改：

```dockerfile
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
```

### 问题6：端口冲突

**症状**：
```
Port 9217 is already in use
```

**解决方案**：

1. 检查Railway端口配置
2. 确保SERVER_PORT环境变量设置为9217
3. 如果本地测试，确保本地端口未被占用

---

## 验证部署

### 1. 检查服务状态

在Railway Dashboard中：
- 确认服务状态为 "Running"
- 确认健康检查通过

### 2. 查看日志

```bash
# 查看构建日志
railway logs jeepay-manager --verbose

# 查看运行时日志
railway logs jeepay-manager
```

### 3. 访问应用

```bash
# 获取应用URL
railway open jeepay-manager

# 或直接访问
curl http://your-app-url.railway.app/health
```

### 4. 验证数据库连接

在应用日志中搜索：
```
Database connection successful
```

或检查错误日志：
```
Failed to configure DataSource
```

### 5. 功能测试

访问管理界面：
- URL: `http://your-app-url.railway.app`
- 默认账号: `admin` / `admin123456`

---

## 完整环境变量清单

以下是Jeepay Manager的所有可选环境变量：

```bash
# ==================== 数据库配置 ====================
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-host:3306/jeepaydb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# ==================== Redis配置 ====================
SPRING_DATA_REDIS_HOST=redis-host
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_PASSWORD=

# ==================== RocketMQ配置 ====================
ROCKETMQ_NAMESRV_ADDR=rocketmq-host:9876

# ==================== 应用配置 ====================
SERVER_PORT=9217
SPRING_APPLICATION_NAME=jeepay-manager

# ==================== 日志配置 ====================
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_JEEQUAN=DEBUG

# ==================== JVM配置 ====================
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC

# ==================== 安全配置（可选）================
# SPRING_SECURITY_ENABLED=false
```

---

## 快速参考

### Railway部署检查清单

- [ ] 已连接GitHub仓库
- [ ] Dockerfile路径设置正确：`jeepay-manager/Dockerfile.railway`
- [ ] 端口设置为：9217
- [ ] MySQL环境变量已配置
- [ ] Redis环境变量已配置
- [ ] RocketMQ环境变量已配置
- [ ] 数据库已初始化
- [ ] 健康检查通过

### 常用Railway命令

```bash
# 部署
railway up

# 查看日志
railway logs jeepay-manager

# 进入容器
railway run jeepay-manager

# 设置环境变量
railway variables set KEY=VALUE

# 查看变量
railway variables

# 打开Dashboard
railway open

# 重启服务
railway redeploy jeepay-manager
```

---

## 获取帮助

如果问题仍未解决：

1. **查看完整日志**：
   ```bash
   railway logs jeepay-manager --verbose
   ```

2. **检查Railway状态**：
   - [Railway Status](https://status.railway.app)
   - [Railway Documentation](https://docs.railway.app)

3. **提交Issue**：
   在GitHub仓库提交问题时，请附上：
   - 完整的错误日志
   - Railway部署配置截图
   - 环境变量配置（隐藏敏感信息）
   - 构建日志

---

**祝你部署顺利！** 🎉
