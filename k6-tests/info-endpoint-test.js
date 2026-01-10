import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const responseTrend = new Trend('response_time');

// Test configuration
export const options = {
  stages: [
    { duration: '10s', target: 100 },   // Ramp up to 100 users over 10 seconds
    { duration: '30s', target: 3000 },  // Ramp up to 3000 users over 30 seconds
    { duration: '20s', target: 3000 },  // Stay at 3000 users for 20 seconds
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests should be below 2s
    errors: ['rate<0.1'],               // Error rate should be less than 10%
    http_req_failed: ['rate<0.1'],     // Failed requests should be less than 10%
  },
};

// Base URL - change this to match your environment
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // Make GET request to /api/info endpoint
  const response = http.get(`${BASE_URL}/api/info`, {
    tags: { name: 'InfoEndpoint' },
  });

  // Check if the response is successful
  const checkRes = check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 2000ms': (r) => r.timings.duration < 2000,
    'response has content': (r) => r.body && r.body.length > 0,
  });

  // Record custom metrics
  errorRate.add(!checkRes);
  responseTrend.add(response.timings.duration);

  // Optional: Add a small delay between requests (simulate real user behavior)
  sleep(0.1);
}

// Setup function - runs once before the test
export function setup() {
  console.log('Starting performance test for /api/info endpoint');
  console.log(`Target URL: ${BASE_URL}/api/info`);
  console.log('Test duration: 60 seconds with 3000 concurrent users');
}

// Teardown function - runs once after the test
export function teardown(data) {
  console.log('Performance test completed');
}

