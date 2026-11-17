package com.vala.gestionStagiaires.DTOs.auth;

import com.vala.gestionStagiaires.entities.Utilisateur;
import com.vala.gestionStagiaires.enums.Role;

/** Compte utilisateur, sans le mot de passe. */
public record UtilisateurDto(Long id, String email, Role role, Long encadrantId, String encadrantNom, boolean actif) {

    public static UtilisateurDto of(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getEmail(), u.getRole(),
                u.getEncadrant() != null ? u.getEncadrant().getId() : null,
                u.getEncadrant() != null ? (u.getEncadrant().getNom() + " " + u.getEncadrant().getPrenom()).trim() : null,
                u.isActif());
    }
}
