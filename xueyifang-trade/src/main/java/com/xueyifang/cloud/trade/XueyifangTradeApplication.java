package com.xueyifang.cloud.trade;

import com.xueyifang.cloud.trade.config.TradeOrderTaskProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TradeOrderTaskProperties.class)
public class XueyifangTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(XueyifangTradeApplication.class, args);
    }
}
