package com.oscar.tienda.dto;

import com.oscar.tienda.enums.SituacionSaldo;

import java.math.BigDecimal;

public record SaldoClienteDTO(
        Long idCliente,
        String nombre,
        BigDecimal saldo,
        SituacionSaldo situacion
) {}
