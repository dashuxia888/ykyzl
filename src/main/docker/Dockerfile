FROM openjdk:8-jdk-alpine

LABEL maintainer="MasterPan <i@hvv.me>"

# Change TimeZone to CST
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add -U --no-cache tzdata
ENV TZ Asia/Shanghai

ADD *.jar app.jar
EXPOSE 8080

ENV JAVA_OPTS="-Xmx1344M -Xms1344M -Xmn448M -XX:MaxMetaspaceSize=192M -XX:MetaspaceSize=192M"

ENTRYPOINT java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar