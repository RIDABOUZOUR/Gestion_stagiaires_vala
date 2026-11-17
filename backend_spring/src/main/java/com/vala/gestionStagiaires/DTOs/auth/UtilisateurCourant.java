package com.vala.gestionStagiaires.DTOs.auth;

import com.vala.gestionStagiaires.enums.Role;

/** Réponse de {@code GET /auth/me}. */
public record UtilisateurCourant(String email, Role role, Long encadrantId) {
}
