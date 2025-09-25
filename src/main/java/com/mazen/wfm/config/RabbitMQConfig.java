//package com.mazen.wfm.config;
//
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.annotation.EnableRabbit;
//import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@EnableRabbit
//public class RabbitMQConfig {
//
//    public static final String TASK_ASSIGNMENT_EXCHANGE = "task.assignment.exchange";
//    public static final String EMAIL_QUEUE = "task.assignment.email";
//    public static final String WEBSOCKET_QUEUE = "task.assignment.websocket";
//
//    @Bean
//    public TopicExchange taskAssignmentExchange() {
//        return new TopicExchange(TASK_ASSIGNMENT_EXCHANGE);
//    }
//
//    @Bean
//    public Queue emailQueue() {
//        return QueueBuilder.durable(EMAIL_QUEUE).build();
//    }
//
//    @Bean
//    public Queue websocketQueue() {
//        return QueueBuilder.durable(WEBSOCKET_QUEUE).build();
//    }
//
//    @Bean
//    public Binding emailBinding() {
//        return BindingBuilder
//                .bind(emailQueue())
//                .to(taskAssignmentExchange())
//                .with("task.assigned.email");
//    }
//
//    @Bean
//    public Binding websocketBinding() {
//        return BindingBuilder
//                .bind(websocketQueue())
//                .to(taskAssignmentExchange())
//                .with("task.assigned.websocket");
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMessageConverter(new Jackson2JsonMessageConverter());
//        return template;
//    }
//    @Bean
//    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
//            ConnectionFactory connectionFactory,
//            Jackson2JsonMessageConverter messageConverter) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setMessageConverter(messageConverter);
//        return factory;
//    }
//}