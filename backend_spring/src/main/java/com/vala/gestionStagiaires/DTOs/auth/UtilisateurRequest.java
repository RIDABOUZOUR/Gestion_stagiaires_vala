package com.vala.gestionStagiaires.DTOs.auth;

import com.vala.gestionStagiaires.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Création d'un compte. {@code encadrantId} est obligatoire pour un ENCADRANT, interdit sinon. */
public record UtilisateurRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
        String motDePasse,

        @NotNull(message = "Le rôle est obligatoire")
        Role role,

        Long encadrantId) {

    @Override
    public String toString() {
        return "UtilisateurRequest[email=" + email + ", role=" + role + ", encadrantId=" + encadrantId + ", motDePasse=***]";
    }
}
