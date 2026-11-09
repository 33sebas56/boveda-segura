package com.seguridad.boveda_segura.service;

import com.seguridad.boveda_segura.entity.Documento;
import com.seguridad.boveda_segura.entity.Usuario;
import com.seguridad.boveda_segura.repository.DocumentoRepository;
import com.seguridad.boveda_segura.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {

    private static final long MAXIMO_BYTES = 20L * 1024 * 1024;

    private final DocumentoRepository documentos;
    private final UsuarioRepository usuarios;
    private final CriptografiaService criptografia;
    private final Path directorio;

    public DocumentoService(
            DocumentoRepository documentos,
            UsuarioRepository usuarios,
            CriptografiaService criptografia,
            @Value("${boveda.directorio}") String directorio
    ) {
        this.documentos = documentos;
        this.usuarios = usuarios;
        this.criptografia = criptografia;
        this.directorio = Paths.get(directorio)
                .toAbsolutePath()
                .normalize();
    }

    public List<Documento> listar(String usuario) {
        return documentos
                .findByPropietario_NombreUsuarioOrderByFechaSubidaDesc(usuario);
    }

    public void guardar(MultipartFile archivo, String usuario)
            throws IOException, GeneralSecurityException {

        if (
                archivo == null
                        || archivo.isEmpty()
                        || archivo.getSize() > MAXIMO_BYTES
        ) {
            throw new IllegalArgumentException(
                    "Selecciona un archivo de hasta 5 MB."
            );
        }

        String recibido = archivo.getOriginalFilename();

        if (recibido == null) {
            throw new IllegalArgumentException(
                    "El archivo no tiene nombre."
            );
        }

        String nombre = StringUtils.getFilename(
                recibido.replace('\\', '/')
        );

        if (nombre == null) {
            throw new IllegalArgumentException(
                    "El archivo no tiene un nombre válido."
            );
        }

        nombre = nombre.replaceAll("[\\p{Cntrl}]", "_").trim();

        if (
                nombre.isBlank()
                        || nombre.length() > 180
                        || nombre.equals(".")
                        || nombre.equals("..")
        ) {
            throw new IllegalArgumentException(
                    "El nombre del archivo no es válido o es demasiado largo."
            );
        }

        Usuario propietario = usuarios.findByNombreUsuario(usuario)
                .orElseThrow(() ->
                        new IllegalStateException("El usuario no existe.")
                );

        byte[] contenido = archivo.getBytes();

        if (contenido.length == 0 || contenido.length > MAXIMO_BYTES) {
            throw new IllegalArgumentException(
                    "Selecciona un archivo de hasta 5 MB."
            );
        }

        String nombreAlmacenado = UUID.randomUUID() + ".enc";

        byte[] cifrado = criptografia.cifrar(
                contenido,
                nombreAlmacenado
        );

        String huella = criptografia.sha256(contenido);

        Files.createDirectories(directorio);

        Path ruta = directorio.resolve(nombreAlmacenado);

        Files.write(
                ruta,
                cifrado,
                StandardOpenOption.CREATE_NEW
        );

        try {
            Documento documento = new Documento(
                    nombre,
                    nombreAlmacenado,
                    huella,
                    contenido.length,
                    propietario
            );

            documentos.save(documento);

        } catch (RuntimeException e) {

            try {
                Files.deleteIfExists(ruta);
            } catch (IOException limpieza) {
                e.addSuppressed(limpieza);
            }

            throw e;
        }
    }

    public Descarga descargar(Long id, String usuario)
            throws IOException, GeneralSecurityException {

        Documento documento = documentos
                .findByIdAndPropietario_NombreUsuario(id, usuario)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND)
                );

        String nombre = documento.getNombreAlmacenado();

        if (!nombre.matches("[0-9a-f-]{36}\\.enc")) {
            throw new IOException(
                    "Identificador de archivo inválido."
            );
        }

        Path ruta = directorio.resolve(nombre).normalize();

        if (
                !ruta.startsWith(directorio)
                        || Files.size(ruta) > MAXIMO_BYTES + 28
        ) {
            throw new IOException(
                    "Archivo almacenado inválido."
            );
        }

        byte[] contenido = criptografia.descifrar(
                Files.readAllBytes(ruta),
                nombre
        );

        String huellaCalculada = criptografia.sha256(contenido);

        if (!huellaCalculada.equals(documento.getHuellaSha256())) {
            throw new GeneralSecurityException(
                    "La huella del archivo no coincide."
            );
        }

        return new Descarga(
                documento.getNombreOriginal(),
                contenido
        );
    }

    public record Descarga(String nombre, byte[] contenido) {
    }
}