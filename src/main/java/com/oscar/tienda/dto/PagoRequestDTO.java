package com.oscar.tienda.dto;

import com.oscar.tienda.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagoRequestDTO(
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "Máximo 2 decimales")
        BigDecimal monto,
        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago,
        Boolean permitirSaldoAFavor
) {}
