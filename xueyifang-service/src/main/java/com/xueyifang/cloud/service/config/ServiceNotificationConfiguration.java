package com.xueyifang.cloud.service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ServiceNotificationProperties.class)
public class ServiceNotificationConfiguration {

    @Bean
    @LoadBalanced
    public RestClient.Builder serviceMessageRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient serviceMessageRestClient(
            @Qualifier("serviceMessageRestClientBuilder") RestClient.Builder builder,
            ServiceNotificationProperties properties) {
        return builder.baseUrl(properties.normalizedMessageBaseUrl()).build();
    }
}
