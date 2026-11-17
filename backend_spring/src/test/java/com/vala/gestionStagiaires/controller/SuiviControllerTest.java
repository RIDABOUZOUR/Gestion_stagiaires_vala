package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.suivi.PresenceDto;
import com.vala.gestionStagiaires.DTOs.suivi.PresenceRequest;
import com.vala.gestionStagiaires.DTOs.suivi.RapportDto;
import com.vala.gestionStagiaires.DTOs.suivi.RapportRequest;
import com.vala.gestionStagiaires.enums.StatutPresence;
import com.vala.gestionStagiaires.enums.StatutRapport;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.service.PresenceService;
import com.vala.gestionStagiaires.service.RapportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
@DisplayName("SuiviController")
class SuiviControllerTest {

    @Mock
    private PresenceService presenceService;

    @Mock
    private RapportService rapportService;

    @InjectMocks
    private SuiviController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                // Même sérialisation des dates que Spring Boot (ISO-8601, pas de tableau).
                .setMessageConverters(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json()
                        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()))
                .build();
    }

    private static RapportDto rapport(StatutRapport statut) {
        return new RapportDto(7L, 5L, LocalDate.of(2026, 3, 9), "API", null, null, Instant.EPOCH, statut, null);
    }

    @Test
    @DisplayName("PUT /stagiaires/{id}/presences/{date} -> 200")
    void enregistrerPresence() throws Exception {
        LocalDate jour = LocalDate.of(2026, 3, 10);
        when(presenceService.enregistrer(eq(5L), eq(jour), any(PresenceRequest.class)))
                .thenReturn(new PresenceDto(1L, 5L, jour, StatutPresence.PRESENT, null));

        mockMvc.perform(put("/stagiaires/5/presences/2026-03-10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"PRESENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-03-10"))
                .andExpect(jsonPath("$.statut").value("PRESENT"));
    }

    @Test
    @DisplayName("PUT présence sans statut -> 400")
    void enregistrerPresence_sansStatut() throws Exception {
        mockMvc.perform(put("/stagiaires/5/presences/2026-03-10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commentaire\":\"?\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.statut").exists());
        verifyNoInteractions(presenceService);
    }

    @Test
    @DisplayName("PUT présence avec une date invalide -> 400")
    void enregistrerPresence_dateInvalide() throws Exception {
        mockMvc.perform(put("/stagiaires/5/presences/10-03-2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"PRESENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Paramètre invalide : date."));
    }

    @Test
    @DisplayName("PUT présence hors période -> 409 avec le message métier")
    void enregistrerPresence_horsPeriode() throws Exception {
        when(presenceService.enregistrer(eq(5L), any(LocalDate.class), any(PresenceRequest.class)))
                .thenThrow(new BusinessRuleException("La date est en dehors de la période du stage."));

        mockMvc.perform(put("/stagiaires/5/presences/2026-01-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"ABSENT\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La date est en dehors de la période du stage."));
    }

    @Test
    @DisplayName("GET /stagiaires/{id}/presences?du=&au= transmet la période")
    void listerPresences() throws Exception {
        when(presenceService.lister(5L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31))).thenReturn(List.of());

        mockMvc.perform(get("/stagiaires/5/presences").param("du", "2026-03-01").param("au", "2026-03-31"))
                .andExpect(status().isOk());

        verify(presenceService).lister(5L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
    }

    @Test
    @DisplayName("DELETE /stagiaires/{id}/presences/{date} -> 204")
    void supprimerPresence() throws Exception {
        mockMvc.perform(delete("/stagiaires/5/presences/2026-03-10")).andExpect(status().isNoContent());

        verify(presenceService).supprimer(5L, LocalDate.of(2026, 3, 10));
    }

    @Test
    @DisplayName("POST /stagiaires/{id}/rapports -> 201")
    void deposerRapport() throws Exception {
        when(rapportService.deposer(eq(5L), any(RapportRequest.class))).thenReturn(rapport(StatutRapport.SOUMIS));

        mockMvc.perform(post("/stagiaires/5/rapports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semaine\":\"2026-03-12\",\"travauxRealises\":\"API\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("SOUMIS"));
    }

    @Test
    @DisplayName("POST rapport sans travaux réalisés -> 400")
    void deposerRapport_invalide() throws Exception {
        mockMvc.perform(post("/stagiaires/5/rapports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semaine\":\"2026-03-12\",\"travauxRealises\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.travauxRealises").exists());
        verifyNoInteractions(rapportService);
    }

    @Test
    @DisplayName("PATCH /rapports/{id}/valider sans corps -> 200, commentaire null")
    void validerRapport_sansCorps() throws Exception {
        when(rapportService.valider(eq(7L), isNull())).thenReturn(rapport(StatutRapport.VALIDE));

        mockMvc.perform(patch("/rapports/7/valider"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDE"));
    }

    @Test
    @DisplayName("PATCH /rapports/{id}/a-revoir sans commentaire -> 400")
    void demanderRevision_sansCommentaire() throws Exception {
        mockMvc.perform(patch("/rapports/7/a-revoir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commentaire\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.commentaire").exists());
        verifyNoInteractions(rapportService);
    }

    @Test
    @DisplayName("PATCH /rapports/{id}/a-revoir -> 200")
    void demanderRevision() throws Exception {
        when(rapportService.demanderRevision(7L, "Détailler")).thenReturn(rapport(StatutRapport.A_REVOIR));

        mockMvc.perform(patch("/rapports/7/a-revoir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commentaire\":\"Détailler\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("A_REVOIR"));
    }

    @Test
    @DisplayName("GET /stagiaires/{id}/rapports et GET /rapports/{id} -> 200")
    void lectureRapports() throws Exception {
        when(rapportService.lister(5L)).thenReturn(List.of(rapport(StatutRapport.SOUMIS)));
        when(rapportService.getRapport(7L)).thenReturn(rapport(StatutRapport.SOUMIS));

        mockMvc.perform(get("/stagiaires/5/rapports")).andExpect(jsonPath("$[0].id").value(7));
        mockMvc.perform(get("/rapports/7")).andExpect(jsonPath("$.semaine").value("2026-03-09"));
    }
}
