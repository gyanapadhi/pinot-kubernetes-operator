package io.pinot.operator.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

import java.util.List;

/**
 * Pinot Custom Resource Definition
 * 
 * Represents a complete Apache Pinot cluster configuration in Kubernetes.
 * This custom resource defines the cluster topology, component specifications,
 * and deployment configuration for all Pinot services.
 * 
 * The Pinot resource manages:
 * - Cluster deployment order and dependencies
 * - Component configurations (controller, broker, server, minion)
 * - External service connections (Zookeeper, deep storage)
 * - Kubernetes-specific settings and resource requirements
 * - Authentication and security configurations
 * - Plugin and extension management
 * 
 * When applied, the operator will automatically create and manage
 * the corresponding Kubernetes resources to deploy the Pinot cluster.
 */
@Group("pinot.io")
@Version("v1")
public class Pinot extends CustomResource<Pinot.PinotSpec, Pinot.PinotStatus> implements Namespaced {

    public static final String KIND = "Pinot";
    public static final String GROUP = "pinot.io";
    public static final String VERSION = "v1";
    public static final String API_VERSION = GROUP + "/" + VERSION;

    public Pinot() {
        setKind(KIND);
        setApiVersion(API_VERSION);
    }

    /**
     * Specification for the Pinot cluster
     */
    public static class PinotSpec {
        @JsonProperty("auth")
        private Auth auth;
        
        @JsonProperty("plugins")
        private List<String> plugins;
        
        @JsonProperty("deploymentOrder")
        private List<PinotNodeType> deploymentOrder;
        
        @JsonProperty("external")
        private ExternalSpec external;
        
        @JsonProperty("k8sConfig")
        private List<K8sConfig> k8sConfig;
        
        @JsonProperty("pinotNodeConfig")
        private List<PinotNodeConfig> pinotNodeConfig;
        
        @JsonProperty("nodes")
        private List<NodeSpec> nodes;

        // Getters and setters
        public Auth getAuth() { return auth; }
        public void setAuth(Auth auth) { this.auth = auth; }
        
        public List<String> getPlugins() { return plugins; }
        public void setPlugins(List<String> plugins) { this.plugins = plugins; }
        
        public List<PinotNodeType> getDeploymentOrder() { return deploymentOrder; }
        public void setDeploymentOrder(List<PinotNodeType> deploymentOrder) { this.deploymentOrder = deploymentOrder; }
        
        public ExternalSpec getExternal() { return external; }
        public void setExternal(ExternalSpec external) { this.external = external; }
        
        public List<K8sConfig> getK8sConfig() { return k8sConfig; }
        public void setK8sConfig(List<K8sConfig> k8sConfig) { this.k8sConfig = k8sConfig; }
        
        public List<PinotNodeConfig> getPinotNodeConfig() { return pinotNodeConfig; }
        public void setPinotNodeConfig(List<PinotNodeConfig> pinotNodeConfig) { this.pinotNodeConfig = pinotNodeConfig; }
        
        public List<NodeSpec> getNodes() { return nodes; }
        public void setNodes(List<NodeSpec> nodes) { this.nodes = nodes; }
    }

    /**
     * Status of the Pinot cluster
     */
    public static class PinotStatus {
        // Status fields will be added as needed
    }

    /**
     * Authentication configuration
     */
    public static class Auth {
        @JsonProperty("type")
        private AuthType type;
        
        @JsonProperty("secretRef")
        private SecretReference secretRef;

        public AuthType getType() { return type; }
        public void setType(AuthType type) { this.type = type; }
        
        public SecretReference getSecretRef() { return secretRef; }
        public void setSecretRef(SecretReference secretRef) { this.secretRef = secretRef; }
    }

    /**
     * External dependencies specification
     */
    public static class ExternalSpec {
        @JsonProperty("zookeeper")
        private ZookeeperSpec zookeeper;
        
        @JsonProperty("deepStorage")
        private DeepStorageSpec deepStorage;

        public ZookeeperSpec getZookeeper() { return zookeeper; }
        public void setZookeeper(ZookeeperSpec zookeeper) { this.zookeeper = zookeeper; }
        
        public DeepStorageSpec getDeepStorage() { return deepStorage; }
        public void setDeepStorage(DeepStorageSpec deepStorage) { this.deepStorage = deepStorage; }
    }

