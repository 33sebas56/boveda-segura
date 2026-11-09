package com.seguridad.boveda_segura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/documentos";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}