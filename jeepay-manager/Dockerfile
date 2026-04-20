#
#   jeepay-manager（运营平台） java程序镜像构建脚本
#

# Eclipse Temurin JRE 17（openjdk 官方镜像已停止维护）
ARG BASE_IMAGE=swr.cn-south-1.myhuaweicloud.com/jeepay/eclipse-temurin:17-jre
FROM ${BASE_IMAGE}

LABEL maintainer="Terrfly"

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai

EXPOSE 9217

VOLUME ["/jeepayhomes/service/logs", "/jeepayhomes/service/uploads"]

RUN mkdir -p /jeepayhomes/service/app

COPY ./target/jeepay-manager.jar /jeepayhomes/service/app/jeepay-manager.jar

WORKDIR /jeepayhomes/service/app

CMD ["java", "-jar", "jeepay-manager.jar", "--spring.config.additional-location=file:/jeepayhomes/service/app/application.yml"]

# 端口探测，不依赖 curl/wget 等外部工具
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'echo > /dev/tcp/localhost/9217'
