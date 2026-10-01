package com.oscar.tienda.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemVentaDTO(
        @NotNull Long idProducto,
        @NotNull @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 7, fraction = 3) BigDecimal cantidad
) {}
