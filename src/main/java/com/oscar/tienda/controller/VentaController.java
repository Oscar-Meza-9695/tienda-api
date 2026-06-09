package com.oscar.tienda.controller;

import com.oscar.tienda.model.Venta;
import com.oscar.tienda.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    @Autowired
    VentaService ventaService;

    @GetMapping
    public ResponseEntity<?> listarVentas(){
        try{
            List<Venta> listaVentas = ventaService.listarVentas();
            return ResponseEntity.ok(listaVentas);
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<?> guardarVenta(@RequestBody Venta venta){
        try{
            Venta guardar = ventaService.guardarVenta(venta);
            return ResponseEntity.ok(guardar);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
