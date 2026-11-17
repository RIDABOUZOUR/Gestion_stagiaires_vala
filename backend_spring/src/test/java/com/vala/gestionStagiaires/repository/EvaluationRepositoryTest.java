package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Attestation;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Evaluation;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Requêtes des évaluations et attestations, sur une vraie base H2. */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Requêtes des évaluations et attestations")
class EvaluationRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private AttestationRepository attestationRepository;

    private Stagiaire evalue;
    private Stagiaire nonEvalue;

    @BeforeEach
    void setUp() {
        Departement dep = em.persist(Departement.builder().nom("Informatique").responsable("Durand").build());
        Encadrant enc = em.persist(Encadrant.builder().nom("Martin").prenom("Paul").email("p@vala.ma").build());
        Stage termine = em.persist(Stage.builder().sujet("IA").status(StageStatus.TERMINE)
                .dateDebut(LocalDate.of(2026, 1, 5)).dateFin(LocalDate.of(2026, 4, 30)).encadrant(enc).build());
        Stage enCours = em.persist(Stage.builder().sujet("Web").status(StageStatus.EN_COURS)
                .dateDebut(LocalDate.of(2026, 3, 1)).dateFin(LocalDate.of(2026, 8, 31)).encadrant(enc).build());

        evalue = em.persist(stagiaire("a@mail.com", termine, dep));
        nonEvalue = em.persist(stagiaire("b@mail.com", termine, dep));
        em.persist(stagiaire("c@mail.com", enCours, dep)); // pas encore évaluable

        em.persist(Evaluation.builder().stagiaire(evalue).noteGlobale(new BigDecimal("15.5"))
                .competenceTechnique(4).communication(4).autonomie(4).assiduite(4)
                .recommande(true).dateEvaluation(LocalDate.of(2026, 5, 2)).build());
        em.persist(attestation(evalue, 2026, 1));
        em.persist(attestation(nonEvalue, 2026, 2));
        em.flush();
        em.clear();
    }

    private static Stagiaire stagiaire(String email, Stage stage, Departement dep) {
        return Stagiaire.builder().nom("Nom").prenom("Prenom").email(email).sexe(Sexe.HOMME)
                .stage(stage).departement(dep).build();
    }

    private static Attestation attestation(Stagiaire s, int annee, int rang) {
        return Attestation.builder().stagiaire(s).annee(annee).sequence(rang)
                .numero(String.format("ATT-%d-%04d", annee, rang)).dateEmission(LocalDate.of(annee, 5, 2)).build();
    }

    @Test
    @DisplayName("compte les stagiaires au stage terminé non évalués")
    void sansEvaluation() {
        assertThat(evaluationRepository.compterStagiairesSansEvaluation(StageStatus.TERMINE)).isEqualTo(1);
    }

    @Test
    @DisplayName("findAll() charge département, stage et encadrant du stagiaire")
    void findAllAvecRelations() {
        List<Evaluation> evaluations = evaluationRepository.findAll();

        assertThat(evaluations).hasSize(1);
        Stagiaire s = evaluations.get(0).getStagiaire();
        assertThat(Hibernate.isInitialized(s)).isTrue();
        assertThat(Hibernate.isInitialized(s.getDepartement())).isTrue();
        assertThat(Hibernate.isInitialized(s.getStage().getEncadrant())).isTrue();
    }

    @Test
    @DisplayName("une seule évaluation par stagiaire")
    void evaluationUnique() {
        Stagiaire ref = em.find(Stagiaire.class, evalue.getId());
        assertThatThrownBy(() -> evaluationRepository.saveAndFlush(Evaluation.builder().stagiaire(ref)
                .noteGlobale(BigDecimal.TEN).competenceTechnique(1).communication(1).autonomie(1).assiduite(1)
                .dateEvaluation(LocalDate.now()).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("dernierRang() renvoie le plus grand rang de l'année, 0 pour une année vide")
    void dernierRang() {
        assertThat(attestationRepository.dernierRang(2026)).isEqualTo(2);
        assertThat(attestationRepository.dernierRang(2027)).isZero();
        assertThat(attestationRepository.findByStagiaireId(evalue.getId()))
                .get().extracting(Attestation::getNumero).isEqualTo("ATT-2026-0001");
        assertThat(attestationRepository.existsByStagiaireId(nonEvalue.getId())).isTrue();
    }

    @Test
    @DisplayName("supprimerParStagiaire() efface l'évaluation")
    void suppression() {
        evaluationRepository.supprimerParStagiaire(evalue.getId());

        assertThat(evaluationRepository.findByStagiaireId(evalue.getId())).isEmpty();
    }
}
