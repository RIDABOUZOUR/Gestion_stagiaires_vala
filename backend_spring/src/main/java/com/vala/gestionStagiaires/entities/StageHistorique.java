package com.vala.gestionStagiaires.entities;

import com.vala.gestionStagiaires.enums.OrigineChangement;
import com.vala.gestionStagiaires.enums.StageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Trace d'un changement de statut d'un stage. */
@Entity
@Table(indexes = @Index(name = "idx_historique_stage", columnList = "stage_id"))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StageHistorique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id")
    private Stage stage;

    /** {@code null} à la création du stage. */
    @Enumerated(EnumType.STRING)
    private StageStatus ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StageStatus nouveauStatut;

    @Column(nullable = false)
    private Instant dateChangement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigineChangement origine;

    /** Utilisateur à l'origine du changement (renseigné une fois l'authentification en place). */
    private String auteur;
}
