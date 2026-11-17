package com.vala.gestionStagiaires.enums;

import java.util.Set;

/**
 * Cycle de vie d'un stage :
 * <pre>
 *   PLANIFIE ──► EN_COURS ──► TERMINE
 *      │            │
 *      └──► ANNULE ◄┘
 * </pre>
 * {@code TERMINE} et {@code ANNULE} sont des états finaux.
 */
public enum StageStatus {
    TERMINE,
    EN_COURS,
    ANNULE,
    PLANIFIE;

    /** Statuts vers lesquels on peut passer depuis celui-ci. */
    public Set<StageStatus> transitionsPossibles() {
        return switch (this) {
            case PLANIFIE -> Set.of(EN_COURS, ANNULE);
            case EN_COURS -> Set.of(TERMINE, ANNULE);
            case TERMINE, ANNULE -> Set.of();
        };
    }

    public boolean canTransitionTo(StageStatus cible) {
        return transitionsPossibles().contains(cible);
    }

    /** Un stage terminé ou annulé ne peut plus recevoir de stagiaire ni changer de statut. */
    public boolean isFinal() {
        return transitionsPossibles().isEmpty();
    }
}
