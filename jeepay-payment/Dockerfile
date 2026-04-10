#
#   jeepay-payment（支付网关） java程序镜像构建脚本
#

# Eclipse Temurin JRE 17（openjdk 官方镜像已停止维护）
FROM eclipse-temurin:17-jre

LABEL maintainer="Terrfly"

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai

EXPOSE 9216

VOLUME ["/jeepayhomes/service/logs", "/jeepayhomes/service/uploads"]

RUN mkdir -p /jeepayhomes/service/app

COPY ./target/jeepay-payment.jar /jeepayhomes/service/app/jeepay-payment.jar

WORKDIR /jeepayhomes/service/app

# 显式指定外部配置路径，便于 Docker 部署时挂载覆盖
CMD ["java", "-jar", "jeepay-payment.jar", "--spring.config.additional-location=file:/jeepayhomes/service/app/application.yml"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -sf http://localhost:9216/ || exit 1
