package com.oscar.tienda.dto;

import java.time.LocalDateTime;

public record ClienteResponseDTO(
        Long idCliente,
        String nombre,
        Boolean activo,
        LocalDateTime fechaRegistro
) {}
