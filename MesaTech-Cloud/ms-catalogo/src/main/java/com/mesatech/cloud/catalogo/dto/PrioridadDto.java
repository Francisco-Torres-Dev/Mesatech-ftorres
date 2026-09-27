package com.mesatech.cloud.catalogo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrioridadDto {
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    private String nombre;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer nivel;

    private Boolean activo = true;
}
