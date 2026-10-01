package com.oscar.tienda.dto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long idProducto,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String codigoBarras,
        Boolean activo){}