package com.seguridad.boveda_segura.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_auditoria")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false, length = 40)
    private String accion;

    @Column(nullable = false, length = 200)
    private String detalle;

    protected EventoAuditoria() {
    }

    public EventoAuditoria(String usuario, String accion, String detalle) {
        this.fecha = LocalDateTime.now();
        this.usuario = usuario;
        this.accion = accion;
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAccion() {
        return accion;
    }

    public String getDetalle() {
        return detalle;
    }
}