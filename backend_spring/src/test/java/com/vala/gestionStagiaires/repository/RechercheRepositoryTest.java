package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.specification.StageSpecifications;
import com.vala.gestionStagiaires.repository.specification.StagiaireSpecifications;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Recherche multicritère et chargement des relations, sur une vraie base H2.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Recherche paginée (Specifications + EntityGraph)")
class RechercheRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private StagiaireRepository stagiaireRepository;

    @Autowired
    private StageRepository stageRepository;

    private Departement info;
    private Departement rh;
    private Encadrant martin;
    private Stage stageIa;
    private Stage stageWeb;

    @BeforeEach
    void setUp() {
        info = em.persist(Departement.builder().nom("Informatique").responsable("Durand").build());
        rh = em.persist(Departement.builder().nom("Ressources humaines").responsable("Leroy").build());
        martin = em.persist(Encadrant.builder().nom("Martin").prenom("Paul").email("paul@vala.ma").build());
        Encadrant bernard = em.persist(Encadrant.builder().nom("Bernard").prenom("Lucie").email("lucie@vala.ma").build());

        stageIa = em.persist(stage("Stage IA", StageStatus.EN_COURS, LocalDate.of(2026, 1, 10), martin));
        stageWeb = em.persist(stage("Stage Web", StageStatus.PLANIFIE, LocalDate.of(2026, 9, 1), bernard));
        em.persist(stage("Stage 100% data", StageStatus.TERMINE, LocalDate.of(2025, 2, 1), martin));

        em.persist(stagiaire("Alami", "Sara", "sara.alami@mail.com", "ENSA", stageIa, info));
        em.persist(stagiaire("Benali", "Omar", "omar@mail.com", "EMSI", stageIa, info));
        em.persist(stagiaire("Chraibi", "Ali", "ali_c@mail.com", "ENSA", stageWeb, rh));
        em.flush();
        em.clear();
    }

    private static Stage stage(String sujet, StageStatus status, LocalDate debut, Encadrant encadrant) {
        return Stage.builder().sujet(sujet).status(status)
                .dateDebut(debut).dateFin(debut.plusMonths(4)).encadrant(encadrant).build();
    }

    private static Stagiaire stagiaire(String nom, String prenom, String email, String organisme,
                                       Stage stage, Departement departement) {
        return Stagiaire.builder().nom(nom).prenom(prenom).email(email).organisme(organisme)
                .sexe(Sexe.FEMME).stage(stage).departement(departement).build();
    }

    private List<String> nomsStagiaires(StagiaireFiltre filtre) {
        return stagiaireRepository
                .findAll(StagiaireSpecifications.avecFiltre(filtre), PageRequest.of(0, 20, Sort.by("nom")))
                .map(Stagiaire::getNom)
                .getContent();
    }

    @Test
    @DisplayName("sans critère, renvoie tous les stagiaires")
    void sansFiltre() {
        assertThat(nomsStagiaires(new StagiaireFiltre(null, null, null, null, null)))
                .containsExactly("Alami", "Benali", "Chraibi");
    }

    @Test
    @DisplayName("le mot-clé cherche dans nom, prénom et email, sans tenir compte de la casse")
    void parMotCle() {
        assertThat(nomsStagiaires(new StagiaireFiltre("OMAR", null, null, null, null))).containsExactly("Benali");
        assertThat(nomsStagiaires(new StagiaireFiltre("sara.alami@", null, null, null, null))).containsExactly("Alami");
        assertThat(nomsStagiaires(new StagiaireFiltre("ali", null, null, null, null)))
                .containsExactly("Benali", "Chraibi"); // « Benali » (nom) et « Ali » (prénom)
    }

    @Test
    @DisplayName("les jokers SQL saisis sont cherchés littéralement")
    void jokersEchappes() {
        assertThat(nomsStagiaires(new StagiaireFiltre("_c@", null, null, null, null))).containsExactly("Chraibi");
        assertThat(nomsStagiaires(new StagiaireFiltre("%", null, null, null, null))).isEmpty();
    }

    @Test
    @DisplayName("filtre par département, stage, statut du stage et organisme (combinables)")
    void parCriteres() {
        assertThat(nomsStagiaires(new StagiaireFiltre(null, rh.getId(), null, null, null))).containsExactly("Chraibi");
        assertThat(nomsStagiaires(new StagiaireFiltre(null, null, stageIa.getId(), null, null)))
                .containsExactly("Alami", "Benali");
        assertThat(nomsStagiaires(new StagiaireFiltre(null, null, null, StageStatus.PLANIFIE, null)))
                .containsExactly("Chraibi");
        assertThat(nomsStagiaires(new StagiaireFiltre(null, null, null, null, "ensa")))
                .containsExactly("Alami", "Chraibi");
        assertThat(nomsStagiaires(new StagiaireFiltre(null, info.getId(), null, StageStatus.EN_COURS, "ENSA")))
                .containsExactly("Alami");
    }

    @Test
    @DisplayName("pagine et trie, avec le nombre total d'éléments")
    void paginationEtTri() {
        Page<Stagiaire> page = stagiaireRepository.findAll(
                StagiaireSpecifications.avecFiltre(null), PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "nom")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Stagiaire::getNom).containsExactly("Alami");
    }

    @Test
    @DisplayName("charge stage, encadrant et département dans la même requête (pas de N+1)")
    void relationsChargees() {
        Page<Stagiaire> page = stagiaireRepository.findAll(
                StagiaireSpecifications.avecFiltre(null), PageRequest.of(0, 20));
        List<Stagiaire> tous = stagiaireRepository.findAll();

        for (Stagiaire s : page.getContent()) {
            assertThat(Hibernate.isInitialized(s.getStage())).isTrue();
            assertThat(Hibernate.isInitialized(s.getStage().getEncadrant())).isTrue();
            assertThat(Hibernate.isInitialized(s.getDepartement())).isTrue();
        }
        assertThat(tous).allSatisfy(s -> assertThat(Hibernate.isInitialized(s.getStage().getEncadrant())).isTrue());
        assertThat(stageRepository.findAll())
                .allSatisfy(st -> assertThat(Hibernate.isInitialized(st.getEncadrant())).isTrue());
    }

    @Test
    @DisplayName("un tri sur une propriété inconnue lève PropertyReferenceException")
    void triInvalide() {
        assertThatThrownBy(() -> stagiaireRepository.findAll(
                StagiaireSpecifications.avecFiltre(null), PageRequest.of(0, 20, Sort.by("inconnu"))))
                .isInstanceOf(PropertyReferenceException.class);
    }

    @Test
    @DisplayName("stages : filtre par sujet, statut et encadrant")
    void rechercheStages() {
        List<String> parEncadrant = stageRepository.findAll(
                        StageSpecifications.avecFiltre(new StageFiltre(null, null, martin.getId())),
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "dateDebut")))
                .map(Stage::getSujet).getContent();
        List<String> parSujetEtStatut = stageRepository.findAll(
                        StageSpecifications.avecFiltre(new StageFiltre("stage", StageStatus.PLANIFIE, null)),
                        PageRequest.of(0, 20))
                .map(Stage::getSujet).getContent();
        List<String> pourcentLitteral = stageRepository.findAll(
                        StageSpecifications.avecFiltre(new StageFiltre("100%", null, null)), PageRequest.of(0, 20))
                .map(Stage::getSujet).getContent();

        assertThat(parEncadrant).containsExactly("Stage IA", "Stage 100% data");
        assertThat(parSujetEtStatut).containsExactly("Stage Web");
        assertThat(pourcentLitteral).containsExactly("Stage 100% data");
    }
}
