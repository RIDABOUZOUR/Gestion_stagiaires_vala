package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Evaluation;
import com.vala.gestionStagiaires.entities.Presence;
import com.vala.gestionStagiaires.entities.RapportHebdomadaire;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.StageHistorique;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.OrigineChangement;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.enums.StatutPresence;
import com.vala.gestionStagiaires.enums.StatutRapport;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.EvaluationRepository;
import com.vala.gestionStagiaires.repository.PresenceRepository;
import com.vala.gestionStagiaires.repository.RapportHebdomadaireRepository;
import com.vala.gestionStagiaires.repository.StageHistoriqueRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Suppressions sur une vraie base (H2) : les données de suivi rattachées disparaissent
 * avec leur parent, sans violation de clé étrangère au flush.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Suppressions en cascade (intégration)")
class SuppressionIntegrationTest {

    @Autowired private StagiaireService stagiaireService;
    @Autowired private StageService stageService;
    @Autowired private EntityManager entityManager;
    @Autowired private DepartementRepository departementRepository;
    @Autowired private EncadrantRepository encadrantRepository;
    @Autowired private StageRepository stageRepository;
    @Autowired private StagiaireRepository stagiaireRepository;
    @Autowired private PresenceRepository presenceRepository;
    @Autowired private RapportHebdomadaireRepository rapportRepository;
    @Autowired private EvaluationRepository evaluationRepository;
    @Autowired private StageHistoriqueRepository historiqueRepository;

    private Stage stage;
    private Stagiaire stagiaire;

    @BeforeEach
    void setUp() {
        Departement dep = departementRepository.save(Departement.builder().nom("Informatique").build());
        Encadrant enc = encadrantRepository.save(Encadrant.builder().nom("Martin").prenom("Paul").email("paul@vala.ma").build());
        stage = stageRepository.save(Stage.builder().sujet("IA").status(StageStatus.EN_COURS)
                .dateDebut(LocalDate.now().minusMonths(1)).dateFin(LocalDate.now().plusMonths(1)).encadrant(enc).build());
        stagiaire = stagiaireRepository.save(Stagiaire.builder().nom("Nom").prenom("Sara").email("sara@mail.com")
                .sexe(Sexe.FEMME).stage(stage).departement(dep).build());

        presenceRepository.save(Presence.builder().stagiaire(stagiaire).date(LocalDate.now())
                .statut(StatutPresence.PRESENT).build());
        rapportRepository.save(RapportHebdomadaire.builder().stagiaire(stagiaire).semaine(LocalDate.now())
                .travauxRealises("Modèle").dateDepot(Instant.now()).statut(StatutRapport.SOUMIS).build());
        evaluationRepository.save(Evaluation.builder().stagiaire(stagiaire).noteGlobale(new BigDecimal("16.0"))
                .competenceTechnique(4).communication(4).autonomie(4).assiduite(4).recommande(true)
                .dateEvaluation(LocalDate.now()).build());
        historiqueRepository.save(StageHistorique.builder().stage(stage).ancienStatut(StageStatus.PLANIFIE)
                .nouveauStatut(StageStatus.EN_COURS).dateChangement(Instant.now()).origine(OrigineChangement.AUTOMATIQUE).build());
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("supprimer un stagiaire efface ses présences, rapports et évaluation")
    void deleteStagiaire_supprimeLeSuivi() {
        stagiaireService.deleteStagiaire(stagiaire.getId());
        entityManager.flush();

        assertThat(stagiaireRepository.existsById(stagiaire.getId())).isFalse();
        assertThat(presenceRepository.count()).isZero();
        assertThat(rapportRepository.count()).isZero();
        assertThat(evaluationRepository.count()).isZero();
    }

    @Test
    @DisplayName("supprimer un stage vide efface son historique ; un stage occupé est refusé")
    void deleteStage_supprimeLHistorique() {
        assertThatThrownBy(() -> stageService.deleteStage(stage.getId())).isInstanceOf(BusinessRuleException.class);

        stagiaireService.deleteStagiaire(stagiaire.getId());
        stageService.deleteStage(stage.getId());
        entityManager.flush();

        assertThat(stageRepository.existsById(stage.getId())).isFalse();
        assertThat(historiqueRepository.count()).isZero();
    }
}
