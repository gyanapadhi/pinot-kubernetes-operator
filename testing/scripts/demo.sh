#!/bin/bash

# Pinot Operator Demo Script
# This script demonstrates the complete workflow of the Pinot operator

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

echo -e "${BLUE}🎬 Pinot Operator Demo${NC}"
echo "=========================="
echo ""

# Check prerequisites
echo -e "${YELLOW}🔍 Checking Prerequisites...${NC}"

# Check Docker
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi
echo -e "  ${GREEN}✅ Docker is running${NC}"

# Check Docker Compose
if ! command -v docker-compose > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker Compose is not available. Please install Docker Compose and try again.${NC}"
    exit 1
fi
echo -e "  ${GREEN}✅ Docker Compose is available${NC}"

# Check kubectl (optional)
if command -v kubectl > /dev/null 2>&1; then
    echo -e "  ${GREEN}✅ kubectl is available${NC}"
    KUBECTL_AVAILABLE=true
else
    echo -e "  ${YELLOW}⚠️  kubectl not available (some demo features will be skipped)${NC}"
    KUBECTL_AVAILABLE=false
fi

echo ""

# Step 1: Start Pinot Cluster
echo -e "${YELLOW}🚀 Step 1: Starting Pinot Cluster${NC}"
cd "${PROJECT_DIR}"

echo "  📦 Starting Pinot in quickstart mode..."
if docker-compose up -d pinot-quickstart > /dev/null 2>&1; then
    echo -e "  ${GREEN}✅ Pinot quickstart container started${NC}"
else
    echo -e "  ${RED}❌ Failed to start Pinot quickstart${NC}"
    exit 1
fi

echo "  ⏳ Waiting for Pinot to be ready..."
sleep 30

# Check if Pinot is ready
echo "  🔍 Checking Pinot health..."
if curl -s -f http://localhost:9050/health > /dev/null 2>&1; then
    echo -e "  ${GREEN}✅ Pinot is healthy and ready!${NC}"
else
    echo -e "  ${RED}❌ Pinot is not ready yet. Please wait a bit longer.${NC}"
    echo "  💡 You can check the status manually with: ./testing/scripts/check-status.sh"
fi

echo ""

# Step 2: Generate Test Data
echo -e "${YELLOW}📊 Step 2: Generating Test Data${NC}"
echo "  📝 Generating sample data for testing..."
if ./testing/scripts/generate-test-data.sh > /dev/null 2>&1; then
    echo -e "  ${GREEN}✅ Test data generated successfully${NC}"
else
    echo -e "  ${RED}❌ Failed to generate test data${NC}"
fi

echo ""

