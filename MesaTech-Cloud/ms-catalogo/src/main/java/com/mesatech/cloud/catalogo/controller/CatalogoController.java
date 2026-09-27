package com.mesatech.cloud.catalogo.controller;

import com.mesatech.cloud.catalogo.dto.CategoriaDto;
import com.mesatech.cloud.catalogo.dto.PrioridadDto;
import com.mesatech.cloud.catalogo.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class CatalogoController {
    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<Object>> getCatalogo() {
        return ResponseEntity.ok(catalogoService.getCatalogo());
    }

    @GetMapping("/catalogo/categorias")
    public ResponseEntity<List<CategoriaDto>> getCategorias() {
        return ResponseEntity.ok(catalogoService.getCategorias());
    }

    @PostMapping("/catalogo/categorias")
    public ResponseEntity<CategoriaDto> crearCategoria(@Valid @RequestBody CategoriaDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.crearCategoria(dto));
    }

    @PutMapping("/catalogo/categorias/{id}")
    public ResponseEntity<CategoriaDto> actualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaDto dto) {
        return ResponseEntity.ok(catalogoService.actualizarCategoria(id, dto));
    }

    @DeleteMapping("/catalogo/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        catalogoService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/catalogo/prioridades")
    public ResponseEntity<List<PrioridadDto>> getPrioridades() {
        return ResponseEntity.ok(catalogoService.getPrioridades());
    }

    @PostMapping("/catalogo/prioridades")
    public ResponseEntity<PrioridadDto> crearPrioridad(@Valid @RequestBody PrioridadDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.crearPrioridad(dto));
    }

    @PutMapping("/catalogo/prioridades/{id}")
    public ResponseEntity<PrioridadDto> actualizarPrioridad(@PathVariable Long id, @Valid @RequestBody PrioridadDto dto) {
        return ResponseEntity.ok(catalogoService.actualizarPrioridad(id, dto));
    }

    @DeleteMapping("/catalogo/prioridades/{id}")
    public ResponseEntity<Void> eliminarPrioridad(@PathVariable Long id) {
        catalogoService.eliminarPrioridad(id);
        return ResponseEntity.noContent().build();
    }
}
