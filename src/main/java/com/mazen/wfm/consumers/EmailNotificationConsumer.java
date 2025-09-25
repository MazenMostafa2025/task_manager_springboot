//package com.mazen.wfm.consumers;
//
//import com.mazen.wfm.config.RabbitMQConfig;
//import com.mazen.wfm.event.TaskAssignmentEvent;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Component;
//
//@Component
//public class EmailNotificationConsumer {
//
//    private JavaMailSender mailSender;
//    public EmailNotificationConsumer(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
//    public void handleEmailNotification(TaskAssignmentEvent event) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(event.getUserEmail());
//            message.setSubject("Task Assigned: " + event.getTaskName());
//            message.setText(String.format(
//                    "Hello %s,\n\nYou have been assigned to task: %s\n\nDescription: %s\n\nBest regards,\nTask Management System",
//                    event.getUserName(),
//                    event.getTaskName(),
//                    event.getTaskDescription()
//            ));
//
//            mailSender.send(message);
//            System.out.println("Email sent successfully to: " + event.getUserEmail());
//
//        } catch (Exception e) {
//            System.err.println("Failed to send email: " + e.getMessage());
//            // hena n-implement retry logic aw dead letter queue b3den
//        }
//    }
//}