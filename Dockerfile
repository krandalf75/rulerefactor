# syntax=docker/dockerfile:1

FROM amazoncorretto:21 AS build
WORKDIR /workspace

RUN yum install -y maven \
    && yum clean all

COPY pom.xml ./
COPY src ./src
COPY README.md ./README.md
COPY docs ./docs

RUN mvn -B clean test package

FROM amazoncorretto:21-alpine AS runtime
WORKDIR /app

COPY --from=build /workspace/target/rulerefactor-0.1.0-SNAPSHOT.jar ./rulerefactor.jar

ENTRYPOINT ["java", "-jar", "/app/rulerefactor.jar"]
