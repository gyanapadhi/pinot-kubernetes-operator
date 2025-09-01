package io.pinot.operator.service;

import io.pinot.operator.api.Pinot;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing Pinot clusters in Kubernetes
 * 
 * This service handles the creation, update, and deletion of
 * Kubernetes resources for Pinot clusters.
 */
@Service
public class PinotClusterService {

    private static final Logger logger = LoggerFactory.getLogger(PinotClusterService.class);
    
    private final KubernetesClient kubernetesClient;

    @Autowired
    public PinotClusterService(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    /**
     * Create or update a Pinot cluster
     */
    public void createOrUpdateCluster(Pinot pinot) {
        try {
            String namespace = pinot.getMetadata().getNamespace();
            String clusterName = pinot.getMetadata().getName();
            
            logger.info("Creating/updating Pinot cluster: {}/{}", namespace, clusterName);
            
            // Deploy nodes in the specified order
            List<Pinot.PinotNodeType> deploymentOrder = pinot.getSpec().getDeploymentOrder();
            for (Pinot.PinotNodeType nodeType : deploymentOrder) {
                deployNodeType(pinot, nodeType);
            }
            
            logger.info("Successfully created/updated Pinot cluster: {}/{}", namespace, clusterName);
        } catch (Exception e) {
            logger.error("Error creating/updating Pinot cluster: {}", pinot.getMetadata().getName(), e);
            throw new RuntimeException("Failed to create/update Pinot cluster", e);
        }
    }

    /**
     * Delete a Pinot cluster
     */
    public void deleteCluster(Pinot pinot) {
        try {
            String namespace = pinot.getMetadata().getNamespace();
            String clusterName = pinot.getMetadata().getName();
            
            logger.info("Deleting Pinot cluster: {}/{}", namespace, clusterName);
            
            // Delete all resources associated with the cluster
            deleteClusterResources(pinot);
            
            logger.info("Successfully deleted Pinot cluster: {}/{}", namespace, clusterName);
        } catch (Exception e) {
            logger.error("Error deleting Pinot cluster: {}", pinot.getMetadata().getName(), e);
            throw new RuntimeException("Failed to delete Pinot cluster", e);
        }
    }

    /**
     * Reconcile a Pinot cluster
     */
    public void reconcileCluster(Pinot pinot) {
        try {
            String namespace = pinot.getMetadata().getNamespace();
            String clusterName = pinot.getMetadata().getName();
            
            logger.debug("Reconciling Pinot cluster: {}/{}", namespace, clusterName);
            
            // Check cluster health and status
            checkClusterHealth(pinot);
            
            // Update status if needed
            updateClusterStatus(pinot);
            
        } catch (Exception e) {
            logger.error("Error reconciling Pinot cluster: {}", pinot.getMetadata().getName(), e);
        }
    }

    /**
     * Deploy a specific node type
     */
    private void deployNodeType(Pinot pinot, Pinot.PinotNodeType nodeType) {
        String namespace = pinot.getMetadata().getNamespace();
        String clusterName = pinot.getMetadata().getName();
        
        logger.info("Deploying {} nodes for cluster: {}/{}", nodeType.getValue(), namespace, clusterName);
        
        // Find nodes of this type
        List<Pinot.NodeSpec> nodesOfType = pinot.getSpec().getNodes().stream()
                .filter(node -> node.getNodeType() == nodeType)
                .collect(Collectors.toList());
        
        for (Pinot.NodeSpec nodeSpec : nodesOfType) {
            deployNode(pinot, nodeSpec);
        }
    }

    /**
     * Deploy a specific node
     */
    private void deployNode(Pinot pinot, Pinot.NodeSpec nodeSpec) {
        String namespace = pinot.getMetadata().getNamespace();
        String clusterName = pinot.getMetadata().getName();
        String nodeName = nodeSpec.getName();
        
        logger.info("Deploying node: {} for cluster: {}/{}", nodeName, namespace, clusterName);
        
        // Find the K8s configuration for this node
        Pinot.K8sConfig k8sConfig = findK8sConfig(pinot, nodeSpec.getK8sConfig());
        if (k8sConfig == null) {
            throw new RuntimeException("K8s configuration not found for node: " + nodeName);
        }
        
        // Find the Pinot node configuration
        Pinot.PinotNodeConfig pinotConfig = findPinotNodeConfig(pinot, nodeSpec.getPinotNodeConfig());
        if (pinotConfig == null) {
            throw new RuntimeException("Pinot configuration not found for node: " + nodeName);
        }
        
        // Create deployment
        createDeployment(pinot, nodeSpec, k8sConfig, pinotConfig);
        
        // Create service
        createService(pinot, nodeSpec, k8sConfig);
        
        // Create config map for Pinot configuration
        createConfigMap(pinot, nodeSpec, pinotConfig);
    }

    /**
     * Find K8s configuration by name
     */
    private Pinot.K8sConfig findK8sConfig(Pinot pinot, String configName) {
        return pinot.getSpec().getK8sConfig().stream()
                .filter(config -> config.getName().equals(configName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find Pinot node configuration by name
     */
    private Pinot.PinotNodeConfig findPinotNodeConfig(Pinot pinot, String configName) {
        return pinot.getSpec().getPinotNodeConfig().stream()
                .filter(config -> config.getName().equals(configName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Create Kubernetes deployment for a Pinot node
     */
    private void createDeployment(Pinot pinot, Pinot.NodeSpec nodeSpec, 
                                 Pinot.K8sConfig k8sConfig, Pinot.PinotNodeConfig pinotConfig) {
        String namespace = pinot.getMetadata().getNamespace();
        String clusterName = pinot.getMetadata().getName();
        String nodeName = nodeSpec.getName();
        
        // Create deployment object
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(nodeName)
                    .withNamespace(namespace)
                    .addToLabels("app", "pinot")
                    .addToLabels("cluster", clusterName)
                    .addToLabels("node-type", nodeSpec.getNodeType().getValue())
                .endMetadata()
                .withNewSpec()
                    .withReplicas(nodeSpec.getReplicas())
                    .withNewSelector()
                        .addToMatchLabels("app", "pinot")
                        .addToMatchLabels("cluster", clusterName)
                        .addToMatchLabels("node", nodeName)
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels("app", "pinot")
                            .addToLabels("cluster", clusterName)
                            .addToLabels("node", nodeName)
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName("pinot")
                                .withImage(k8sConfig.getImage())
                                .withImagePullPolicy("IfNotPresent")
                                .addToEnv(new EnvVarBuilder()
                                    .withName("PINOT_NODE_TYPE")
                                    .withValue(nodeSpec.getNodeType().getValue())
                                    .build())
                                .addToEnv(new EnvVarBuilder()
                                    .withName("PINOT_CLUSTER_NAME")
                                    .withValue(clusterName)
                                    .build())
                                .addToEnv(new EnvVarBuilder()
                                    .withName("JAVA_OPTS")
                                    .withValue(pinotConfig.getJavaOpts())
                                    .build())
                                .addToPorts(new ContainerPortBuilder()
                                    .withContainerPort(8090)
                                    .withProtocol("TCP")
                                    .build())
                                .withNewResources()
                                    .withRequests(Map.of("memory", new Quantity("512Mi"), "cpu", new Quantity("250m")))
                                    .withLimits(Map.of("memory", new Quantity("1Gi"), "cpu", new Quantity("500m")))
                                .endResources()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();
        
        // Apply deployment
        kubernetesClient.apps().deployments()
                .inNamespace(namespace)
                .resource(deployment)
                .createOrReplace();
        
        logger.info("Created/updated deployment for node: {}", nodeName);
    }

    /**
     * Create Kubernetes service for a Pinot node
     */
    private void createService(Pinot pinot, Pinot.NodeSpec nodeSpec, Pinot.K8sConfig k8sConfig) {
        String namespace = pinot.getMetadata().getNamespace();
        String clusterName = pinot.getMetadata().getName();
        String nodeName = nodeSpec.getName();
        
        // Create service object
        io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName(nodeName + "-service")
                    .withNamespace(namespace)
                    .addToLabels("app", "pinot")
                    .addToLabels("cluster", clusterName)
                    .addToLabels("node", nodeName)
                .endMetadata()
                .withNewSpec()
                    .withType("ClusterIP")
                    .addToSelector("app", "pinot")
                    .addToSelector("cluster", clusterName)
                    .addToSelector("node", nodeName)
                    .addNewPort()
                        .withPort(8090)
                        .withTargetPort(new IntOrString(8090))
                        .withProtocol("TCP")
                        .withName("http")
                    .endPort()
                .endSpec()
                .build();
        
        // Apply service
        kubernetesClient.services()
                .inNamespace(namespace)
                .resource(service)
                .createOrReplace();
        
        logger.info("Created/updated service for node: {}", nodeName);
    }

    /**
     * Create config map for Pinot configuration
     */
    private void createConfigMap(Pinot pinot, Pinot.NodeSpec nodeSpec, Pinot.PinotNodeConfig pinotConfig) {
        String namespace = pinot.getMetadata().getNamespace();
        String clusterName = pinot.getMetadata().getName();
        String nodeName = nodeSpec.getName();
        
        // Create config map data
        Map<String, String> data = new HashMap<>();
        data.put("pinot.properties", generatePinotProperties(pinot, nodeSpec, pinotConfig));
        
        // Create config map object
        ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                    .withName(nodeName + "-config")
                    .withNamespace(namespace)
                    .addToLabels("app", "pinot")
                    .addToLabels("cluster", clusterName)
                    .addToLabels("node", nodeName)
                .endMetadata()
                .withData(data)
                .build();
        
        // Apply config map
        kubernetesClient.configMaps()
                .inNamespace(namespace)
                .resource(configMap)
                .createOrReplace();
        
        logger.info("Created/updated config map for node: {}", nodeName);
    }

    /**
     * Generate Pinot properties configuration
     */
    private String generatePinotProperties(Pinot pinot, Pinot.NodeSpec nodeSpec, Pinot.PinotNodeConfig pinotConfig) {
        StringBuilder properties = new StringBuilder();
        
        // Basic Pinot configuration
        properties.append("pinot.node.type=").append(nodeSpec.getNodeType().getValue()).append("\n");
        properties.append("pinot.cluster.name=").append(pinot.getMetadata().getName()).append("\n");
        
        // Zookeeper configuration
        if (pinot.getSpec().getExternal() != null && pinot.getSpec().getExternal().getZookeeper() != null) {
            properties.append("pinot.zookeeper.address=")
                    .append(pinot.getSpec().getExternal().getZookeeper().getSpec().getZkAddress())
                    .append("\n");
        }
        
        // Data directory
        properties.append("pinot.data.dir=").append(pinotConfig.getData()).append("\n");
        
        // Java options
        properties.append("pinot.java.opts=").append(pinotConfig.getJavaOpts()).append("\n");
        
        return properties.toString();
    }

    /**
     * Delete all resources associated with a cluster
     */
    private void deleteClusterResources(Pinot pinot) {
        String namespace = pinot.getMetadata().getNamespace();
        String clusterName = pinot.getMetadata().getName();
        
        // Delete deployments
        kubernetesClient.apps().deployments()
                .inNamespace(namespace)
                .withLabel("cluster", clusterName)
                .delete();
        
        // Delete services
        kubernetesClient.services()
                .inNamespace(namespace)
                .withLabel("cluster", clusterName)
                .delete();
        
        // Delete config maps
        kubernetesClient.configMaps()
                .inNamespace(namespace)
                .withLabel("cluster", clusterName)
                .delete();
        
        logger.info("Deleted all resources for cluster: {}/{}", namespace, clusterName);
    }

    /**
     * Check cluster health
     */
    private void checkClusterHealth(Pinot pinot) {
        // Implementation for health checking
        // This would typically involve checking pod status, service endpoints, etc.
        logger.debug("Checking health for cluster: {}", pinot.getMetadata().getName());
    }

    /**
     * Update cluster status
     */
    private void updateClusterStatus(Pinot pinot) {
        // Implementation for status updates
        // This would typically involve updating the custom resource status
        logger.debug("Updating status for cluster: {}", pinot.getMetadata().getName());
    }
}
