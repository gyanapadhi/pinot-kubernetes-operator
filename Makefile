# Apache Pinot Control Plane Operator - Java
# Makefile for common operations

.PHONY: help build test clean package docker-build docker-run deploy-crds deploy-operator undeploy run-local

# Default target
help:
	@echo "Available targets:"
	@echo "  build         - Compile the project"
	@echo "  test          - Run tests"
	@echo "  clean         - Clean build artifacts"
	@echo "  package       - Build and package the application"
	@echo "  docker-build  - Build Docker image"
	@echo "  docker-run    - Run Docker container locally"
	@echo "  deploy-crds   - Deploy CRDs to Kubernetes"
	@echo "  deploy-operator - Deploy operator to Kubernetes"
	@echo "  undeploy      - Remove operator from Kubernetes"
	@echo "  run-local     - Run application locally"

# Build the project
build:
	mvn compile

# Run tests
test:
	mvn test

# Clean build artifacts
clean:
	mvn clean

# Build and package the application
package: clean
	mvn package

# Build Docker image
docker-build: package
	docker build -t pinot-kubernetes-operator:latest .

# Run Docker container locally
docker-run: docker-build
	docker run -p 8080:8080 -p 8081:8081 \
		-e KUBECONFIG=/tmp/kubeconfig \
		-v $(HOME)/.kube:/tmp/kubeconfig \
		pinot-kubernetes-operator:latest

# Deploy CRDs to Kubernetes
deploy-crds:
	kubectl apply -f k8s/crds.yaml

# Deploy operator to Kubernetes
deploy-operator:
	kubectl apply -f k8s/operator.yaml

# Undeploy operator from Kubernetes
undeploy:
	kubectl delete -f k8s/operator.yaml
	kubectl delete -f k8s/crds.yaml

# Run application locally
run-local: package
	java -jar target/pinot-kubernetes-operator-*.jar

# Check Kubernetes cluster
check-k8s:
	kubectl cluster-info
	kubectl get nodes

# View operator logs
logs:
	kubectl logs -f deployment/pinot-operator -n pinot-operator

# View operator status
status:
	kubectl get pods -n pinot-operator
	kubectl get crd | grep pinot.io

# Port forward for local access
port-forward:
	kubectl port-forward -n pinot-operator deployment/pinot-operator 8080:8080 8081:8081
