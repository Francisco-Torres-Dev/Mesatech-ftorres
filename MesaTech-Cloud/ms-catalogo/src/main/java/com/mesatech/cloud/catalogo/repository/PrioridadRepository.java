package com.mesatech.cloud.catalogo.repository;

import com.mesatech.cloud.catalogo.entity.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {
    Optional<Prioridad> findByNombre(String nombre);
    List<Prioridad> findByActivoTrue();
}
