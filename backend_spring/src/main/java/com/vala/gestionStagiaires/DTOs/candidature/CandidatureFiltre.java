package com.vala.gestionStagiaires.DTOs.candidature;

import com.vala.gestionStagiaires.enums.CandidatureStatus;

/**
 * Critères de recherche des candidatures ; chaque critère {@code null} est ignoré.
 *
 * @param keyword       recherche dans le nom, le prénom, l'email et l'organisme
 * @param statut        statut de la candidature
 * @param departementId département souhaité
 */
public record CandidatureFiltre(String keyword, CandidatureStatus statut, Long departementId) {
}
