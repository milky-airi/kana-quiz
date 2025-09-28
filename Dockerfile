# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
COPY --from=build /app/target/kana-quiz-*.jar /app/app.jar
CMD ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

