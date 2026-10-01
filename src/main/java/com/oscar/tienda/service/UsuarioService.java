package com.oscar.tienda.service;

import com.oscar.tienda.exception.RecursoNoEncontradoException;
import com.oscar.tienda.exception.ReglaNegocioException;
import com.oscar.tienda.model.Usuario;
import com.oscar.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// La verificación de credenciales ya no vive aquí: la hace Spring Security
// (AuthenticationManager + DbUserDetailsService), con un mensaje único de error.
@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public Usuario registrarUsuario(String nombre, String password) {
        if (usuarioRepository.findByUsername(nombre).isPresent()) {
            throw new ReglaNegocioException("El usuario ya existe");
        }
        Usuario nuevo = new Usuario();
        nuevo.setUsername(nombre);
        nuevo.setPassword(encoder.encode(password));
        return usuarioRepository.save(nuevo);
    }

    @Transactional
    public void cambiarPassword(String username, String actual, String nueva) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!encoder.matches(actual, u.getPassword())) {
            throw new ReglaNegocioException("La contraseña actual es incorrecta");
        }
        u.setPassword(encoder.encode(nueva));
        usuarioRepository.save(u);
    }
}