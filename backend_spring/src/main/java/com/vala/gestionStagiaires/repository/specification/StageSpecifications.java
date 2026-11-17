package com.vala.gestionStagiaires.repository.specification;

import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.entities.Stage;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static com.vala.gestionStagiaires.repository.specification.SpecificationUtils.contientIgnoreCase;
import static com.vala.gestionStagiaires.repository.specification.SpecificationUtils.hasText;

/** Construit la requête de recherche multicritère des stages. */
public final class StageSpecifications {

    private StageSpecifications() {
    }

    public static Specification<Stage> avecFiltre(StageFiltre filtre) {
        return (root, query, cb) -> {
            List<Predicate> predicats = new ArrayList<>();
            if (filtre == null) {
                return cb.conjunction();
            }
            if (hasText(filtre.keyword())) {
                predicats.add(contientIgnoreCase(cb, root.get("sujet"), filtre.keyword()));
            }
            if (filtre.status() != null) {
                predicats.add(cb.equal(root.get("status"), filtre.status()));
            }
            if (filtre.encadrantId() != null) {
                predicats.add(cb.equal(root.get("encadrant").get("id"), filtre.encadrantId()));
            }
            return cb.and(predicats.toArray(Predicate[]::new));
        };
    }
}
