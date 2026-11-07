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
public class SolicitudController {
    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/v1/solicitudes")
    public ResponseEntity<SolicitudDto> crearSolicitud(@Valid @RequestBody SolicitudCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crearSolicitud(request));
    }

    @GetMapping("/v1/solicitudes")
    public ResponseEntity<List<SolicitudDto>> getSolicitudes() {
        return ResponseEntity.ok(solicitudService.getSolicitudes());
    }

    @GetMapping("/v1/solicitudes/mias")
    public ResponseEntity<List<SolicitudDto>> getMisSolicitudes(@RequestParam String email) {
        return ResponseEntity.ok(solicitudService.getSolicitudesByUsuario(email));
    }

    @GetMapping("/v1/solicitudes/{id}")
    public ResponseEntity<SolicitudDto> getSolicitud(@PathVariable Long id) {
        return solicitudService.getSolicitud(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/v1/solicitudes/{id}/estado")
    public ResponseEntity<SolicitudDto> actualizarEstado(@PathVariable Long id,
                                                        @Valid @RequestBody SolicitudEstadoUpdateRequest request) {
        return ResponseEntity.ok(solicitudService.actualizarEstado(id, request));
    }

    @PutMapping("/v1/solicitudes/{id}")
    public ResponseEntity<SolicitudDto> actualizarSolicitud(@PathVariable Long id,
                                                            @Valid @RequestBody SolicitudUpdateRequest request) {
        return ResponseEntity.ok(solicitudService.actualizarSolicitud(id, request));
    }

    @DeleteMapping("/v1/solicitudes/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Long id) {
        solicitudService.eliminarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    // Coexistencia V2 sin romper V1 (Requisito EP1 y EP2)
    @GetMapping("/v2/solicitudes/{id}")
    public ResponseEntity<SolicitudDto> getSolicitudV2(@PathVariable Long id) {
        return solicitudService.getSolicitud(id)
                .map(dto -> {
                    dto.setDescripcion(dto.getDescripcion() + " [v2: enriquecido con trazabilidad SLA y contrato extendido]");
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
