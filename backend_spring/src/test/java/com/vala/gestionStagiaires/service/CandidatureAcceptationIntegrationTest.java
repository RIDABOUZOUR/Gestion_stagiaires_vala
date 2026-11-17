package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.candidature.AcceptationRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureDto;
import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.repository.CandidatureRepository;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.StageHistoriqueRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Acceptation d'une candidature de bout en bout (vrais services, base H2) : le
 * stagiaire est créé avec les règles habituelles, et un refus de ces règles
 * annule toute l'opération. Volontairement non transactionnel, pour observer
 * l'état réellement enregistré.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Acceptation d'une candidature (intégration)")
class CandidatureAcceptationIntegrationTest {

    @Autowired private CandidatureService candidatureService;
    @Autowired private CandidatureRepository candidatureRepository;
    @Autowired private StagiaireRepository stagiaireRepository;
    @Autowired private StageRepository stageRepository;
    @Autowired private StageHistoriqueRepository historiqueRepository;
    @Autowired private EncadrantRepository encadrantRepository;
    @Autowired private DepartementRepository departementRepository;

    private Departement departement;
    private Stage stagePlanifie;
    private Stage stageTermine;

    @BeforeEach
    void setUp() {
        departement = departementRepository.save(Departement.builder().nom("Informatique").responsable("Durand").build());
        Encadrant encadrant = encadrantRepository.save(Encadrant.builder().nom("Martin").prenom("Paul").email("p@vala.ma").build());
        stagePlanifie = stageRepository.save(Stage.builder().sujet("IA").status(StageStatus.PLANIFIE)
                .dateDebut(LocalDate.now().plusMonths(1)).dateFin(LocalDate.now().plusMonths(5)).encadrant(encadrant).build());
        stageTermine = stageRepository.save(Stage.builder().sujet("Web").status(StageStatus.TERMINE)
                .dateDebut(LocalDate.now().minusMonths(6)).dateFin(LocalDate.now().minusMonths(2)).encadrant(encadrant).build());
    }

    @AfterEach
    void nettoyer() {
        candidatureRepository.deleteAll();
        stagiaireRepository.deleteAll();
        historiqueRepository.deleteAll();
        stageRepository.deleteAll();
        encadrantRepository.deleteAll();
        departementRepository.deleteAll();
    }

    private Candidature candidatureEnEtude() {
        return candidatureRepository.save(Candidature.builder().nom("Alami").prenom("Sara").email("sara@mail.com")
                .sexe(Sexe.FEMME).dateNaissance(LocalDate.of(2003, 5, 14)).organisme("ENSA").filiere("Génie Info")
                .departementSouhaite(departement).statut(CandidatureStatus.EN_ETUDE)
                .dateSoumission(Instant.now().minusSeconds(3600)).build());
    }

    @Test
    @DisplayName("crée le stagiaire, l'affecte au stage et lie la candidature")
    void acceptation() {
        Candidature candidature = candidatureEnEtude();

        CandidatureDto dto = candidatureService.accepter(candidature.getId(), new AcceptationRequest(stagePlanifie.getId(), null));

        assertThat(dto.statut()).isEqualTo(CandidatureStatus.ACCEPTEE);
        assertThat(dto.stagiaireId()).isNotNull();
        Stagiaire stagiaire = stagiaireRepository.findById(dto.stagiaireId()).orElseThrow();
        assertThat(stagiaire.getEmail()).isEqualTo("sara@mail.com");
        assertThat(stagiaire.getFiliere()).isEqualTo("Génie Info");
        assertThat(stageRepository.findById(stagePlanifie.getId())).isPresent();
        assertThat(stagiaireRepository.existsByStageId(stagePlanifie.getId())).isTrue();
    }

    @Test
    @DisplayName("si la création du stagiaire est refusée, rien n'est enregistré")
    void acceptationRefusee() {
        Candidature candidature = candidatureEnEtude();

        assertThatThrownBy(() -> candidatureService.accepter(candidature.getId(),
                new AcceptationRequest(stageTermine.getId(), null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("TERMINE");

        Candidature relue = candidatureRepository.findById(candidature.getId()).orElseThrow();
        assertThat(relue.getStatut()).isEqualTo(CandidatureStatus.EN_ETUDE);
        assertThat(relue.getDateDecision()).isNull();
        assertThat(stagiaireRepository.count()).isZero();
    }
}
