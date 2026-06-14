package com.xueyifang.cloud.user.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(UserNotificationProperties.class)
public class UserNotificationConfiguration {

    @Bean
    @LoadBalanced
    public RestClient.Builder userMessageRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient userMessageRestClient(
            @Qualifier("userMessageRestClientBuilder") RestClient.Builder builder,
            UserNotificationProperties properties) {
        return builder.baseUrl(properties.normalizedMessageBaseUrl()).build();
    }
}
