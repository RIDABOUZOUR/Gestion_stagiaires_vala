package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.Mappers.EncadrantMapper;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.StageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StageServiceImpl")
class StageServiceImplTest {

    @Mock
    private StageRepository stageRepository;

    @Mock
    private StageMapper stageMapper;

    @Mock
    private EncadrantMapper encadrantMapper;

    @InjectMocks
    private StageServiceImpl stageService;

    private StageDto stageDto(Long id, String sujet, EncadrantDto encadrant) {
        StageDto d = new StageDto();
        d.setId(id);
        d.setSujet(sujet);
        d.setDateDebut(LocalDate.of(2026, 1, 1));
        d.setDateFin(LocalDate.of(2026, 6, 30));
        d.setStatus(StageStatus.EN_COURS);
        d.setEncadrant(encadrant);
        return d;
    }

    private Stage stageEntity(Long id, String sujet, Encadrant encadrant) {
        return Stage.builder()
                .id(id)
                .sujet(sujet)
                .dateDebut(LocalDate.of(2026, 1, 1))
                .dateFin(LocalDate.of(2026, 6, 30))
                .status(StageStatus.EN_COURS)
                .encadrant(encadrant)
                .build();
    }

    @Test
    @DisplayName("getStages() mappe tous les stages")
    void getStages_shouldReturnMappedList() {
        Encadrant e = Encadrant.builder().id(1L).nom("Martin").build();
        Stage s1 = stageEntity(1L, "Stage IA", e);
        Stage s2 = stageEntity(2L, "Stage Web", e);
        StageDto d1 = stageDto(1L, "Stage IA", new EncadrantDto());
        StageDto d2 = stageDto(2L, "Stage Web", new EncadrantDto());
        when(stageRepository.findAll()).thenReturn(List.of(s1, s2));
        when(stageMapper.fromStage(s1)).thenReturn(d1);
        when(stageMapper.fromStage(s2)).thenReturn(d2);

        assertThat(stageService.getStages()).containsExactly(d1, d2);
    }

    @Test
    @DisplayName("addStage() mappe, sauvegarde et retourne le DTO")
    void addStage_shouldSaveAndReturnDto() {
        EncadrantDto encDto = new EncadrantDto();
        encDto.setId(1L);
        StageDto input = stageDto(null, "Stage IA", encDto);
        Encadrant encEntity = Encadrant.builder().id(1L).build();
        Stage toSave = stageEntity(null, "Stage IA", encEntity);
        Stage saved = stageEntity(1L, "Stage IA", encEntity);
        StageDto output = stageDto(1L, "Stage IA", encDto);
        when(stageMapper.fromStageDto(input)).thenReturn(toSave);
        when(stageRepository.save(toSave)).thenReturn(saved);
        when(stageMapper.fromStage(saved)).thenReturn(output);

        assertThat(stageService.addStage(input)).isEqualTo(output);
        verify(stageRepository).save(toSave);
    }

    @Test
    @DisplayName("searchStage() filtre par sujet")
    void searchStage_shouldReturnMappedList() {
        Encadrant e = Encadrant.builder().id(1L).build();
        Stage s1 = stageEntity(1L, "Stage IA", e);
        StageDto d1 = stageDto(1L, "Stage IA", new EncadrantDto());
        when(stageRepository.findBySujetIgnoreCaseContaining("ia")).thenReturn(List.of(s1));
        when(stageMapper.fromStage(s1)).thenReturn(d1);

        assertThat(stageService.searchStage("ia")).containsExactly(d1);
    }

    @Test
    @DisplayName("deleteStage() délègue au repository")
    void deleteStage_shouldDelegateToRepository() {
        stageService.deleteStage(7L);

        verify(stageRepository).deleteById(7L);
    }

    @Test
    @DisplayName("updateStage() met à jour le stage et son encadrant")
    void updateStage_shouldUpdateExistingEntity() {
        EncadrantDto encDto = new EncadrantDto();
        encDto.setId(1L);
        StageDto input = stageDto(null, "Nouveau sujet", encDto);
        Encadrant encEntity = Encadrant.builder().id(1L).build();
        Stage existing = stageEntity(1L, "Ancien sujet", Encadrant.builder().id(2L).build());
        Stage saved = stageEntity(1L, "Nouveau sujet", encEntity);
        StageDto output = stageDto(1L, "Nouveau sujet", encDto);
        when(stageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(encadrantMapper.fromEncadrantDto(encDto)).thenReturn(encEntity);
        when(stageRepository.save(existing)).thenReturn(saved);
        when(stageMapper.fromStage(saved)).thenReturn(output);

        assertThat(stageService.updateStage(input, 1L)).isEqualTo(output);
        assertThat(existing.getSujet()).isEqualTo("Nouveau sujet");
        assertThat(existing.getEncadrant()).isEqualTo(encEntity);
        assertThat(existing.getDateDebut()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("updateStage() lève ResourceNotFoundException si absent")
    void updateStage_shouldThrowWhenNotFound() {
        EncadrantDto encDto = new EncadrantDto();
        encDto.setId(1L);
        StageDto input = stageDto(null, "Nouveau sujet", encDto);
        when(stageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stageService.updateStage(input, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getStageById() retourne le DTO")
    void getStageById_shouldReturnDto() {
        Stage s = stageEntity(1L, "Stage IA", Encadrant.builder().id(1L).build());
        StageDto dto = stageDto(1L, "Stage IA", new EncadrantDto());
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
}
