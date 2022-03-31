FROM maven:3-eclipse-temurin-17

WORKDIR /build
COPY . .

RUN mkdir -p /root/.m2
COPY ./docs/settings.xml /root/.m2/settings.xml

RUN mvn dependency:go-offline