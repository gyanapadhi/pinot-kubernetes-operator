package io.pinot.operator.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

import java.util.List;

/**
 * PinotTable Custom Resource Definition
 * 
 * Represents a Pinot table configuration that defines how data is stored,
 * indexed, and queried within a Pinot cluster. Tables are the primary
 * data containers in Pinot and must reference a valid schema.
 * 
 * The PinotTable resource manages:
 * - Table type configuration (offline, realtime, hybrid)
 * - Schema association and validation
 * - Segment configuration and partitioning
 * - Ingestion settings and data retention policies
 * - Indexing and optimization configurations
 * - Table-level security and access controls
 * 
 * Tables are created after schemas and can be configured for
 * different use cases including batch analytics, real-time streaming,
 * or hybrid approaches combining both.
 */
@Group("pinot.io")
@Version("v1")
public class PinotTable extends CustomResource<PinotTable.PinotTableSpec, PinotTable.PinotTableStatus> implements Namespaced {

    public static final String KIND = "PinotTable";
    public static final String GROUP = "pinot.io";
    public static final String VERSION = "v1";
    public static final String API_VERSION = GROUP + "/" + VERSION;

    public PinotTable() {
        setKind(KIND);
        setApiVersion(API_VERSION);
    }

    /**
     * Specification for the Pinot table
     */
    public static class PinotTableSpec {
        @JsonProperty("pinotCluster")
        private String pinotCluster;
        
        @JsonProperty("pinotSchema")
        private String pinotSchema;
        
        @JsonProperty("pinotTableType")
        private PinotTableType pinotTableType;
        
        @JsonProperty("tables.json")
        private String pinotTablesJson;
        
        @JsonProperty("segmentReload")
        private boolean segmentReload;

        public String getPinotCluster() { return pinotCluster; }
        public void setPinotCluster(String pinotCluster) { this.pinotCluster = pinotCluster; }
        
        public String getPinotSchema() { return pinotSchema; }
        public void setPinotSchema(String pinotSchema) { this.pinotSchema = pinotSchema; }
        
        public PinotTableType getPinotTableType() { return pinotTableType; }
        public void setPinotTableType(PinotTableType pinotTableType) { this.pinotTableType = pinotTableType; }
        
        public String getPinotTablesJson() { return pinotTablesJson; }
        public void setPinotTablesJson(String pinotTablesJson) { this.pinotTablesJson = pinotTablesJson; }
        
        public boolean isSegmentReload() { return segmentReload; }
        public void setSegmentReload(boolean segmentReload) { this.segmentReload = segmentReload; }
    }

    /**
     * Status of the Pinot table
     */
    public static class PinotTableStatus {
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
        
        @JsonProperty("currentTable.json")
        private String currentTableJson;
        
        @JsonProperty("reloadStatus")
        private List<String> reloadStatus;

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
        
        public String getCurrentTableJson() { return currentTableJson; }
        public void setCurrentTableJson(String currentTableJson) { this.currentTableJson = currentTableJson; }
        
        public List<String> getReloadStatus() { return reloadStatus; }
        public void setReloadStatus(List<String> reloadStatus) { this.reloadStatus = reloadStatus; }
    }

    /**
     * Pinot table types
     */
    public enum PinotTableType {
        REALTIME("realtime"),
        OFFLINE("offline"),
        HYBRID("hybrid");

        private final String value;

        PinotTableType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
