package com.oscar.tienda.repository;

import com.oscar.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    List<Producto> findByActivoTrue();

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    List<Producto> findByActivoFalse();

    List<Producto> findByNombreContainingIgnoreCaseAndActivoFalse(String nombre);
}
