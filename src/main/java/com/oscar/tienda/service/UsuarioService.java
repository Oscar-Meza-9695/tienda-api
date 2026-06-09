package com.oscar.tienda.service;

import com.oscar.tienda.model.Usuario;
import com.oscar.tienda.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {
    @Autowired
    UsuarioRepository usuarioRepository;

    //Metodo para encriptar contraseña
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    //Metodo para registrar usuario
    public Usuario registrarUsuario (String nombre, String contraseña){
        if(usuarioRepository.findByUsuario(nombre).isPresent()){
            throw new RuntimeException("El usuario ya existe");
        }
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuario(nombre);
        nuevoUsuario.setPassword(encoder.encode(contraseña));
        return usuarioRepository.save(nuevoUsuario);
    }

    //Metodo del login
    public boolean login(String nombre, String contraseña){
        Optional<Usuario> usuario = usuarioRepository.findByUsuario(nombre);
        if(usuario.isEmpty()){
            throw new RuntimeException("Usuario no encontrado");
        }
        // matches() compara el password plano con el encriptado
        // nunca desencripta — solo verifica si coinciden
        boolean contraseñaCorrecta = encoder.matches(contraseña,usuario.get().getPassword());
        if(!contraseñaCorrecta){
            throw new RuntimeException("Contraseña incorrecta");
        }
        return true;
    }

}
