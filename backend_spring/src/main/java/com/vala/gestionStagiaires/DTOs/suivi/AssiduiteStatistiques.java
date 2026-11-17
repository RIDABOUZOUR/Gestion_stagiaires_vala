package com.vala.gestionStagiaires.DTOs.suivi;

import java.util.List;
import java.util.Map;

/**
 * Assiduité des stagiaires en cours de stage.
 *
 * @param tauxGlobal      pourcentage global ({@code null} si rien n'est saisi)
 * @param parDepartement  pourcentage par département (départements sans saisie exclus), décroissant
 * @param stagiaires      détail par stagiaire, du moins assidu au plus assidu (sans saisie en dernier)
 */
public record AssiduiteStatistiques(
        Double tauxGlobal,
        Map<String, Double> parDepartement,
        List<AssiduiteStagiaire> stagiaires) {
}
