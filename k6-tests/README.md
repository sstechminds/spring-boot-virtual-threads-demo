# k6 Performance Tests

This directory contains k6 performance tests for the Spring Boot Web Demo application.

## Prerequisites

Install k6:

**macOS:**
```bash
brew install k6
```

**Linux:**
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

**Windows:**
```powershell
choco install k6
```

Or download from: https://k6.io/docs/get-started/installation/

## Test Scripts

### 1. info-endpoint-test.js
Main performance test that ramps up to 3000 concurrent users over 1 minute.

**Run the test:**
```bash
k6 run info-endpoint-test.js
```

**Run with custom base URL:**
```bash
k6 run -e BASE_URL=http://production-server:8080 info-endpoint-test.js
```

**Stages:**
- 10s: Ramp up to 100 users
- 30s: Ramp up to 3000 users
- 20s: Stay at 3000 users

**Thresholds:**
- 95% of requests should complete in < 2s
- Error rate should be < 10%

### 2. spike-test.js
Spike test that suddenly increases load to 3000 users to test system resilience.

**Run the test:**
```bash
k6 run spike-test.js
```

**Generates HTML report:** `summary.html`

### 3. soak-test.js
Endurance test that maintains 1000 users for 10 minutes to detect memory leaks and performance degradation.

**Run the test:**
```bash
k6 run soak-test.js
```

## Running Tests

### Before Running Tests

1. **Start the application:**
```bash
cd /Users/g3753/IdeaProjects/spring-boot-virtual-thread-demo
mvn spring-boot:run
```

2. **Verify the endpoint is accessible:**
```bash
curl http://localhost:8080/api/info
```

### Basic Test Execution

```bash
# Run the main performance test
k6 run info-endpoint-test.js

# Run with more virtual users
k6 run --vus 5000 --duration 60s info-endpoint-test.js

# Run with output to InfluxDB (if you have it set up)
k6 run --out influxdb=http://localhost:8086/k6 info-endpoint-test.js
```

### Advanced Options

```bash
# Run with custom environment variables
k6 run -e BASE_URL=http://localhost:8080 -e DURATION=2m info-endpoint-test.js

# Run with JSON output
k6 run --out json=test-results.json info-endpoint-test.js

# Run in cloud (requires k6 cloud account)
k6 cloud info-endpoint-test.js
```

## Understanding Results

### Key Metrics

- **http_req_duration**: Time for complete request/response
  - p(95): 95th percentile - 95% of requests were faster than this
  - p(99): 99th percentile - 99% of requests were faster than this

- **http_req_waiting**: Time waiting for response (TTFB)

- **http_reqs**: Total number of HTTP requests

- **vus**: Current number of virtual users

- **errors**: Custom error rate metric

### Sample Output

```
     ✓ status is 200
     ✓ response time < 2000ms
     ✓ response has content

     checks.........................: 100.00% ✓ 45000    ✗ 0
     data_received..................: 1.2 GB  20 MB/s
     data_sent......................: 3.6 MB  60 kB/s
     http_req_duration..............: avg=145ms min=45ms med=120ms max=1.2s p(95)=350ms p(99)=800ms
     http_req_failed................: 0.00%   ✓ 0        ✗ 15000
     http_reqs......................: 15000   250/s
     vus............................: 3000    min=0      max=3000
```

## Best Practices

1. **Warm up the application** before running tests
2. **Monitor server resources** (CPU, memory, threads) during tests
3. **Run tests multiple times** to get consistent results
4. **Start with lower load** and gradually increase
5. **Test in isolation** - avoid running other heavy processes
6. **Document your results** with timestamps and configurations

## Monitoring Virtual Threads

While running the test, you can monitor the application:

```bash
# Check application metrics
curl http://localhost:8080/actuator/metrics

# Check health
curl http://localhost:8080/actuator/health

# Monitor JVM threads (in a separate terminal)
jcmd <pid> Thread.dump_to_file -format=json threads.json
```

## Troubleshooting

### Connection Refused
- Ensure the Spring Boot application is running
- Check the correct port (default: 8080)
- Verify firewall settings

### High Error Rate
- Check application logs
- Monitor server resources
- Consider reducing the number of virtual users
- Check if Google is blocking requests (rate limiting)

### Timeout Issues
- Increase thresholds in the test script
- Check network latency
- Monitor application response times

## CI/CD Integration

Add to your CI/CD pipeline:

```bash
# Run test and fail if thresholds are not met
k6 run --quiet info-endpoint-test.js

# Exit code will be non-zero if thresholds fail
```

## Next Steps

1. Set up monitoring (Prometheus + Grafana)
2. Configure k6 Cloud for distributed testing
3. Add more test scenarios (edge cases, error conditions)
4. Integrate with CI/CD pipeline
5. Set up automated performance regression testing

