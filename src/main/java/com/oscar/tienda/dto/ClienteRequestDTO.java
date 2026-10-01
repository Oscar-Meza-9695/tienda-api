package com.oscar.tienda.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDTO(
        @NotBlank(message = "El nombre es obligatorio") String nombre
        ) {}