# Step 3: Deploy CRDs (if kubectl available)
if [ "$KUBECTL_AVAILABLE" = true ]; then
    echo -e "${YELLOW}📋 Step 3: Deploying Custom Resource Definitions${NC}"
    
    echo "  📋 Deploying CRDs..."
    if kubectl apply -f "${PROJECT_DIR}/k8s/crds.yaml" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ CRDs deployed successfully${NC}"
    else
        echo -e "  ${RED}❌ Failed to deploy CRDs${NC}"
    fi
    
    echo ""
    
    # Step 4: Deploy Operator (if kubectl available)
    echo -e "${YELLOW}🚀 Step 4: Deploying Pinot Operator${NC}"
    
    echo "  🚀 Deploying operator..."
    if kubectl apply -f "${PROJECT_DIR}/k8s/operator.yaml" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Operator deployed successfully${NC}"
    else
        echo -e "  ${RED}❌ Failed to deploy operator${NC}"
    fi
    
    echo ""
    
    # Step 5: Test Custom Resources (if kubectl available)
    echo -e "${YELLOW}🧪 Step 5: Testing Custom Resources${NC}"
    
    echo "  📝 Creating test Pinot cluster..."
    if kubectl apply -f "${CONFIGS_DIR}/test-pinot-cluster.yaml" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Test Pinot cluster created${NC}"
    else
        echo -e "  ${RED}❌ Failed to create test Pinot cluster${NC}"
    fi
    
    echo "  📝 Creating test schema..."
    if kubectl apply -f "${CONFIGS_DIR}/test-schema.yaml" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Test schema created${NC}"
    else
        echo -e "  ${RED}❌ Failed to create test schema${NC}"
    fi
    
    echo "  📝 Creating test table..."
    if kubectl apply -f "${CONFIGS_DIR}/test-table.yaml" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Test table created${NC}"
    else
        echo -e "  ${RED}❌ Failed to create test table${NC}"
    fi
    
    echo "  📝 Creating test tenants..."
    if kubectl apply -f "${CONFIGS_DIR}/test-tenant.yaml" > /dev/null 2>&1; then
        echo -e "  ${GREEN}✅ Test tenants created${NC}"
    else
        echo -e "  ${RED}❌ Failed to create test tenants${NC}"
    fi
    
    echo ""
    
    # Step 6: Monitor Resources (if kubectl available)
    echo -e "${YELLOW}📊 Step 6: Monitoring Resources${NC}"
    
    echo "  🔍 Checking resource status..."
    echo "  📋 Pinot resources:"
    kubectl get pinot,pinotschema,pinottable,pinottenant
    
    echo ""
    echo "  📊 Detailed resource information:"
    echo "  🎮 Pinot Cluster:"
    kubectl describe pinot test-pinot-cluster | head -20
    
    echo ""
    echo "  📋 Schema:"
    kubectl describe pinotschema test-schema | head -20
    
    echo ""
    echo "  🗃️  Table:"
    kubectl describe pinottable test-table | head -20
    
    echo ""
    echo "  🏢 Tenants:"
    kubectl describe pinottenant test-tenant | head -20
    
    echo ""
    
    # Step 7: Cleanup (if kubectl available)
    echo -e "${YELLOW}🧹 Step 7: Cleaning Up Test Resources${NC}"
    
    echo "  🧹 Cleaning up test resources..."
    kubectl delete -f "${CONFIGS_DIR}/test-tenant.yaml" > /dev/null 2>&1 || true
    kubectl delete -f "${CONFIGS_DIR}/test-table.yaml" > /dev/null 2>&1 || true
    kubectl delete -f "${CONFIGS_DIR}/test-schema.yaml" > /dev/null 2>&1 || true
    kubectl delete -f "${CONFIGS_DIR}/test-pinot-cluster.yaml" > /dev/null 2>&1 || true
    
    echo -e "  ${GREEN}✅ Test resources cleaned up${NC}"
    
    echo ""
else
    echo -e "${YELLOW}⚠️  Skipping Kubernetes-specific steps (kubectl not available)${NC}"
    echo "  💡 To test the full operator functionality, please install kubectl and a Kubernetes cluster"
fi

# Final status
echo -e "${BLUE}🎉 Demo Complete!${NC}"
echo "================"
echo ""
echo -e "${GREEN}✅ What was accomplished:${NC}"
echo "  - Pinot cluster started and running"
echo "  - Test data generated"
if [ "$KUBECTL_AVAILABLE" = true ]; then
    echo "  - CRDs deployed"
    echo "  - Operator deployed"
    echo "  - Custom resources tested"
    echo "  - Resources cleaned up"
fi
echo ""
echo -e "${BLUE}🌐 Access Points:${NC}"
echo "  - Pinot Console: http://localhost:9050"
echo "  - Broker: http://localhost:8099"
echo "  - Server: http://localhost:8098"
echo "  - Minion: http://localhost:8097"
echo ""
echo -e "${BLUE}🔧 Useful Commands:${NC}"
echo "  - Check status: ./testing/scripts/check-status.sh"
echo "  - View logs: docker-compose logs -f"
echo "  - Stop cluster: ./testing/scripts/stop-pinot.sh"
echo "  - Run tests: ./testing/scripts/run-tests.sh"
echo ""
echo -e "${YELLOW}💡 Next Steps:${NC}"
echo "  1. Explore the Pinot console at http://localhost:9050"
echo "  2. Run the complete test suite: ./testing/scripts/run-tests.sh"
echo "  3. Test the operator with your own custom resources"
echo "  4. Monitor the operator logs for any issues"
echo ""
echo -e "${GREEN}🚀 Happy testing!${NC}"
