package com.vala.gestionStagiaires.DTOs.auth;

import com.vala.gestionStagiaires.enums.Role;

import java.time.Instant;

/** Jeton à envoyer ensuite dans l'en-tête {@code Authorization: Bearer <token>}. */
public record LoginResponse(String token, Instant expiresAt, String email, Role role, Long encadrantId) {

    @Override
    public String toString() {
        return "LoginResponse[email=" + email + ", role=" + role + ", expiresAt=" + expiresAt + ", token=***]";
    }
}
