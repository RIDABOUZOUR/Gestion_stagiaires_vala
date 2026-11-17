package com.vala.gestionStagiaires.DTOs;

import com.vala.gestionStagiaires.enums.StageStatus;

/**
 * Critères de recherche des stages ; chaque critère {@code null} est ignoré.
 *
 * @param keyword     recherche dans le sujet
 * @param status      statut du stage
 * @param encadrantId encadrant du stage
 */
public record StageFiltre(
        String keyword,
        StageStatus status,
        Long encadrantId) {
}
