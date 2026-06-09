package com.oscar.tienda.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import  jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table (name = "detalle_deuda")
public class DetalleDeuda {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id_detalle")
    private Long idDetalle;

    @Column (nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double precioUnitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_deuda")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "pagos", "detalles"})
    private Deuda deuda;

    public Double getSubtotal() {
        if (cantidad == null || precioUnitario == null) return 0.0;
        return cantidad * precioUnitario;
    }
}
