package com.vala.gestionStagiaires.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Évaluation de fin de stage rédigée par l'encadrant (une par stagiaire). */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stagiaire_id", unique = true)
    private Stagiaire stagiaire;

    /** Note sur 20, une décimale. */
    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal noteGlobale;

    /** Compétences notées de 1 (insuffisant) à 5 (excellent). */
    @Column(nullable = false)
    private Integer competenceTechnique;

    @Column(nullable = false)
    private Integer communication;

    @Column(nullable = false)
    private Integer autonomie;

    @Column(nullable = false)
    private Integer assiduite;

    @Column(length = 2000)
    private String appreciation;

    @Column(nullable = false)
    private boolean recommande;

    @Column(nullable = false)
    private LocalDate dateEvaluation;
}
