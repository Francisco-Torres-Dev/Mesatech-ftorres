package com.mesatech.cloud.solicitudes.consumer;

import com.mesatech.cloud.solicitudes.config.RabbitConfig;
import com.mesatech.cloud.solicitudes.entity.Categoria;
import com.mesatech.cloud.solicitudes.event.CategoriaCreadaEvent;
import com.mesatech.cloud.solicitudes.repository.CategoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RabbitConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitConsumer.class);
    private final CategoriaRepository categoriaRepository;

    public RabbitConsumer(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    @Transactional
    public void recibirEventoCategoriaCreada(CategoriaCreadaEvent evento) {
        log.info("[CONSUMIDOR] Mensaje recibido desde RabbitMQ -> eventId: {}, tipo: {}, categoriaId: {}, nombre: {}",
                evento.eventId(), evento.tipoEvento(), evento.categoriaId(), evento.nombre());

        try {
            Categoria categoria = categoriaRepository.findById(evento.categoriaId())
                    .orElse(new Categoria());

            categoria.setId(evento.categoriaId());
            categoria.setNombre(evento.nombre());
            categoria.setActivo(true);
            categoria.setDescripcion("Sincronizada vía RabbitMQ (" + evento.tipoEvento() + ")");

            categoriaRepository.save(categoria);

            log.info("[CONSUMIDOR OK] Categoría persistida exitosamente en RDS Solicitudes con ID origen: {}", evento.categoriaId());
        } catch (Exception e) {
            log.error("[CONSUMIDOR ERROR] Error al persistir copia local de categoría: {}", e.getMessage(), e);
            throw e; // Propagar para que Spring AMQP gestione según la política de acuse
        }
    }
}
