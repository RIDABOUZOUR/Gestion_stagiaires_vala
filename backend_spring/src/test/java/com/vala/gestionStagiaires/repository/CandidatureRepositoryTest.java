package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatutCandidature;
import com.vala.gestionStagiaires.repository.specification.CandidatureSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Requêtes des candidatures, sur une vraie base H2. */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Requêtes des candidatures")
class CandidatureRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CandidatureRepository repository;

    private Departement info;
    private Candidature refusee;

    @BeforeEach
    void setUp() {
        info = em.persist(Departement.builder().nom("Informatique").responsable("Durand").build());
        em.persist(candidature("Alami", "sara@mail.com", "ENSA", CandidatureStatus.SOUMISE, info,
                Instant.parse("2026-03-01T10:00:00Z"), null));
        em.persist(candidature("Benali", "omar@mail.com", "EMSI", CandidatureStatus.EN_ETUDE, null,
                Instant.parse("2026-02-10T10:00:00Z"), null));
        refusee = em.persist(candidature("Chraibi", "ali@mail.com", "ENSA", CandidatureStatus.REFUSEE, info,
                Instant.parse("2025-12-01T10:00:00Z"), Instant.parse("2025-12-04T10:00:00Z")));
        em.persist(candidature("Alami", "SARA@mail.com", null, CandidatureStatus.RETIREE, null,
                Instant.parse("2025-06-01T10:00:00Z"), Instant.parse("2025-06-02T10:00:00Z")));
        em.flush();
        em.clear();
    }

    private static Candidature candidature(String nom, String email, String organisme, CandidatureStatus statut,
                                           Departement dep, Instant soumission, Instant decision) {
        return Candidature.builder().nom(nom).prenom("Prenom").email(email).sexe(Sexe.FEMME).organisme(organisme)
                .statut(statut).departementSouhaite(dep).dateSoumission(soumission).dateDecision(decision).build();
    }

    private List<String> noms(CandidatureFiltre filtre) {
        return repository.findAll(CandidatureSpecifications.avecFiltre(filtre),
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "dateSoumission")))
                .map(Candidature::getNom).getContent();
    }

    @Test
    @DisplayName("une seule candidature active par email (insensible à la casse)")
    void candidatureActiveParEmail() {
        assertThat(repository.existsByEmailIgnoreCaseAndStatutIn("SARA@MAIL.COM", CandidatureStatus.ACTIVES)).isTrue();
        assertThat(repository.existsByEmailIgnoreCaseAndStatutIn("ali@mail.com", CandidatureStatus.ACTIVES)).isFalse();
        Long idSara = repository.findAll().stream()
                .filter(c -> c.getStatut() == CandidatureStatus.SOUMISE).findFirst().orElseThrow().getId();
        assertThat(repository.existsByEmailIgnoreCaseAndStatutInAndIdNot("sara@mail.com", CandidatureStatus.ACTIVES, idSara))
                .isFalse();
    }

    @Test
    @DisplayName("recherche par mot-clé (nom, email, organisme), statut et département souhaité")
    void recherche() {
        assertThat(noms(new CandidatureFiltre(null, null, null))).containsExactly("Alami", "Benali", "Chraibi", "Alami");
        assertThat(noms(new CandidatureFiltre("ensa", null, null))).containsExactly("Alami", "Chraibi");
        assertThat(noms(new CandidatureFiltre("omar@", null, null))).containsExactly("Benali");
        assertThat(noms(new CandidatureFiltre(null, CandidatureStatus.EN_ETUDE, null))).containsExactly("Benali");
        assertThat(noms(new CandidatureFiltre(null, null, info.getId()))).containsExactly("Alami", "Chraibi");
    }

    @Test
    @DisplayName("statistiques : statuts, organismes, dates de soumission et délais")
    void statistiques() {
        assertThat(repository.compterParStatut()).containsExactlyInAnyOrder(
                new ComptageParStatutCandidature(CandidatureStatus.SOUMISE, 1L),
                new ComptageParStatutCandidature(CandidatureStatus.EN_ETUDE, 1L),
                new ComptageParStatutCandidature(CandidatureStatus.REFUSEE, 1L),
                new ComptageParStatutCandidature(CandidatureStatus.RETIREE, 1L));
        assertThat(repository.compterParOrganisme()).containsExactlyInAnyOrder(
                new ComptageParLibelle("ENSA", 2L), new ComptageParLibelle("EMSI", 1L), new ComptageParLibelle(null, 1L));
        assertThat(repository.datesSoumissionDepuis(Instant.parse("2026-01-01T00:00:00Z"))).hasSize(2);
        assertThat(repository.delaisDecision(Set.of(CandidatureStatus.ACCEPTEE, CandidatureStatus.REFUSEE)))
                .singleElement()
                .satisfies(d -> assertThat(d.dateDecision()).isEqualTo(Instant.parse("2025-12-04T10:00:00Z")));
    }

    @Test
    @DisplayName("detacherDepartement() efface le département souhaité sans supprimer les candidatures")
    void detacherDepartement() {
        repository.detacherDepartement(info.getId());
        em.clear();

        assertThat(repository.count()).isEqualTo(4);
        assertThat(repository.findById(refusee.getId())).get()
                .extracting(Candidature::getDepartementSouhaite).isNull();
    }
}
