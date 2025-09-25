//package com.mazen.wfm.interceptor;
//
//import com.mazen.wfm.security.JwtService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//import jakarta.servlet.http.HttpServletRequest;
//import java.util.Map;
//
//@RequiredArgsConstructor
//public class JwtHandshakeInterceptor implements HandshakeInterceptor {
//
//    private final JwtService jwtService;
//
//    @Override
//    public boolean beforeHandshake(
//            ServerHttpRequest request,
//            ServerHttpResponse response,
//            WebSocketHandler wsHandler,
//            Map<String, Object> attributes) {
//
//        if (request instanceof ServletServerHttpRequest servletRequest) {
//            HttpServletRequest httpRequest = servletRequest.getServletRequest();
//            String authHeader = httpRequest.getHeader("Authorization");
//
//            if (authHeader == null) {
//                authHeader = httpRequest.getParameter("token");
//                System.err.println("query param here?");
//            }
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                String token = authHeader.substring(7);
//                System.err.println("token here?");
//
//                try {
//                    String username = jwtService.extractUsername(token);
//                    System.err.println("interceptor username: " + username);
////                    System.err.println("Are we even here?");
//
//                    attributes.put("user", username); // store in session attributes
//                    attributes.put("token", token);
//                    System.out.println("WebSocket handshake successful for user: " + username);
//                    return true;
//                } catch (Exception e) {
//                    System.out.println("token validation failed: " + e.getMessage());
//                    return false; // invalid token → reject handshake
//                }
//            }
//        }
//        System.err.println("WebSocket handshake failed: No valid token found");
//        return false; // no token → reject
//    }
//
//    @Override
//    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
//                               WebSocketHandler wsHandler, Exception exception) {
//        // Log handshake completion
//        if (exception != null) {
//            System.err.println("WebSocket handshake completed with error: " + exception.getMessage());
//        } else {
//            System.out.println("WebSocket handshake completed successfully");
//        }
//    }
//}
