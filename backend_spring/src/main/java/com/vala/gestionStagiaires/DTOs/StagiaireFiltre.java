package com.vala.gestionStagiaires.DTOs;

import com.vala.gestionStagiaires.enums.StageStatus;

/**
 * Critères de recherche des stagiaires ; chaque critère {@code null} est ignoré.
 *
 * @param keyword       recherche dans le nom, le prénom et l'email
 * @param departementId département du stagiaire
 * @param stageId       stage du stagiaire
 * @param status        statut du stage du stagiaire
 * @param organisme     organisme d'origine (égalité, insensible à la casse)
 * @param encadrantId   encadrant du stage du stagiaire
 */
public record StagiaireFiltre(
        String keyword,
        Long departementId,
        Long stageId,
        StageStatus status,
        String organisme,
        Long encadrantId) {

    public StagiaireFiltre(String keyword, Long departementId, Long stageId, StageStatus status, String organisme) {
        this(keyword, departementId, stageId, status, organisme, null);
    }

    /** Même filtre, limité aux stagiaires de cet encadrant. */
    public StagiaireFiltre pourEncadrant(Long encadrant) {
        return new StagiaireFiltre(keyword, departementId, stageId, status, organisme, encadrant);
    }
}
