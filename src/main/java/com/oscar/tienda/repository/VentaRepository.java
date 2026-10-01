package com.oscar.tienda.repository;

import com.oscar.tienda.dto.ProductoVendidoDTO;
import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.enums.TipoPago;
import com.oscar.tienda.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByCliente_IdClienteAndTipoPagoOrderByFechaVentaDesc(Long idCliente,
                                                                        TipoPago tipoPago);

    long countByFechaVentaGreaterThanEqualAndFechaVentaLessThan(LocalDateTime inicio,
                                                                LocalDateTime fin);

    @Query("""
           SELECT COALESCE(SUM(v.total), 0) FROM Venta v
           WHERE v.cliente.idCliente = :idCliente
             AND v.tipoPago = com.oscar.tienda.enums.TipoPago.CREDITO
           """)
    BigDecimal totalCreditoPorCliente(@Param("idCliente") Long idCliente);

    @Query("""
           SELECT COALESCE(SUM(v.total), 0) FROM Venta v
           WHERE v.tipoPago = :tipo
             AND v.fechaVenta >= :inicio AND v.fechaVenta < :fin
           """)
    BigDecimal totalPorTipoPago(@Param("tipo") TipoPago tipo,
                                @Param("inicio") LocalDateTime inicio,
                                @Param("fin") LocalDateTime fin);

    @Query("""
           SELECT COALESCE(SUM(v.total), 0) FROM Venta v
           WHERE v.tipoPago = com.oscar.tienda.enums.TipoPago.CONTADO
             AND v.metodoPago = :metodo
             AND v.fechaVenta >= :inicio AND v.fechaVenta < :fin
           """)
    BigDecimal totalContadoPorMetodo(@Param("metodo") MetodoPago metodo,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fin") LocalDateTime fin);

    @Query("""
           SELECT new com.oscar.tienda.dto.ProductoVendidoDTO(
                  dv.producto.nombre, SUM(dv.cantidad), SUM(dv.subtotal))
           FROM DetalleVenta dv
           WHERE dv.venta.fechaVenta >= :inicio AND dv.venta.fechaVenta < :fin
           GROUP BY dv.producto.idProducto, dv.producto.nombre
           ORDER BY SUM(dv.cantidad) DESC
           """)
    List<ProductoVendidoDTO> topProductosVendidos(@Param("inicio") LocalDateTime inicio,
                                                  @Param("fin") LocalDateTime fin);
}