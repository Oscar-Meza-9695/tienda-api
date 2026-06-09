package com.oscar.tienda.controller;

import com.oscar.tienda.model.Producto;
import com.oscar.tienda.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    @Autowired
    ProductoService productoService;

    //Metodo para listar todos los productos
    @GetMapping
    public ResponseEntity<?> listarProductos() {
        try {
            List<Producto> listaProductos = productoService.listarProductos();
            return ResponseEntity.ok(listaProductos); // ← solo la lista
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarNombreProducto(@RequestParam String nombre) {
        try {
            List<Producto> productos = productoService.buscarProductoNombre(nombre);
            return ResponseEntity.ok(productos); // ← solo la lista
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/buscar/codigo")
    public ResponseEntity<?> buscarCodigoBarras(@RequestParam String codigoBarras){
        try{
            Producto producto = productoService.buscarPorCodigoBarras(codigoBarras);
            return ResponseEntity.ok(producto);
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> guardarProducto(@RequestBody Producto producto) {
        try {
            Producto nuevo = productoService.guardarProducto(producto);
            return ResponseEntity.ok(nuevo); // ← solo el objeto
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok("Producto eliminado con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto){
        try{
            Producto actualizado = productoService.actualizarProducto(id,producto);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
