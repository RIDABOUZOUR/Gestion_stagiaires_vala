package com.vala.gestionStagiaires.enums;

/**
 * Cycle de vie d'un rapport hebdomadaire :
 * SOUMIS → VALIDE (final) ou SOUMIS → A_REVOIR → (resoumission) SOUMIS.
 */
public enum StatutRapport {
    SOUMIS,
    A_REVOIR,
    VALIDE
}
