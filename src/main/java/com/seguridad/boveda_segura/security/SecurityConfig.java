package com.seguridad.boveda_segura.security;

import com.seguridad.boveda_segura.service.AuditoriaService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UsuarioDetailsService usuarios,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider proveedor =
                new DaoAuthenticationProvider(usuarios);

        proveedor.setPasswordEncoder(passwordEncoder);

        return proveedor;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider proveedor,
            AuditoriaService auditoria
    ) throws Exception {

        http.authenticationProvider(proveedor)

                .authorizeHttpRequests(reglas -> reglas

                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .formLogin(login -> login

                        .loginPage("/login")

                        .successHandler((
                                request,
                                response,
                                autenticacion
                        ) -> {
                            auditoria.registrar(
                                    autenticacion.getName(),
                                    "LOGIN_OK",
                                    "Inicio de sesión correcto"
                            );

                            response.sendRedirect(
                                    request.getContextPath() + "/documentos"
                            );
                        })

                        .failureHandler((
                                request,
                                response,
                                error
                        ) -> {
                            auditoria.registrar(
                                    request.getParameter("username"),
                                    "LOGIN_FALLIDO",
                                    "Credenciales inválidas"
                            );

                            response.sendRedirect(
                                    request.getContextPath() + "/login?error"
                            );
                        })

                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?salio")
                        .permitAll()
                );

        return http.build();
    }
}