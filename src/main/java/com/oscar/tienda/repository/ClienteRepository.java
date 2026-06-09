package com.oscar.tienda.repository;

import com.oscar.tienda.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Búsqueda exacta (ignorando mayúsculas)
    List<Cliente> findByNombreIgnoreCase(String nombre);

    // Búsqueda por similitud
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    // Para verificar si ya existe un cliente con ese nombre exacto
    Optional<Cliente> findFirstByNombreIgnoreCase(String nombre);
}