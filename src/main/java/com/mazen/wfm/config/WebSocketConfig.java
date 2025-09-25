//package com.mazen.wfm.config;
//
//import com.mazen.wfm.interceptor.JwtHandshakeInterceptor;
//import com.mazen.wfm.security.JwtService;
//import com.mazen.wfm.utils.UserHandshakeHandler;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//    private final JwtService jwtService;
//
//    public WebSocketConfig(JwtService jwtService) {
//        this.jwtService = jwtService;
//    }
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        config.enableSimpleBroker("/topic", "/queue"); // client subscribes here
//        config.setApplicationDestinationPrefixes("/app"); // client sends here
//        config.setUserDestinationPrefix("/user");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns("*")
//                .addInterceptors(new JwtHandshakeInterceptor(jwtService))
//                .setHandshakeHandler(new UserHandshakeHandler())
//                .withSockJS();
//
//    }
//}
//
//
