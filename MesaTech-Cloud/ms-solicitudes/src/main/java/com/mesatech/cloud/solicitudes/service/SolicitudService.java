package com.mesatech.cloud.solicitudes.service;

import com.mesatech.cloud.solicitudes.dto.SolicitudCreateRequest;
import com.mesatech.cloud.solicitudes.dto.SolicitudDto;
import com.mesatech.cloud.solicitudes.dto.SolicitudEstadoUpdateRequest;
import com.mesatech.cloud.solicitudes.dto.SolicitudUpdateRequest;
import com.mesatech.cloud.solicitudes.entity.Categoria;
import com.mesatech.cloud.solicitudes.entity.EstadoSolicitud;
import com.mesatech.cloud.solicitudes.entity.Prioridad;
import com.mesatech.cloud.solicitudes.entity.Solicitud;
import com.mesatech.cloud.solicitudes.entity.Usuario;
import com.mesatech.cloud.solicitudes.exception.ApiException;
import com.mesatech.cloud.solicitudes.repository.CategoriaRepository;
import com.mesatech.cloud.solicitudes.repository.PrioridadRepository;
import com.mesatech.cloud.solicitudes.repository.SolicitudRepository;
import com.mesatech.cloud.solicitudes.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudService {
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrioridadRepository prioridadRepository;

    public SolicitudService(SolicitudRepository solicitudRepository,
                            UsuarioRepository usuarioRepository,
                            CategoriaRepository categoriaRepository,
                            PrioridadRepository prioridadRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.prioridadRepository = prioridadRepository;
    }

    public List<SolicitudDto> getSolicitudes() {
        return solicitudRepository.findAllByOrderByFechaCreacionDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public List<SolicitudDto> getSolicitudesByUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) {
            return List.of();
        }
        return solicitudRepository.findByUsuarioSolicitanteId(usuario.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<SolicitudDto> getSolicitud(Long id) {
        return solicitudRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public SolicitudDto crearSolicitud(SolicitudCreateRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getUsuarioEmail())
            .orElseGet(() -> crearUsuarioCliente(request.getUsuarioEmail()));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        Prioridad prioridad = prioridadRepository.findById(request.getPrioridadId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prioridad no encontrada"));

        if (!categoria.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La categoría no está activa");
        }

        if (!prioridad.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La prioridad no está activa");
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setTitulo(request.getTitulo().trim());
        solicitud.setDescripcion(request.getDescripcion().trim());
        solicitud.setCategoria(categoria);
        solicitud.setPrioridad(prioridad);
        solicitud.setUsuarioSolicitante(usuario);
        solicitud.setEstado(EstadoSolicitud.CREADA);
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitud.setFechaActualizacion(LocalDateTime.now());

        return toDto(solicitudRepository.save(solicitud));
    }

    private Usuario crearUsuarioCliente(String email) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre(email);
        usuario.setRol("CLIENTE");
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public SolicitudDto actualizarEstado(Long id, SolicitudEstadoUpdateRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        EstadoSolicitud nuevoEstado = request.getEstado();
        validarTransicion(solicitud.getEstado(), nuevoEstado);

        solicitud.setEstado(nuevoEstado);
        solicitud.setFechaActualizacion(LocalDateTime.now());
        solicitud.setObservacion(request.getObservacion());
        return toDto(solicitudRepository.save(solicitud));
    }

    @Transactional
    public SolicitudDto actualizarSolicitud(Long id, SolicitudUpdateRequest request) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        Prioridad prioridad = prioridadRepository.findById(request.getPrioridadId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prioridad no encontrada"));

        if (!categoria.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La categoría no está activa");
        }

        if (!prioridad.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La prioridad no está activa");
        }

        solicitud.setTitulo(request.getTitulo().trim());
        solicitud.setDescripcion(request.getDescripcion().trim());
        solicitud.setCategoria(categoria);
        solicitud.setPrioridad(prioridad);
        solicitud.setObservacion(request.getObservacion());
        solicitud.setFechaActualizacion(LocalDateTime.now());

        return toDto(solicitudRepository.save(solicitud));
    }

    @Transactional
    public void eliminarSolicitud(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        try {
            solicitudRepository.delete(solicitud);
            solicitudRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "La solicitud no puede eliminarse por restricciones de integridad");
        }
    }

    private void validarTransicion(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (actual == nuevo) {
            return;
        }

        if (nuevo == EstadoSolicitud.RESUELTA && actual != EstadoSolicitud.EN_PROCESO) {
            throw new ApiException(HttpStatus.CONFLICT, "Transición de estado no permitida: una solicitud solo puede resolverse desde EN_PROCESO");
        }

        boolean transicionValida = switch (actual) {
            case CREADA -> nuevo == EstadoSolicitud.ASIGNADA || nuevo == EstadoSolicitud.CANCELADA;
            case ASIGNADA -> nuevo == EstadoSolicitud.EN_PROCESO || nuevo == EstadoSolicitud.CANCELADA;
            case EN_PROCESO -> nuevo == EstadoSolicitud.RESUELTA || nuevo == EstadoSolicitud.CANCELADA;
            case RESUELTA -> nuevo == EstadoSolicitud.CERRADA;
            case CERRADA -> false;
            case CANCELADA -> false;
        };

        if (!transicionValida) {
            throw new ApiException(HttpStatus.CONFLICT, "Transición de estado no permitida: " + actual + " -> " + nuevo);
        }
    }

    private SolicitudDto toDto(Solicitud solicitud) {
        SolicitudDto dto = new SolicitudDto();
        dto.setId(solicitud.getId());
        dto.setTitulo(solicitud.getTitulo());
        dto.setDescripcion(solicitud.getDescripcion());
        dto.setCategoriaId(solicitud.getCategoria().getId());
        dto.setCategoriaNombre(solicitud.getCategoria().getNombre());
        dto.setPrioridadId(solicitud.getPrioridad().getId());
        dto.setPrioridadNombre(solicitud.getPrioridad().getNombre());
        dto.setUsuarioSolicitante(solicitud.getUsuarioSolicitante().getNombre());
        dto.setUsuarioSolicitanteEmail(solicitud.getUsuarioSolicitante().getEmail());
        dto.setEstado(solicitud.getEstado().name());
        dto.setFechaCreacion(solicitud.getFechaCreacion());
        dto.setObservacion(solicitud.getObservacion());
        return dto;
    }
}
