package com.oscar.tienda.model;

import com.oscar.tienda.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.oscar.tienda.enums.TipoPago;
import com.oscar.tienda.exception.ReglaNegocioException;

@Entity
@NoArgsConstructor
@Getter @Setter
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;

    @Column(nullable = false)
    private LocalDateTime fechaVenta = LocalDateTime.now();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPago tipoPago = TipoPago.CONTADO;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente; //null -> Venta de mostrador

    @OneToMany (mappedBy = "venta",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleVenta detalle){
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public void calcularTotal(){
        this.total = detalles.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}