package com.oscar.tienda.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalleVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    public DetalleVenta(Producto producto, BigDecimal cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
        this.subtotal = precioUnitario.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
    }
}
