package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.ResumeStatistiques;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.projection.ComptageParEncadrant;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatut;
import com.vala.gestionStagiaires.repository.projection.ProfilLigne;
import com.vala.gestionStagiaires.repository.projection.StageDepartement;
import com.vala.gestionStagiaires.repository.projection.StageDuree;
import com.vala.gestionStagiaires.DTOs.stats.DureesEtTaux;
import com.vala.gestionStagiaires.DTOs.stats.IndicateursStages;
import com.vala.gestionStagiaires.DTOs.stats.ProfilStagiaires;
import com.vala.gestionStagiaires.enums.Sexe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatistiqueServiceImpl")
class StatistiqueServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);

    @Mock
    private StagiaireRepository stagiaireRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private EncadrantRepository encadrantRepository;

    @Mock
    private DepartementRepository departementRepository;

    @Mock
    private StageMapper stageMapper;

    private StatistiqueServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new StatistiqueServiceImpl(stagiaireRepository, stageRepository, encadrantRepository,
                departementRepository, stageMapper, clock);
    }

    @Test
    @DisplayName("les répartitions sont triées par effectif décroissant puis par libellé")
    void repartitionTriee() {
        when(departementRepository.compterStagiairesParDepartement()).thenReturn(List.of(
                new ComptageParLibelle("RH", 1L),
                new ComptageParLibelle("Informatique", 5L),
                new ComptageParLibelle("Finance", 1L),
                new ComptageParLibelle("Marketing", 0L)));

        assertThat(service.stagiairesParDepartement()).containsExactly(
                entry("Informatique", 5L), entry("Finance", 1L), entry("RH", 1L), entry("Marketing", 0L));
    }

    @Test
    @DisplayName("les valeurs nulles ou vides sont regroupées sous « Non renseigné »")
    void valeursNonRenseignees() {
        when(stagiaireRepository.compterParFiliere()).thenReturn(List.of(
                new ComptageParLibelle("Génie Info", 4L),
                new ComptageParLibelle(null, 2L),
                new ComptageParLibelle("  ", 1L),
                new ComptageParLibelle("Génie Info ", 1L)));

        assertThat(service.stagiairesParFiliere()).containsExactly(
                entry("Génie Info", 5L), entry(StatistiqueService.NON_RENSEIGNE, 3L));
    }

    @Test
    @DisplayName("stagiairesParOrganisme() délègue et normalise")
    void parOrganisme() {
        when(stagiaireRepository.compterParOrganisme()).thenReturn(List.of(new ComptageParLibelle("ENSA", 3L)));

        assertThat(service.stagiairesParOrganisme()).containsExactly(entry("ENSA", 3L));
    }

    @Test
    @DisplayName("deux encadrants homonymes ne sont pas fusionnés")
    void encadrantsHomonymes() {
        when(encadrantRepository.compterStagiairesParEncadrant()).thenReturn(List.of(
                new ComptageParEncadrant(1L, "Martin", "Paul", 3L),
                new ComptageParEncadrant(2L, "Martin", "Paul", 2L),
                new ComptageParEncadrant(3L, "Bernard", "Lucie", 1L)));

        assertThat(service.stagiairesParEncadrant()).containsExactly(
                entry("Martin Paul (#1)", 3L), entry("Martin Paul (#2)", 2L), entry("Bernard Lucie", 1L));
    }

    @Test
    @DisplayName("stagesParStatut() renvoie tous les statuts, à 0 si absents")
    void stagesParStatut() {
        when(stageRepository.compterParStatut()).thenReturn(List.of(
                new ComptageParStatut(StageStatus.EN_COURS, 4L),
                new ComptageParStatut(StageStatus.TERMINE, 2L)));

        assertThat(service.stagesParStatut()).containsOnly(
                entry(StageStatus.EN_COURS, 4L), entry(StageStatus.TERMINE, 2L),
                entry(StageStatus.PLANIFIE, 0L), entry(StageStatus.ANNULE, 0L));
    }

    @Test
    @DisplayName("stagesAEcheance() cherche les stages en cours qui finissent dans l'horizon")
    void stagesAEcheance() {
        Stage stage = Stage.builder().id(1L).build();
        StageDto dto = new StageDto();
        when(stageRepository.findByStatusAndDateFinBetweenOrderByDateFinAsc(
                StageStatus.EN_COURS, TODAY, TODAY.plusDays(30))).thenReturn(List.of(stage));
        when(stageMapper.fromStage(stage)).thenReturn(dto);

        assertThat(service.stagesAEcheance(30)).containsExactly(dto);
    }

    @ParameterizedTest(name = "jours={0} → horizon de {1} jour(s)")
    @CsvSource({"0, 1", "-5, 1", "500, 365", "7, 7"})
    void stagesAEcheance_bornesHorizon(int jours, int horizonAttendu) {
        service.stagesAEcheance(jours);

        verify(stageRepository).findByStatusAndDateFinBetweenOrderByDateFinAsc(
                StageStatus.EN_COURS, TODAY, TODAY.plusDays(horizonAttendu));
    }

    @Test
    @DisplayName("resume() agrège les indicateurs clés")
    void resume() {
        when(stagiaireRepository.count()).thenReturn(12L);
        when(stagiaireRepository.countByStageStatus(StageStatus.EN_COURS)).thenReturn(7L);
        when(stageRepository.count()).thenReturn(6L);
        when(stageRepository.countByStatus(StageStatus.EN_COURS)).thenReturn(3L);
        when(stageRepository.countByStatus(StageStatus.PLANIFIE)).thenReturn(2L);
        when(stageRepository.countByStatusAndDateFinBetween(StageStatus.EN_COURS, TODAY, TODAY.plusDays(30)))
                .thenReturn(1L);
        when(encadrantRepository.count()).thenReturn(4L);
        when(departementRepository.count()).thenReturn(3L);

        assertThat(service.resume()).isEqualTo(new ResumeStatistiques(12, 7, 6, 3, 2, 1, 30, 4, 3));
    }

    private static StageDuree stage(long id, StageStatus status, LocalDate debut, LocalDate fin, long encadrantId, String nom) {
        return new StageDuree(id, status, debut, fin, encadrantId, nom, "Paul");
    }

    @Test
    @DisplayName("dureesEtTaux() : durée moyenne des stages terminés, taux d'achèvement et d'annulation")
    void dureesEtTaux() {
        when(stageRepository.donneesDurees()).thenReturn(List.of(
                stage(1, StageStatus.TERMINE, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 1, "Martin"), // 31 j
                stage(2, StageStatus.TERMINE, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 10), 1, "Martin"), // 10 j
                stage(3, StageStatus.ANNULE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 2, "Martin"),
                stage(4, StageStatus.EN_COURS, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, "Bernard")));
        when(stagiaireRepository.departementsDesStages()).thenReturn(List.of(
                new StageDepartement(1L, "Informatique"),
                new StageDepartement(2L, "Informatique"),
                new StageDepartement(2L, "RH"),           // stage partagé entre deux départements
                new StageDepartement(4L, null)));         // stagiaire sans département

        DureesEtTaux d = service.dureesEtTaux();

        assertThat(d.global()).isEqualTo(new IndicateursStages(4, 2, 1, 20.5, 66.7, 25.0));
        assertThat(d.parEncadrant()).containsOnlyKeys("Martin Paul (#1)", "Martin Paul (#2)", "Bernard Paul");
        assertThat(d.parEncadrant().keySet()).first().isEqualTo("Martin Paul (#1)");
        assertThat(d.parEncadrant().get("Martin Paul (#1)")).isEqualTo(new IndicateursStages(2, 2, 0, 20.5, 100.0, 0.0));
        assertThat(d.parEncadrant().get("Bernard Paul").dureeMoyenneJours()).isNull();
        assertThat(d.parDepartement().keySet())
                .containsExactly("Informatique", "Non renseigné", "RH", StatistiqueServiceImpl.SANS_STAGIAIRE);
        assertThat(d.parDepartement().get("RH")).isEqualTo(new IndicateursStages(1, 1, 0, 10.0, 100.0, 0.0));
        assertThat(d.parDepartement().get(StatistiqueServiceImpl.SANS_STAGIAIRE).stagesAnnules()).isEqualTo(1);
    }

    @Test
    @DisplayName("dureesEtTaux() sans stage clôturé : taux nuls")
    void dureesEtTaux_sansStageCloture() {
        when(stageRepository.donneesDurees()).thenReturn(List.of(
                stage(1, StageStatus.EN_COURS, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 1, "Martin")));
        when(stagiaireRepository.departementsDesStages()).thenReturn(List.of());

        IndicateursStages global = service.dureesEtTaux().global();

        assertThat(global.tauxAchevement()).isNull();
        assertThat(global.dureeMoyenneJours()).isNull();
        assertThat(global.tauxAnnulation()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("profil() : répartitions et croisements par département, triés par effectif")
    void profil() {
        when(stagiaireRepository.profils(null)).thenReturn(List.of(
                new ProfilLigne("Informatique", Sexe.FEMME, "Bac+5", "Génie Info"),
                new ProfilLigne("Informatique", Sexe.HOMME, "Bac+5", "Génie Info"),
                new ProfilLigne("Informatique", Sexe.FEMME, "Bac+3", "Réseaux"),
                new ProfilLigne("RH", Sexe.FEMME, null, " ")));

        ProfilStagiaires p = service.profil(null);

        assertThat(p.total()).isEqualTo(4);
        assertThat(p.parSexe()).containsExactly(entry("FEMME", 3L), entry("HOMME", 1L));
        assertThat(p.parNiveau()).containsExactly(entry("Bac+5", 2L), entry("Bac+3", 1L), entry("Non renseigné", 1L));
        assertThat(p.parFiliere()).containsEntry("Non renseigné", 1L).containsEntry("Génie Info", 2L);
        assertThat(p.parDepartementEtSexe().keySet()).containsExactly("Informatique", "RH");
        assertThat(p.parDepartementEtSexe().get("Informatique")).containsExactly(entry("FEMME", 2L), entry("HOMME", 1L));
        assertThat(p.parDepartementEtNiveau().get("RH")).containsExactly(entry("Non renseigné", 1L));
    }
}
