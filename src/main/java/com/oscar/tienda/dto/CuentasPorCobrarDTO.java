package com.oscar.tienda.dto;

import java.math.BigDecimal;
import java.util.List;

public record CuentasPorCobrarDTO(
        BigDecimal totalPorCobrar,
        BigDecimal totalSaldoAFavor,
        List<SaldoClienteDTO> deudores
) {
}
