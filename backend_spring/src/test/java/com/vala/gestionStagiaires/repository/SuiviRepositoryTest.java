package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
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
import com.vala.gestionStagiaires.repository.projection.ComptagePresence;
import com.vala.gestionStagiaires.repository.projection.DernierRapport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Requêtes du suivi (présences, rapports, historique), sur une vraie base H2. */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Requêtes du suivi")
class SuiviRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PresenceRepository presenceRepository;

    @Autowired
    private RapportHebdomadaireRepository rapportRepository;

    @Autowired
    private StageHistoriqueRepository historiqueRepository;

    private Stagiaire actif;
    private Stagiaire termine;
    private Stage stageEnCours;

    @BeforeEach
    void setUp() {
        Departement dep = em.persist(Departement.builder().nom("Informatique").responsable("Durand").build());
        Encadrant enc = em.persist(Encadrant.builder().nom("Martin").prenom("Paul").email("p@vala.ma").build());
        stageEnCours = em.persist(Stage.builder().sujet("IA").status(StageStatus.EN_COURS)
                .dateDebut(LocalDate.of(2026, 3, 1)).dateFin(LocalDate.of(2026, 6, 30)).encadrant(enc).build());
        Stage stageTermine = em.persist(Stage.builder().sujet("Web").status(StageStatus.TERMINE)
                .dateDebut(LocalDate.of(2025, 9, 1)).dateFin(LocalDate.of(2025, 12, 31)).encadrant(enc).build());
        actif = em.persist(stagiaire("actif@mail.com", stageEnCours, dep));
        termine = em.persist(stagiaire("ancien@mail.com", stageTermine, dep));

        em.persist(presence(actif, LocalDate.of(2026, 3, 2), StatutPresence.PRESENT));
        em.persist(presence(actif, LocalDate.of(2026, 3, 3), StatutPresence.PRESENT));
        em.persist(presence(actif, LocalDate.of(2026, 3, 4), StatutPresence.ABSENT));
        em.persist(presence(termine, LocalDate.of(2025, 9, 2), StatutPresence.PRESENT));

        em.persist(rapport(actif, LocalDate.of(2026, 3, 2)));
        em.persist(rapport(actif, LocalDate.of(2026, 3, 9)));
        em.persist(rapport(termine, LocalDate.of(2025, 9, 1)));

        em.persist(historique(stageEnCours, null, StageStatus.PLANIFIE, Instant.parse("2026-02-01T10:00:00Z")));
        em.persist(historique(stageEnCours, StageStatus.PLANIFIE, StageStatus.EN_COURS, Instant.parse("2026-03-01T00:05:00Z")));
        em.flush();
        em.clear();
    }

    private static Stagiaire stagiaire(String email, Stage stage, Departement dep) {
        return Stagiaire.builder().nom("Nom").prenom("Prenom").email(email).sexe(Sexe.FEMME)
                .stage(stage).departement(dep).build();
    }

    private static Presence presence(Stagiaire s, LocalDate date, StatutPresence statut) {
        return Presence.builder().stagiaire(s).date(date).statut(statut).build();
    }

    private static RapportHebdomadaire rapport(Stagiaire s, LocalDate lundi) {
        return RapportHebdomadaire.builder().stagiaire(s).semaine(lundi).travauxRealises("Travaux")
                .dateDepot(Instant.now()).statut(StatutRapport.SOUMIS).build();
    }

    private static StageHistorique historique(Stage stage, StageStatus ancien, StageStatus nouveau, Instant date) {
        return StageHistorique.builder().stage(stage).ancienStatut(ancien).nouveauStatut(nouveau)
                .dateChangement(date).origine(OrigineChangement.MANUEL).build();
    }

    @Test
    @DisplayName("présences : comptage par statut limité aux stages en cours")
    void comptagePresences() {
        assertThat(presenceRepository.compterParStagiaireEtStatut(StageStatus.EN_COURS)).containsExactlyInAnyOrder(
                new ComptagePresence(actif.getId(), StatutPresence.PRESENT, 2L),
                new ComptagePresence(actif.getId(), StatutPresence.ABSENT, 1L));
    }

    @Test
    @DisplayName("présences : recherche par période, triée par date")
    void presencesParPeriode() {
        assertThat(presenceRepository.findByStagiaireIdAndDateBetweenOrderByDateAsc(
                actif.getId(), LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 31)))
                .extracting(Presence::getDate)
                .containsExactly(LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 4));
    }

    @Test
    @DisplayName("présences : un seul enregistrement par stagiaire et par jour")
    void presenceUnique() {
        Stagiaire ref = em.find(Stagiaire.class, actif.getId());
        assertThatThrownBy(() -> {
            presenceRepository.saveAndFlush(presence(ref, LocalDate.of(2026, 3, 2), StatutPresence.RETARD));
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("rapports : dernière semaine par stagiaire en cours")
    void derniereSemaine() {
        assertThat(rapportRepository.derniereSemaineParStagiaire(StageStatus.EN_COURS))
                .containsExactly(new DernierRapport(actif.getId(), LocalDate.of(2026, 3, 9)));
        assertThat(rapportRepository.existsByStagiaireIdAndSemaine(actif.getId(), LocalDate.of(2026, 3, 2))).isTrue();
    }

    @Test
    @DisplayName("historique : trié par date de changement")
    void historiqueTrie() {
        assertThat(historiqueRepository.findByStageIdOrderByDateChangementAscIdAsc(stageEnCours.getId()))
                .extracting(StageHistorique::getNouveauStatut)
                .containsExactly(StageStatus.PLANIFIE, StageStatus.EN_COURS);
    }

    @Test
    @DisplayName("suppressions en masse par stagiaire et par stage")
    void suppressions() {
        presenceRepository.supprimerParStagiaire(actif.getId());
        rapportRepository.supprimerParStagiaire(actif.getId());
        historiqueRepository.supprimerParStage(stageEnCours.getId());

        assertThat(presenceRepository.count()).isEqualTo(1);    // reste celle de l'ancien stagiaire
        assertThat(rapportRepository.count()).isEqualTo(1);
        assertThat(historiqueRepository.count()).isZero();
    }
}
