package com.example.shop.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String supportEmail;

    public void sendRegisterVerificationEmail(String email, String verificationCode) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(supportEmail);
        mail.setTo(email);
        mail.setSubject("Подтверждение регистрации");
        mail.setText("Ваш код подтверждения: " + verificationCode);

        mailSender.send(mail);
        log.info("Sent verification email to={}", email);
    }
}
