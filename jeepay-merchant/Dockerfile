#
#   jeepay-merchant（商户系统） java程序镜像构建脚本
#

# Eclipse Temurin JRE 17（openjdk 官方镜像已停止维护）
FROM eclipse-temurin:17-jre

LABEL maintainer="Terrfly"

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai

EXPOSE 9218

VOLUME ["/jeepayhomes/service/logs", "/jeepayhomes/service/uploads"]

RUN mkdir -p /jeepayhomes/service/app

COPY ./target/jeepay-merchant.jar /jeepayhomes/service/app/jeepay-merchant.jar

WORKDIR /jeepayhomes/service/app

CMD ["java", "-jar", "jeepay-merchant.jar", "--spring.config.additional-location=file:/jeepayhomes/service/app/application.yml"]

# 端口探测，不依赖 curl/wget 等外部工具
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'echo > /dev/tcp/localhost/9218'
