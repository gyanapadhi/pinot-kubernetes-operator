#!/bin/bash

# Stop Pinot Cluster Script
# This script stops the local Pinot cluster

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "🛑 Stopping Pinot Cluster..."

# Navigate to project directory
cd "${PROJECT_DIR}"

# Stop all services
echo "⏹️  Stopping all Pinot services..."
docker-compose down

# Remove volumes if requested
if [ "$1" = "clean" ]; then
    echo "🧹 Removing volumes..."
    docker-compose down -v
    echo "✅ Volumes removed"
else
    echo "💾 Volumes preserved (use 'clean' argument to remove them)"
fi

echo "✅ Pinot cluster stopped!"
echo ""
echo "📊 To start again:"
echo "   ./testing/scripts/start-pinot.sh"
echo "   ./testing/scripts/start-pinot.sh quickstart"
