package com.xueyifang.cloud.message.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class MessageWebSocketConfiguration implements WebSocketConfigurer {

    private final MessageWebSocketHandler webSocketHandler;

    private final MessageWebSocketAuthInterceptor authInterceptor;

    public MessageWebSocketConfiguration(MessageWebSocketHandler webSocketHandler,
                                         MessageWebSocketAuthInterceptor authInterceptor) {
        this.webSocketHandler = webSocketHandler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*");
    }
}
