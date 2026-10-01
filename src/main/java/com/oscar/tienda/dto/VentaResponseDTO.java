package com.oscar.tienda.dto;

import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponseDTO(
        Long idVenta,
        LocalDateTime fechaVenta,
        TipoPago tipoPago,
        MetodoPago metodoPago,
        Long idCliente,
        String nombreCliente,
        BigDecimal total,
        List<DetalleVentaResponseDTO> detalles
) {}
