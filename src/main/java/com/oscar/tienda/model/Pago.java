package com.oscar.tienda.model;

import com.oscar.tienda.enums.MetodoPago;
import com.oscar.tienda.exception.ReglaNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter @Setter
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private Boolean anulada = false;

    private LocalDateTime fechaAnulacion;

    private String motivoAnulacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    public void anular(String motivo) {
        if (Boolean.TRUE.equals(anulada)) {
            throw new ReglaNegocioException("El pago ya está anulado");
        }
        this.anulada = true;
        this.fechaAnulacion = LocalDateTime.now();
        this.motivoAnulacion = motivo;
    }
}