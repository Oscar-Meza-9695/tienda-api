package com.oscar.tienda.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "deuda")
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_deuda")
    private Long idDeuda;

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    // FetchType.EAGER para que los detalles siempre se carguen
    // junto con la deuda — sin esto el endpoint /detalles devuelve vacío
    @OneToMany(mappedBy = "deuda", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("deuda")
    private List<DetalleDeuda> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "deuda", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties("deuda")
    private List<Pago> pagos = new ArrayList<>();

    // Fuerza a Jackson a incluir estos campos calculados en el JSON
    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("pagado")
    public Double getPagado() {
        if (pagos == null || pagos.isEmpty()) return 0.0;
        return pagos.stream()
                .mapToDouble(Pago::getMonto)
                .sum();
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("pendiente")
    public Double getPendiente() {
        if (total == null) return 0.0;
        return total - getPagado();
    }
}