FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

ARG MVN_COMMON_OPTS="-Dmaven.repo.local=/root/.m2 -Dmaven.resolver.transport=wagon -Djava.net.preferIPv4Stack=true"

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B ${MVN_COMMON_OPTS} dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B ${MVN_COMMON_OPTS} -Dmaven.test.skip=true package \
 && JAR="$(ls -1 target/*.jar | grep -v '\.original$' | head -n 1)" \
 && cp "$JAR" /tmp/app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /tmp/app.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
