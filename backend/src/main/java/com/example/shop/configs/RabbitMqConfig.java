package com.example.shop.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REGISTER_VERIFICATION_CODE_QUEUE = "queueVerificationCodeRegister";
    public static final String REGISTER_VERIFICATION_CODE_KEY = "registerVerificationCode.key";
    public static final String EXCHANGE_NAME = "exchange";

    @Bean
    public Queue registerVerificationCodeQueue() {
        return new Queue(REGISTER_VERIFICATION_CODE_QUEUE, true);
    }

    @Bean
    public Exchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding registerVerificationCodeQueueBinding(Queue registerVerificationCodeQueue, Exchange exchange) {
        return BindingBuilder
                .bind(registerVerificationCodeQueue)
                .to(exchange)
                .with(REGISTER_VERIFICATION_CODE_KEY)
                .noargs();
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
