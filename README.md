# Spring Boot Virtual Threads Demo

A Spring Boot web application to demonstrate MDC tracking with Open Telemetry with Virtual Threads!

## Prerequisites

- Java 25 or higher
- Maven 3.9+
- Spring Boot 4.0.1 (latest version)
  - https://medium.com/@sibinraziya/spring-boot-4-http-interfaces-declarative-rest-clients-with-httpexchange-a-complete-guide-3e4dd2686a1f
- Open Telemetry - Tracing & Metrics :
  - https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot
  - https://github.com/mhalbritter/spring-boot-and-opentelemetry
  - https://docs.spring.io/spring-boot/reference/actuator/tracing.html

## Build and Run

### Using Maven

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Using Java

```bash
# Build the JAR
mvn clean package

# Run the JAR
java -jar target/spring-boot-virtual-threads-demo-0.0.1-SNAPSHOT.jar
```

## API Endpoints

Once the application is running, you can access:

- **Hello endpoint**: http://localhost:8080/api/hello
- **Info endpoint**: http://localhost:8080/api/info
- **Health check**: http://localhost:8080/actuator/health
- **Actuator info**: http://localhost:8080/actuator/info

## Testing

```bash
# Run tests
mvn test
```

## Performance Testing

This project includes k6 performance tests to validate the application's scalability with virtual threads.

### Prerequisites

Install k6:
```bash
# macOS
brew install k6

# Or download from: https://k6.io/docs/get-started/installation/
```

### Quick Start

```bash
# Start the application

## Maven build and run
mvn spring-boot:run

## Docker build and run
docker build --progress plain -t virtual-threads-demo .
docker run virtual-threads-demo

# In another terminal, run the performance test
cd k6-tests
./quick-test.sh
```



### Available Tests

1. **Standard Load Test** - 3000 users over 60 seconds
   ```bash
   k6 run k6-tests/info-endpoint-test.js
   ```

2. **Spike Test** - Sudden spike to 3000 users with HTML report
   ```bash
   k6 run k6-tests/spike-test.js
   ```

3. **Soak Test** - 1000 users for 10 minutes (endurance test)
   ```bash
   k6 run k6-tests/soak-test.js
   ```

### Interactive Test Runner

```bash
cd k6-tests
./run-tests.sh
```

See [k6-tests/README.md](k6-tests/README.md) for detailed documentation.

## Virtual Threads Configuration

This application is configured to use Java 21 virtual threads for improved scalability:

- **Application-level**: `spring.threads.virtual.enabled=true`
- **RestClient**: Uses `JdkClientHttpRequestFactory` with `Executors.newVirtualThreadPerTaskExecutor()`

The performance tests demonstrate the benefits of virtual threads when handling thousands of concurrent requests.

## Project Structure

```
spring-boot-virtual-thread-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/demo/
│   │   │       ├── SpringBootVirtualThreadsDemoApplication.java
│   │   │       └── controller/
│   │   │           └── HelloController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/demo/
│               └── SpringBootVirtualThreadsDemoApplicationTests.java
└── pom.xml
```

## Configuration

The application runs on port 8080 by default. You can change this in `application.properties`:

```properties
server.port=8080
```

=======
# spring-boot-virtual-threads-demo



## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

* [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
* [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.com/rvangala/spring-boot-virtual-threads-demo.git
git branch -M main
git push -uf origin main

## References

https://www.baeldung.com/spring-6-virtual-threads

```
