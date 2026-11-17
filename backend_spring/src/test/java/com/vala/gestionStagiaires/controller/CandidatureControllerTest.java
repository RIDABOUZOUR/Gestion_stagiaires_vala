package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.candidature.AcceptationRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureDto;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureRequest;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.service.CandidatureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CandidatureController")
class CandidatureControllerTest {

    private static final String CANDIDATURE_VALIDE = "{\"nom\":\"Alami\",\"prenom\":\"Sara\",\"email\":\"sara@mail.com\","
            + "\"sexe\":\"FEMME\",\"organisme\":\"ENSA\"}";

    @Mock
    private CandidatureService candidatureService;

    @InjectMocks
    private CandidatureController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private static CandidatureDto dto(CandidatureStatus statut) {
        return new CandidatureDto(1L, "Alami", "Sara", "sara@mail.com", null, null, Sexe.FEMME, "ENSA", null, null,
                null, null, null, null, null, statut, null, null, null, statut == CandidatureStatus.ACCEPTEE ? 40L : null);
    }

    @Test
    @DisplayName("POST /candidatures -> 201")
    void soumettre() throws Exception {
        when(candidatureService.soumettre(any(CandidatureRequest.class))).thenReturn(dto(CandidatureStatus.SOUMISE));

        mockMvc.perform(post("/candidatures").contentType(MediaType.APPLICATION_JSON).content(CANDIDATURE_VALIDE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("SOUMISE"));
    }

    @Test
    @DisplayName("POST candidature invalide (email, période incohérente) -> 400")
    void soumettre_invalide() throws Exception {
        mockMvc.perform(post("/candidatures").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Alami\",\"prenom\":\"Sara\",\"email\":\"pas-un-email\",\"sexe\":\"FEMME\","
                                + "\"dateDebutSouhaitee\":\"2026-07-01\",\"dateFinSouhaitee\":\"2026-04-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.periodeCoherente").exists());
        verifyNoInteractions(candidatureService);
    }

    @Test
    @DisplayName("POST candidature en double -> 409")
    void soumettre_doublon() throws Exception {
        when(candidatureService.soumettre(any(CandidatureRequest.class)))
                .thenThrow(new BusinessRuleException("Une candidature est déjà en cours de traitement pour l'email sara@mail.com."));

        mockMvc.perform(post("/candidatures").contentType(MediaType.APPLICATION_JSON).content(CANDIDATURE_VALIDE))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /candidatures/page -> filtres transmis, tri par défaut dateSoumission décroissante")
    void rechercher() throws Exception {
        when(candidatureService.rechercher(any(CandidatureFiltre.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(dto(CandidatureStatus.SOUMISE)), 0, 20, 1, 1));

        mockMvc.perform(get("/candidatures/page").param("keyword", "ensa").param("statut", "SOUMISE").param("departementId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nom").value("Alami"));

        ArgumentCaptor<CandidatureFiltre> filtre = ArgumentCaptor.forClass(CandidatureFiltre.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(candidatureService).rechercher(filtre.capture(), pageable.capture());
        assertThat(filtre.getValue()).isEqualTo(new CandidatureFiltre("ensa", CandidatureStatus.SOUMISE, 2L));
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "dateSoumission"));
    }

    @Test
    @DisplayName("PATCH /candidatures/{id}/etudier et /retirer -> 200")
    void etudierEtRetirer() throws Exception {
        when(candidatureService.etudier(1L)).thenReturn(dto(CandidatureStatus.EN_ETUDE));
        when(candidatureService.retirer(1L)).thenReturn(dto(CandidatureStatus.RETIREE));

        mockMvc.perform(patch("/candidatures/1/etudier")).andExpect(jsonPath("$.statut").value("EN_ETUDE"));
        mockMvc.perform(patch("/candidatures/1/retirer")).andExpect(jsonPath("$.statut").value("RETIREE"));
    }

    @Test
    @DisplayName("PATCH /candidatures/{id}/refuser sans motif -> 400")
    void refuser_sansMotif() throws Exception {
        mockMvc.perform(patch("/candidatures/1/refuser").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.motif").exists());
        verifyNoInteractions(candidatureService);
    }

    @Test
    @DisplayName("PATCH /candidatures/{id}/refuser -> 200")
    void refuser() throws Exception {
        when(candidatureService.refuser(1L, "Profil incomplet")).thenReturn(dto(CandidatureStatus.REFUSEE));

        mockMvc.perform(patch("/candidatures/1/refuser").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motif\":\"Profil incomplet\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REFUSEE"));
    }

    @Test
    @DisplayName("POST /candidatures/{id}/accepter -> 200 + id du stagiaire créé")
    void accepter() throws Exception {
        when(candidatureService.accepter(eq(1L), any(AcceptationRequest.class))).thenReturn(dto(CandidatureStatus.ACCEPTEE));

        mockMvc.perform(post("/candidatures/1/accepter").contentType(MediaType.APPLICATION_JSON).content("{\"stageId\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACCEPTEE"))
                .andExpect(jsonPath("$.stagiaireId").value(40));

        verify(candidatureService).accepter(1L, new AcceptationRequest(7L, null));
    }

    @Test
    @DisplayName("POST accepter sans stage -> 400")
    void accepter_sansStage() throws Exception {
        mockMvc.perform(post("/candidatures/1/accepter").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.stageId").exists());
    }

    @Test
    @DisplayName("GET, DELETE /candidatures/{id}")
    void lectureEtSuppression() throws Exception {
        when(candidatureService.getCandidature(1L)).thenReturn(dto(CandidatureStatus.SOUMISE));

        mockMvc.perform(get("/candidatures/1")).andExpect(jsonPath("$.email").value("sara@mail.com"));
        mockMvc.perform(delete("/candidatures/1")).andExpect(status().isNoContent());
        verify(candidatureService).supprimer(1L);
    }
}
