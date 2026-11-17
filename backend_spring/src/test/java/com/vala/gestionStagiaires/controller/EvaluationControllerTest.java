package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.evaluation.AttestationPdf;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationDto;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationRequest;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.service.AttestationService;
import com.vala.gestionStagiaires.service.EvaluationService;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluationController")
class EvaluationControllerTest {

    private static final String EVALUATION_VALIDE = "{\"noteGlobale\":15.5,\"competenceTechnique\":4,"
            + "\"communication\":5,\"autonomie\":3,\"assiduite\":4,\"recommande\":true}";

    @Mock
    private EvaluationService evaluationService;

    @Mock
    private AttestationService attestationService;

    @InjectMocks
    private EvaluationController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private static EvaluationDto dto() {
        return new EvaluationDto(1L, 5L, new BigDecimal("15.5"), 4, 5, 3, 4, null, true, LocalDate.of(2026, 7, 2));
    }

    @Test
    @DisplayName("PUT /stagiaires/{id}/evaluation -> 200")
    void enregistrer() throws Exception {
        when(evaluationService.enregistrer(eq(5L), any(EvaluationRequest.class))).thenReturn(dto());

        mockMvc.perform(put("/stagiaires/5/evaluation").contentType(MediaType.APPLICATION_JSON).content(EVALUATION_VALIDE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteGlobale").value(15.5))
                .andExpect(jsonPath("$.recommande").value(true));
    }

    @Test
    @DisplayName("PUT évaluation avec note > 20, compétence hors 1–5 et 2 décimales -> 400 par champ")
    void enregistrer_invalide() throws Exception {
        mockMvc.perform(put("/stagiaires/5/evaluation").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"noteGlobale\":20.55,\"competenceTechnique\":6,\"communication\":0,"
                                + "\"autonomie\":3,\"assiduite\":4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.noteGlobale").exists())
                .andExpect(jsonPath("$.errors.competenceTechnique").value("Les compétences sont notées de 1 à 5"))
                .andExpect(jsonPath("$.errors.communication").exists())
                .andExpect(jsonPath("$.errors.recommande").exists());
        verifyNoInteractions(evaluationService);
    }

    @Test
    @DisplayName("PUT évaluation d'un stage non terminé -> 409")
    void enregistrer_stageNonTermine() throws Exception {
        when(evaluationService.enregistrer(eq(5L), any(EvaluationRequest.class)))
                .thenThrow(new BusinessRuleException("L'évaluation n'est possible qu'une fois le stage terminé (statut actuel : EN_COURS)."));

        mockMvc.perform(put("/stagiaires/5/evaluation").contentType(MediaType.APPLICATION_JSON).content(EVALUATION_VALIDE))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /stagiaires/{id}/evaluation -> 200")
    void getEvaluation() throws Exception {
        when(evaluationService.getEvaluation(5L)).thenReturn(dto());

        mockMvc.perform(get("/stagiaires/5/evaluation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stagiaireId").value(5));
    }

    @Test
    @DisplayName("GET /stagiaires/{id}/attestation -> PDF en pièce jointe avec le numéro")
    void attestation() throws Exception {
        byte[] pdf = "%PDF-1.4 test".getBytes();
        when(attestationService.generer(5L))
                .thenReturn(new AttestationPdf("ATT-2026-0001", "attestation-ATT-2026-0001-alami-sara.pdf", pdf));

        mockMvc.perform(get("/stagiaires/5/attestation"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"attestation-ATT-2026-0001-alami-sara.pdf\""))
                .andExpect(header().string(EvaluationController.HEADER_NUMERO, "ATT-2026-0001"))
                .andExpect(content().bytes(pdf));
    }

    @Test
    @DisplayName("GET attestation d'un stage annulé -> 409 en JSON")
    void attestation_stageAnnule() throws Exception {
        when(attestationService.generer(5L))
                .thenThrow(new BusinessRuleException("Le stage a été annulé : aucune attestation ne peut être délivrée."));

        mockMvc.perform(get("/stagiaires/5/attestation"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Le stage a été annulé : aucune attestation ne peut être délivrée."));
    }
}
