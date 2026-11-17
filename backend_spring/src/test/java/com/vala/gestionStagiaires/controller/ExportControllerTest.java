package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.export.ExportCsv;
import com.vala.gestionStagiaires.export.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportController")
class ExportControllerTest {

    @Mock
    private ExportService exportService;

    @InjectMocks
    private ExportController controller;

    private MockMvc mockMvc;

    private final ExportCsv export = new ExportCsv("stagiaires-2026-03-15.csv", "Nom\r\n".getBytes(), 0);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new SortHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /stagiaires/export.csv -> fichier CSV en pièce jointe, filtres et tri transmis")
    void exporterStagiaires() throws Exception {
        when(exportService.exporterStagiaires(any(), any())).thenReturn(export);

        mockMvc.perform(get("/stagiaires/export.csv").param("keyword", "ali").param("status", "EN_COURS")
                        .param("sort", "prenom,desc"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"stagiaires-2026-03-15.csv\""))
                .andExpect(content().bytes("Nom\r\n".getBytes()));

        verify(exportService).exporterStagiaires(new StagiaireFiltre("ali", null, null, StageStatus.EN_COURS, null),
                Sort.by(Sort.Direction.DESC, "prenom"));
    }

    @Test
    @DisplayName("tri par défaut identique à /page (stages : dateDebut décroissante, candidatures : dateSoumission décroissante)")
    void trisParDefaut() throws Exception {
        when(exportService.exporterStages(any(), any())).thenReturn(export);
        when(exportService.exporterCandidatures(any(), any())).thenReturn(export);

        mockMvc.perform(get("/stages/export.csv").param("encadrantId", "3")).andExpect(status().isOk());
        mockMvc.perform(get("/candidatures/export.csv").param("statut", "SOUMISE")).andExpect(status().isOk());

        verify(exportService).exporterStages(new StageFiltre(null, null, 3L), Sort.by(Sort.Direction.DESC, "dateDebut"));
        verify(exportService).exporterCandidatures(new CandidatureFiltre(null, CandidatureStatus.SOUMISE, null),
                Sort.by(Sort.Direction.DESC, "dateSoumission"));
    }

    @Test
    @DisplayName("trop de lignes -> 409 en JSON")
    void tropDeLignes() throws Exception {
        when(exportService.exporterStagiaires(any(), any()))
                .thenThrow(new BusinessRuleException("L'export contiendrait 12000 lignes (maximum 10000) : affinez les filtres."));

        mockMvc.perform(get("/stagiaires/export.csv"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("L'export contiendrait 12000 lignes (maximum 10000) : affinez les filtres."));
    }
}
