package com.mesatech.cloud.solicitudes.controller;

import com.mesatech.cloud.solicitudes.dto.SolicitudCreateRequest;
import com.mesatech.cloud.solicitudes.dto.SolicitudDto;
import com.mesatech.cloud.solicitudes.dto.SolicitudEstadoUpdateRequest;
import com.mesatech.cloud.solicitudes.dto.SolicitudUpdateRequest;
import com.mesatech.cloud.solicitudes.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class SolicitudController {
    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudDto> crearSolicitud(@Valid @RequestBody SolicitudCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crearSolicitud(request));
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudDto>> getSolicitudes() {
        return ResponseEntity.ok(solicitudService.getSolicitudes());
    }

    @GetMapping("/solicitudes/mias")
    public ResponseEntity<List<SolicitudDto>> getMisSolicitudes(@RequestParam String email) {
        return ResponseEntity.ok(solicitudService.getSolicitudesByUsuario(email));
    }

    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<SolicitudDto> getSolicitud(@PathVariable Long id) {
        return solicitudService.getSolicitud(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/solicitudes/{id}/estado")
    public ResponseEntity<SolicitudDto> actualizarEstado(@PathVariable Long id,
                                                       @Valid @RequestBody SolicitudEstadoUpdateRequest request) {
        return ResponseEntity.ok(solicitudService.actualizarEstado(id, request));
    }

    @PutMapping("/solicitudes/{id}")
    public ResponseEntity<SolicitudDto> actualizarSolicitud(@PathVariable Long id,
                                                            @Valid @RequestBody SolicitudUpdateRequest request) {
        return ResponseEntity.ok(solicitudService.actualizarSolicitud(id, request));
    }

    @DeleteMapping("/solicitudes/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        solicitudService.eliminarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v2/solicitudes/{id}")
    public ResponseEntity<SolicitudDto> getSolicitudV2(@PathVariable Long id) {
        return solicitudService.getSolicitud(id)
                .map(dto -> {
                    dto.setDescripcion(dto.getDescripcion() + " (v2: detalle ampliado)");
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
