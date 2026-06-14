package com.xueyifang.cloud.trade.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TradeNotificationProperties.class)
public class TradeNotificationConfiguration {

    @Bean
    @LoadBalanced
    public RestClient.Builder tradeMessageRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient tradeMessageRestClient(
            @Qualifier("tradeMessageRestClientBuilder") RestClient.Builder builder,
            TradeNotificationProperties properties) {
        return builder.baseUrl(properties.normalizedMessageBaseUrl()).build();
    }
}
