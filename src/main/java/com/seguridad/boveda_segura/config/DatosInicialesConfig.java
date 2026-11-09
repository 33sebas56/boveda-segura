package com.seguridad.boveda_segura.config;

import com.seguridad.boveda_segura.entity.Rol;
import com.seguridad.boveda_segura.entity.Usuario;
import com.seguridad.boveda_segura.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatosInicialesConfig {

    @Bean
    public CommandLineRunner crearUsuarios(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,

            @Value("${BOVEDA_ADMIN_PASSWORD:}")
            String admin,

            @Value("${BOVEDA_ANA_PASSWORD:}")
            String ana,

            @Value("${BOVEDA_JUAN_PASSWORD:}")
            String juan
    ) {
        return args -> {
            crearSiNoExiste(
                    usuarios, encoder, "admin", admin, Rol.ADMIN
            );

            crearSiNoExiste(
                    usuarios, encoder, "ana", ana, Rol.USUARIO
            );

            crearSiNoExiste(
                    usuarios, encoder, "juan", juan, Rol.USUARIO
            );
        };
    }

    private void crearSiNoExiste(
            UsuarioRepository usuarios,
            PasswordEncoder encoder,
            String nombre,
            String clave,
            Rol rol
    ) {
        if (usuarios.existsByNombreUsuario(nombre)) {
            return;
        }

        if (clave == null || clave.length() < 8) {
            throw new IllegalStateException(
                    "Falta configurar una contraseña de al menos "
                            + "8 caracteres para " + nombre
            );
        }

        Usuario usuario = new Usuario(
                nombre,
                encoder.encode(clave),
                rol
        );

        usuarios.save(usuario);
    }
}