package com.example.shop.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String supportEmail;

    public void sendRegisterVerificationEmail(String email, String verificationCode)
            throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(supportEmail);
        helper.setTo(email);
        helper.setSubject("Подтверждение регистрации");

        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        helper.setText(templateEngine.process("auth/register", context), true);

        mailSender.send(mimeMessage);
        log.info("Sent verification email to={}", email);
    }
}
