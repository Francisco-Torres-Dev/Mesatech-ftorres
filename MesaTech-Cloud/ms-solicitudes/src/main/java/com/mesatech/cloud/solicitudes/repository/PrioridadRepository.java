package com.mesatech.cloud.solicitudes.repository;

import com.mesatech.cloud.solicitudes.entity.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {
    Optional<Prioridad> findByNombre(String nombre);
}
