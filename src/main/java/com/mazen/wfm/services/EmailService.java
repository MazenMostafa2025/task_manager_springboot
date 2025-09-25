//package com.mazen.wfm.services;
//
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    public EmailService(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    public void sendTaskAssignmentEmail(String to, String taskName, String taskDescription) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("New Task Assigned: " + taskName);
//        message.setText("You have been assigned a new task '" + taskName +
//                " with description: " + taskDescription + "'. Please check your dashboard.");
//
//        mailSender.send(message);
//    }
//}