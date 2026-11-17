package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.AssiduiteStagiaire;
import com.vala.gestionStagiaires.DTOs.suivi.AssiduiteStatistiques;
import com.vala.gestionStagiaires.DTOs.suivi.RapportEnRetard;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.enums.StatutPresence;
import com.vala.gestionStagiaires.repository.PresenceRepository;
import com.vala.gestionStagiaires.repository.RapportHebdomadaireRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.projection.ComptagePresence;
import com.vala.gestionStagiaires.repository.projection.DernierRapport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuiviStatistiqueServiceImpl")
class SuiviStatistiqueServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 31);

    @Mock
    private StagiaireRepository stagiaireRepository;

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private RapportHebdomadaireRepository rapportRepository;

    private SuiviStatistiqueServiceImpl service;

    private final Departement info = Departement.builder().id(1L).nom("Informatique").build();
    private final Departement rh = Departement.builder().id(2L).nom("RH").build();
    private final Encadrant martin = Encadrant.builder().id(1L).nom("Martin").prenom("Paul").build();

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new SuiviStatistiqueServiceImpl(stagiaireRepository, presenceRepository, rapportRepository, clock);
    }

    private Stagiaire stagiaire(long id, String nom, Departement dep, LocalDate debutStage) {
        Stage stage = Stage.builder().id(id).sujet("Stage " + nom).status(StageStatus.EN_COURS)
                .dateDebut(debutStage).dateFin(debutStage.plusMonths(4)).encadrant(martin).build();
        return Stagiaire.builder().id(id).nom(nom).prenom("P").email(nom + "@mail.com")
                .stage(stage).departement(dep).build();
    }

    @Test
    @DisplayName("assiduite() calcule les taux global, par département et par stagiaire")
    void assiduite() {
        Stagiaire alami = stagiaire(1, "Alami", info, LocalDate.of(2026, 3, 1));
        Stagiaire benali = stagiaire(2, "Benali", info, LocalDate.of(2026, 3, 1));
        Stagiaire chraibi = stagiaire(3, "Chraibi", rh, LocalDate.of(2026, 3, 1)); // rien de saisi
        when(stagiaireRepository.findByStageStatus(StageStatus.EN_COURS)).thenReturn(List.of(alami, benali, chraibi));
        when(presenceRepository.compterParStagiaireEtStatut(StageStatus.EN_COURS)).thenReturn(List.of(
                new ComptagePresence(1L, StatutPresence.PRESENT, 8L),
                new ComptagePresence(1L, StatutPresence.RETARD, 1L),
                new ComptagePresence(1L, StatutPresence.ABSENT, 1L),
                new ComptagePresence(2L, StatutPresence.PRESENT, 3L),
                new ComptagePresence(2L, StatutPresence.ABSENCE_JUSTIFIEE, 1L)));

        AssiduiteStatistiques stats = service.assiduite();

        assertThat(stats.tauxGlobal()).isEqualTo(85.7);            // 12 présents / 14 saisis
        assertThat(stats.parDepartement()).containsExactly(entry("Informatique", 85.7)); // RH sans saisie exclu
        assertThat(stats.stagiaires())
                .extracting(AssiduiteStagiaire::nom, AssiduiteStagiaire::tauxPresence)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Benali", 75.0),
                        org.assertj.core.groups.Tuple.tuple("Alami", 90.0),
                        org.assertj.core.groups.Tuple.tuple("Chraibi", null));
        assertThat(stats.stagiaires().get(1).retards()).isEqualTo(1);
    }

    @Test
    @DisplayName("assiduite() sans aucune saisie : taux null")
    void assiduite_sansSaisie() {
        when(stagiaireRepository.findByStageStatus(StageStatus.EN_COURS)).thenReturn(List.of());
        when(presenceRepository.compterParStagiaireEtStatut(StageStatus.EN_COURS)).thenReturn(List.of());

        AssiduiteStatistiques stats = service.assiduite();

        assertThat(stats.tauxGlobal()).isNull();
        assertThat(stats.parDepartement()).isEmpty();
    }

    @Test
    @DisplayName("rapportsEnRetard() liste les stagiaires sans rapport récent, les plus en retard d'abord")
    void rapportsEnRetard() {
        Stagiaire aJour = stagiaire(1, "AJour", info, LocalDate.of(2026, 1, 5));
        Stagiaire enRetard = stagiaire(2, "EnRetard", info, LocalDate.of(2026, 1, 5));
        Stagiaire jamais = stagiaire(3, "Jamais", rh, LocalDate.of(2026, 1, 5));
        Stagiaire nouveau = stagiaire(4, "Nouveau", rh, LocalDate.of(2026, 3, 25)); // début il y a 6 jours
        when(stagiaireRepository.findByStageStatus(StageStatus.EN_COURS)).thenReturn(List.of(aJour, enRetard, jamais, nouveau));
        when(rapportRepository.derniereSemaineParStagiaire(StageStatus.EN_COURS)).thenReturn(List.of(
                new DernierRapport(1L, LocalDate.of(2026, 3, 23)),
                new DernierRapport(2L, LocalDate.of(2026, 3, 2))));

        List<RapportEnRetard> resultat = service.rapportsEnRetard(14);

        assertThat(resultat).extracting(RapportEnRetard::nom).containsExactly("Jamais", "EnRetard");
        assertThat(resultat.get(0).dernierRapport()).isNull();
        assertThat(resultat.get(0).joursSansRapport()).isEqualTo(85); // depuis le 05/01
        assertThat(resultat.get(1).joursSansRapport()).isEqualTo(29); // depuis le 02/03
        assertThat(resultat.get(1).encadrant()).isEqualTo("Martin Paul");
    }

    @Test
    @DisplayName("pourcentage() arrondit à une décimale et gère le dénominateur nul")
    void pourcentage() {
        assertThat(SuiviStatistiqueServiceImpl.pourcentage(2, 3)).isEqualTo(66.7);
        assertThat(SuiviStatistiqueServiceImpl.pourcentage(0, 0)).isNull();
    }
}
