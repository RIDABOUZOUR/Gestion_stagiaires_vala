package com.vala.gestionStagiaires.entities;

import com.vala.gestionStagiaires.enums.StatutRapport;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/** Rapport d'activité rédigé par le stagiaire pour une semaine donnée. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_rapport_stagiaire_semaine",
        columnNames = {"stagiaire_id", "semaine"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RapportHebdomadaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stagiaire_id")
    private Stagiaire stagiaire;

    /** Lundi de la semaine concernée. */
    @Column(nullable = false)
    private LocalDate semaine;

    @Column(nullable = false, length = 4000)
    private String travauxRealises;

    @Column(length = 2000)
    private String difficultes;

    @Column(length = 2000)
    private String objectifs;

    @Column(nullable = false)
    private Instant dateDepot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutRapport statut;

    @Column(length = 2000)
    private String commentaireEncadrant;
}
