package com.seguridad.boveda_segura.repository;

import com.seguridad.boveda_segura.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByPropietario_NombreUsuarioOrderByFechaSubidaDesc(
            String usuario
    );

    Optional<Documento> findByIdAndPropietario_NombreUsuario(
            Long id,
            String usuario
    );
}