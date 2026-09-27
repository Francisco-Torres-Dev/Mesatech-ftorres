package com.mesatech.cloud.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDto {
    private String id;
    private String nombre;
    private String email;
    private String rol;
    private String token;
    private String authMode;
}
