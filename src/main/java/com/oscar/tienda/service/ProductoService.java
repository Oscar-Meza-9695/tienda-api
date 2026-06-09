package com.oscar.tienda.service;

import com.oscar.tienda.model.Producto;
import com.oscar.tienda.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {
    @Autowired
    ProductoRepository productoRepository;

    public List<Producto>listarProductos(){
        return productoRepository.findAll();
    }

    public List<Producto> buscarProductoNombre(String nombre) {
        List<Producto> productos = productoRepository
                .findByNombreContainingIgnoreCase(nombre);
        if (productos.isEmpty()) {
            throw new RuntimeException("No se encontró ningún producto con ese nombre");
        }
        return productos;
    }

    public Producto buscarPorCodigoBarras(String codigoBarras){
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras);
        if(producto == null){
            throw new RuntimeException("No se encontro ningun producto relacionado con el codigo de barras");
        }
        return producto;
    }

    public Producto guardarProducto (Producto producto){
        Producto existente = productoRepository.findByCodigoBarras(producto.getCodigoBarras());
        if(existente != null){
            throw new RuntimeException("Ya existe un producto con ese codigo de barras: "+producto.getCodigoBarras());
        }
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id){
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado");
        }
        // Verificar que no tenga detalles de deuda asociados
        // Si tiene deudas no se puede eliminar
        Producto producto = productoRepository.findById(id).get();
        if (producto != null) {
            try {
                productoRepository.deleteById(id);
            } catch (Exception e) {
                throw new RuntimeException(
                        "No se puede eliminar el producto porque tiene deudas asociadas"
                );
            }
        }
    }

    public Producto actualizarProducto(Long id, Producto productoNuevo){
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        existente.setNombre(productoNuevo.getNombre());
        existente.setDescripcion(productoNuevo.getDescripcion());
        existente.setPrecio(productoNuevo.getPrecio());
        return productoRepository.save(existente);
    }
}
