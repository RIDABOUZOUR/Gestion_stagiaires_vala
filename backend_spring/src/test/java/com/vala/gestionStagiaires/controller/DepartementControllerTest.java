package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.service.DepartementService;
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
@DisplayName("DepartementController")
class DepartementControllerTest {

    @Mock
    private DepartementService departementService;

    @InjectMocks
    private DepartementController departementController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(departementController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private DepartementDto dto(Long id, String nom, String responsable) {
        DepartementDto d = new DepartementDto();
        d.setId(id);
        d.setNom(nom);
        d.setResponsable(responsable);
        return d;
    }

    @Test
    @DisplayName("GET /departements -> 200 + liste")
    void getDepartements_shouldReturn200() throws Exception {
        when(departementService.getDepartement())
                .thenReturn(List.of(dto(1L, "Informatique", "M. Dupont")));

        mockMvc.perform(get("/departements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Informatique"));
    }

    @Test
    @DisplayName("GET /departements/search -> 200")
    void searchDepartements_shouldReturn200() throws Exception {
        when(departementService.searchDepartement("info"))
                .thenReturn(List.of(dto(1L, "Informatique", "M. Dupont")));

        mockMvc.perform(get("/departements/search").param("keyword", "info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Informatique"));
    }

    @Test
    @DisplayName("POST /departements valide -> 201")
    void addDepartement_valid_shouldReturn201() throws Exception {
        when(departementService.addDepartement(any(DepartementDto.class)))
                .thenReturn(dto(1L, "Informatique", "M. Dupont"));

        mockMvc.perform(post("/departements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Informatique\",\"responsable\":\"M. Dupont\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Informatique"));
    }

    @Test
    @DisplayName("POST /departements invalide -> 400 + détail des champs")
    void addDepartement_invalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/departements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"AB\",\"responsable\":\"X\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nom").exists())
                .andExpect(jsonPath("$.errors.responsable").exists());

        verifyNoInteractions(departementService);
    }

    @Test
    @DisplayName("GET /departements/{id} -> 200")
    void getDepartementById_shouldReturn200() throws Exception {
        when(departementService.getDepartementById(1L)).thenReturn(dto(1L, "Informatique", "M. Dupont"));

        mockMvc.perform(get("/departements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Informatique"));
    }

    @Test
    @DisplayName("GET /departements/{id} absent -> 404")
    void getDepartementById_notFound_shouldReturn404() throws Exception {
        when(departementService.getDepartementById(99L))
                .thenThrow(new ResourceNotFoundException("Département", 99L));

        mockMvc.perform(get("/departements/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Département avec l'identifiant 99 introuvable."));
    }

    @Test
    @DisplayName("PUT /departements/{id} -> 200")
    void updateDepartement_shouldReturn200() throws Exception {
        when(departementService.updateDepartement(any(DepartementDto.class), anyLong()))
                .thenReturn(dto(1L, "Informatique", "Mme Durand"));

        mockMvc.perform(put("/departements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Informatique\",\"responsable\":\"Mme Durand\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsable").value("Mme Durand"));
    }

    @Test
    @DisplayName("DELETE /departements/{id} -> 204")
    void deleteDepartement_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/departements/1"))
                .andExpect(status().isNoContent());

        verify(departementService).deleteDepartement(1L);
    }
}
