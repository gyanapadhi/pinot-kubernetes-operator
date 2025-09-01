# Pinot Operator Testing Guide

This directory contains comprehensive testing tools and configurations for the Pinot Kubernetes Operator.

## 🏗️ Testing Architecture

The testing setup consists of:

- **Local Pinot Cluster**: Docker-based Pinot deployment for testing
- **Test Configurations**: Sample CRDs and configurations
- **Test Scripts**: Automated testing and validation scripts
- **Test Suite**: Comprehensive end-to-end testing

## 📋 Prerequisites

Before running the tests, ensure you have:

- Docker Desktop running
- Docker Compose installed
- At least 8GB of available memory
- Ports 2181, 9000, 8099, 8098, 8097 available

## 🚀 Quick Start

### 1. Start Pinot Cluster (Quickstart Mode)

```bash
# Start Pinot in quickstart mode (all-in-one)
./testing/scripts/start-pinot.sh quickstart
```

### 2. Start Pinot Cluster (Full Mode)

```bash
# Start full Pinot cluster with separate components
./testing/scripts/start-pinot.sh
```

### 3. Run Complete Test Suite

```bash
# Run all tests
./testing/scripts/run-tests.sh
```

## 📁 Directory Structure

```
testing/
├── bin/                    # Pinot binaries (downloaded)
├── configs/               # Test configurations
│   ├── test-pinot-cluster.yaml
│   ├── test-schema.yaml
│   ├── test-table.yaml
│   └── test-tenant.yaml
├── scripts/               # Test scripts
│   ├── download-pinot.sh
│   ├── start-pinot.sh
│   ├── stop-pinot.sh
│   ├── check-status.sh
│   └── run-tests.sh
├── docker-compose.yml     # Docker Compose configuration
└── README.md             # This file
```

## 🔧 Scripts Overview

### Core Scripts

| Script | Purpose | Usage |
|--------|---------|-------|
| `download-pinot.sh` | Downloads Pinot binaries | `./testing/scripts/download-pinot.sh` |
| `start-pinot.sh` | Starts Pinot cluster | `./testing/scripts/start-pinot.sh [quickstart]` |
| `stop-pinot.sh` | Stops Pinot cluster | `./testing/scripts/stop-pinot.sh [clean]` |
| `check-status.sh` | Checks cluster health | `./testing/scripts/check-status.sh` |
| `run-tests.sh` | Runs complete test suite | `./testing/scripts/run-tests.sh` |

### Script Options

- **start-pinot.sh quickstart**: Starts all-in-one Pinot cluster
- **start-pinot.sh**: Starts full Pinot cluster with separate components
- **stop-pinot.sh clean**: Stops cluster and removes volumes

## 🧪 Test Suite

The comprehensive test suite covers:

### Test 1: Docker Environment
- Verifies Docker daemon is accessible
- Checks Docker Compose availability

### Test 2: Pinot Cluster Startup
- Starts Zookeeper container
- Waits for Zookeeper readiness
- Starts Pinot Controller
- Starts Pinot Broker, Server, and Minion
- Verifies all components are healthy

### Test 3: Service Health Checks
- Tests Controller health endpoint
- Tests Broker health endpoint
- Tests Server health endpoint
- Tests Minion health endpoint

### Test 4: Cluster Information
- Retrieves cluster information from Controller
- Validates cluster configuration

### Test 5: Operator Connectivity
- Verifies Controller API accessibility
- Tests operator connection capabilities

### Test 6: Kubernetes Integration (if kubectl available)
- Deploys Custom Resource Definitions
- Deploys the Pinot operator
- Tests custom resource creation
- Tests custom resource cleanup

## 📊 Monitoring and Debugging

### Check Cluster Status

```bash
./testing/scripts/check-status.sh
```

### View Service Logs

```bash
# View all logs
docker-compose logs

# View specific service logs
docker-compose logs -f pinot-controller
docker-compose logs -f pinot-broker
docker-compose logs -f pinot-server
docker-compose logs -f pinot-minion
```

