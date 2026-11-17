package com.vala.gestionStagiaires.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

final class SpecificationUtils {

    private static final char ESCAPE = '\\';

    private SpecificationUtils() {
    }

    static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * {@code LOWER(champ) LIKE %motcle%}, où les jokers {@code %} et {@code _}
     * saisis par l'utilisateur sont échappés (ils sont cherchés littéralement).
     */
    static Predicate contientIgnoreCase(CriteriaBuilder cb, Expression<String> champ, String motCle) {
        String echappe = motCle.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return cb.like(cb.lower(champ), "%" + echappe + "%", ESCAPE);
    }
}
