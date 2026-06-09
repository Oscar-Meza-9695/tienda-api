package com.oscar.tienda.repository;

import com.oscar.tienda.model.Deuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeudaRepository extends JpaRepository<Deuda, Long> {

    List<Deuda> findByClienteNombreIgnoreCase(String nombre);

    @Query("""
        SELECT d.cliente.nombre, 
               SUM(d.total), 
               SUM(COALESCE(p.monto, 0))
        FROM Deuda d 
        LEFT JOIN d.pagos p
        GROUP BY d.cliente.nombre
        ORDER BY SUM(d.total) DESC
        """)
    List<Object[]> resumenDeudaPorCliente();

    @Query("SELECT COALESCE(SUM(d.total), 0) FROM Deuda d")
    Double totalDeudaGlobal();
}