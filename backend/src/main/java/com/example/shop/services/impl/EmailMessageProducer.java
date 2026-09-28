package com.example.shop.services.impl;

import com.example.shop.configs.RabbitMqConfig;
import com.example.shop.dtos.mail.VerificationCodeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendRegisterVerificationEmail(String email, String verificationCode) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_NAME,
                RabbitMqConfig.REGISTER_VERIFICATION_CODE_KEY,
                new VerificationCodeMessage(email, verificationCode));
        log.info("Published verification message to queue, email={}", email);
    }
}
