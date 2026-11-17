package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.config.ReglesMetierProperties;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StageServiceImpl")
class StageServiceImplTest {

    /** « Aujourd'hui » figé pour rendre les règles de date déterministes. */
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);

    @Mock
    private StageRepository stageRepository;

    @Mock
    private EncadrantRepository encadrantRepository;

    @Mock
    private StagiaireRepository stagiaireRepository;

    @Mock
    private StageMapper stageMapper;

    private StageServiceImpl stageService;

    private Clock clock;

    private final Encadrant encadrant = Encadrant.builder().id(1L).nom("Martin").build();

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        stageService = serviceAvecCapacite(0);
    }

    /** Service configuré avec une capacité maximale par encadrant (0 = illimitée). */
    private StageServiceImpl serviceAvecCapacite(int maxStagesEnCours) {
        return new StageServiceImpl(stageRepository, encadrantRepository, stagiaireRepository, stageMapper,
                clock, new ReglesMetierProperties(16, maxStagesEnCours));
    }

    private static EncadrantDto encadrantDto(Long id) {
        EncadrantDto e = new EncadrantDto();
        e.setId(id);
        return e;
    }

    private static StageDto stageDto(Long id, String sujet, StageStatus status) {
        StageDto d = new StageDto();
        d.setId(id);
        d.setSujet(sujet);
        d.setDateDebut(LocalDate.of(2026, 1, 1));
        d.setDateFin(LocalDate.of(2026, 6, 30));
        d.setStatus(status);
        d.setEncadrant(encadrantDto(1L));
        return d;
    }

    private static Stage stageEntity(Long id, String sujet, StageStatus status) {
        return Stage.builder()
                .id(id)
                .sujet(sujet)
                .dateDebut(LocalDate.of(2026, 1, 1))
                .dateFin(LocalDate.of(2026, 6, 30))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("getStages() mappe tous les stages")
    void getStages_shouldReturnMappedList() {
        Stage s1 = stageEntity(1L, "Stage IA", StageStatus.EN_COURS);
        Stage s2 = stageEntity(2L, "Stage Web", StageStatus.EN_COURS);
        StageDto d1 = stageDto(1L, "Stage IA", StageStatus.EN_COURS);
        StageDto d2 = stageDto(2L, "Stage Web", StageStatus.EN_COURS);
        when(stageRepository.findAll()).thenReturn(List.of(s1, s2));
        when(stageMapper.fromStage(s1)).thenReturn(d1);
        when(stageMapper.fromStage(s2)).thenReturn(d2);

        assertThat(stageService.getStages()).containsExactly(d1, d2);
    }

    @Test
    @DisplayName("searchStage() filtre par sujet")
    void searchStage_shouldReturnMappedList() {
        Stage s1 = stageEntity(1L, "Stage IA", StageStatus.EN_COURS);
        StageDto d1 = stageDto(1L, "Stage IA", StageStatus.EN_COURS);
        when(stageRepository.findBySujetIgnoreCaseContaining("ia")).thenReturn(List.of(s1));
        when(stageMapper.fromStage(s1)).thenReturn(d1);

        assertThat(stageService.searchStage("ia")).containsExactly(d1);
    }

    @Nested
    @DisplayName("addStage()")
    class AddStage {

        @Test
        @DisplayName("charge l'encadrant en base, sauvegarde et retourne le DTO")
        void shouldLoadEncadrantAndSave() {
            StageDto input = stageDto(null, "Stage IA", StageStatus.EN_COURS);
            Stage toSave = stageEntity(null, "Stage IA", null);
            Stage saved = stageEntity(1L, "Stage IA", StageStatus.EN_COURS);
            StageDto output = stageDto(1L, "Stage IA", StageStatus.EN_COURS);
            when(stageMapper.fromStageDto(input)).thenReturn(toSave);
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));
            when(stageRepository.save(toSave)).thenReturn(saved);
            when(stageMapper.fromStage(saved)).thenReturn(output);

            assertThat(stageService.addStage(input)).isEqualTo(output);
            assertThat(toSave.getEncadrant()).isSameAs(encadrant);
            assertThat(toSave.getStatus()).isEqualTo(StageStatus.EN_COURS);
        }

        @Test
        @DisplayName("met le statut PLANIFIE par défaut")
        void shouldDefaultToPlanifie() {
            StageDto input = stageDto(null, "Stage IA", null);
            Stage toSave = stageEntity(null, "Stage IA", null);
            when(stageMapper.fromStageDto(input)).thenReturn(toSave);
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));
            when(stageRepository.save(toSave)).thenReturn(toSave);

            stageService.addStage(input);

            assertThat(toSave.getStatus()).isEqualTo(StageStatus.PLANIFIE);
        }

        @Test
        @DisplayName("lève ResourceNotFoundException si l'encadrant n'existe pas")
        void shouldThrowWhenEncadrantNotFound() {
            StageDto input = stageDto(null, "Stage IA", null);
            when(stageMapper.fromStageDto(input)).thenReturn(stageEntity(null, "Stage IA", null));
            when(encadrantRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stageService.addStage(input))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Encadrant");
            verify(stageRepository, never()).save(any());
        }

        @Test
        @DisplayName("refuse un encadrant sans identifiant")
        void shouldRefuseEncadrantWithoutId() {
            StageDto input = stageDto(null, "Stage IA", null);
            input.setEncadrant(encadrantDto(null));
            when(stageMapper.fromStageDto(input)).thenReturn(stageEntity(null, "Stage IA", null));

            assertThatThrownBy(() -> stageService.addStage(input))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("refuse un stage EN_COURS qui n'a pas encore commencé")
        void shouldRefuseEnCoursBeforeStart() {
            StageDto input = stageDto(null, "Stage IA", StageStatus.EN_COURS);
            input.setDateDebut(TODAY.plusDays(1));
            input.setDateFin(TODAY.plusMonths(3));
            Stage toSave = stageEntity(null, "Stage IA", null);
            toSave.setDateDebut(input.getDateDebut());
            toSave.setDateFin(input.getDateFin());
            when(stageMapper.fromStageDto(input)).thenReturn(toSave);
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));

            assertThatThrownBy(() -> stageService.addStage(input))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("date de début");
        }

        @Test
        @DisplayName("refuse un stage TERMINE dont la date de fin n'est pas passée")
        void shouldRefuseTermineBeforeEnd() {
            StageDto input = stageDto(null, "Stage IA", StageStatus.TERMINE);
            Stage toSave = stageEntity(null, "Stage IA", null); // fin le 30/06, après TODAY
            when(stageMapper.fromStageDto(input)).thenReturn(toSave);
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));

            assertThatThrownBy(() -> stageService.addStage(input))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("date de fin");
        }
    }

    @Nested
    @DisplayName("updateStage()")
    class UpdateStage {

        @Test
        @DisplayName("met à jour le stage et recharge l'encadrant")
        void shouldUpdateExistingEntity() {
            StageDto input = stageDto(null, "Nouveau sujet", StageStatus.EN_COURS);
            Stage existing = stageEntity(1L, "Ancien sujet", StageStatus.PLANIFIE);
            StageDto output = stageDto(1L, "Nouveau sujet", StageStatus.EN_COURS);
            when(stageRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));
            when(stageRepository.save(existing)).thenReturn(existing);
            when(stageMapper.fromStage(existing)).thenReturn(output);

            assertThat(stageService.updateStage(input, 1L)).isEqualTo(output);
            assertThat(existing.getSujet()).isEqualTo("Nouveau sujet");
            assertThat(existing.getEncadrant()).isSameAs(encadrant);
            assertThat(existing.getStatus()).isEqualTo(StageStatus.EN_COURS);
        }

        @Test
        @DisplayName("conserve le statut actuel si aucun statut n'est fourni")
        void shouldKeepStatusWhenNull() {
            StageDto input = stageDto(null, "Nouveau sujet", null);
            Stage existing = stageEntity(1L, "Ancien sujet", StageStatus.EN_COURS);
            when(stageRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));
            when(stageRepository.save(existing)).thenReturn(existing);

            stageService.updateStage(input, 1L);

            assertThat(existing.getStatus()).isEqualTo(StageStatus.EN_COURS);
        }

        @Test
        @DisplayName("refuse une transition interdite (TERMINE → EN_COURS)")
        void shouldRefuseForbiddenTransition() {
            StageDto input = stageDto(null, "Sujet", StageStatus.EN_COURS);
            when(stageRepository.findById(1L)).thenReturn(Optional.of(stageEntity(1L, "Sujet", StageStatus.TERMINE)));

            assertThatThrownBy(() -> stageService.updateStage(input, 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("TERMINE");
            verify(stageRepository, never()).save(any());
        }

        @Test
        @DisplayName("lève ResourceNotFoundException si absent")
        void shouldThrowWhenNotFound() {
            StageDto input = stageDto(null, "Nouveau sujet", StageStatus.EN_COURS);
            when(stageRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stageService.updateStage(input, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("deleteStage()")
    class DeleteStage {

        @Test
        @DisplayName("supprime un stage sans stagiaire")
        void shouldDeleteWhenUnused() {
            when(stageRepository.existsById(7L)).thenReturn(true);
            when(stagiaireRepository.existsByStageId(7L)).thenReturn(false);

            stageService.deleteStage(7L);

            verify(stageRepository).deleteById(7L);
        }

        @Test
        @DisplayName("lève ResourceNotFoundException si absent")
        void shouldThrowWhenNotFound() {
            when(stageRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> stageService.deleteStage(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(stageRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("refuse la suppression si des stagiaires y sont affectés")
        void shouldRefuseWhenStagiairesAssigned() {
            when(stageRepository.existsById(7L)).thenReturn(true);
            when(stagiaireRepository.existsByStageId(7L)).thenReturn(true);

            assertThatThrownBy(() -> stageService.deleteStage(7L))
                    .isInstanceOf(BusinessRuleException.class);
            verify(stageRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("changerStatut()")
    class ChangerStatut {

        @ParameterizedTest(name = "{0} → {1} autorisé")
        @CsvSource({"PLANIFIE, EN_COURS", "PLANIFIE, ANNULE", "EN_COURS, TERMINE", "EN_COURS, ANNULE"})
        void shouldAllowValidTransitions(StageStatus depart, StageStatus cible) {
            Stage stage = stageEntity(1L, "Sujet", depart);
            stage.setDateFin(TODAY.minusDays(1)); // permet aussi TERMINE
            when(stageRepository.findById(1L)).thenReturn(Optional.of(stage));
            when(stageRepository.save(stage)).thenReturn(stage);

            stageService.changerStatut(1L, cible);

            assertThat(stage.getStatus()).isEqualTo(cible);
        }

        @ParameterizedTest(name = "{0} → {1} refusé")
        @CsvSource({"PLANIFIE, TERMINE", "EN_COURS, PLANIFIE", "TERMINE, EN_COURS", "TERMINE, ANNULE", "ANNULE, PLANIFIE", "ANNULE, EN_COURS"})
        void shouldRefuseInvalidTransitions(StageStatus depart, StageStatus cible) {
            when(stageRepository.findById(1L)).thenReturn(Optional.of(stageEntity(1L, "Sujet", depart)));

            assertThatThrownBy(() -> stageService.changerStatut(1L, cible))
                    .isInstanceOf(BusinessRuleException.class);
            verify(stageRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("mettreAJourStatutsAutomatiques() fait avancer les stages selon la date du jour")
    void mettreAJourStatutsAutomatiques_shouldAdvanceStatuses() {
        Stage aDemarrer = stageEntity(1L, "Démarre", StageStatus.PLANIFIE);           // fin 30/06 → EN_COURS
        Stage dejaFini = stageEntity(2L, "Fini", StageStatus.PLANIFIE);
        dejaFini.setDateFin(TODAY.minusDays(1));                                       // fin passée → TERMINE
        Stage aTerminer = stageEntity(3L, "Se termine", StageStatus.EN_COURS);
        aTerminer.setDateFin(TODAY.minusDays(1));
        when(stageRepository.findByStatusAndDateDebutLessThanEqual(StageStatus.PLANIFIE, TODAY))
                .thenReturn(List.of(aDemarrer, dejaFini));
        when(stageRepository.findByStatusAndDateFinBefore(StageStatus.EN_COURS, TODAY))
                .thenReturn(List.of(aTerminer));

        int modifies = stageService.mettreAJourStatutsAutomatiques();

        assertThat(modifies).isEqualTo(3);
        assertThat(aDemarrer.getStatus()).isEqualTo(StageStatus.EN_COURS);
        assertThat(dejaFini.getStatus()).isEqualTo(StageStatus.TERMINE);
        assertThat(aTerminer.getStatus()).isEqualTo(StageStatus.TERMINE);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Stage>> captor = ArgumentCaptor.forClass(List.class);
        verify(stageRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(aDemarrer, dejaFini, aTerminer);
    }

    @Test
    @DisplayName("getStageById() retourne le DTO")
    void getStageById_shouldReturnDto() {
        Stage s = stageEntity(1L, "Stage IA", StageStatus.EN_COURS);
        StageDto dto = stageDto(1L, "Stage IA", StageStatus.EN_COURS);
        when(stageRepository.findById(1L)).thenReturn(Optional.of(s));
        when(stageMapper.fromStage(s)).thenReturn(dto);

        assertThat(stageService.getStageById(1L)).isEqualTo(dto);
    }

    @Test
    @DisplayName("getStageById() lève ResourceNotFoundException si absent")
    void getStageById_shouldThrowWhenNotFound() {
        when(stageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stageService.getStageById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Nested
    @DisplayName("Capacité des encadrants")
    class CapaciteEncadrant {

        @Test
        @DisplayName("addStage() refuse un stage EN_COURS si l'encadrant est au maximum")
        void addStage_shouldRefuseWhenEncadrantFull() {
            StageServiceImpl service = serviceAvecCapacite(2);
            StageDto input = stageDto(null, "Stage IA", StageStatus.EN_COURS);
            when(stageMapper.fromStageDto(input)).thenReturn(stageEntity(null, "Stage IA", null));
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));
            when(stageRepository.countByEncadrantIdAndStatus(1L, StageStatus.EN_COURS)).thenReturn(2L);

            assertThatThrownBy(() -> service.addStage(input))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("maximum : 2");
            verify(stageRepository, never()).save(any());
        }

        @Test
        @DisplayName("addStage() accepte un stage EN_COURS sous la limite")
        void addStage_shouldAcceptUnderLimit() {
            StageServiceImpl service = serviceAvecCapacite(2);
            StageDto input = stageDto(null, "Stage IA", StageStatus.EN_COURS);
            Stage toSave = stageEntity(null, "Stage IA", null);
            when(stageMapper.fromStageDto(input)).thenReturn(toSave);
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));
            when(stageRepository.countByEncadrantIdAndStatus(1L, StageStatus.EN_COURS)).thenReturn(1L);
            when(stageRepository.save(toSave)).thenReturn(toSave);

            service.addStage(input);

            verify(stageRepository).save(toSave);
        }

        @Test
        @DisplayName("changerStatut() vers EN_COURS exclut le stage lui-même du décompte")
        void changerStatut_shouldRefuseWhenEncadrantFull() {
            StageServiceImpl service = serviceAvecCapacite(1);
            Stage stage = stageEntity(1L, "Sujet", StageStatus.PLANIFIE);
            stage.setEncadrant(encadrant);
            when(stageRepository.findById(1L)).thenReturn(Optional.of(stage));
            when(stageRepository.countByEncadrantIdAndStatusAndIdNot(1L, StageStatus.EN_COURS, 1L)).thenReturn(1L);

            assertThatThrownBy(() -> service.changerStatut(1L, StageStatus.EN_COURS))
                    .isInstanceOf(BusinessRuleException.class);
            assertThat(stage.getStatus()).isEqualTo(StageStatus.PLANIFIE);
        }

        @Test
        @DisplayName("aucune limite quand la capacité vaut 0")
        void shouldNotCountWhenUnlimited() {
            StageDto input = stageDto(null, "Stage IA", StageStatus.EN_COURS);
            Stage toSave = stageEntity(null, "Stage IA", null);
            when(stageMapper.fromStageDto(input)).thenReturn(toSave);
            when(encadrantRepository.findById(1L)).thenReturn(Optional.of(encadrant));
            when(stageRepository.save(toSave)).thenReturn(toSave);

            stageService.addStage(input);

            verify(stageRepository, never()).countByEncadrantIdAndStatus(any(), any());
        }
    }
}
