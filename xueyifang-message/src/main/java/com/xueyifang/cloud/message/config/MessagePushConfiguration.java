package com.xueyifang.cloud.message.config;

import com.xueyifang.cloud.message.websocket.RedisMessagePushSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@EnableConfigurationProperties(MessagePushProperties.class)
public class MessagePushConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "xueyifang.message.push.redis", name = "enabled", havingValue = "true")
    public RedisMessageListenerContainer messageRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisMessagePushSubscriber subscriber,
            MessagePushProperties properties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(properties.getRedis().getChannel()));
        return container;
    }
}
