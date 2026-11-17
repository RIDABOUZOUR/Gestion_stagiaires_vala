package com.vala.gestionStagiaires.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Outils communs aux services de statistiques. */
public final class StatistiquesUtils {

    /** Libellé utilisé quand la donnée n'est pas renseignée (null ou vide). */
    public static final String NON_RENSEIGNE = "Non renseigné";

    private StatistiquesUtils() {
    }

    /** null ou vide → « Non renseigné », sinon la valeur sans espaces superflus. */
    public static String libelle(String valeur) {
        return valeur == null || valeur.isBlank() ? NON_RENSEIGNE : valeur.trim();
    }

    /** Trie par valeur décroissante puis par clé ; l'ordre est conservé dans le JSON. */
    public static <V extends Comparable<? super V>> Map<String, V> trierDecroissant(Map<String, V> valeurs) {
        return valeurs.entrySet().stream()
                .sorted(Map.Entry.<String, V>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    /** Pourcentage avec une décimale, ou {@code null} si le dénominateur est nul. */
    public static Double pourcentage(long numerateur, long denominateur) {
        if (denominateur == 0) {
            return null;
        }
        return Math.round(1000.0 * numerateur / denominateur) / 10.0;
    }

    /** Moyenne arrondie à 2 décimales, ou {@code null} pour une liste vide. */
    public static Double moyenne(List<? extends Number> valeurs) {
        OptionalDouble m = valeurs.stream().mapToDouble(Number::doubleValue).average();
        return m.isPresent() ? Math.round(m.getAsDouble() * 100.0) / 100.0 : null;
    }

    /** Regroupe des éléments par libellé et calcule la moyenne d'une valeur, triée par moyenne décroissante. */
    public static <T> Map<String, Double> moyenneParLibelle(List<T> elements, Function<T, String> libelle,
                                                            Function<T, ? extends Number> valeur) {
        Map<String, List<Number>> groupes = elements.stream().collect(Collectors.groupingBy(
                e -> libelle(libelle.apply(e)), Collectors.mapping(valeur, Collectors.toList())));
        Map<String, Double> moyennes = new LinkedHashMap<>();
        groupes.forEach((k, v) -> moyennes.put(k, moyenne(v)));
        return trierDecroissant(moyennes);
    }

    /**
     * Libellés uniques par identifiant : deux éléments homonymes (ex. deux encadrants
     * « Martin Paul ») sont distingués par leur id, « Martin Paul (#1) ».
     */
    public static Map<Long, String> libellesUniques(Map<Long, String> libelles) {
        Map<String, Long> occurrences = libelles.values().stream()
                .collect(Collectors.groupingBy(StatistiquesUtils::libelle, Collectors.counting()));
        Map<Long, String> resultat = new LinkedHashMap<>();
        libelles.forEach((id, nom) -> {
            String l = libelle(nom);
            resultat.put(id, occurrences.get(l) > 1 ? l + " (#" + id + ")" : l);
        });
        return resultat;
    }
}
