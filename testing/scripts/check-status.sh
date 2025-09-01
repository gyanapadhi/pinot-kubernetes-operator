#!/bin/bash

# Check Pinot Cluster Status Script
# This script checks the status of the local Pinot cluster

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "📊 Checking Pinot Cluster Status..."
echo "=================================="

# Navigate to project directory
cd "${PROJECT_DIR}"

# Check Docker Compose services
echo "🐳 Docker Services Status:"
docker-compose ps

echo ""
echo "🔍 Health Checks:"
echo "================="

    # Check Zookeeper
    echo "🦘 Zookeeper (localhost:2182):"
    if curl -s http://localhost:2182 > /dev/null 2>&1; then
        echo "   ✅ Running"
    else
        echo "   ❌ Not accessible"
    fi

    # Check Pinot Controller
    echo "🎮 Pinot Controller (http://localhost:9050):"
    if curl -s -f http://localhost:9050/health > /dev/null 2>&1; then
        echo "   ✅ Healthy"
        # Get cluster info
        CLUSTER_INFO=$(curl -s http://localhost:9050/cluster/info 2>/dev/null || echo "{}")
        echo "   📊 Cluster Info: $CLUSTER_INFO"
    else
        echo "   ❌ Not healthy"
    fi

# Check Pinot Broker
echo "🔄 Pinot Broker (http://localhost:8099):"
if curl -s -f http://localhost:8099/health > /dev/null 2>&1; then
    echo "   ✅ Healthy"
else
    echo "   ❌ Not healthy"
fi

# Check Pinot Server
echo "💾 Pinot Server (http://localhost:8098):"
if curl -s -f http://localhost:8098/health > /dev/null 2>&1; then
    echo "   ✅ Healthy"
else
    echo "   ❌ Not healthy"
fi

# Check Pinot Minion
echo "⚙️  Pinot Minion (http://localhost:8097):"
if curl -s -f http://localhost:8097/health > /dev/null 2>&1; then
    echo "   ✅ Healthy"
else
    echo "   ❌ Not healthy"
fi

echo ""
echo "📝 Recent Logs:"
echo "==============="

# Show recent logs for each service
echo "🎮 Controller logs (last 5 lines):"
docker-compose logs --tail=5 pinot-controller 2>/dev/null || echo "   No logs available"

echo ""
echo "🔄 Broker logs (last 5 lines):"
docker-compose logs --tail=5 pinot-broker 2>/dev/null || echo "   No logs available"

echo ""
echo "💾 Server logs (last 5 lines):"
docker-compose logs --tail=5 pinot-server 2>/dev/null || echo "   No logs available"

echo ""
echo "🌐 Access URLs:"
echo "==============="
echo "   - Pinot Console: http://localhost:9050"
echo "   - Broker: http://localhost:8099"
echo "   - Server: http://localhost:8098"
echo "   - Minion: http://localhost:8097"
echo "   - Zookeeper: localhost:2181"
