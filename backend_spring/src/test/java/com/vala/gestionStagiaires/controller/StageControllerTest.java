package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.service.StageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("StageController")
class StageControllerTest {

    @Mock
    private StageService stageService;

    @InjectMocks
    private StageController stageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(stageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private StageDto dto(Long id, String sujet) {
        StageDto d = new StageDto();
        d.setId(id);
        d.setSujet(sujet);
        d.setDateDebut(LocalDate.of(2026, 1, 1));
        d.setDateFin(LocalDate.of(2026, 6, 30));
        d.setStatus(StageStatus.EN_COURS);
        EncadrantDto e = new EncadrantDto();
        e.setId(1L);
        d.setEncadrant(e);
        return d;
    }

    @Test
    @DisplayName("GET /stages -> 200 + liste")
    void getStages_shouldReturn200() throws Exception {
        when(stageService.getStages()).thenReturn(List.of(dto(1L, "Stage IA")));

        mockMvc.perform(get("/stages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sujet").value("Stage IA"));
    }

    @Test
    @DisplayName("GET /stages/search -> 200")
    void searchStages_shouldReturn200() throws Exception {
        when(stageService.searchStage("ia")).thenReturn(List.of(dto(1L, "Stage IA")));

        mockMvc.perform(get("/stages/search").param("keyword", "ia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sujet").value("Stage IA"));
    }

    @Test
    @DisplayName("POST /stages valide -> 201")
    void addStage_valid_shouldReturn201() throws Exception {
        when(stageService.addStage(any(StageDto.class))).thenReturn(dto(1L, "Stage IA"));

        mockMvc.perform(post("/stages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sujet\":\"Stage IA\",\"dateDebut\":\"2026-01-01\",\"dateFin\":\"2026-06-30\",\"status\":\"EN_COURS\",\"encadrant\":{\"id\":1}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sujet").value("Stage IA"));
    }

    @Test
    @DisplayName("POST /stages dates incohérentes -> 400")
    void addStage_invalidDates_shouldReturn400() throws Exception {
        mockMvc.perform(post("/stages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sujet\":\"Stage\",\"dateDebut\":\"2026-06-30\",\"dateFin\":\"2026-01-01\",\"encadrant\":{\"id\":1}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dateDebutBeforeDateFin").exists());

        verifyNoInteractions(stageService);
    }

    @Test
    @DisplayName("POST /stages sans encadrant -> 400")
    void addStage_missingEncadrant_shouldReturn400() throws Exception {
        mockMvc.perform(post("/stages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sujet\":\"Stage\",\"dateDebut\":\"2026-01-01\",\"dateFin\":\"2026-06-30\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.encadrant").exists());

        verifyNoInteractions(stageService);
    }

    @Test
    @DisplayName("GET /stages/{id} -> 200")
    void getStageById_shouldReturn200() throws Exception {
        when(stageService.getStageById(1L)).thenReturn(dto(1L, "Stage IA"));

        mockMvc.perform(get("/stages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /stages/{id} absent -> 404")
    void getStageById_notFound_shouldReturn404() throws Exception {
        when(stageService.getStageById(99L)).thenThrow(new ResourceNotFoundException("Stage", 99L));

        mockMvc.perform(get("/stages/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /stages/{id} -> 200")
    void updateStage_shouldReturn200() throws Exception {
        when(stageService.updateStage(any(StageDto.class), anyLong())).thenReturn(dto(1L, "Nouveau sujet"));

        mockMvc.perform(put("/stages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sujet\":\"Nouveau sujet\",\"dateDebut\":\"2026-01-01\",\"dateFin\":\"2026-06-30\",\"status\":\"EN_COURS\",\"encadrant\":{\"id\":1}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sujet").value("Nouveau sujet"));
    }

    @Test
    @DisplayName("DELETE /stages/{id} -> 204")
    void deleteStage_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/stages/1"))
                .andExpect(status().isNoContent());

        verify(stageService).deleteStage(1L);
    }

    @Test
    @DisplayName("PATCH /stages/{id}/demarrer -> 200 + statut EN_COURS")
    void demarrerStage_shouldReturn200() throws Exception {
        when(stageService.changerStatut(1L, StageStatus.EN_COURS)).thenReturn(dto(1L, "Stage IA"));

        mockMvc.perform(patch("/stages/1/demarrer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_COURS"));
    }

    @Test
    @DisplayName("PATCH /stages/{id}/terminer -> délègue avec TERMINE")
    void terminerStage_shouldDelegate() throws Exception {
        when(stageService.changerStatut(1L, StageStatus.TERMINE)).thenReturn(dto(1L, "Stage IA"));

        mockMvc.perform(patch("/stages/1/terminer")).andExpect(status().isOk());

        verify(stageService).changerStatut(1L, StageStatus.TERMINE);
    }

    @Test
    @DisplayName("PATCH /stages/{id}/annuler transition interdite -> 409 + message")
    void annulerStage_forbidden_shouldReturn409() throws Exception {
        when(stageService.changerStatut(1L, StageStatus.ANNULE))
                .thenThrow(new BusinessRuleException("Transition de statut interdite : TERMINE → ANNULE."));

        mockMvc.perform(patch("/stages/1/annuler"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Transition de statut interdite : TERMINE → ANNULE."));
    }

    @Test
    @DisplayName("POST /stages sujet vide -> 400")
    void addStage_blankSujet_shouldReturn400() throws Exception {
        mockMvc.perform(post("/stages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sujet\":\"  \",\"dateDebut\":\"2026-01-01\",\"dateFin\":\"2026-06-30\",\"encadrant\":{\"id\":1}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sujet").exists());
        verifyNoInteractions(stageService);
    }
}
