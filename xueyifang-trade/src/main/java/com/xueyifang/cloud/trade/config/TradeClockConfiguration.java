package com.xueyifang.cloud.trade.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TradeClockConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock tradeClock() {
        return Clock.systemDefaultZone();
    }
}
