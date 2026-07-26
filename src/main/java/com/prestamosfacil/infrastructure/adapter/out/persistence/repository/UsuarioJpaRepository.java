package com.prestamosfacil.infrastructure.adapter.out.persistence.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByNumeroDocumento(String numeroDocumento);
}
