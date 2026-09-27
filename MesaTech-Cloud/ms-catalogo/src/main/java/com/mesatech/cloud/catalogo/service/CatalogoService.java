package com.mesatech.cloud.catalogo.service;

import com.mesatech.cloud.catalogo.dto.CategoriaDto;
import com.mesatech.cloud.catalogo.dto.PrioridadDto;
import com.mesatech.cloud.catalogo.entity.Categoria;
import com.mesatech.cloud.catalogo.entity.Prioridad;
import com.mesatech.cloud.catalogo.exception.ApiException;
import com.mesatech.cloud.catalogo.repository.CategoriaRepository;
import com.mesatech.cloud.catalogo.repository.PrioridadRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CatalogoService {
    private final CategoriaRepository categoriaRepository;
    private final PrioridadRepository prioridadRepository;

    public CatalogoService(CategoriaRepository categoriaRepository, PrioridadRepository prioridadRepository) {
        this.categoriaRepository = categoriaRepository;
        this.prioridadRepository = prioridadRepository;
    }

    public List<Object> getCatalogo() {
        List<Object> catalogo = new ArrayList<>();
        catalogo.addAll(categoriaRepository.findByActivoTrue().stream().map(this::toCategoriaDto).toList());
        catalogo.addAll(prioridadRepository.findByActivoTrue().stream().map(this::toPrioridadDto).toList());
        return catalogo;
    }

    public List<CategoriaDto> getCategorias() {
        return categoriaRepository.findByActivoTrue().stream().map(this::toCategoriaDto).toList();
    }

    @Transactional
    public CategoriaDto crearCategoria(CategoriaDto dto) {
        if (categoriaRepository.findByNombre(dto.getNombre().trim()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "La categoría ya existe");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre().trim());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setActivo(Boolean.TRUE.equals(dto.getActivo()));
        return toCategoriaDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDto actualizarCategoria(Long id, CategoriaDto dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        if (!Objects.equals(categoria.getNombre(), dto.getNombre().trim())
                && categoriaRepository.findByNombre(dto.getNombre().trim()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "La categoría ya existe");
        }

        categoria.setNombre(dto.getNombre().trim());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setActivo(Boolean.TRUE.equals(dto.getActivo()));
        return toCategoriaDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        categoriaRepository.delete(categoria);
    }

    public List<PrioridadDto> getPrioridades() {
        return prioridadRepository.findByActivoTrue().stream().map(this::toPrioridadDto).toList();
    }

    @Transactional
    public PrioridadDto crearPrioridad(PrioridadDto dto) {
        if (prioridadRepository.findByNombre(dto.getNombre().trim()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "La prioridad ya existe");
        }
        Prioridad prioridad = new Prioridad();
        prioridad.setNombre(dto.getNombre().trim());
        prioridad.setNivel(dto.getNivel());
        prioridad.setActivo(Boolean.TRUE.equals(dto.getActivo()));
        return toPrioridadDto(prioridadRepository.save(prioridad));
    }

    @Transactional
    public PrioridadDto actualizarPrioridad(Long id, PrioridadDto dto) {
        Prioridad prioridad = prioridadRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prioridad no encontrada"));

        if (!Objects.equals(prioridad.getNombre(), dto.getNombre().trim())
                && prioridadRepository.findByNombre(dto.getNombre().trim()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "La prioridad ya existe");
        }

        prioridad.setNombre(dto.getNombre().trim());
        prioridad.setNivel(dto.getNivel());
        prioridad.setActivo(Boolean.TRUE.equals(dto.getActivo()));
        return toPrioridadDto(prioridadRepository.save(prioridad));
    }

    @Transactional
    public void eliminarPrioridad(Long id) {
        Prioridad prioridad = prioridadRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prioridad no encontrada"));
        prioridadRepository.delete(prioridad);
    }

    private CategoriaDto toCategoriaDto(Categoria categoria) {
        CategoriaDto dto = new CategoriaDto();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setActivo(categoria.isActivo());
        return dto;
    }

    private PrioridadDto toPrioridadDto(Prioridad prioridad) {
        PrioridadDto dto = new PrioridadDto();
        dto.setId(prioridad.getId());
        dto.setNombre(prioridad.getNombre());
        dto.setNivel(prioridad.getNivel());
        dto.setActivo(prioridad.isActivo());
        return dto;
    }
}
