package com.oscar.tienda.service;

import com.oscar.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

// Crea el primer usuario al arrancar, solo si todavía no existe ninguno.
//   ADMIN_USER (opcional, por defecto "admin")
//   ADMIN_PASS (opcional): si no la defines, se genera una al azar y se imprime UNA vez en el log.
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Value("${ADMIN_USER:admin}") private String usuario;
    @Value("${ADMIN_PASS:}") private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) return;

        boolean generada = password.isBlank();
        if (!generada && password.length() < 8) {
            throw new IllegalStateException("ADMIN_PASS debe tener al menos 8 caracteres");
        }
        String clave = generada ? generar() : password;
        usuarioService.registrarUsuario(usuario, clave);

        if (generada) {
            log.warn("""

                    ==========================================================
                     Usuario inicial creado
                       usuario:    {}
                       contraseña: {}
                     Guárdala y cámbiala desde la pantalla. No se vuelve a mostrar.
                    ==========================================================""", usuario, clave);
        } else {
            log.info("Usuario inicial '{}' creado con ADMIN_PASS", usuario);
        }
    }

    private String generar() {
        String alfabeto = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin 0/O/1/l/I
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) sb.append(alfabeto.charAt(r.nextInt(alfabeto.length())));
        return sb.toString();
    }
}