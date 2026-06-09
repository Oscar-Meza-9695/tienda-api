package com.oscar.tienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table (name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_cliente")
    private Long idCliente;

    @Column(nullable = false)
    private String nombre;
}
