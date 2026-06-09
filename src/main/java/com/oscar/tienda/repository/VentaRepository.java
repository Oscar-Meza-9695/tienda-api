package com.oscar.tienda.repository;

import com.oscar.tienda.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Ventas entre dos fechas
    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Total de ingresos por ventas en un rango
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Double sumTotalByFechaVentaBetween(@Param("inicio") LocalDateTime inicio,
                                       @Param("fin") LocalDateTime fin);

    // Conteo de ventas en un rango
    long countByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Top productos más vendidos
    @Query("""
        SELECT dv.producto.nombre,
               SUM(dv.cantidad),
               SUM(dv.subtotal)
        FROM DetalleVenta dv
        WHERE dv.venta.fechaVenta BETWEEN :inicio AND :fin
        GROUP BY dv.producto.nombre
        ORDER BY SUM(dv.cantidad) DESC
        """)
    List<Object[]> topProductosVendidos(@Param("inicio") LocalDateTime inicio,
                                        @Param("fin") LocalDateTime fin);
}