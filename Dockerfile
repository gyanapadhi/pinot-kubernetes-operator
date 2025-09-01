# Multi-stage build for Apache Pinot Control Plane Operator
FROM openjdk:11-jre-slim as runtime

# Set working directory
WORKDIR /app

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r pinot && useradd -r -g pinot pinot

# Copy the application jar
COPY target/pinot-kubernetes-operator-*.jar app.jar

# Change ownership to non-root user
RUN chown pinot:pinot app.jar

# Switch to non-root user
USER pinot

# Expose ports
EXPOSE 8080 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
