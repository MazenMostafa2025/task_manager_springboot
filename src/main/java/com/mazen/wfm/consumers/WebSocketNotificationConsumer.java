//package com.mazen.wfm.consumers;
//
//import com.mazen.wfm.config.RabbitMQConfig;
//import com.mazen.wfm.dtos.request.NotificationMessage;
//import com.mazen.wfm.event.TaskAssignmentEvent;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.messaging.simp.user.SimpUser;
//import org.springframework.messaging.simp.user.SimpUserRegistry;
//import org.springframework.stereotype.Component;
//
//
//@Component
//public class WebSocketNotificationConsumer {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final SimpUserRegistry userRegistry;
//    WebSocketNotificationConsumer(SimpMessagingTemplate messagingTemplate, SimpUserRegistry userRegistry) {
//        this.messagingTemplate = messagingTemplate;
//        this.userRegistry = userRegistry;
//    }
//
//    @RabbitListener(queues = RabbitMQConfig.WEBSOCKET_QUEUE)
//    public void handleWebSocketNotification(TaskAssignmentEvent event) {
//        try {
//            NotificationMessage notification = new NotificationMessage(
//                    "TASK_ASSIGNED",
//                    "You have been assigned to task: " + event.getTaskName(),
//                    event.getTaskId(),
//                    event.getUserId(),
//                    event.getAssignedAt().toLocalDate()
//            );
//
//
//            // Send to specific user
//            System.out.println("event username " + event.getUserName());
//            userRegistry.getUsers().forEach(u -> System.out.println("online user: " + u.getName()));
//                messagingTemplate.convertAndSendToUser(
//                        event.getUserName(), // Principal.getName(),
//                        "/queue/notifications",
//                        notification
//                );
////            messagingTemplate.convertAndSend("/topic/test", notification);
//
//
//            System.out.println("WebSocket notification sent to user: " + event.getUserId());
//
//        } catch (Exception e) {
//            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
//        }
//    }
//    }