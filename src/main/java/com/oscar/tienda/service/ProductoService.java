package com.oscar.tienda.service;

import com.oscar.tienda.dto.ProductoRequestDTO;
import com.oscar.tienda.dto.ProductoResponseDTO;
import com.oscar.tienda.exception.RecursoNoEncontradoException;
import com.oscar.tienda.exception.ReglaNegocioException;
import com.oscar.tienda.model.Producto;
import com.oscar.tienda.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarActivos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarInactivos(String nombre) {
        List<Producto> productos = (nombre == null || nombre.isBlank())
                ? productoRepository.findByActivoFalse()
                : productoRepository.findByNombreContainingIgnoreCaseAndActivoFalse(nombre);
        return productos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorNombre(String nombre){
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
                .stream().map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorCodigoBarras(String codigoBarras){
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .filter(Producto::getActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un producto con ese codigo de barras: " +
                                codigoBarras));
        return toResponse(producto);
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto){
        if(productoRepository.existsByCodigoBarras(dto.codigoBarras())){
            throw new ReglaNegocioException(
                    "Ya existe un producto con ese codigo de barras: " + dto.codigoBarras());
        }
        Producto producto = new Producto();
        aplicarDatos(producto, dto);
        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long idProducto, ProductoRequestDTO dto){
        Producto producto = obtenerPorId(idProducto);

        boolean cambioCodigo = !producto.getCodigoBarras().equals(dto.codigoBarras());
        if(cambioCodigo && productoRepository.existsByCodigoBarras(dto.codigoBarras())){
            throw new ReglaNegocioException(
                    "Ya existe otro producto con ese codigo de barras: " + dto.codigoBarras()
            );
        }
        aplicarDatos(producto, dto);
        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public void desactivar(Long idProducto){
        Producto producto = obtenerPorId(idProducto);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Transactional
    public void reactivar(Long idProducto){
        Producto producto = obtenerPorId(idProducto);
        producto.setActivo(true);
        productoRepository.save(producto);
    }

    //Funcionas para busar y guardar datos a producto e imprimir
    private Producto obtenerPorId(Long idProducto){
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con el id: " +idProducto
                ));
    }

    private void aplicarDatos(Producto producto, ProductoRequestDTO dto){
        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setCodigoBarras(dto.codigoBarras());
    }

    private ProductoResponseDTO toResponse(Producto p){
        return new ProductoResponseDTO(
                p.getIdProducto(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getCodigoBarras(),
                p.getActivo()
        );
    }
}
