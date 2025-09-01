#!/bin/bash

# Start Pinot Cluster Script
# This script starts a local Pinot cluster using Docker Compose

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="${PROJECT_DIR}/docker-compose.yml"

echo "🚀 Starting Pinot Cluster..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose > /dev/null 2>&1; then
    echo "❌ Docker Compose is not available. Please install Docker Compose and try again."
    exit 1
fi

# Navigate to project directory
cd "${PROJECT_DIR}"

# Check if we want to use quickstart or full cluster
if [ "$1" = "quickstart" ]; then
    echo "📦 Starting Pinot Quickstart (all-in-one)..."
    docker-compose up -d pinot-quickstart
    echo "⏳ Waiting for Pinot Quickstart to be ready..."
    sleep 30
    echo "✅ Pinot Quickstart is running!"
    echo "🌐 Access points:"
    echo "   - Controller: http://localhost:9050"
    echo "   - Broker: http://localhost:8099"
    echo "   - Server: http://localhost:8098"
    echo "   - Minion: http://localhost:8097"
else
    echo "🏗️  Starting full Pinot cluster..."
    
    # Start Zookeeper first
    echo "🦘 Starting Zookeeper..."
    docker-compose up -d zookeeper
    
    # Wait for Zookeeper to be ready
    echo "⏳ Waiting for Zookeeper to be ready..."
    while ! docker-compose exec -T zookeeper zkServer.sh status > /dev/null 2>&1; do
        sleep 5
        echo "   Still waiting for Zookeeper..."
    done
    echo "✅ Zookeeper is ready!"
    
    # Start Pinot components
    echo "🎮 Starting Pinot Controller..."
    docker-compose up -d pinot-controller
    
    echo "⏳ Waiting for Controller to be ready..."
    while ! curl -f http://localhost:9050/health > /dev/null 2>&1; do
        sleep 5
        echo "   Still waiting for Controller..."
    done
    echo "✅ Controller is ready!"
    
    echo "🔄 Starting Pinot Broker..."
    docker-compose up -d pinot-broker
    
    echo "💾 Starting Pinot Server..."
    docker-compose up -d pinot-server
    
    echo "⚙️  Starting Pinot Minion..."
    docker-compose up -d pinot-minion
    
    echo "⏳ Waiting for all components to be ready..."
    sleep 30
    
    echo "✅ Full Pinot cluster is running!"
    echo "🌐 Access points:"
    echo "   - Zookeeper: localhost:2182"
    echo "   - Controller: http://localhost:9050"
    echo "   - Broker: http://localhost:8099"
    echo "   - Server: http://localhost:8098"
    echo "   - Minion: http://localhost:8097"
fi

echo ""
echo "📊 Check cluster status:"
echo "   docker-compose ps"
echo ""
echo "📝 View logs:"
echo "   docker-compose logs -f [service-name]"
echo ""
echo "🛑 Stop cluster:"
echo "   docker-compose down"
