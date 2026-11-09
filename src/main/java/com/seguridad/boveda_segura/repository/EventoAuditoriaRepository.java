package com.seguridad.boveda_segura.repository;

import com.seguridad.boveda_segura.entity.EventoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoAuditoriaRepository
        extends JpaRepository<EventoAuditoria, Long> {

    List<EventoAuditoria> findTop100ByOrderByFechaDesc();
}