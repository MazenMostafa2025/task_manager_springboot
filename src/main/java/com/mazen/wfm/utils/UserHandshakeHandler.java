//package com.mazen.wfm.utils;
//
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
//
//import java.security.Principal;
//import java.util.Map;
//
//public class UserHandshakeHandler extends DefaultHandshakeHandler {
//    @Override
//    protected Principal determineUser(
//            org.springframework.http.server.ServerHttpRequest request,
//            WebSocketHandler wsHandler,
//            Map<String, Object> attributes) {
//        String username = (String) attributes.get("user");
//        System.out.println("alo + " + username);
//        return () -> username; // Principal with username as name
//    }
//}
