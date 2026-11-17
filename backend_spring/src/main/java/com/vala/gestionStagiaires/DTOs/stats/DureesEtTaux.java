package com.vala.gestionStagiaires.DTOs.stats;

import java.util.Map;

/**
 * Durées et taux des stages : globalement, par encadrant et par département.
 * Le département d'un stage est celui de ses stagiaires : un stage partagé entre
 * plusieurs départements compte dans chacun ; un stage sans stagiaire est classé
 * sous « Sans stagiaire ». Les regroupements sont triés par nombre de stages décroissant.
 */
public record DureesEtTaux(
        IndicateursStages global,
        Map<String, IndicateursStages> parEncadrant,
        Map<String, IndicateursStages> parDepartement) {
}
