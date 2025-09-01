#!/bin/bash

# Generate Test Data Script
# This script generates sample data for testing Pinot tables

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="${SCRIPT_DIR}/../data"

echo "📊 Generating Test Data for Pinot..."

# Create data directory
mkdir -p "${DATA_DIR}"

# Generate sample CSV data
echo "📝 Generating sample CSV data..."

cat > "${DATA_DIR}/sample_data.csv" << 'EOF'
id,name,category,timestamp,value,count
1,Product A,Electronics,1640995200000,99.99,100
2,Product B,Clothing,1640995200000,29.99,50
3,Product C,Books,1640995200000,19.99,200
4,Product D,Electronics,1640995200000,199.99,25
5,Product E,Clothing,1640995200000,39.99,75
6,Product F,Books,1640995200000,9.99,300
7,Product G,Electronics,1640995200000,299.99,15
8,Product H,Clothing,1640995200000,49.99,60
9,Product I,Books,1640995200000,14.99,150
10,Product J,Electronics,1640995200000,399.99,10
EOF

# Generate JSON data
echo "📝 Generating sample JSON data..."

cat > "${DATA_DIR}/sample_data.json" << 'EOF'
[
  {"id": "1", "name": "Product A", "category": "Electronics", "timestamp": 1640995200000, "value": 99.99, "count": 100},
  {"id": "2", "name": "Product B", "category": "Clothing", "timestamp": 1640995200000, "value": 29.99, "count": 50},
  {"id": "3", "name": "Product C", "category": "Books", "timestamp": 1640995200000, "value": 19.99, "count": 200},
  {"id": "4", "name": "Product D", "category": "Electronics", "timestamp": 1640995200000, "value": 199.99, "count": 25},
  {"id": "5", "name": "Product E", "category": "Clothing", "timestamp": 1640995200000, "value": 39.99, "count": 75},
  {"id": "6", "name": "Product F", "category": "Books", "timestamp": 1640995200000, "value": 9.99, "count": 300},
  {"id": "7", "name": "Product G", "category": "Electronics", "timestamp": 1640995200000, "value": 299.99, "count": 15},
  {"id": "8", "name": "Product H", "category": "Clothing", "timestamp": 1640995200000, "value": 49.99, "count": 60},
  {"id": "9", "name": "Product I", "category": "Books", "timestamp": 1640995200000, "value": 14.99, "count": 150},
  {"id": "10", "name": "Product J", "category": "Electronics", "timestamp": 1640995200000, "value": 399.99, "count": 10}
]
EOF

# Generate larger dataset for performance testing
echo "📝 Generating larger dataset for performance testing..."

cat > "${DATA_DIR}/large_dataset.csv" << 'EOF'
id,name,category,timestamp,value,count
EOF

# Generate 1000 records
for i in {1..1000}; do
    category=$((RANDOM % 3))
    case $category in
        0) cat="Electronics"; value=$((RANDOM % 500 + 50));;
        1) cat="Clothing"; value=$((RANDOM % 100 + 20));;
        2) cat="Books"; value=$((RANDOM % 30 + 10));;
    esac
    
    timestamp=$((1640995200000 + (RANDOM % 86400000)))
    count=$((RANDOM % 500 + 10))
    
    echo "$i,Product_$i,$cat,$timestamp,$value.$((RANDOM % 100)),$count" >> "${DATA_DIR}/large_dataset.csv"
done

# Generate Pinot ingestion job configuration
echo "📝 Generating Pinot ingestion job configuration..."

cat > "${DATA_DIR}/ingestion-job-spec.yaml" << 'EOF'
executionFrameworkSpec:
  name: 'standalone'
  segmentGenerationJobRunnerClassName: 'org.apache.pinot.plugin.ingestion.batch.standalone.SegmentGenerationJobRunner'
  segmentTarPushJobRunnerClassName: 'org.apache.pinot.plugin.ingestion.batch.standalone.SegmentTarPushJobRunner'
  segmentUriPushJobRunnerClassName: 'org.apache.pinot.plugin.ingestion.batch.standalone.SegmentUriPushJobRunner'

jobType: SegmentCreationAndTarPush
inputDirURI: 'file:///data/input'
outputDirURI: 'file:///data/output'
overwriteOutput: true

pinotFSSpecs:
  - scheme: file
    className: org.apache.pinot.spi.filesystem.LocalPinotFS

recordReaderSpec:
  dataFormat: 'csv'
  className: 'org.apache.pinot.plugin.inputformat.csv.CSVRecordReader'
  configClassName: 'org.apache.pinot.plugin.inputformat.csv.CSVRecordReaderConfig'
  configs:
    fileFormat: 'csv'
    header: 'true'
    delimiter: ','

tableSpec:
  tableName: 'test_table'
  schemaURI: 'http://localhost:9050/tables/test_table/schema'
  tableConfigURI: 'http://localhost:9050/tables/test_table'

tableConfigSpec:
  tableConfigURI: 'http://localhost:9050/tables/test_table'
  validationConfigSpec:
    enablePinotClusterUpdate: true
EOF

# Generate Pinot query examples
echo "📝 Generating Pinot query examples..."

cat > "${DATA_DIR}/sample_queries.sql" << 'EOF'
-- Basic SELECT query
SELECT * FROM test_table LIMIT 10;

-- Aggregation query
SELECT category, COUNT(*) as count, AVG(value) as avg_value 
FROM test_table 
GROUP BY category;

-- Time-based query
SELECT * FROM test_table 
WHERE timestamp >= 1640995200000 AND timestamp < 1641081600000;

-- Filtered query
SELECT name, value, count 
FROM test_table 
WHERE category = 'Electronics' AND value > 100;

-- Top N query
SELECT name, value 
FROM test_table 
ORDER BY value DESC 
LIMIT 5;

-- Complex aggregation
SELECT 
    category,
    COUNT(*) as total_products,
    SUM(count) as total_quantity,
    AVG(value) as average_price,
    MIN(value) as min_price,
    MAX(value) as max_price
FROM test_table 
GROUP BY category 
ORDER BY total_products DESC;
EOF

echo "✅ Test data generated successfully!"
echo ""
echo "📁 Generated files:"
echo "  - ${DATA_DIR}/sample_data.csv (10 records)"
echo "  - ${DATA_DIR}/large_dataset.csv (1000 records)"
echo "  - ${DATA_DIR}/sample_data.json (10 records)"
echo "  - ${DATA_DIR}/ingestion-job-spec.yaml (Pinot ingestion config)"
echo "  - ${DATA_DIR}/sample_queries.sql (Sample SQL queries)"
echo ""
echo "🚀 You can now use these files to test Pinot data ingestion and querying!"
