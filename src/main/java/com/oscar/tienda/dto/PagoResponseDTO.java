package com.oscar.tienda.dto;

import com.oscar.tienda.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponseDTO(
        Long idPago,
        Long idCliente,
        BigDecimal monto,
        LocalDateTime fechaPago,
        MetodoPago metodoPago,
        Boolean anulada
) {}
