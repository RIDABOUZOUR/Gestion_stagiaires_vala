package com.vala.gestionStagiaires.DTOs.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "L'email est obligatoire") String email,
        @NotBlank(message = "Le mot de passe est obligatoire") String motDePasse) {

    /** Le mot de passe n'apparaît jamais dans les journaux (aspect de journalisation en DEBUG). */
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", motDePasse=***]";
    }
}
