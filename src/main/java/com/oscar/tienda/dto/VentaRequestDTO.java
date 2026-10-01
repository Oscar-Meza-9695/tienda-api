package com.oscar.tienda.dto;

import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.enums.TipoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record VentaRequestDTO(
        Long idCliente,
        @NotNull TipoPago tipoPago,
        MetodoPago metodoPago,              // obligatorio en CONTADO, null en CREDITO
        @Positive @Digits(integer = 8, fraction = 2) BigDecimal montoPagado,
        MetodoPago metodoPagado,            // obligatorio si hay montoPagado
        Boolean permitirSaldoAFavor,        // null = false
        @NotEmpty @Valid List<ItemVentaDTO> items
        ) {}
