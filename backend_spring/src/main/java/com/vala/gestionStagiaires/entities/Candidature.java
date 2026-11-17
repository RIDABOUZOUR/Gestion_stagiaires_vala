package com.vala.gestionStagiaires.entities;

import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.Sexe;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

/** Demande de stage ; une fois acceptée, elle est liée au stagiaire créé. */
@Entity
@Table(indexes = {
        @Index(name = "idx_candidature_email", columnList = "email"),
        @Index(name = "idx_candidature_statut", columnList = "statut")})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String email;

    private String telephone;

    private LocalDate dateNaissance;

    @Enumerated(EnumType.STRING)
    private Sexe sexe;

    private String organisme;

    private String niveau;

    private String filiere;

    /** Département souhaité par le candidat (facultatif). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_souhaite_id")
    private Departement departementSouhaite;

    private LocalDate dateDebutSouhaitee;

    private LocalDate dateFinSouhaitee;

    @Column(length = 4000)
    private String motivation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidatureStatus statut;

    @Column(length = 1000)
    private String motifRefus;

    @Column(nullable = false)
    private Instant dateSoumission;

    /** Date d'acceptation, de refus ou de retrait. */
    private Instant dateDecision;

    /** Un CV est-il joint ? (le fichier lui-même est dans CvCandidature) */
    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    private boolean cvFourni = false;

    /** Stagiaire créé à l'acceptation. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stagiaire_id", unique = true)
    private Stagiaire stagiaire;
}
