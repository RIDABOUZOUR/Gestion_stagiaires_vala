package com.vala.gestionStagiaires.enums;

import java.util.Set;

/**
 * Cycle de vie d'une candidature :
 * <pre>
 *   SOUMISE ──► EN_ETUDE ──► ACCEPTEE
 *      │           │
 *      ├──► REFUSEE ◄┤
 *      └──► RETIREE ◄┘
 * </pre>
 * ACCEPTEE, REFUSEE et RETIREE sont des états finaux.
 */
public enum CandidatureStatus {
    SOUMISE,
    EN_ETUDE,
    ACCEPTEE,
    REFUSEE,
    RETIREE;

    /** Candidatures encore en cours de traitement. */
    public static final Set<CandidatureStatus> ACTIVES = Set.of(SOUMISE, EN_ETUDE);

    public Set<CandidatureStatus> transitionsPossibles() {
        return switch (this) {
            case SOUMISE -> Set.of(EN_ETUDE, REFUSEE, RETIREE);
            case EN_ETUDE -> Set.of(ACCEPTEE, REFUSEE, RETIREE);
            case ACCEPTEE, REFUSEE, RETIREE -> Set.of();
        };
    }

    public boolean canTransitionTo(CandidatureStatus cible) {
        return transitionsPossibles().contains(cible);
    }

    public boolean isActive() {
        return ACTIVES.contains(this);
    }
}
