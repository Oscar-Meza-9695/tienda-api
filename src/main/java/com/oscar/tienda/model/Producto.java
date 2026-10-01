package com.oscar.tienda.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter @Setter
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, unique = true)
    private String codigoBarras;

    @Column(nullable = false)
    private Boolean activo = true; // nuevo: para no borrar productos con ventas
}
