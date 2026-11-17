package com.vala.gestionStagiaires.repository.projection;

/** Nombre de stagiaires suivis par un encadrant (l'id distingue les homonymes). */
public record ComptageParEncadrant(Long encadrantId, String nom, String prenom, Long nombre) {
}
