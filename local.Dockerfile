FROM amazoncorretto:25-alpine-jdk AS builder
RUN apk add --no-cache maven
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package

FROM amazoncorretto:25-alpine-jdk

WORKDIR /
COPY --from=builder /workspace/target/*.jar app.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=local","/app.jar"]