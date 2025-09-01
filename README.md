# Pinot Kubernetes Operator

A Kubernetes operator written in Java for managing Apache Pinot clusters, schemas, tables, and tenants on Kubernetes.

## Overview

This project provides a complete Kubernetes operator implementation for Apache Pinot, built using Spring Boot and the Fabric8 Kubernetes client. The operator manages the complete lifecycle of Pinot clusters and their components through Kubernetes custom resources.

## Features

- **Cluster Management**: Deploy and manage heterogeneous Apache Pinot clusters
- **Schema Management**: Create, update, and delete Pinot schemas
- **Table Management**: Manage Pinot tables with support for realtime, offline, and hybrid types
- **Tenant Management**: Configure and manage Pinot tenants
- **Rolling Upgrades**: Support for incremental cluster updates
- **Ordered Deployment**: Deploy nodes in specified order
- **Health Monitoring**: Built-in health checks and metrics
- **Status Management**: Comprehensive status tracking and reconciliation

## Architecture

The operator follows the standard Kubernetes operator pattern:

- **Custom Resource Definitions (CRDs)**: Define Pinot resources in Kubernetes
- **Controllers**: Watch and reconcile custom resources
- **Services**: Handle business logic and Pinot cluster communication
- **Watchers**: Monitor Kubernetes resource changes
- **Reconciliation**: Periodic status updates and health checks

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Kubernetes cluster (1.19+)
- Apache Pinot cluster (for testing)

## Installation

### 1. Build the Project

```bash
mvn clean package
```

### 2. Deploy CRDs

```bash
kubectl apply -f k8s/crds.yaml
```

### 3. Deploy the Operator

```bash
kubectl apply -f k8s/operator.yaml
```

### 4. Verify Installation

```bash
kubectl get pods -n pinot-operator
kubectl get crd | grep pinot.io
```

## Usage

### Creating a Pinot Cluster

```yaml
apiVersion: pinot.io/v1
kind: Pinot
metadata:
  name: my-pinot-cluster
  namespace: default
spec:
  deploymentOrder:
    - controller
    - broker
    - server
    - minion
  external:
    zookeeper:
      spec:
        zkAddress: "zookeeper:2181"
    deepStorage:
      spec:
        - nodeType: server
          data: "/data"
  k8sConfig:
    - name: controller-config
      image: "apachepinot/pinot:latest"
      port:
        - containerPort: 9000
          protocol: TCP
      service:
        type: ClusterIP
        ports:
          - port: 9000
            targetPort: 9000
            protocol: TCP
  pinotNodeConfig:
    - name: controller-config
      java_opts: "-Xmx1g -Xms1g"
      data: "/data"
  nodes:
    - name: controller
      kind: Deployment
      nodeType: controller
      replicas: 1
      k8sConfig: controller-config
      pinotNodeConfig: controller-config
```

### Creating a Schema

```yaml
apiVersion: pinot.io/v1
kind: PinotSchema
metadata:
  name: user-schema
  namespace: default
spec:
  pinotCluster: my-pinot-cluster
  schema.json: |
    {
      "schemaName": "user",
      "dimensionFieldSpecs": [
        {"name": "userId", "dataType": "STRING"},
        {"name": "name", "dataType": "STRING"}
      ],
      "metricFieldSpecs": [
        {"name": "age", "dataType": "INT"}
      ]
    }
```

### Creating a Table

```yaml
apiVersion: pinot.io/v1
kind: PinotTable
metadata:
  name: user-table
  namespace: default
spec:
  pinotCluster: my-pinot-cluster
  pinotSchema: user-schema
  pinotTableType: offline
  tables.json: |
    {
      "tableName": "user",
      "tableType": "OFFLINE",
      "segmentsConfig": {
        "timeColumnName": "time",
        "schemaName": "user"
      }
    }
```

### Creating a Tenant

```yaml
apiVersion: pinot.io/v1
kind: PinotTenant
metadata:
  name: analytics-tenant
  namespace: default
spec:
  pinotCluster: my-pinot-cluster
  tenantConfig: |
    {
      "tenantRole": "BROKER",
      "numberOfInstances": 2
    }
```

## Configuration

The operator can be configured through environment variables or application properties:

| Property | Description | Default |
|----------|-------------|---------|
| `pinot.operator.reconciliation-interval` | Reconciliation interval in milliseconds | 30000 |
| `pinot.operator.watcher-reconnect-delay` | Watcher reconnection delay in milliseconds | 5000 |
| `pinot.cluster.default-controller-port` | Default Pinot controller port | 9000 |
| `pinot.cluster.default-broker-port` | Default Pinot broker port | 8099 |

## API Reference

### Pinot Resource

The main Pinot cluster resource with the following components:

- **Auth**: Authentication configuration
- **External**: External dependencies (Zookeeper, Deep Storage)
- **K8sConfig**: Kubernetes-specific configuration
- **PinotNodeConfig**: Pinot-specific configuration
- **Nodes**: Node specifications and deployment order

### PinotSchema Resource

Manages Pinot schemas with JSON configuration and cluster association.

### PinotTable Resource

Manages Pinot tables with support for different table types and schema associations.

### PinotTenant Resource

Manages Pinot tenants with role-based configuration and resource allocation.

## Development

### Project Structure

```
src/main/java/io/pinot/operator/
├── api/                    # Custom resource definitions
├── controller/            # Kubernetes controllers
├── service/              # Business logic services
├── config/               # Configuration classes
├── util/                 # Utility classes
└── PinotControlPlaneApplication.java
```

### Building

```bash
# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package

# Run locally
mvn spring-boot:run
```

### Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=test
```

## Monitoring

The operator provides several monitoring endpoints:

- **Health Check**: `/actuator/health` (port 8081)
- **Metrics**: `/actuator/metrics` (port 8081)
- **Prometheus**: `/actuator/prometheus` (port 8081)
- **Info**: `/actuator/info` (port 8081)

## Troubleshooting

### Common Issues

1. **CRD Not Found**: Ensure CRDs are deployed before the operator
2. **Permission Denied**: Check RBAC configuration and service account permissions
3. **Connection Timeout**: Verify Kubernetes cluster connectivity and network policies

### Logs

```bash
# Operator logs
kubectl logs -f deployment/pinot-operator -n pinot-operator

# Resource events
kubectl get events --sort-by='.lastTimestamp' | grep pinot
```

### Debug Mode

Enable debug logging by setting the log level:

```bash
kubectl set env deployment/pinot-operator LOG_LEVEL=DEBUG -n pinot-operator
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request



## Support

For issues and questions:
- Create a GitHub issue
- Check the troubleshooting section
- Review the logs and events

## Roadmap

- [ ] Enhanced health checking
- [ ] Metrics collection
- [ ] Backup and restore
- [ ] Multi-cluster support
- [ ] Advanced scheduling
- [ ] Integration with monitoring systems
