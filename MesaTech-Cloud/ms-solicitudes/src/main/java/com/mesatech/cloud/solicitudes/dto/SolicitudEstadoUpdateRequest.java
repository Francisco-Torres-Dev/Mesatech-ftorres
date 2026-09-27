package com.mesatech.cloud.solicitudes.dto;

import com.mesatech.cloud.solicitudes.entity.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudEstadoUpdateRequest {
    @NotNull
    private EstadoSolicitud estado;

    private String observacion;
}
