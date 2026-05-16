#
#   Jeepay 多阶段构建 Dockerfiles
#   适用于 GitHub Actions、GitLab CI 等 CI/CD 平台
#

# ==================== Jeepay Payment Gateway ====================
FROM maven:3.9-eclipse-temurin-17 AS payment-builder

WORKDIR /build

COPY pom.xml .
COPY jeepay-core pom.xml jeepay-core/
COPY jeepay-service pom.xml jeepay-service/
COPY jeepay-components pom.xml jeepay-components/
COPY jeepay-z-codegen pom.xml jeepay-z-codegen/
COPY jeepay-payment pom.xml jeepay-payment/

RUN mvn -B -DskipTests clean package -pl jeepay-payment -am

FROM eclipse-temurin:17-jre

LABEL maintainer="jeepay"
description="Jeepay Payment Gateway Service"

ENV LANG=C.UTF-8
ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

EXPOSE 9216

RUN mkdir -p /jeepayhomes/service/app \
    /jeepayhomes/service/logs \
    /jeepayhomes/service/uploads

COPY --from=payment-builder /build/jeepay-payment/target/jeepay-payment.jar /jeepayhomes/service/app/jeepay-payment.jar

WORKDIR /jeepayhomes/service/app

CMD ["sh", "-c", "java $JAVA_OPTS -jar jeepay-payment.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=12 \
    CMD curl -f http://localhost:9216/ || exit 1
