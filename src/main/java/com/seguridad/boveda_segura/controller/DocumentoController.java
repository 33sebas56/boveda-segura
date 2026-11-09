package com.seguridad.boveda_segura.controller;

import com.seguridad.boveda_segura.service.AuditoriaService;
import com.seguridad.boveda_segura.service.DocumentoService;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Controller
public class DocumentoController {

    private final DocumentoService documentos;
    private final AuditoriaService auditoria;

    public DocumentoController(
            DocumentoService documentos,
            AuditoriaService auditoria
    ) {
        this.documentos = documentos;
        this.auditoria = auditoria;
    }

    @GetMapping("/documentos")
    public String listar(
            Authentication autenticacion,
            Model model
    ) {
        String usuario = autenticacion.getName();

        boolean esAdmin = autenticacion.getAuthorities()
                .stream()
                .anyMatch(permiso ->
                        permiso.getAuthority().equals("ROLE_ADMIN")
                );

        model.addAttribute("usuario", usuario);
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("documentos", documentos.listar(usuario));

        return "documentos";
    }

    @PostMapping("/documentos")
    public String subir(
            @RequestParam("archivo") MultipartFile archivo,
            Authentication autenticacion,
            RedirectAttributes mensajes
    ) {
        try {
            documentos.guardar(
                    archivo,
                    autenticacion.getName()
            );

            auditoria.registrar(
                    autenticacion.getName(),
                    "SUBIDA",
                    "Documento guardado"
            );

            mensajes.addFlashAttribute(
                    "exito",
                    "El documento se guardó cifrado."
            );

        } catch (IllegalArgumentException e) {

            mensajes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (IOException | GeneralSecurityException e) {

            auditoria.registrar(
                    autenticacion.getName(),
                    "ERROR_SUBIDA",
                    "No se pudo guardar"
            );

            mensajes.addFlashAttribute(
                    "error",
                    "No se pudo guardar el documento."
            );
        }

        return "redirect:/documentos";
    }

    @GetMapping("/documentos/{id}/descargar")
    public ResponseEntity<byte[]> descargar(
            @PathVariable Long id,
            Authentication autenticacion
    ) {
        String usuario = autenticacion.getName();

        try {
            DocumentoService.Descarga descarga =
                    documentos.descargar(id, usuario);

            auditoria.registrar(
                    usuario,
                    "DESCARGA",
                    "Documento " + id
            );

            ContentDisposition disposicion =
                    ContentDisposition.attachment()
                            .filename(
                                    descarga.nombre(),
                                    StandardCharsets.UTF_8
                            )
                            .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            disposicion.toString()
                    )
                    .body(descarga.contenido());

        } catch (ResponseStatusException e) {

            auditoria.registrar(
                    usuario,
                    "DESCARGA_DENEGADA",
                    "Documento inexistente o sin permiso"
            );

            throw e;

        } catch (IOException | GeneralSecurityException e) {

            auditoria.registrar(
                    usuario,
                    "ERROR_DESCARGA",
                    "No se pudo verificar el archivo"
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible verificar o descargar el archivo."
            );
        }
    }
}