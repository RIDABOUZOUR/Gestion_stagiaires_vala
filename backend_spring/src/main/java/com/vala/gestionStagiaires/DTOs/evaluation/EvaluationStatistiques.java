package com.vala.gestionStagiaires.DTOs.evaluation;

import java.util.Map;

/**
 * Bilan des évaluations de fin de stage.
 *
 * @param noteMoyenne                       moyenne des notes globales sur 20 (2 décimales), {@code null} sans évaluation
 * @param tauxRecommandation                pourcentage de stagiaires recommandés (1 décimale)
 * @param stagiairesTerminesSansEvaluation  stagiaires au stage TERMINE encore non évalués
 * @param moyennesCompetences               moyenne de chaque compétence sur 5
 * @param repartitionNotes                  nombre d'évaluations par tranche de note, de la plus basse à la plus haute
 * @param moyenneParDepartement             note moyenne par département, décroissante
 * @param moyenneParEncadrant               note moyenne par encadrant, décroissante
 * @param moyenneParFiliere                 note moyenne par filière, décroissante
 */
public record EvaluationStatistiques(
        long nombreEvaluations,
        Double noteMoyenne,
        Double tauxRecommandation,
        long stagiairesTerminesSansEvaluation,
        Map<String, Double> moyennesCompetences,
        Map<String, Long> repartitionNotes,
        Map<String, Double> moyenneParDepartement,
        Map<String, Double> moyenneParEncadrant,
        Map<String, Double> moyenneParFiliere) {
}
