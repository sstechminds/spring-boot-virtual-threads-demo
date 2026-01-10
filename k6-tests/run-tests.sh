#!/bin/bash

# Performance Test Runner for Spring Boot Web Demo
# This script helps you run k6 performance tests easily

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Default values
BASE_URL="${BASE_URL:-http://localhost:8080}"
TEST_SCRIPT="info-endpoint-test.js"

echo -e "${GREEN}Spring Boot Web Demo - k6 Performance Test Runner${NC}"
echo "=================================================="
echo ""

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}Error: k6 is not installed${NC}"
    echo ""
    echo "Install k6:"
    echo "  macOS:   brew install k6"
    echo "  Linux:   See https://k6.io/docs/get-started/installation/"
    echo "  Windows: choco install k6"
    echo ""
    exit 1
fi

# Check if application is running
echo -e "${YELLOW}Checking if application is running at ${BASE_URL}...${NC}"
if ! curl -s -f "${BASE_URL}/api/info" > /dev/null 2>&1; then
    echo -e "${RED}Error: Application is not responding at ${BASE_URL}/api/info${NC}"
    echo ""
    echo "Please start the application first:"
    echo "  mvn spring-boot:run"
    echo ""
    exit 1
fi
echo -e "${GREEN}✓ Application is running${NC}"
echo ""

# Show menu
echo "Select test to run:"
echo "  1) Standard Load Test (3000 users over 1 minute)"
echo "  2) Spike Test (sudden spike to 3000 users)"
echo "  3) Soak Test (1000 users for 10 minutes)"
echo "  4) Custom test"
echo ""
read -p "Enter choice [1-4]: " choice

case $choice in
    1)
        TEST_SCRIPT="info-endpoint-test.js"
        echo -e "${GREEN}Running standard load test...${NC}"
        ;;
    2)
        TEST_SCRIPT="spike-test.js"
        echo -e "${GREEN}Running spike test...${NC}"
        ;;
    3)
        TEST_SCRIPT="soak-test.js"
        echo -e "${GREEN}Running soak test (this will take ~14 minutes)...${NC}"
        ;;
    4)
        read -p "Enter test script name: " TEST_SCRIPT
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo "Test Configuration:"
echo "  Base URL: ${BASE_URL}"
echo "  Test Script: ${TEST_SCRIPT}"
echo ""
echo -e "${YELLOW}Starting test in 3 seconds...${NC}"
sleep 3

# Run the test
cd "$(dirname "$0")"
k6 run -e BASE_URL="${BASE_URL}" "${TEST_SCRIPT}"

echo ""
echo -e "${GREEN}Test completed!${NC}"

# If spike test was run, show where the report is
if [ "$TEST_SCRIPT" = "spike-test.js" ]; then
    echo -e "${GREEN}HTML report generated: summary.html${NC}"
    echo "Open it with: open summary.html (macOS) or xdg-open summary.html (Linux)"
fi

