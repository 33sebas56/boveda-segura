package com.seguridad.boveda_segura.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String nombreOriginal;

    @Column(nullable = false, unique = true, length = 50)
    private String nombreAlmacenado;

    @Column(nullable = false, length = 64)
    private String huellaSha256;

    @Column(nullable = false)
    private long tamano;

    @Column(nullable = false)
    private LocalDateTime fechaSubida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario propietario;

    protected Documento() {
    }

    public Documento(
            String nombreOriginal,
            String nombreAlmacenado,
            String huellaSha256,
            long tamano,
            Usuario propietario
    ) {
        this.nombreOriginal = nombreOriginal;
        this.nombreAlmacenado = nombreAlmacenado;
        this.huellaSha256 = huellaSha256;
        this.tamano = tamano;
        this.propietario = propietario;
        this.fechaSubida = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public String getNombreAlmacenado() {
        return nombreAlmacenado;
    }

    public String getHuellaSha256() {
        return huellaSha256;
    }

    public long getTamano() {
        return tamano;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }
}