package com.vala.gestionStagiaires.entities;

import com.vala.gestionStagiaires.enums.Role;
import jakarta.persistence.*;
import lombok.*;

/** Compte de connexion. Un compte ENCADRANT est lié à la fiche encadrant correspondante. */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Empreinte BCrypt, jamais le mot de passe en clair. */
    @Column(nullable = false, length = 100)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encadrant_id", unique = true)
    private Encadrant encadrant;

    @Column(nullable = false)
    private boolean actif;
}
