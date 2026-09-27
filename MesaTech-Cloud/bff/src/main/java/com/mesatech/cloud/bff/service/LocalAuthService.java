package com.mesatech.cloud.bff.service;

import com.mesatech.cloud.bff.dto.AuthUserDto;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LocalAuthService {
    private static final Map<String, AuthUserDto> USERS = Map.of(
        "CLIENTE", new AuthUserDto("11111111-1111-1111-1111-111111111111", "Cliente Demo", "cliente@mesatech.local", "CLIENTE", "local-token-cliente", "local"),
        "OPERADOR", new AuthUserDto("22222222-2222-2222-2222-222222222222", "Operador Demo", "operador@mesatech.local", "OPERADOR", "local-token-operador", "local"),
        "ADMINISTRADOR", new AuthUserDto("33333333-3333-3333-3333-333333333333", "Admin Demo", "admin@mesatech.local", "ADMINISTRADOR", "local-token-admin", "local")
    );

    public AuthUserDto login(String rol) {
        String normalized = rol == null ? "" : rol.trim().toUpperCase();
        AuthUserDto user = USERS.get(normalized);
        if (user == null) {
            throw new IllegalArgumentException("Rol no válido. Usa CLIENTE, OPERADOR o ADMINISTRADOR");
        }
        return user;
    }
}
