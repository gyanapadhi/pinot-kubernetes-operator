package io.pinot.operator.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

/**
 * PinotTenant custom resource for managing Pinot tenants in Kubernetes
 * 
 * This class defines the structure for managing Apache Pinot tenants,
 * including their configuration and associated cluster.
 */
@Group("pinot.io")
@Version("v1")
public class PinotTenant extends CustomResource<PinotTenant.PinotTenantSpec, PinotTenant.PinotTenantStatus> implements Namespaced {

    public static final String KIND = "PinotTenant";
    public static final String GROUP = "pinot.io";
    public static final String VERSION = "v1";
    public static final String API_VERSION = GROUP + "/" + VERSION;

    public PinotTenant() {
        setKind(KIND);
        setApiVersion(API_VERSION);
    }

    /**
     * Specification for the Pinot tenant
     */
    public static class PinotTenantSpec {
        @JsonProperty("pinotCluster")
        private String pinotCluster;
        
        @JsonProperty("tenantConfig")
        private String tenantConfig;

        public String getPinotCluster() { return pinotCluster; }
        public void setPinotCluster(String pinotCluster) { this.pinotCluster = pinotCluster; }
        
        public String getTenantConfig() { return tenantConfig; }
        public void setTenantConfig(String tenantConfig) { this.tenantConfig = tenantConfig; }
    }

    /**
     * Status of the Pinot tenant
     */
    public static class PinotTenantStatus {
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("reason")
        private String reason;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("lastUpdateTime")
        private String lastUpdateTime;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(String lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }
}
