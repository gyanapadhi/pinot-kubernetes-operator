package io.pinot.operator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Pinot Kubernetes Operator - Main Application
 * 
 * A Spring Boot-based Kubernetes operator that automates the deployment,
 * configuration, and lifecycle management of Apache Pinot clusters on Kubernetes.
 * 
 * The operator watches for custom resources (Pinot, PinotSchema, PinotTable, PinotTenant)
 * and automatically manages the corresponding Pinot cluster components including
 * controllers, brokers, servers, and minions.
 * 
 * Features:
 * - Automated cluster deployment and scaling
 * - Schema and table management
 * - Tenant configuration and resource allocation
 * - Health monitoring and reconciliation
 * - Rolling updates and configuration changes
 */
@SpringBootApplication
@EnableScheduling
public class PinotControlPlaneApplication {

    public static void main(String[] args) {
        SpringApplication.run(PinotControlPlaneApplication.class, args);
    }
}
