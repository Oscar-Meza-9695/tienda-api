package com.oscar.tienda.dto;

import java.math.BigDecimal;

public record DetalleVentaResponseDTO(
        Long idProducto,
        String nombreProducto,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
