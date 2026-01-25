# --- Stage 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
# Copy the rest of the source code
COPY --chown=app:app . .
# Package the application into a JAR file
 RUN mvn -f pom.xml clean package -DskipTests

# --- Stage 2: Use an official JDK 25 image (e.g., eclipse-temurin:25) ---
FROM eclipse-temurin:25 AS runner
# Set the working directory in the final image
LABEL com.sstechminds.demo="java25 sb4"
WORKDIR /app
# Copy the JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar
RUN  echo "Compile and Run app in optimized mode to improve startup time..."
RUN java -Djarmode=tools -jar /app/app.jar extract --destination application
RUN cp -fr application/* .
# https://katyella.com/blog/java-25-performance-breakthrough-30-percent-cpu-reduction/
RUN java -XX:+UseCompactObjectHeaders -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar app.jar

EXPOSE 8080
# 75% ensures allocated memory and resources are used efficiently. AND, enough memory is reserved/left(25%) for the non-heap usage(avoids OutOfMemory errors).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XshowSettings:system", "-XX:AOTCache=app.aot", "-XX:+UseCompactObjectHeaders", "-jar", "app.jar"]
