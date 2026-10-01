package com.oscar.tienda.repository;

import com.oscar.tienda.model.Cliente;
import com.oscar.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByActivoTrue();

    List<Cliente> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    List<Cliente> findByActivoFalse();

    List<Cliente> findByNombreContainingIgnoreCaseAndActivoFalse(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}