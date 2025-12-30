FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/target/evenly-core-1.0.0.jar /app/evenly-core.jar
EXPOSE 8080
ENV DB_URL=jdbc:postgresql://db:5432/evenly
ENV DB_USER=evenly
ENV DB_PASSWORD=evenly
ENV JWT_SECRET=fe97b7ab529670b6f9ea2378a0ea6ad8
CMD ["java","-jar","/app/evenly-core.jar"]