    /**
     * Zookeeper configuration
     */
    public static class ZookeeperSpec {
        @JsonProperty("spec")
        private ZookeeperConfig spec;

        public ZookeeperConfig getSpec() { return spec; }
        public void setSpec(ZookeeperConfig spec) { this.spec = spec; }
    }

    /**
     * Zookeeper configuration details
     */
    public static class ZookeeperConfig {
        @JsonProperty("zkAddress")
        private String zkAddress;

        public String getZkAddress() { return zkAddress; }
        public void setZkAddress(String zkAddress) { this.zkAddress = zkAddress; }
    }

    /**
     * Deep storage configuration
     */
    public static class DeepStorageSpec {
        @JsonProperty("spec")
        private List<DeepStorageConfig> spec;

        public List<DeepStorageConfig> getSpec() { return spec; }
        public void setSpec(List<DeepStorageConfig> spec) { this.spec = spec; }
    }

    /**
     * Deep storage configuration details
     */
    public static class DeepStorageConfig {
        @JsonProperty("nodeType")
        private PinotNodeType nodeType;
        
        @JsonProperty("data")
        private String data;

        public PinotNodeType getNodeType() { return nodeType; }
        public void setNodeType(PinotNodeType nodeType) { this.nodeType = nodeType; }
        
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    /**
     * Kubernetes configuration for Pinot nodes
     */
    public static class K8sConfig {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("image")
        private String image;
        
        @JsonProperty("port")
        private List<ContainerPort> port;
        
        @JsonProperty("service")
        private ServiceSpec service;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        
        public List<ContainerPort> getPort() { return port; }
        public void setPort(List<ContainerPort> port) { this.port = port; }
        
        public ServiceSpec getService() { return service; }
        public void setService(ServiceSpec service) { this.service = service; }
    }

    /**
     * Pinot node configuration
     */
    public static class PinotNodeConfig {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("java_opts")
        private String javaOpts;
        
        @JsonProperty("data")
        private String data;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getJavaOpts() { return javaOpts; }
        public void setJavaOpts(String javaOpts) { this.javaOpts = javaOpts; }
        
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    /**
     * Node specification
     */
    public static class NodeSpec {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("kind")
        private String kind;
        
        @JsonProperty("nodeType")
        private PinotNodeType nodeType;
        
        @JsonProperty("replicas")
        private int replicas;
        
        @JsonProperty("k8sConfig")
        private String k8sConfig;
        
        @JsonProperty("pinotNodeConfig")
        private String pinotNodeConfig;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        
        public PinotNodeType getNodeType() { return nodeType; }
        public void setNodeType(PinotNodeType nodeType) { this.nodeType = nodeType; }
        
        public int getReplicas() { return replicas; }
        public void setReplicas(int replicas) { this.replicas = replicas; }
        
        public String getK8sConfig() { return k8sConfig; }
        public void setK8sConfig(String k8sConfig) { this.k8sConfig = k8sConfig; }
        
        public String getPinotNodeConfig() { return pinotNodeConfig; }
        public void setPinotNodeConfig(String pinotNodeConfig) { this.pinotNodeConfig = pinotNodeConfig; }
    }

    /**
     * Pinot node types
     */
    public enum PinotNodeType {
        CONTROLLER("controller"),
        BROKER("broker"),
        SERVER("server"),
        MINION("minion");

        private final String value;

        PinotNodeType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Authentication types
     */
    public enum AuthType {
        BASIC_AUTH("basic-auth");

        private final String value;

        AuthType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Simplified versions of Kubernetes types for brevity
    public static class SecretReference {
        private String name;
        private String namespace;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }
    }

    public static class ContainerPort {
        private int containerPort;
        private String protocol;

        public int getContainerPort() { return containerPort; }
        public void setContainerPort(int containerPort) { this.containerPort = containerPort; }
        
        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
    }

    public static class ServiceSpec {
        private List<ServicePort> ports;
        private String type;

        public List<ServicePort> getPorts() { return ports; }
        public void setPorts(List<ServicePort> ports) { this.ports = ports; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class ServicePort {
        private int port;
        private int targetPort;
        private String protocol;

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public int getTargetPort() { return targetPort; }
        public void setTargetPort(int targetPort) { this.targetPort = targetPort; }
        
        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
    }
}
