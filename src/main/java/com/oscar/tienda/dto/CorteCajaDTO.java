package com.oscar.tienda.dto;

import com.oscar.tienda.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record CorteCajaDTO(
        LocalDate desde,
        LocalDate hasta,
        Map<MetodoPago, BigDecimal> cajaPorMetodo,
        BigDecimal totalCaja,
        BigDecimal ventasContado,
        BigDecimal abonosRecibidos,
        BigDecimal ventasACuenta,       // informativo, NO es dinero
        long numeroVentas
) {}
