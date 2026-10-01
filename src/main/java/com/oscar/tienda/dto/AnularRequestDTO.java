package com.oscar.tienda.dto;

import jakarta.validation.constraints.NotBlank;

public record AnularRequestDTO(
        @NotBlank (message = "El motivo es obligatorio") String motivo
) {
}
