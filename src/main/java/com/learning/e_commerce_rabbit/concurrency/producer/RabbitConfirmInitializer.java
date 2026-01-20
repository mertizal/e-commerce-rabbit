//package com.learning.concurrency_practice.concurrency.producer;
//
//import com.learning.concurrency_practice.concurrency.exception.MessageConfirmationException;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class RabbitConfirmInitializer {
//
//    private final RabbitTemplate rabbitTemplate;
//
//    @PostConstruct
//    public void init() {
//
//        rabbitTemplate.setConfirmCallback(
//                (correlationData, ack, cause) -> {
//
//                    if (ack) {
//                        log.info(
//                                "MESSAGE CONFIRMED | correlationId={}",
//                                correlationData != null
//                                        ? correlationData.getId()
//                                        : "null"
//                        );
//                    } else {
//                        log.error(
//                                "MESSAGE NACKED | correlationId={} | cause={}",
//                                correlationData != null
//                                        ? correlationData.getId()
//                                        : "null",
//                                cause
//                        );
//
//                        throw new MessageConfirmationException(
//                                "Message not confirmed by broker"
//                        );
//                    }
//                }
//        );
//    }
//}
//
