package com.mesatech.cloud.catalogo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);
    public static final String EXCHANGE_NAME = "catalogo.exchange";
    public static final String ROUTING_KEY = "categoria.creada";

    @Bean
    public DirectExchange catalogoExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("[CONFIRM] Mensaje confirmado por el broker RabbitMQ. Correlacion: {}", correlationData != null ? correlationData.getId() : "null");
            } else {
                log.error("[NACK] El broker rechazo el mensaje. Causa: {}", cause);
            }
        });

        template.setReturnsCallback(returned -> {
            log.warn("[RETURN] Mensaje no enrutable devuelto por el broker. ReplyCode: {}, Exchange: {}, RoutingKey: {}",
                    returned.getReplyCode(), returned.getExchange(), returned.getRoutingKey());
        });

        return template;
    }
}
