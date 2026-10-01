package com.oscar.tienda.dto;

import java.math.BigDecimal;

public record ProductoVendidoDTO(
        String producto,
        BigDecimal cantidadVendida,
        BigDecimal totalIngresos
) {}
