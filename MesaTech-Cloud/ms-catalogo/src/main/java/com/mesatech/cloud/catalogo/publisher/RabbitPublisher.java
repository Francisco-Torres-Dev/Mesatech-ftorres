package com.mesatech.cloud.catalogo.publisher;

import com.mesatech.cloud.catalogo.config.RabbitConfig;
import com.mesatech.cloud.catalogo.event.CategoriaCreadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RabbitPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean publishCategoriaCreada(Long categoriaId, String nombre) {
        String eventId = UUID.randomUUID().toString();
        String fecha = Instant.now().toString();
        CategoriaCreadaEvent event = new CategoriaCreadaEvent(eventId, "CATEGORIA_CREADA", fecha, categoriaId, nombre);

        CorrelationData correlationData = new CorrelationData(eventId);

        log.info("[PUBLICADOR] Publicando evento CategoriaCreada -> eventId: {}, categoriaId: {}, nombre: {}",
                eventId, categoriaId, nombre);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY,
                    event,
                    m -> {
                        m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return m;
                    },
                    correlationData
            );

            // Esperar confirmacion del broker (máximo 3 segundos)
            CorrelationData.Confirm confirm = correlationData.getFuture().get(3, TimeUnit.SECONDS);
            if (confirm != null && confirm.isAck()) {
                log.info("[PUBLICADOR OK] Broker confirmo recepcion del evento: {}", eventId);
                return true;
            } else {
                log.warn("[PUBLICADOR FALLO] Broker no confirmo el evento: {}", eventId);
                return false;
            }
        } catch (Exception e) {
            log.error("[PUBLICADOR ERROR] Excepcion al publicar en RabbitMQ: {}", e.getMessage());
            return false;
        }
    }
}
