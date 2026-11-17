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
 */
public record StagiaireFiltre(
        String keyword,
        Long departementId,
        Long stageId,
        StageStatus status,
        String organisme) {
}
