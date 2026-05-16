#
#   Jeepay 多阶段构建 Dockerfiles
#   适用于 GitHub Actions、GitLab CI 等 CI/CD 平台
#

# ==================== Jeepay Merchant (商户平台) ====================
FROM maven:3.9-eclipse-temurin-17 AS merchant-builder

WORKDIR /build

COPY pom.xml .
COPY jeepay-core pom.xml jeepay-core/
COPY jeepay-service pom.xml jeepay-service/
COPY jeepay-components pom.xml jeepay-components/
COPY jeepay-z-codegen pom.xml jeepay-z-codegen/
COPY jeepay-merchant pom.xml jeepay-merchant/

RUN mvn -B -DskipTests clean package -pl jeepay-merchant -am

FROM eclipse-temurin:17-jre

LABEL maintainer="jeepay"
description="Jeepay Merchant Portal"

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 9218

RUN mkdir -p /jeepayhomes/service/app \
    /jeepayhomes/service/logs \
    /jeepayhomes/service/uploads

COPY --from=merchant-builder /build/jeepay-merchant/target/jeepay-merchant.jar /jeepayhomes/service/app/jeepay-merchant.jar

WORKDIR /jeepayhomes/service/app

CMD ["sh", "-c", "java $JAVA_OPTS -jar jeepay-merchant.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=12 \
    CMD curl -f http://localhost:9218/ || exit 1
