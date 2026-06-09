package com.oscar.tienda.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table (name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(nullable = false)
    private String password;
}
