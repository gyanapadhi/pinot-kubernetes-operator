package io.pinot.operator.config;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Kubernetes client
 * 
 * This class provides the Fabric8 Kubernetes client bean
 * that will be used throughout the application.
 */
@Configuration
public class KubernetesConfig {

    /**
     * Create and configure the Kubernetes client
     */
    @Bean
    public KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient();
    }
}
