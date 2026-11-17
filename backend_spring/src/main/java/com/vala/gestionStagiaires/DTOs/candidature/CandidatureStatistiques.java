package com.vala.gestionStagiaires.DTOs.candidature;

import com.vala.gestionStagiaires.enums.CandidatureStatus;

import java.util.Map;

/**
 * Bilan des candidatures.
 *
 * @param parStatut               nombre par statut (tous présents, éventuellement à 0)
 * @param parMois                 candidatures reçues par mois ("2026-03") sur les 12 derniers mois, du plus ancien au plus récent
 * @param tauxAcceptation         acceptées / (acceptées + refusées), en % ; {@code null} si aucune décision
 * @param delaiMoyenDecisionJours délai moyen entre soumission et acceptation ou refus, en jours (1 décimale)
 * @param parOrganisme            candidatures par organisme, décroissant
 */
public record CandidatureStatistiques(
        long total,
        Map<CandidatureStatus, Long> parStatut,
        Map<String, Long> parMois,
        Double tauxAcceptation,
        Double delaiMoyenDecisionJours,
        Map<String, Long> parOrganisme) {
}
