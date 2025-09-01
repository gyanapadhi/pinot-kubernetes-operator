package io.pinot.operator.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

/**
 * PinotSchema Custom Resource Definition
 * 
 * Defines the schema structure for Pinot tables, including field specifications,
 * data types, and validation rules. Schemas are essential for defining the
 * structure of data that will be ingested into Pinot tables.
 * 
 * The PinotSchema resource manages:
 * - Field definitions (dimensions, metrics, time columns)
 * - Data type specifications and constraints
 * - Schema validation and compatibility rules
 * - Association with specific Pinot clusters
 * - Schema evolution and versioning support
 * 
 * Schemas must be created before tables and are referenced by
 * PinotTable resources to ensure data consistency and proper indexing.
 */
@Group("pinot.io")
@Version("v1")
public class PinotSchema extends CustomResource<PinotSchema.PinotSchemaSpec, PinotSchema.PinotSchemaStatus> implements Namespaced {

    public static final String KIND = "PinotSchema";
    public static final String GROUP = "pinot.io";
    public static final String VERSION = "v1";
    public static final String API_VERSION = GROUP + "/" + VERSION;

    public PinotSchema() {
        setKind(KIND);
        setApiVersion(API_VERSION);
    }

    /**
     * Specification for the Pinot schema
     */
    public static class PinotSchemaSpec {
        @JsonProperty("pinotCluster")
        private String pinotCluster;
        
        @JsonProperty("schema.json")
        private String pinotSchemaJson;

        public String getPinotCluster() { return pinotCluster; }
        public void setPinotCluster(String pinotCluster) { this.pinotCluster = pinotCluster; }
        
        public String getPinotSchemaJson() { return pinotSchemaJson; }
        public void setPinotSchemaJson(String pinotSchemaJson) { this.pinotSchemaJson = pinotSchemaJson; }
    }

    /**
     * Status of the Pinot schema
     */
    public static class PinotSchemaStatus {
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
        
        @JsonProperty("currentSchemas.json")
        private String currentSchemasJson;

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
        
        public String getCurrentSchemasJson() { return currentSchemasJson; }
        public void setCurrentSchemasJson(String currentSchemasJson) { this.currentSchemasJson = currentSchemasJson; }
    }
}
