package com.vala.gestionStagiaires.enums;

/**
 * Rôles applicatifs.
 * <ul>
 *   <li>ADMIN : tout, y compris la gestion des comptes utilisateurs ;</li>
 *   <li>RH : toute la gestion métier (stagiaires, stages, candidatures, statistiques, exports) ;</li>
 *   <li>ENCADRANT : consultation et suivi de <b>ses</b> stagiaires uniquement.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    RH,
    ENCADRANT
}
