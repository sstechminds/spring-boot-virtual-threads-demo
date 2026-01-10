#!/bin/bash

# Quick test runner - runs the main performance test immediately
# Usage: ./quick-test.sh [BASE_URL]

BASE_URL="${1:-http://localhost:8080}"

echo "Running k6 performance test..."
echo "Target: ${BASE_URL}/api/info"
echo "Load: 3000 users over 60 seconds"
echo ""

cd "$(dirname "$0")"
k6 run -e BASE_URL="${BASE_URL}" info-endpoint-test.js

