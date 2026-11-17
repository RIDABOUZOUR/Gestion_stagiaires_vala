package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Utilisateur;
import com.vala.gestionStagiaires.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    @EntityGraph(attributePaths = "encadrant")
    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "encadrant")
    List<Utilisateur> findAllByOrderByEmailAsc();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByEncadrantId(Long encadrantId);

    boolean existsByEncadrantIdAndIdNot(Long encadrantId, Long id);

    long countByRoleAndActifTrue(Role role);
}
