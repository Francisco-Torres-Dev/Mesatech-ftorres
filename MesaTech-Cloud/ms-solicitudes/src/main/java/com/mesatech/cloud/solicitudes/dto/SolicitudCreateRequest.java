package com.mesatech.cloud.solicitudes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreateRequest {
    @NotBlank
    @Size(min = 5, max = 150)
    private String titulo;

    @NotBlank
    @Size(min = 10, max = 2000)
    private String descripcion;

    @NotNull
    private Long categoriaId;

    @NotNull
    private Long prioridadId;

    @NotBlank
    private String usuarioEmail;
}
