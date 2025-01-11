#
#   jeepay-manager（管理平台） java程序镜像构建脚本   .Power by terrfly
#

# ------   START   ------

# 基础镜像来自： https://hub.docker.com/ 官方OFFICIAL
FROM openjdk:17

MAINTAINER Terrfly

# 配置环境变量，支持中文。
ENV LANG=C.UTF-8

# 设置时区 东八区， 解决日志时间不正确的问题。
ENV TZ=Asia/Shanghai

# 对外映射的端口 ( 不明确EXPOSE 也不影响映射  )
EXPOSE 9217

# 挂载目录 "/jeepayhomes/service/app/application.yml 这个文件不能写到VOLUME中， 否则将映射成为了目录导致启动异常。
VOLUME ["/jeepayhomes/service/logs", "/jeepayhomes/service/uploads"]

# 使用jeepay用户启动。 ( 需要 RUN adduser等一系列操作 )
# USER jeepay:jeepay

# 创建目录
RUN mkdir /jeepayhomes/service/app -p

# 安装
COPY ./target/jeepay-manager.jar /jeepayhomes/service/app/jeepay-manager.jar

# 设置工作目录
WORKDIR /jeepayhomes/service/app

CMD ["java", "-jar", "jeepay-manager.jar"]

# ------   END   ------




