package com.seguridad.boveda_segura.security;

import com.seguridad.boveda_segura.entity.Usuario;
import com.seguridad.boveda_segura.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String nombre)
            throws UsernameNotFoundException {

        Usuario usuario = usuarios.findByNombreUsuario(nombre)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        return User.withUsername(usuario.getNombreUsuario())
                .password(usuario.getContrasena())
                .roles(usuario.getRol().name())
                .build();
    }
}