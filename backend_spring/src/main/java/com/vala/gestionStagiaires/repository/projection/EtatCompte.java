package com.vala.gestionStagiaires.repository.projection;

import com.vala.gestionStagiaires.enums.Role;

/** État courant d'un compte, relu à chaque requête pour valider le jeton. */
public record EtatCompte(boolean actif, Role role, Long encadrantId) {
}
