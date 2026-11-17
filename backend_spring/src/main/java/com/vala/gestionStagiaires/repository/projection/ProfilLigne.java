package com.vala.gestionStagiaires.repository.projection;

import com.vala.gestionStagiaires.enums.Sexe;

/** Caractéristiques d'un stagiaire pour les statistiques de profil. */
public record ProfilLigne(String departement, Sexe sexe, String niveau, String filiere) {
}
