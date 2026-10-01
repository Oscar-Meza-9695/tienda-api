package com.oscar.tienda.controller;

import com.oscar.tienda.dto.ProductoRequestDTO;
import com.oscar.tienda.dto.ProductoResponseDTO;
import com.oscar.tienda.model.Producto;
import com.oscar.tienda.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;

    //Metodo para listar todos los productos
    @GetMapping
    public List<ProductoResponseDTO> listar(){
        return productoService.listarActivos();
    }

    @GetMapping("/inactivos")
    public List<ProductoResponseDTO> inactivos(@RequestParam(required = false) String nombre){
        return productoService.listarInactivos(nombre);
    }

    @PatchMapping("/{idProducto}/reactivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reactivar(@PathVariable Long idProducto){
        productoService.reactivar(idProducto);
    }

    @GetMapping("/buscar")
    public List<ProductoResponseDTO> buscarPorNombre(@RequestParam String nombre){
        return productoService.buscarPorNombre(nombre);
    }

    @GetMapping("/codigo/{codigoBarras}")
    public ProductoResponseDTO buscarPorCodigo(@PathVariable String codigoBarras){
        return productoService.buscarPorCodigoBarras(codigoBarras);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponseDTO crear(@Valid @RequestBody ProductoRequestDTO dto){
        return productoService.crear(dto);
    }

    @PutMapping("/{idProducto}")
    public ProductoResponseDTO actualizar(@PathVariable Long idProducto,
                                          @Valid @RequestBody ProductoRequestDTO dto){
        return productoService.actualizar(idProducto,dto);
    }

    @DeleteMapping("/{idProducto}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long idProducto){
        productoService.desactivar(idProducto);
    }

}
