package com.mesatech.cloud.solicitudes.dto;

import com.mesatech.cloud.solicitudes.entity.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private Long categoriaId;
    private String categoriaNombre;
    private Long prioridadId;
    private String prioridadNombre;
    private String usuarioSolicitante;
    private String usuarioSolicitanteEmail;
    private String estado;
    private LocalDateTime fechaCreacion;
    private String observacion;
    private String usuarioAsignado;
}
