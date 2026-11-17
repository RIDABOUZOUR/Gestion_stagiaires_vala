package com.vala.gestionStagiaires.securite;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Configuration de l'authentification (préfixe {@code app.securite}).
 *
 * @param jwtSecret        clé de signature HS256 des jetons (variable JWT_SECRET, 32 octets minimum) — obligatoire
 * @param jwtDuree         durée de validité d'un jeton
 * @param emetteur         émetteur (claim {@code iss}) des jetons, vérifié à la réception
 * @param adminEmail       compte administrateur créé au premier démarrage si aucun utilisateur n'existe
 * @param adminMotDePasse  mot de passe de ce compte
 */
@ConfigurationProperties(prefix = "app.securite")
public record SecuriteProperties(
        String jwtSecret,
        @DefaultValue("8h") Duration jwtDuree,
        @DefaultValue("gestion-stagiaires") String emetteur,
        String adminEmail,
        String adminMotDePasse) {

    public static final int LONGUEUR_MIN_SECRET = 32;

    public SecuriteProperties {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < LONGUEUR_MIN_SECRET) {
            throw new IllegalStateException("La variable JWT_SECRET doit être définie avec au moins "
                    + LONGUEUR_MIN_SECRET + " caractères (ex. : openssl rand -base64 48).");
        }
    }
}
