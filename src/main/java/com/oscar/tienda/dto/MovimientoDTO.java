package com.oscar.tienda.dto;

import com.oscar.tienda.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MovimientoDTO (
        TipoMovimiento tipo,
        LocalDateTime fecha,
        BigDecimal monto,
        String detalle,
        Boolean anulada,
        List<DetalleVentaResponseDTO> productos
){}
