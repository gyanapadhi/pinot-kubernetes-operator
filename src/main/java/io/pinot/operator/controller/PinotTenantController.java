package io.pinot.operator.controller;

import io.pinot.operator.api.PinotTenant;
import io.pinot.operator.service.PinotTenantService;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Controller for managing Pinot tenants in Kubernetes
 * 
 * This controller watches for PinotTenant custom resources and manages
 * their lifecycle, including creation, updates, and deletion.
 */
@Component
public class PinotTenantController {

    private static final Logger logger = LoggerFactory.getLogger(PinotTenantController.class);
    
    private final KubernetesClient kubernetesClient;
    private final PinotTenantService pinotTenantService;
    private final ConcurrentMap<String, PinotTenant> managedTenants = new ConcurrentHashMap<>();
    
    private Watcher<PinotTenant> tenantWatcher;

    @Autowired
    public PinotTenantController(KubernetesClient kubernetesClient, PinotTenantService pinotTenantService) {
        this.kubernetesClient = kubernetesClient;
        this.pinotTenantService = pinotTenantService;
        initializeWatcher();
    }

    /**
     * Initialize the Kubernetes watcher for PinotTenant resources
     */
    private void initializeWatcher() {
        try {
            tenantWatcher = new Watcher<PinotTenant>() {
                @Override
                public void eventReceived(Action action, PinotTenant resource) {
                    handleTenantEvent(action, resource);
                }

                @Override
                public void onClose(WatcherException cause) {
                    logger.error("PinotTenant watcher closed", cause);
                    // Reinitialize watcher after delay
                    try {
                        Thread.sleep(5000);
                        initializeWatcher();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };

            // Start watching PinotTenant resources across all namespaces
            kubernetesClient.resources(PinotTenant.class)
                    .inAnyNamespace()
                    .watch(tenantWatcher);

            logger.info("PinotTenant watcher initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize PinotTenant watcher", e);
        }
    }

    /**
     * Handle PinotTenant resource events
     */
    private void handleTenantEvent(Watcher.Action action, PinotTenant resource) {
        String resourceKey = getResourceKey(resource);
        
        try {
            switch (action) {
                case ADDED:
                    handleTenantAdded(resource);
                    break;
                case MODIFIED:
                    handleTenantModified(resource);
                    break;
                case DELETED:
                    handleTenantDeleted(resource);
                    break;
                case ERROR:
                    logger.error("Error event received for PinotTenant resource: {}", resourceKey);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling PinotTenant event: {} for resource: {}", action, resourceKey, e);
        }
    }

    /**
     * Handle PinotTenant resource addition
     */
    private void handleTenantAdded(PinotTenant resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotTenant resource added: {}", resourceKey);
        
        managedTenants.put(resourceKey, resource);
        pinotTenantService.createOrUpdateTenant(resource);
    }

    /**
     * Handle PinotTenant resource modification
     */
    private void handleTenantModified(PinotTenant resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotTenant resource modified: {}", resourceKey);
        
        PinotTenant existingResource = managedTenants.get(resourceKey);
        if (existingResource != null) {
            managedTenants.put(resourceKey, resource);
            pinotTenantService.createOrUpdateTenant(resource);
        }
    }

    /**
     * Handle PinotTenant resource deletion
     */
    private void handleTenantDeleted(PinotTenant resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotTenant resource deleted: {}", resourceKey);
        
        managedTenants.remove(resourceKey);
        pinotTenantService.deleteTenant(resource);
    }

    /**
     * Get a unique key for the resource
     */
    private String getResourceKey(PinotTenant resource) {
        ObjectMeta metadata = resource.getMetadata();
        if (metadata != null && metadata.getNamespace() != null) {
            return metadata.getNamespace() + "/" + metadata.getName();
        }
        return metadata != null ? metadata.getName() : "unknown";
    }

    /**
     * Periodic reconciliation of managed tenants
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void reconcileTenants() {
        logger.debug("Starting periodic reconciliation of {} managed tenants", managedTenants.size());
        
        for (PinotTenant tenant : managedTenants.values()) {
            try {
                pinotTenantService.reconcileTenant(tenant);
            } catch (Exception e) {
                logger.error("Error reconciling tenant: {}", getResourceKey(tenant), e);
            }
        }
    }

    /**
     * Get list of managed tenants
     */
    public List<PinotTenant> getManagedTenants() {
        return List.copyOf(managedTenants.values());
    }

    /**
     * Get a specific managed tenant
     */
    public PinotTenant getManagedTenant(String namespace, String name) {
        String key = namespace + "/" + name;
        return managedTenants.get(key);
    }

    /**
     * Check if a tenant is being managed
     */
    public boolean isTenantManaged(String namespace, String name) {
        String key = namespace + "/" + name;
        return managedTenants.containsKey(key);
    }
}
