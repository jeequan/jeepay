# syntax = docker/dockerfile:experimental
# 使用了 Docker 特性 Buildx 请开启相关特性

# 切换 JDK 请修改后面的 17 到对应版本
# docker build -t jeepay-deps:latest -f docs/Dockerfile .

# 编译依赖缓存，请先执行上方命令
FROM jeepay-deps:latest AS builder

WORKDIR /workspace

COPY . /workspace

RUN mkdir -p /root/.m2
COPY ./docs/settings.xml /root/.m2/settings.xml

RUN mvn clean package -Dmaven.test.skip=true -Ptest

# 以下为运行容器 切换 JDK 到对应版本
# jdk8对应：mcr.microsoft.com/java/jre:8-zulu-alpine
# jdk17对应：mcr.microsoft.com/java/jre:17-zulu-alpine
FROM mcr.microsoft.com/java/jre:8-zulu-alpine

ARG PLATFORM=$PLATFORM

WORKDIR /workspace

COPY --from=builder /workspace/jeepay-${PLATFORM}/target/jeepay-${PLATFORM}.jar /workspace/jeepay-app.jar
COPY --from=builder /workspace/conf/${PLATFORM}/application.yml /workspace/application.yml

EXPOSE $PORT

CMD ["java", "-jar", "/workspace/jeepay-app.jar"]

# 编译命令
# docker buildx build . --build-arg PORT=9216 --build-arg PLATFORM=payment -t jeepay-payment:latest
# docker buildx build . --build-arg PORT=9217 --build-arg PLATFORM=manager -t jeepay-manager:latest
# docker buildx build . --build-arg PORT=9218 --build-arg PLATFORM=merchant -t jeepay-merchant:latest
#
# 如果你需要多平台镜像，你可以使用 --platform linux/amd64,linux/arm64
# 比如 docker buildx build . --build-arg PORT=9218 --build-arg PLATFORM=merchant -t jeepay-merchant:latest --platform linux/amd64,linux/arm64
#
# 启动命令
# docker run -d -p 9216:9216 jeepay-payment:latest
# docker run -d -p 9217:9217 jeepay-manager:latest
# docker run -d -p 9218:9218 jeepay-merchant:latest