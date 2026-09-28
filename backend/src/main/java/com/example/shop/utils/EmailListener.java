package com.example.shop.utils;

import com.example.shop.configs.RabbitMqConfig;
import com.example.shop.dtos.mail.VerificationCodeMessage;
import com.example.shop.services.impl.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMqConfig.REGISTER_VERIFICATION_CODE_QUEUE)
    public void receiveRegisterVerificationCodeMessage(VerificationCodeMessage message) {
        log.debug("Received verification message from queue, to={}", message.email());
        emailService.sendRegisterVerificationEmail(message.email(), message.verificationCode());
    }
}
