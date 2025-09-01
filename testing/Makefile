# Pinot Operator Testing Makefile
# This Makefile provides convenient commands for testing the Pinot operator

.PHONY: help start stop status test clean data quickstart

# Default target
help:
	@echo "🧪 Pinot Operator Testing Commands"
	@echo "=================================="
	@echo ""
	@echo "🚀 Cluster Management:"
	@echo "  start        - Start full Pinot cluster"
	@echo "  quickstart   - Start Pinot in quickstart mode (all-in-one)"
	@echo "  stop         - Stop Pinot cluster"
	@echo "  stop-clean   - Stop cluster and remove volumes"
	@echo "  status       - Check cluster status"
	@echo ""
	@echo "🧪 Testing:"
	@echo "  test         - Run complete test suite"
	@echo "  test-crds    - Test CRD deployment"
	@echo "  test-operator - Test operator deployment"
	@echo "  test-resources - Test custom resources"
	@echo ""
	@echo "📊 Data and Monitoring:"
	@echo "  data         - Generate test data"
	@echo "  logs         - View service logs"
	@echo "  logs-follow  - Follow service logs"
	@echo ""
	@echo "🧹 Maintenance:"
	@echo "  clean        - Clean up all test resources"
	@echo "  reset        - Stop cluster, clean, and restart"

# Start full Pinot cluster
start:
	@echo "🚀 Starting full Pinot cluster..."
	@./scripts/start-pinot.sh

# Start Pinot in quickstart mode
quickstart:
	@echo "📦 Starting Pinot in quickstart mode..."
	@./scripts/start-pinot.sh quickstart

# Stop Pinot cluster
stop:
	@echo "🛑 Stopping Pinot cluster..."
	@./scripts/stop-pinot.sh

# Stop cluster and remove volumes
stop-clean:
	@echo "🧹 Stopping cluster and removing volumes..."
	@./scripts/stop-pinot.sh clean

# Check cluster status
status:
	@echo "📊 Checking cluster status..."
	@./scripts/check-status.sh

# Run complete test suite
test:
	@echo "🧪 Running complete test suite..."
	@./scripts/run-tests.sh

# Test CRD deployment
test-crds:
	@echo "📋 Testing CRD deployment..."
	@if command -v kubectl > /dev/null 2>&1; then \
		kubectl apply -f ../k8s/crds.yaml; \
		echo "✅ CRDs deployed successfully"; \
	else \
		echo "❌ kubectl not available, skipping CRD test"; \
	fi

# Test operator deployment
test-operator:
	@echo "🚀 Testing operator deployment..."
	@if command -v kubectl > /dev/null 2>&1; then \
		kubectl apply -f ../k8s/operator.yaml; \
		echo "✅ Operator deployed successfully"; \
	else \
		echo "❌ kubectl not available, skipping operator test"; \
	fi

# Test custom resources
test-resources:
	@echo "📝 Testing custom resources..."
	@if command -v kubectl > /dev/null 2>&1; then \
		kubectl apply -f configs/test-pinot-cluster.yaml; \
		kubectl apply -f configs/test-schema.yaml; \
		kubectl apply -f configs/test-table.yaml; \
		kubectl apply -f configs/test-tenant.yaml; \
		echo "✅ Custom resources deployed successfully"; \
	else \
		echo "❌ kubectl not available, skipping resource tests"; \
	fi

# Generate test data
data:
	@echo "📊 Generating test data..."
	@./scripts/generate-test-data.sh

# View service logs
logs:
	@echo "📝 Viewing service logs..."
	@docker-compose logs

# Follow service logs
logs-follow:
	@echo "📝 Following service logs..."
	@docker-compose logs -f

# Clean up all test resources
clean:
	@echo "🧹 Cleaning up test resources..."
	@if command -v kubectl > /dev/null 2>&1; then \
		kubectl delete -f configs/test-tenant.yaml 2>/dev/null || true; \
		kubectl delete -f configs/test-table.yaml 2>/dev/null || true; \
		kubectl delete -f configs/test-schema.yaml 2>/dev/null || true; \
		kubectl delete -f configs/test-pinot-cluster.yaml 2>/dev/null || true; \
		echo "✅ Test resources cleaned up"; \
	else \
		echo "⚠️  kubectl not available, skipping resource cleanup"; \
	fi
	@docker-compose down 2>/dev/null || true
	@echo "✅ Docker containers stopped"

# Reset everything
reset: stop-clean clean start
	@echo "🔄 Reset complete!"

# Show cluster info
info:
	@echo "📊 Cluster Information:"
	@echo "========================"
	@docker-compose ps
	@echo ""
	@echo "🌐 Access URLs:"
	@echo "  - Controller: http://localhost:9050"
	@echo "  - Broker: http://localhost:8099"
	@echo "  - Server: http://localhost:8098"
	@echo "  - Minion: http://localhost:8097"
	@echo "  - Zookeeper: localhost:2182"

# Health check
health:
	@echo "🔍 Health Check:"
	@echo "================"
	@curl -s -f http://localhost:9050/health > /dev/null && echo "✅ Controller: Healthy" || echo "❌ Controller: Unhealthy"
	@curl -s -f http://localhost:8099/health > /dev/null && echo "✅ Broker: Healthy" || echo "❌ Broker: Unhealthy"
	@curl -s -f http://localhost:8098/health > /dev/null && echo "✅ Server: Healthy" || echo "❌ Server: Unhealthy"
	@curl -s -f http://localhost:8097/health > /dev/null && echo "✅ Minion: Healthy" || echo "❌ Minion: Unhealthy"
