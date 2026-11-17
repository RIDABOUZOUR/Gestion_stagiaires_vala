package com.vala.gestionStagiaires.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Organisme d'accueil, tel qu'il apparaît sur les attestations (préfixe {@code app.organisation}).
 *
 * @param signataire nom de la personne qui signe (facultatif : sinon l'attestation est rédigée au nom de la fonction)
 * @param fonction   fonction du signataire
 */
@ConfigurationProperties(prefix = "app.organisation")
public record OrganisationProperties(
        @DefaultValue("Organisme d'accueil") String nom,
        @DefaultValue("Casablanca") String ville,
        @DefaultValue("") String signataire,
        @DefaultValue("Directeur des Ressources Humaines") String fonction) {
}
