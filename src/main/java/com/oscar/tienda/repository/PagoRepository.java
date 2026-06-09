package com.oscar.tienda.repository;

import com.oscar.tienda.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Total cobrado en pagos de deudas en un rango de fechas
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.fecha BETWEEN :inicio AND :fin")
    Double sumMontoPagadoEnPeriodo(@Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin);

    // Pagos agrupados por método
    @Query("""
        SELECT p.metodoPago, COUNT(p), SUM(p.monto)
        FROM Pago p
        WHERE p.fecha BETWEEN :inicio AND :fin
        GROUP BY p.metodoPago
        ORDER BY SUM(p.monto) DESC
        """)
    List<Object[]> resumenPorMetodoPago(@Param("inicio") LocalDateTime inicio,
                                        @Param("fin") LocalDateTime fin);
}