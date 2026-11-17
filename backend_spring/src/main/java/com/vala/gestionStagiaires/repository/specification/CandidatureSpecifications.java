package com.vala.gestionStagiaires.repository.specification;

import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.entities.Candidature;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static com.vala.gestionStagiaires.repository.specification.SpecificationUtils.contientIgnoreCase;
import static com.vala.gestionStagiaires.repository.specification.SpecificationUtils.hasText;

/** Construit la requête de recherche multicritère des candidatures. */
public final class CandidatureSpecifications {

    private CandidatureSpecifications() {
    }

    public static Specification<Candidature> avecFiltre(CandidatureFiltre filtre) {
        return (root, query, cb) -> {
            if (filtre == null) {
                return cb.conjunction();
            }
            List<Predicate> predicats = new ArrayList<>();
            if (hasText(filtre.keyword())) {
                predicats.add(cb.or(
                        contientIgnoreCase(cb, root.get("nom"), filtre.keyword()),
                        contientIgnoreCase(cb, root.get("prenom"), filtre.keyword()),
                        contientIgnoreCase(cb, root.get("email"), filtre.keyword()),
                        contientIgnoreCase(cb, root.get("organisme"), filtre.keyword())));
            }
            if (filtre.statut() != null) {
                predicats.add(cb.equal(root.get("statut"), filtre.statut()));
            }
            if (filtre.departementId() != null) {
                predicats.add(cb.equal(root.get("departementSouhaite").get("id"), filtre.departementId()));
            }
            return cb.and(predicats.toArray(Predicate[]::new));
        };
    }
}
