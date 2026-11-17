package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.service.StagiaireService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("StagiaireController")
class StagiaireControllerTest {

    private static final String VALID_PAYLOAD =
            "{\"nom\":\"Rida\",\"prenom\":\"Bouzour\",\"sexe\":\"HOMME\",\"email\":\"rida@example.com\","
                    + "\"departement\":{\"id\":1},"
                    + "\"stage\":{\"id\":1,\"sujet\":\"Stage\",\"dateDebut\":\"2026-01-01\",\"dateFin\":\"2026-06-30\",\"encadrant\":{\"id\":1}}}";

    @Mock
    private StagiaireService stagiaireService;

    @InjectMocks
    private StagiaireController stagiaireController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(stagiaireController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private StagiaireDto dto(Long id, String nom, String prenom) {
        StagiaireDto d = new StagiaireDto();
        d.setId(id);
        d.setNom(nom);
        d.setPrenom(prenom);
        d.setEmail("stagiaire@example.com");
        d.setSexe(Sexe.HOMME);
        return d;
    }

    @Test
    @DisplayName("GET /stagiaires -> 200 + liste")
    void getAllStagiaires_shouldReturn200() throws Exception {
        when(stagiaireService.getAllStagiares()).thenReturn(List.of(dto(1L, "Rida", "Bouzour")));

        mockMvc.perform(get("/stagiaires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Rida"));
    }

    @Test
    @DisplayName("GET /stagiaires/search -> 200")
    void searchStagiaires_shouldReturn200() throws Exception {
        when(stagiaireService.searchStagiaire("ri")).thenReturn(List.of(dto(1L, "Rida", "Bouzour")));

        mockMvc.perform(get("/stagiaires/search").param("keyword", "ri"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Rida"));
    }

    @Test
    @DisplayName("GET /stagiaires/{id} -> 200")
    void getStagiaire_shouldReturn200() throws Exception {
        when(stagiaireService.getStagiaire(1L)).thenReturn(dto(1L, "Rida", "Bouzour"));

        mockMvc.perform(get("/stagiaires/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /stagiaires/{id} absent -> 404")
    void getStagiaire_notFound_shouldReturn404() throws Exception {
        when(stagiaireService.getStagiaire(99L)).thenThrow(new StagiaireNotFoundException(99L));

        mockMvc.perform(get("/stagiaires/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Stagiaire avec l'identifiant 99 introuvable."));
    }

    @Test
    @DisplayName("POST /stagiaires valide -> 201")
    void saveStagiaire_valid_shouldReturn201() throws Exception {
        when(stagiaireService.addStagiare(any(StagiaireDto.class))).thenReturn(dto(1L, "Rida", "Bouzour"));

        mockMvc.perform(post("/stagiaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Rida"));
    }

    @Test
    @DisplayName("POST /stagiaires sans sexe -> 400")
    void saveStagiaire_invalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/stagiaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Rida\",\"prenom\":\"Bouzour\","
                                + "\"departement\":{\"id\":1},"
                                + "\"stage\":{\"id\":1,\"sujet\":\"Stage\",\"dateDebut\":\"2026-01-01\",\"dateFin\":\"2026-06-30\",\"encadrant\":{\"id\":1}}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sexe").exists());

        verifyNoInteractions(stagiaireService);
    }

    @Test
    @DisplayName("PUT /stagiaires/{id} -> 200")
    void updateStagiaire_shouldReturn200() throws Exception {
        when(stagiaireService.updateStagiaire(any(StagiaireDto.class), anyLong()))
                .thenReturn(dto(1L, "Rida", "Bouzour"));

        mockMvc.perform(put("/stagiaires/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Rida"));
    }

    @Test
    @DisplayName("DELETE /stagiaires/{id} -> 204")
    void deleteStagiaire_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/stagiaires/1"))
                .andExpect(status().isNoContent());

        verify(stagiaireService).deleteStagiaire(1L);
    }

    @Test
    @DisplayName("GET /statistiques/departements -> 200 + map")
    void getStatsParDepartement_shouldReturn200() throws Exception {
        when(stagiaireService.countStagiairesParDepartement()).thenReturn(Map.of("Informatique", 2L));

        mockMvc.perform(get("/statistiques/departements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Informatique").value(2));
    }

    @Test
    @DisplayName("GET /statistiques/filieres -> 200 + map")
    void getStatsParFiliere_shouldReturn200() throws Exception {
        when(stagiaireService.getNombreStagiairesParFiliere()).thenReturn(Map.of("Génie Info", 5L));

        mockMvc.perform(get("/statistiques/filieres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['Génie Info']").value(5));
    }

    @Test
    @DisplayName("GET /statistiques/organismes -> 200 + map")
    void getStatsParOrganisme_shouldReturn200() throws Exception {
        when(stagiaireService.getStagiairesParOrganisme()).thenReturn(Map.of("ENSA", 3L));

        mockMvc.perform(get("/statistiques/organismes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ENSA").value(3));
    }

    @Test
    @DisplayName("GET /statistiques/encadrants -> 200 + map")
    void getStatsParEncadrant_shouldReturn200() throws Exception {
        when(stagiaireService.getStagiairesParEncadrant()).thenReturn(Map.of("Martin Paul", 4L));

        mockMvc.perform(get("/statistiques/encadrants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['Martin Paul']").value(4));
    }
}
