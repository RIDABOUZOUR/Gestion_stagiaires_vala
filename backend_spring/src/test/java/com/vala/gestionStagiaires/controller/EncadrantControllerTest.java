package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.service.EncadrantService;
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
@DisplayName("EncadrantController")
class EncadrantControllerTest {

    @Mock
    private EncadrantService encadrantService;

    @InjectMocks
    private EncadrantController encadrantController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(encadrantController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private EncadrantDto dto(Long id, String nom, String prenom, String email, String telephone) {
        EncadrantDto d = new EncadrantDto();
        d.setId(id);
        d.setNom(nom);
        d.setPrenom(prenom);
        d.setEmail(email);
        d.setTelephone(telephone);
        return d;
    }

    @Test
    @DisplayName("GET /encadrants -> 200 + liste")
    void getEncadrants_shouldReturn200() throws Exception {
        when(encadrantService.getEncadrant())
                .thenReturn(List.of(dto(1L, "Martin", "Paul", "paul@test.com", "0612345678")));

        mockMvc.perform(get("/encadrants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Martin"));
    }

    @Test
    @DisplayName("GET /encadrants/search -> 200")
    void searchEncadrants_shouldReturn200() throws Exception {
        when(encadrantService.searchEncadrant("mar"))
                .thenReturn(List.of(dto(1L, "Martin", "Paul", "paul@test.com", "0612345678")));

        mockMvc.perform(get("/encadrants/search").param("keyword", "mar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Martin"));
    }

    @Test
    @DisplayName("POST /encadrants valide -> 201")
    void addEncadrant_valid_shouldReturn201() throws Exception {
        when(encadrantService.addEncadrant(any(EncadrantDto.class)))
                .thenReturn(dto(1L, "Martin", "Paul", "paul@test.com", "0612345678"));

        mockMvc.perform(post("/encadrants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Martin\",\"prenom\":\"Paul\",\"email\":\"paul@test.com\",\"telephone\":\"0612345678\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("paul@test.com"));
    }

    @Test
    @DisplayName("POST /encadrants invalide -> 400 + détail des champs")
    void addEncadrant_invalid_shouldReturn400() throws Exception {
        mockMvc.perform(post("/encadrants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Ma\",\"prenom\":\"Paul\",\"email\":\"pas-un-email\",\"telephone\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nom").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.telephone").exists());

        verifyNoInteractions(encadrantService);
    }

    @Test
    @DisplayName("GET /encadrants/{id} -> 200")
    void getEncadrantById_shouldReturn200() throws Exception {
        when(encadrantService.getEncadrantById(1L))
                .thenReturn(dto(1L, "Martin", "Paul", "paul@test.com", "0612345678"));

        mockMvc.perform(get("/encadrants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /encadrants/{id} absent -> 404")
    void getEncadrantById_notFound_shouldReturn404() throws Exception {
        when(encadrantService.getEncadrantById(99L))
                .thenThrow(new ResourceNotFoundException("Encadrant", 99L));

        mockMvc.perform(get("/encadrants/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /encadrants/{id} -> 200")
    void updateEncadrant_shouldReturn200() throws Exception {
        when(encadrantService.updateEncadrant(any(EncadrantDto.class), anyLong()))
                .thenReturn(dto(1L, "Martin", "Paul", "new@test.com", "0699999999"));

        mockMvc.perform(put("/encadrants/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Martin\",\"prenom\":\"Paul\",\"email\":\"new@test.com\",\"telephone\":\"0699999999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@test.com"));
    }

    @Test
    @DisplayName("DELETE /encadrants/{id} -> 204")
    void deleteEncadrant_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/encadrants/1"))
                .andExpect(status().isNoContent());

        verify(encadrantService).deleteEncadrant(1L);
    }
}
