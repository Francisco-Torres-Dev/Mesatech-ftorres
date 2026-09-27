package com.mesatech.cloud.solicitudes.repository;

import com.mesatech.cloud.solicitudes.entity.EstadoSolicitud;
import com.mesatech.cloud.solicitudes.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByUsuarioSolicitanteId(UUID usuarioSolicitanteId);
    List<Solicitud> findByEstado(EstadoSolicitud estado);
    List<Solicitud> findByUsuarioAsignadoId(UUID usuarioAsignadoId);
    List<Solicitud> findAllByOrderByFechaCreacionDesc();
}
