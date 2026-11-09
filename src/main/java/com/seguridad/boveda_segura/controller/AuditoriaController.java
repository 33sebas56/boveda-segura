package com.seguridad.boveda_segura.controller;

import com.seguridad.boveda_segura.service.AuditoriaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditoriaController {

    private final AuditoriaService auditoria;

    public AuditoriaController(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping("/admin/auditoria")
    public String verAuditoria(Model model) {
        model.addAttribute(
                "eventos",
                auditoria.ultimosEventos()
        );

        return "auditoria";
    }
}