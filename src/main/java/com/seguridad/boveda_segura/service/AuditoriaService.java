package com.seguridad.boveda_segura.service;

import com.seguridad.boveda_segura.entity.EventoAuditoria;
import com.seguridad.boveda_segura.repository.EventoAuditoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaService {

    private final EventoAuditoriaRepository repository;

    public AuditoriaService(EventoAuditoriaRepository repository) {
        this.repository = repository;
    }

    public void registrar(String usuario, String accion, String detalle) {
        repository.save(new EventoAuditoria(
                limitar(usuario, 50),
                limitar(accion, 40),
                limitar(detalle, 200)
        ));
    }

    public List<EventoAuditoria> ultimosEventos() {
        return repository.findTop100ByOrderByFechaDesc();
    }

    private String limitar(String texto, int maximo) {
        if (texto == null || texto.isBlank()) {
            return "-";
        }

        String limpio = texto.replaceAll("[\\p{Cntrl}]", " ").trim();

        if (limpio.isBlank()) {
            return "-";
        }

        return limpio.length() > maximo
                ? limpio.substring(0, maximo)
                : limpio;
    }
}