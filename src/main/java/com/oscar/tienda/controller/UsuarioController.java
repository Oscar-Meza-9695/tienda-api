package com.oscar.tienda.controller;

import com.oscar.tienda.model.Usuario;
import com.oscar.tienda.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/api/auth")
public class UsuarioController {
    @Autowired
    UsuarioService usuarioService;

    //Metodo para registrar un usuario Solo una vez lo mando a llamar
    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario){
        try{
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario.getUsuario(), usuario.getPassword());
            return ResponseEntity.ok("Usuario: "+nuevoUsuario.getUsuario()+" guardado con exito");
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    //Metodo para el Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario){
        try{
            boolean exito = usuarioService.login(usuario.getUsuario(), usuario.getPassword());
            return ResponseEntity.ok("Login exitoso");
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
