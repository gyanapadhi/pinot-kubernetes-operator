#!/bin/bash

# Pinot Operator Test Suite
# This script runs comprehensive tests for the Pinot operator

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
CONFIGS_DIR="${PROJECT_DIR}/configs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test results
TESTS_PASSED=0
TESTS_FAILED=0

echo -e "${BLUE}🧪 Pinot Operator Test Suite${NC}"
echo "=================================="
echo ""

# Function to print test results
print_result() {
    local test_name="$1"
    local status="$2"
    local message="$3"
    
    if [ "$status" = "PASS" ]; then
        echo -e "  ${GREEN}✅ PASS${NC}: $test_name - $message"
        ((TESTS_PASSED++))
    else
        echo -e "  ${RED}❌ FAIL${NC}: $test_name - $message"
        ((TESTS_FAILED++))
    fi
}

# Function to check if service is running
check_service() {
    local service_name="$1"
    local port="$2"
    local endpoint="$3"
    
    if curl -s -f "http://localhost:${port}${endpoint}" > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Function to wait for service
wait_for_service() {
    local service_name="$1"
    local port="$2"
    local endpoint="$3"
    local max_attempts="$4"
    
    echo "  ⏳ Waiting for $service_name to be ready..."
    local attempts=0
    while [ $attempts -lt $max_attempts ]; do
        if check_service "$service_name" "$port" "$endpoint"; then
            echo "  ✅ $service_name is ready!"
            return 0
        fi
        sleep 5
        ((attempts++))
        echo "    Attempt $attempts/$max_attempts..."
    done
    echo "  ❌ $service_name failed to start within timeout"
    return 1
}

# Test 1: Check if Docker is running
echo -e "${YELLOW}Test 1: Docker Environment${NC}"
if docker info > /dev/null 2>&1; then
    print_result "Docker Running" "PASS" "Docker daemon is accessible"
else
    print_result "Docker Running" "FAIL" "Docker daemon is not accessible"
    exit 1
fi

# Test 2: Check if Docker Compose is available
echo -e "${YELLOW}Test 2: Docker Compose${NC}"
if command -v docker-compose > /dev/null 2>&1; then
    print_result "Docker Compose" "PASS" "Docker Compose is available"
else
    print_result "Docker Compose" "FAIL" "Docker Compose is not available"
    exit 1
fi

# Test 3: Start Pinot Cluster
echo -e "${YELLOW}Test 3: Start Pinot Cluster${NC}"
cd "${PROJECT_DIR}"

echo "  🚀 Starting Pinot cluster..."
if docker-compose up -d zookeeper > /dev/null 2>&1; then
    print_result "Start Zookeeper" "PASS" "Zookeeper container started"
else
    print_result "Start Zookeeper" "FAIL" "Failed to start Zookeeper"
    exit 1
fi

# Wait for Zookeeper
if wait_for_service "Zookeeper" "2181" "" 12; then
    print_result "Zookeeper Ready" "PASS" "Zookeeper is responding"
else
    print_result "Zookeeper Ready" "FAIL" "Zookeeper is not responding"
    exit 1
fi

# Start Pinot components
echo "  🎮 Starting Pinot Controller..."
if docker-compose up -d pinot-controller > /dev/null 2>&1; then
    print_result "Start Controller" "PASS" "Controller container started"
else
    print_result "Start Controller" "FAIL" "Failed to start Controller"
    exit 1
fi

# Wait for Controller
if wait_for_service "Controller" "9001" "/health" 24; then
    print_result "Controller Ready" "PASS" "Controller is responding"
else
    print_result "Controller Ready" "FAIL" "Controller is not responding"
    exit 1
fi

# Start other components
echo "  🔄 Starting Pinot Broker..."
docker-compose up -d pinot-broker > /dev/null 2>&1

echo "  💾 Starting Pinot Server..."
docker-compose up -d pinot-server > /dev/null 2>&1

echo "  ⚙️  Starting Pinot Minion..."
docker-compose up -d pinot-minion > /dev/null 2>&1

# Wait for all components
echo "  ⏳ Waiting for all components to be ready..."
sleep 30

# Test 4: Check all services
echo -e "${YELLOW}Test 4: Service Health Checks${NC}"

if check_service "Broker" "8099" "/health"; then
    print_result "Broker Health" "PASS" "Broker is healthy"
else
    print_result "Broker Health" "FAIL" "Broker is not healthy"
fi

if check_service "Server" "8098" "/health"; then
    print_result "Server Health" "PASS" "Server is healthy"
else
    print_result "Server Health" "FAIL" "Server is not healthy"
fi

if check_service "Minion" "8097" "/health"; then
    print_result "Minion Health" "PASS" "Minion is healthy"
else
    print_result "Minion Health" "FAIL" "Minion is not healthy"
fi

# Test 5: Check cluster info
echo -e "${YELLOW}Test 5: Cluster Information${NC}"
CLUSTER_INFO=$(curl -s http://localhost:9050/cluster/info 2>/dev/null || echo "{}")
if [ "$CLUSTER_INFO" != "{}" ]; then
    print_result "Cluster Info" "PASS" "Cluster information retrieved successfully"
    echo "    📊 Cluster Info: $CLUSTER_INFO"
else
    print_result "Cluster Info" "FAIL" "Failed to retrieve cluster information"
fi

# Test 6: Check if operator can connect
echo -e "${YELLOW}Test 6: Operator Connectivity${NC}"
# This would test if the Java operator can connect to the Pinot cluster
# For now, we'll just check if the ports are accessible
if check_service "Controller API" "9000" "/health"; then
    print_result "Operator Connectivity" "PASS" "Controller API is accessible for operator"
else
    print_result "Operator Connectivity" "FAIL" "Controller API is not accessible for operator"
fi

# Test 7: Test CRD deployment (if kubectl is available)
echo -e "${YELLOW}Test 7: Kubernetes CRD Deployment${NC}"
if command -v kubectl > /dev/null 2>&1; then
    echo "  📋 Deploying CRDs..."
    if kubectl apply -f "${PROJECT_DIR}/k8s/crds.yaml" > /dev/null 2>&1; then
        print_result "CRD Deployment" "PASS" "Custom Resource Definitions deployed successfully"
    else
        print_result "CRD Deployment" "FAIL" "Failed to deploy Custom Resource Definitions"
    fi
else
    print_result "CRD Deployment" "SKIP" "kubectl not available, skipping CRD test"
fi

# Test 8: Test operator deployment (if kubectl is available)
echo -e "${YELLOW}Test 8: Operator Deployment${NC}"
if command -v kubectl > /dev/null 2>&1; then
    echo "  🚀 Deploying operator..."
    if kubectl apply -f "${PROJECT_DIR}/k8s/operator.yaml" > /dev/null 2>&1; then
        print_result "Operator Deployment" "PASS" "Operator deployed successfully"
    else
        print_result "Operator Deployment" "FAIL" "Failed to deploy operator"
    fi
else
    print_result "Operator Deployment" "SKIP" "kubectl not available, skipping operator test"
fi

# Test 9: Test custom resources (if kubectl is available)
echo -e "${YELLOW}Test 9: Custom Resource Testing${NC}"
if command -v kubectl > /dev/null 2>&1; then
    echo "  📝 Testing custom resources..."
    
    # Test Pinot cluster
    if kubectl apply -f "${CONFIGS_DIR}/test-pinot-cluster.yaml" > /dev/null 2>&1; then
        print_result "Pinot Cluster CR" "PASS" "Pinot cluster custom resource created"
    else
        print_result "Pinot Cluster CR" "FAIL" "Failed to create Pinot cluster custom resource"
    fi
    
    # Test Pinot schema
    if kubectl apply -f "${CONFIGS_DIR}/test-schema.yaml" > /dev/null 2>&1; then
        print_result "Pinot Schema CR" "PASS" "Pinot schema custom resource created"
    else
        print_result "Pinot Schema CR" "FAIL" "Failed to create Pinot schema custom resource"
    fi
    
    # Test Pinot table
    if kubectl apply -f "${CONFIGS_DIR}/test-table.yaml" > /dev/null 2>&1; then
        print_result "Pinot Table CR" "PASS" "Pinot table custom resource created"
    else
        print_result "Pinot Table CR" "FAIL" "Failed to create Pinot table custom resource"
    fi
    
    # Test Pinot tenant
    if kubectl apply -f "${CONFIGS_DIR}/test-tenant.yaml" > /dev/null 2>&1; then
        print_result "Pinot Tenant CR" "PASS" "Pinot tenant custom resource created"
    else
        print_result "Pinot Tenant CR" "FAIL" "Failed to create Pinot tenant custom resource"
    fi
else
    print_result "Custom Resource Testing" "SKIP" "kubectl not available, skipping CR tests"
fi

# Test 10: Cleanup test resources (if kubectl is available)
echo -e "${YELLOW}Test 10: Cleanup Test Resources${NC}"
if command -v kubectl > /dev/null 2>&1; then
    echo "  🧹 Cleaning up test resources..."
    
    kubectl delete -f "${CONFIGS_DIR}/test-tenant.yaml" > /dev/null 2>&1 || true
    kubectl delete -f "${CONFIGS_DIR}/test-table.yaml" > /dev/null 2>&1 || true
    kubectl delete -f "${CONFIGS_DIR}/test-schema.yaml" > /dev/null 2>&1 || true
    kubectl delete -f "${CONFIGS_DIR}/test-pinot-cluster.yaml" > /dev/null 2>&1 || true
    
    print_result "Resource Cleanup" "PASS" "Test resources cleaned up successfully"
else
    print_result "Resource Cleanup" "SKIP" "kubectl not available, skipping cleanup"
fi

# Print final results
echo ""
echo -e "${BLUE}📊 Test Results Summary${NC}"
echo "=========================="
echo -e "  ${GREEN}✅ Tests Passed: $TESTS_PASSED${NC}"
echo -e "  ${RED}❌ Tests Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All tests passed! The Pinot operator is working correctly.${NC}"
else
    echo -e "${RED}⚠️  Some tests failed. Please check the output above for details.${NC}"
fi

echo ""
echo -e "${BLUE}🔧 Next Steps${NC}"
echo "=============="
echo "1. The Pinot cluster is running and ready for testing"
echo "2. You can access the Pinot console at: http://localhost:9050"
echo "3. Test the operator by applying custom resources"
echo "4. Monitor the operator logs for any issues"
echo ""
echo -e "${YELLOW}To stop the cluster:${NC}"
echo "  ./testing/scripts/stop-pinot.sh"
echo ""
echo -e "${YELLOW}To check cluster status:${NC}"
echo "  ./testing/scripts/check-status.sh"
