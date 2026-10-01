package com.oscar.tienda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductoRequestDTO (
        @NotBlank String nombre,
        String descripcion,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2)
        BigDecimal precio,
        @NotBlank String codigoBarras) {}