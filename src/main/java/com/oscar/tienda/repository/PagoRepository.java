package com.oscar.tienda.repository;

import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByCliente_IdClienteOrderByFechaPagoDesc(Long idCliente);

    @Query("""
           SELECT COALESCE(SUM(p.monto), 0) FROM Pago p
           WHERE p.cliente.idCliente = :idCliente AND p.anulada = false
           """)
    BigDecimal totalPagadoPorCliente(@Param("idCliente") Long idCliente);

    @Query("""
           SELECT COALESCE(SUM(p.monto), 0) FROM Pago p
           WHERE p.anulada = false
             AND p.fechaPago >= :inicio AND p.fechaPago < :fin
           """)
    BigDecimal totalPagadoEntre(@Param("inicio") LocalDateTime inicio,
                                @Param("fin") LocalDateTime fin);

    @Query("""
           SELECT COALESCE(SUM(p.monto), 0) FROM Pago p
           WHERE p.metodoPago = :metodo AND p.anulada = false
             AND p.fechaPago >= :inicio AND p.fechaPago < :fin
           """)
    BigDecimal totalPagadoPorMetodo(@Param("metodo") MetodoPago metodo,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fin") LocalDateTime fin);
}