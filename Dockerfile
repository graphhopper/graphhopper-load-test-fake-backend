FROM maven:3.6.3-jdk-8 as build
COPY pom.xml .
COPY src/ src/
RUN mvn package -DskipTests=true

FROM openjdk:11.0.5-jre-slim
WORKDIR /opt/backend
COPY --from=build target/*.jar ./target/

ENTRYPOINT ["java"]
CMD ["-jar", "target/fake-server.jar"]
