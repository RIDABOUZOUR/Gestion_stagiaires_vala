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
}
