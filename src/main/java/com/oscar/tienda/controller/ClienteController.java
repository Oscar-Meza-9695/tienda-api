package com.oscar.tienda.controller;

import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Autowired
    ClienteService clienteService;

    // Listar todos los clientes
    @GetMapping
    public ResponseEntity<?> listarClientes(){
        try{
            List<Cliente> clientes = clienteService.listaClientes();
            return ResponseEntity.ok(clientes);
        } catch (RuntimeException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarClienteNombre(@RequestParam String nombre){
        try{
            List<Cliente> nombresClientes = clienteService.buscarClientePorSimilitud(nombre);
            return ResponseEntity.ok(nombresClientes);
        } catch (RuntimeException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> guardarCliente(@RequestBody Cliente cliente){
        try{
            Cliente clienteGuardado = clienteService.guardarCliente(cliente);
            return ResponseEntity.ok(clienteGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Eliminar cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCliente(@PathVariable Long id){
        try{
            clienteService.eliminarCliente(id);
            return ResponseEntity.ok("Cliente eliminado con éxito");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}