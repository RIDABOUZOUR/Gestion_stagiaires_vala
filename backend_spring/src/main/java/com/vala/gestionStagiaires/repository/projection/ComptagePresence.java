package com.vala.gestionStagiaires.repository.projection;

import com.vala.gestionStagiaires.enums.StatutPresence;

/** Nombre de jours d'un statut de présence donné pour un stagiaire. */
public record ComptagePresence(Long stagiaireId, StatutPresence statut, Long nombre) {
}
