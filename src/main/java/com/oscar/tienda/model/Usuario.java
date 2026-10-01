package com.oscar.tienda.model;

import com.oscar.tienda.enums.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter @Setter
public class Usuario {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.ADMIN;
}
