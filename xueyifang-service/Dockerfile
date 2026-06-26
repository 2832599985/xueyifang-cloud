FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="xueyifang"

WORKDIR /app

COPY target/xueyifang-service-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8300

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseStringDeduplication"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]