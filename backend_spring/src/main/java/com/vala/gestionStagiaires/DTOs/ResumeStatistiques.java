package com.vala.gestionStagiaires.DTOs;

/**
 * Indicateurs clés du tableau de bord.
 *
 * @param totalStagiaires          nombre total de stagiaires
 * @param stagiairesActifs         stagiaires dont le stage est EN_COURS
 * @param totalStages              nombre total de stages
 * @param stagesEnCours            stages EN_COURS
 * @param stagesPlanifies          stages PLANIFIE
 * @param stagesAEcheance          stages EN_COURS qui se terminent dans les {@code joursEcheance} jours
 * @param joursEcheance            horizon utilisé pour {@code stagesAEcheance}
 * @param totalEncadrants          nombre total d'encadrants
 * @param totalDepartements        nombre total de départements
 */
public record ResumeStatistiques(
        long totalStagiaires,
        long stagiairesActifs,
        long totalStages,
        long stagesEnCours,
        long stagesPlanifies,
        long stagesAEcheance,
        int joursEcheance,
        long totalEncadrants,
        long totalDepartements) {
}
