package com.oscar.tienda.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter @Setter
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
