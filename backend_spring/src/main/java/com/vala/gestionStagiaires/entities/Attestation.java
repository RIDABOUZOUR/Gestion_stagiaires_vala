package com.vala.gestionStagiaires.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Attestation de stage délivrée à un stagiaire. Le numéro est attribué à la
 * première génération et conservé ensuite : régénérer le PDF ne crée pas de
 * nouvelle attestation.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_attestation_annee_sequence", columnNames = {"annee", "sequence"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Attestation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stagiaire_id", unique = true)
    private Stagiaire stagiaire;

    /** Ex. {@code ATT-2026-0001}. */
    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(nullable = false)
    private int annee;

    /** Rang dans l'année (1, 2, 3…). */
    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private LocalDate dateEmission;
}
