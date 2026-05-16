# Jeepay Manager 部署错误修复指南

## 问题症状

```
Error: Unable to access jarfile target/*jar
```

## 根本原因

这个错误表明Docker容器尝试运行jar文件时找不到它。通常发生在：

1. **Maven构建未成功完成** - jar文件未被生成
2. **Dockerfile配置错误** - 构建命令与实际构建流程不匹配
3. **文件路径错误** - COPY命令中指定的源路径不存在

## 解决方案

### 方案一：使用优化后的 Dockerfile（推荐）

我已经更新了 `/jeepay-manager/Dockerfile.railway`，新版本会：

- ✅ 使用多阶段构建自动编译项目
- ✅ 不依赖外部构建命令
- ✅ 确保jar文件在镜像中正确存在

**主要改进：**
- 添加了Maven构建阶段
- 复制所有必需的源码和配置文件
- 使用`mvn clean package -DskipTests`构建

### 方案二：手动构建后部署

如果使用Railway的构建命令，需要确保：

1. **构建命令正确：**
   ```bash
   cd jeepay-manager && mvn clean package -DskipTests
   ```

2. **Dockerfile路径：**
   ```
   jeepay-manager/Dockerfile.railway
   ```

3. **启动命令：**
   ```bash
   java -jar jeepay-manager/target/jeepay-manager.jar
   ```

### 方案三：预构建JAR方式

如果已经有构建好的jar文件，使用简单版本：

1. 将`jeepay-manager/Dockerfile.railway.simple`重命名为`Dockerfile.railway`
2. 确保jar文件在正确位置

## Railway部署配置示例

在Railway Dashboard中设置：

```yaml
# Dockerfile Path
jeepay-manager/Dockerfile.railway

# 启动命令（如果Dockerfile中已包含CMD）
# 不需要额外设置启动命令
```

## 验证构建

本地验证Dockerfile：

```bash
cd /workspace
docker build -f jeepay-manager/Dockerfile.railway -t jeepay-manager:test .
docker run -p 9217:9217 jeepay-manager:test
```

## 常见问题

### Q: 构建时间太长怎么办？
A: 首次构建需要下载所有Maven依赖。Railway会缓存层，后续构建会更快。

### Q: 内存不足构建失败？
A: 在Railway的Start Command中添加：
```bash
export MAVEN_OPTS="-Xmx512m"
```

### Q: 如何查看构建日志？
```bash
railway logs jeepay-manager --verbose
```

## 环境变量

确保配置以下环境变量：

| 变量 | 说明 | 示例 |
|------|------|------|
| `DB_HOST` | MySQL主机 | mysql.railway.internal |
| `DB_PORT` | MySQL端口 | 3306 |
| `DB_NAME` | 数据库名 | jeepaydb |
| `DB_USERNAME` | 用户名 | root |
| `DB_PASSWORD` | 密码 | yourpassword |
| `REDIS_HOST` | Redis主机 | redis.railway.internal |
| `REDIS_PORT` | Redis端口 | 6379 |
