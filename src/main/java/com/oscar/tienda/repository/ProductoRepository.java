package com.oscar.tienda.repository;

import com.oscar.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    Producto findByCodigoBarras(String codigoBarras);
}
