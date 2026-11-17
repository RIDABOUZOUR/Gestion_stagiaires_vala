package com.vala.gestionStagiaires.repository.specification;

import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.entities.Stagiaire;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static com.vala.gestionStagiaires.repository.specification.SpecificationUtils.contientIgnoreCase;
import static com.vala.gestionStagiaires.repository.specification.SpecificationUtils.hasText;

/** Construit la requête de recherche multicritère des stagiaires. */
public final class StagiaireSpecifications {

    private StagiaireSpecifications() {
    }

    public static Specification<Stagiaire> avecFiltre(StagiaireFiltre filtre) {
        return (root, query, cb) -> {
            List<Predicate> predicats = new ArrayList<>();
            if (filtre == null) {
                return cb.conjunction();
            }
            if (hasText(filtre.keyword())) {
                predicats.add(cb.or(
                        contientIgnoreCase(cb, root.get("nom"), filtre.keyword()),
                        contientIgnoreCase(cb, root.get("prenom"), filtre.keyword()),
                        contientIgnoreCase(cb, root.get("email"), filtre.keyword())));
            }
            if (filtre.departementId() != null) {
                predicats.add(cb.equal(root.get("departement").get("id"), filtre.departementId()));
            }
            if (filtre.stageId() != null) {
                predicats.add(cb.equal(root.get("stage").get("id"), filtre.stageId()));
            }
            if (filtre.status() != null) {
                predicats.add(cb.equal(root.get("stage").get("status"), filtre.status()));
            }
            if (hasText(filtre.organisme())) {
                predicats.add(cb.equal(cb.lower(root.get("organisme")), filtre.organisme().trim().toLowerCase()));
            }
            return cb.and(predicats.toArray(Predicate[]::new));
        };
    }
}