### Access Pinot Console

- **Controller Console**: http://localhost:9050
- **Broker**: http://localhost:8099
- **Server**: http://localhost:8098
- **Minion**: http://localhost:8097

## 🔍 Testing Custom Resources

### 1. Deploy Test Cluster

```bash
kubectl apply -f testing/configs/test-pinot-cluster.yaml
```

### 2. Deploy Test Schema

```bash
kubectl apply -f testing/configs/test-schema.yaml
```

### 3. Deploy Test Table

```bash
kubectl apply -f testing/configs/test-table.yaml
```

### 4. Deploy Test Tenants

```bash
kubectl apply -f testing/configs/test-tenant.yaml
```

### 5. Monitor Resources

```bash
# Check all Pinot resources
kubectl get pinot,pinotschema,pinottable,pinottenant

# Check specific resource
kubectl describe pinot test-pinot-cluster
kubectl describe pinotschema test-schema
kubectl describe pinottable test-table
kubectl describe pinottenant test-tenant
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Port Conflicts
```
Error: Port already in use
```
**Solution**: Stop existing services or change ports in docker-compose.yml

#### 2. Insufficient Memory
```
Error: Container failed to start
```
**Solution**: Increase Docker memory limit to at least 8GB

#### 3. Zookeeper Connection Issues
```
Error: Cannot connect to Zookeeper
```
**Solution**: Wait for Zookeeper to fully start, check logs

#### 4. Pinot Component Startup Failures
```
Error: Component health check failed
```
**Solution**: Check component logs, verify dependencies are ready

### Debug Commands

```bash
# Check Docker containers
docker ps -a

# Check container logs
docker logs pinot-controller

# Check container resources
docker stats

# Check network connectivity
docker network ls
docker network inspect pinot-kubernetes-operator_default
```

## 📈 Performance Testing

### Load Testing

```bash
# Test with multiple tables
for i in {1..10}; do
  kubectl apply -f testing/configs/test-table.yaml
done

# Monitor resource usage
docker stats
```

### Stress Testing

```bash
# Create multiple schemas
for i in {1..5}; do
  sed "s/test-schema/test-schema-$i/g" testing/configs/test-schema.yaml | kubectl apply -f -
done
```

## 🔄 Continuous Testing

### Automated Test Execution

```bash
# Run tests in CI/CD pipeline
./testing/scripts/run-tests.sh > test-results.log 2>&1

# Check exit code
if [ $? -eq 0 ]; then
  echo "All tests passed"
else
  echo "Some tests failed"
  exit 1
fi
```

### Test Results

The test suite provides:
- Pass/Fail status for each test
- Detailed error messages
- Service health information
- Resource deployment validation
- Cleanup verification

## 📚 Additional Resources

### Pinot Documentation
- [Apache Pinot Official Docs](https://docs.pinot.apache.org/)
- [Pinot Quickstart](https://docs.pinot.apache.org/basics/getting-started/quick-start)
- [Pinot Architecture](https://docs.pinot.apache.org/basics/architecture)

### Kubernetes Operator Testing
- [Operator SDK Testing](https://sdk.operatorframework.io/docs/building-operators/golang/testing/)
- [Custom Resource Testing](https://kubernetes.io/docs/tasks/extend-kubernetes/custom-resources/)

### Docker Compose
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Health Checks](https://docs.docker.com/compose/compose-file/compose-file-v3/#healthcheck)

## 🤝 Contributing

To add new tests:

1. Create test configuration in `configs/`
2. Add test logic to `run-tests.sh`
3. Update this documentation
4. Test locally before committing

## 📞 Support

For issues with the testing setup:

1. Check the troubleshooting section
2. Review service logs
3. Verify Docker and Docker Compose versions
4. Check system resources (memory, ports)
5. Create an issue with detailed error information
