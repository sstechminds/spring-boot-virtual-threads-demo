#!/bin/sh

mvn clean install -DskipTests

cp target/*.jar app.jar

improve_startup_time_spring_way=false

if $improve_startup_time_spring_way; then
  (for f in $(seq 1 3); do wget -q -O - http://localhost:8080/users && break; sleep 5; done; killall -2 java) & java -XX:+UseCompactObjectHeaders -XX:AOTCacheOutput=aop.aot -jar app.jar || true
  java -XX:+UseCompactObjectHeaders -XX:AOTCache=aop.aot -jar app.jar
else
  echo "Compile and Run app in optimized mode to improve startup time..."
  # Reference: https://docs.spring.io/spring-boot/reference/packaging/aot-cache.html
  rm -r application
  java -Djarmode=tools -jar app.jar extract --destination application

  cd application

  java -XX:+UseCompactObjectHeaders -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -jar app.jar
  java -XX:+UseCompactObjectHeaders -XX:AOTCache=app.aot -jar app.jar
fi