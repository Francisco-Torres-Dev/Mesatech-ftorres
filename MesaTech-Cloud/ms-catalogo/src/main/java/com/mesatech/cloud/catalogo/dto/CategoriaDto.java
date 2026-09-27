package com.mesatech.cloud.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDto {
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    private Boolean activo = true;
}
