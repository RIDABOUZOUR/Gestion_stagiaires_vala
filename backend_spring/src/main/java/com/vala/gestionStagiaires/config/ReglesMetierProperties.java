package com.vala.gestionStagiaires.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Paramètres des règles métier (préfixe {@code app.regles}).
 *
 * @param ageMinimum                   âge minimum du stagiaire à la date de début du stage (0 = pas de contrôle)
 * @param maxStagesEnCoursParEncadrant nombre maximal de stages EN_COURS par encadrant (0 = illimité)
 */
@ConfigurationProperties(prefix = "app.regles")
public record ReglesMetierProperties(
        @DefaultValue("16") int ageMinimum,
        @DefaultValue("0") int maxStagesEnCoursParEncadrant) {
}
