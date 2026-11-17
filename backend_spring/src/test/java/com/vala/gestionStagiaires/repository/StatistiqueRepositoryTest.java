package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.projection.ComptageParEncadrant;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Requêtes JPQL des statistiques, exécutées sur une vraie base H2. */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Requêtes de statistiques")
class StatistiqueRepositoryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private StagiaireRepository stagiaireRepository;

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private EncadrantRepository encadrantRepository;

    @Autowired
    private DepartementRepository departementRepository;

    private Encadrant martin1;
    private Encadrant martin2;

    @BeforeEach
    void setUp() {
        Departement info = em.persist(Departement.builder().nom("Informatique").responsable("Durand").build());
        em.persist(Departement.builder().nom("Marketing").responsable("Roux").build()); // sans stagiaire

        // Deux encadrants homonymes + un encadrant sans stage
        martin1 = em.persist(Encadrant.builder().nom("Martin").prenom("Paul").email("p1@vala.ma").build());
        martin2 = em.persist(Encadrant.builder().nom("Martin").prenom("Paul").email("p2@vala.ma").build());
        em.persist(Encadrant.builder().nom("Bernard").prenom("Lucie").email("lucie@vala.ma").build());

        Stage enCoursBientotFini = em.persist(stage("IA", StageStatus.EN_COURS, TODAY.plusDays(10), martin1));
        Stage enCoursLongtemps = em.persist(stage("Web", StageStatus.EN_COURS, TODAY.plusDays(90), martin2));
        em.persist(stage("Data", StageStatus.PLANIFIE, TODAY.plusDays(120), martin1));

        em.persist(stagiaire("a@mail.com", "Génie Info", "ENSA", enCoursBientotFini, info));
        em.persist(stagiaire("b@mail.com", "Génie Info", null, enCoursBientotFini, info));
        em.persist(stagiaire("c@mail.com", null, "ENSA", enCoursLongtemps, info));
        em.flush();
        em.clear();
    }

    private static Stage stage(String sujet, StageStatus status, LocalDate fin, Encadrant encadrant) {
        return Stage.builder().sujet(sujet).status(status)
                .dateDebut(fin.minusMonths(4)).dateFin(fin).encadrant(encadrant).build();
    }

    private static Stagiaire stagiaire(String email, String filiere, String organisme, Stage stage, Departement dep) {
        return Stagiaire.builder().nom("Nom").prenom("Prenom").email(email).sexe(Sexe.HOMME)
                .filiere(filiere).organisme(organisme).stage(stage).departement(dep).build();
    }

    @Test
    @DisplayName("par département : inclut les départements vides (0)")
    void parDepartement() {
        assertThat(departementRepository.compterStagiairesParDepartement()).containsExactlyInAnyOrder(
                new ComptageParLibelle("Informatique", 3L),
                new ComptageParLibelle("Marketing", 0L));
    }

    @Test
    @DisplayName("par filière et organisme : les valeurs nulles forment un groupe")
    void parFiliereEtOrganisme() {
        assertThat(stagiaireRepository.compterParFiliere()).containsExactlyInAnyOrder(
                new ComptageParLibelle("Génie Info", 2L),
                new ComptageParLibelle(null, 1L));
        assertThat(stagiaireRepository.compterParOrganisme()).containsExactlyInAnyOrder(
                new ComptageParLibelle("ENSA", 2L),
                new ComptageParLibelle(null, 1L));
    }

    @Test
    @DisplayName("par encadrant : regroupe par id (homonymes distincts) et inclut les encadrants sans stagiaire")
    void parEncadrant() {
        assertThat(encadrantRepository.compterStagiairesParEncadrant())
                .hasSize(3)
                .contains(
                        new ComptageParEncadrant(martin1.getId(), "Martin", "Paul", 2L),
                        new ComptageParEncadrant(martin2.getId(), "Martin", "Paul", 1L))
                .anySatisfy(l -> {
                    assertThat(l.nom()).isEqualTo("Bernard");
                    assertThat(l.nombre()).isZero();
                });
    }

    @Test
    @DisplayName("stages par statut, stagiaires actifs et échéances")
    void stagesEtEcheances() {
        assertThat(stageRepository.compterParStatut()).containsExactlyInAnyOrder(
                new ComptageParStatut(StageStatus.EN_COURS, 2L),
                new ComptageParStatut(StageStatus.PLANIFIE, 1L));
        assertThat(stagiaireRepository.countByStageStatus(StageStatus.EN_COURS)).isEqualTo(3);
        assertThat(stageRepository.countByStatus(StageStatus.PLANIFIE)).isEqualTo(1);

        assertThat(stageRepository.findByStatusAndDateFinBetweenOrderByDateFinAsc(
                StageStatus.EN_COURS, TODAY, TODAY.plusDays(30)))
                .extracting(Stage::getSujet).containsExactly("IA");
        assertThat(stageRepository.countByStatusAndDateFinBetween(
                StageStatus.EN_COURS, TODAY, TODAY.plusDays(30))).isEqualTo(1);
    }
}
