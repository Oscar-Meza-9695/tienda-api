package com.oscar.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(
        @NotBlank String passwordActual,
        @NotBlank @Size(min = 8, message = "minimo 8 caracteres") String newPassword
) {}
