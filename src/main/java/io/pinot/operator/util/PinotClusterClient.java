package io.pinot.operator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for communicating with Pinot clusters
 * 
 * This class provides methods to interact with Pinot clusters via HTTP API,
 * including schema management, table management, and tenant management.
 */
@Component
public class PinotClusterClient {

    private static final Logger logger = LoggerFactory.getLogger(PinotClusterClient.class);
    
    private final HttpClient httpClient;
    private final Map<String, String> clusterEndpoints = new ConcurrentHashMap<>();
    
    public PinotClusterClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Register a cluster endpoint
     */
    public void registerClusterEndpoint(String clusterName, String controllerEndpoint) {
        clusterEndpoints.put(clusterName, controllerEndpoint);
        logger.info("Registered cluster endpoint: {} -> {}", clusterName, controllerEndpoint);
    }

    /**
     * Get cluster endpoint
     */
    public String getClusterEndpoint(String clusterName) {
        String endpoint = clusterEndpoints.get(clusterName);
        if (endpoint == null) {
            throw new IllegalArgumentException("Cluster endpoint not found for: " + clusterName);
        }
        return endpoint;
    }

    /**
     * Create or update a schema
     */
    public boolean createOrUpdateSchema(String clusterName, String schemaName, String schemaJson) {
        try {
            String endpoint = getClusterEndpoint(clusterName);
            String url = endpoint + "/schemas";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(schemaJson))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                logger.info("Successfully created/updated schema: {} in cluster: {}", schemaName, clusterName);
                return true;
            } else {
                logger.error("Failed to create/update schema: {} in cluster: {}. Status: {}, Response: {}", 
                        schemaName, clusterName, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error creating/updating schema: {} in cluster: {}", schemaName, clusterName, e);
            return false;
        }
    }

    /**
     * Delete a schema
     */
    public boolean deleteSchema(String clusterName, String schemaName) {
        try {
            String endpoint = getClusterEndpoint(clusterName);
            String url = endpoint + "/schemas/" + schemaName;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                logger.info("Successfully deleted schema: {} from cluster: {}", schemaName, clusterName);
                return true;
            } else {
                logger.error("Failed to delete schema: {} from cluster: {}. Status: {}, Response: {}", 
                        schemaName, clusterName, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting schema: {} from cluster: {}", schemaName, clusterName, e);
            return false;
        }
    }

    /**
     * Create or update a table
     */
    public boolean createOrUpdateTable(String clusterName, String tableName, String tableJson) {
        try {
            String endpoint = getClusterEndpoint(clusterName);
            String url = endpoint + "/tables";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(tableJson))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                logger.info("Successfully created/updated table: {} in cluster: {}", tableName, clusterName);
                return true;
            } else {
                logger.error("Failed to create/update table: {} in cluster: {}. Status: {}, Response: {}", 
                        tableName, clusterName, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error creating/updating table: {} in cluster: {}", tableName, clusterName, e);
            return false;
        }
    }

    /**
     * Delete a table
     */
    public boolean deleteTable(String clusterName, String tableName) {
        try {
            String endpoint = getClusterEndpoint(clusterName);
            String url = endpoint + "/tables/" + tableName;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                logger.info("Successfully deleted table: {} from cluster: {}", tableName, clusterName);
                return true;
            } else {
                logger.error("Failed to delete table: {} from cluster: {}. Status: {}, Response: {}", 
                        tableName, clusterName, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting table: {} from cluster: {}", tableName, clusterName, e);
            return false;
        }
    }

    /**
     * Create or update a tenant
     */
    public boolean createOrUpdateTenant(String clusterName, String tenantName, String tenantConfig) {
        try {
            String endpoint = getClusterEndpoint(clusterName);
            String url = endpoint + "/tenants";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(tenantConfig))
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                logger.info("Successfully created/updated tenant: {} in cluster: {}", tenantName, clusterName);
                return true;
            } else {
                logger.error("Failed to create/update tenant: {} in cluster: {}. Status: {}, Response: {}", 
                        tenantName, clusterName, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error creating/updating tenant: {} in cluster: {}", tenantName, clusterName, e);
            return false;
        }
    }

    /**
     * Delete a tenant
     */
    public boolean deleteTenant(String clusterName, String tenantName) {
        try {
            String endpoint = getClusterEndpoint(clusterName);
            String url = endpoint + "/tenants/" + tenantName;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .timeout(Duration.ofSeconds(30))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 204) {
                logger.info("Successfully deleted tenant: {} from cluster: {}", tenantName, clusterName);
                return true;
            } else {
                logger.error("Failed to delete tenant: {} from cluster: {}. Status: {}, Response: {}", 
                        tenantName, clusterName, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting tenant: {} from cluster: {}", tenantName, clusterName, e);
            return false;
        }
    }

    /**
     * Check cluster health
     */
    public boolean checkClusterHealth(String clusterName) {
        try {
            String endpoint = getClusterEndpoint(clusterName);
            String url = endpoint + "/health";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                logger.debug("Cluster health check passed for: {}", clusterName);
                return true;
            } else {
                logger.warn("Cluster health check failed for: {}. Status: {}", clusterName, response.statusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error checking cluster health for: {}", clusterName, e);
            return false;
        }
    }

    /**
     * Get cluster information
     */
    public String getClusterInfo(String clusterName) {
        try {
            String endpoint = getClusterEndpoint(clusterName);
            String url = endpoint + "/cluster/info";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                logger.error("Failed to get cluster info for: {}. Status: {}", clusterName, response.statusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error getting cluster info for: {}", clusterName, e);
            return null;
        }
    }
}
