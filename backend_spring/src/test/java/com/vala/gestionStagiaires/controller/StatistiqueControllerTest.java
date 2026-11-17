package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.ResumeStatistiques;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.GlobalExceptionHandler;
import com.vala.gestionStagiaires.service.StatistiqueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatistiqueController")
class StatistiqueControllerTest {

    @Mock
    private StatistiqueService statistiqueService;

    @Mock
    private com.vala.gestionStagiaires.service.SuiviStatistiqueService suiviStatistiqueService;

    @Mock
    private com.vala.gestionStagiaires.service.EvaluationService evaluationService;

    @Mock
    private com.vala.gestionStagiaires.service.CandidatureService candidatureService;

    @InjectMocks
    private StatistiqueController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static Map<String, Long> ordonne(Object... cleValeur) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (int i = 0; i < cleValeur.length; i += 2) {
            map.put((String) cleValeur[i], (Long) cleValeur[i + 1]);
        }
        return map;
    }

    @Test
    @DisplayName("GET /statistiques/departements -> objet {libellé: effectif}, ordre conservé")
    void parDepartement() throws Exception {
        when(statistiqueService.stagiairesParDepartement())
                .thenReturn(ordonne("Informatique", 5L, "RH", 1L));

        mockMvc.perform(get("/statistiques/departements"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"Informatique\":5,\"RH\":1}", true))
                .andExpect(content().string("{\"Informatique\":5,\"RH\":1}"));
    }

    @Test
    @DisplayName("GET /statistiques/filieres|organismes|encadrants -> 200")
    void autresRepartitions() throws Exception {
        when(statistiqueService.stagiairesParFiliere()).thenReturn(ordonne("Génie Info", 5L));
        when(statistiqueService.stagiairesParOrganisme()).thenReturn(ordonne("ENSA", 3L));
        when(statistiqueService.stagiairesParEncadrant()).thenReturn(ordonne("Martin Paul", 4L));

        mockMvc.perform(get("/statistiques/filieres")).andExpect(jsonPath("$['Génie Info']").value(5));
        mockMvc.perform(get("/statistiques/organismes")).andExpect(jsonPath("$.ENSA").value(3));
        mockMvc.perform(get("/statistiques/encadrants")).andExpect(jsonPath("$['Martin Paul']").value(4));
    }

    @Test
    @DisplayName("GET /statistiques/stages/statuts -> effectif par statut")
    void stagesParStatut() throws Exception {
        Map<StageStatus, Long> parStatut = new EnumMap<>(StageStatus.class);
        parStatut.put(StageStatus.EN_COURS, 4L);
        parStatut.put(StageStatus.PLANIFIE, 0L);
        when(statistiqueService.stagesParStatut()).thenReturn(parStatut);

        mockMvc.perform(get("/statistiques/stages/statuts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.EN_COURS").value(4))
                .andExpect(jsonPath("$.PLANIFIE").value(0));
    }

    @Test
    @DisplayName("GET /statistiques/stages/echeances -> 30 jours par défaut, paramètre jours sinon")
    void stagesAEcheance() throws Exception {
        StageDto stage = new StageDto();
        stage.setSujet("Stage IA");
        when(statistiqueService.stagesAEcheance(30)).thenReturn(List.of(stage));

        mockMvc.perform(get("/statistiques/stages/echeances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sujet").value("Stage IA"));
        mockMvc.perform(get("/statistiques/stages/echeances").param("jours", "7"))
                .andExpect(status().isOk());

        verify(statistiqueService).stagesAEcheance(7);
    }

    @Test
    @DisplayName("GET /statistiques/stages/echeances?jours=abc -> 400")
    void stagesAEcheance_parametreInvalide() throws Exception {
        mockMvc.perform(get("/statistiques/stages/echeances").param("jours", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Paramètre invalide : jours."));
    }

    @Test
    @DisplayName("GET /statistiques/resume -> indicateurs clés")
    void resume() throws Exception {
        when(statistiqueService.resume()).thenReturn(new ResumeStatistiques(12, 7, 6, 3, 2, 1, 30, 4, 3));

        mockMvc.perform(get("/statistiques/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStagiaires").value(12))
                .andExpect(jsonPath("$.stagiairesActifs").value(7))
                .andExpect(jsonPath("$.stagesAEcheance").value(1))
                .andExpect(jsonPath("$.joursEcheance").value(30));
    }

    @Test
    @DisplayName("GET /statistiques/evaluations -> bilan des évaluations")
    void evaluations() throws Exception {
        Map<String, Long> repartition = new LinkedHashMap<>();
        repartition.put("< 10", 0L);
        repartition.put("16 et plus", 2L);
        when(evaluationService.statistiques()).thenReturn(new com.vala.gestionStagiaires.DTOs.evaluation.EvaluationStatistiques(
                2, 16.5, 100.0, 1, Map.of("Autonomie", 4.5), repartition,
                Map.of("Informatique", 16.5), Map.of("Martin Paul", 16.5), Map.of("Génie Info", 16.5)));

        mockMvc.perform(get("/statistiques/evaluations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteMoyenne").value(16.5))
                .andExpect(jsonPath("$.stagiairesTerminesSansEvaluation").value(1))
                .andExpect(jsonPath("$.repartitionNotes['16 et plus']").value(2));
    }

    @Test
    @DisplayName("GET /statistiques/candidatures -> bilan des candidatures")
    void candidatures() throws Exception {
        Map<com.vala.gestionStagiaires.enums.CandidatureStatus, Long> parStatut =
                new EnumMap<>(com.vala.gestionStagiaires.enums.CandidatureStatus.class);
        parStatut.put(com.vala.gestionStagiaires.enums.CandidatureStatus.SOUMISE, 3L);
        when(candidatureService.statistiques()).thenReturn(new com.vala.gestionStagiaires.DTOs.candidature.CandidatureStatistiques(
                3, parStatut, ordonne("2026-03", 3L), 50.0, 4.5, ordonne("ENSA", 2L)));

        mockMvc.perform(get("/statistiques/candidatures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parStatut.SOUMISE").value(3))
                .andExpect(jsonPath("$.parMois['2026-03']").value(3))
                .andExpect(jsonPath("$.tauxAcceptation").value(50.0))
                .andExpect(jsonPath("$.delaiMoyenDecisionJours").value(4.5));
    }

    @Test
    @DisplayName("GET /statistiques/stages/durees-et-taux et /statistiques/stagiaires/profil")
    void dureesEtProfil() throws Exception {
        com.vala.gestionStagiaires.DTOs.stats.IndicateursStages global =
                new com.vala.gestionStagiaires.DTOs.stats.IndicateursStages(4, 2, 1, 20.5, 66.7, 25.0);
        when(statistiqueService.dureesEtTaux()).thenReturn(
                new com.vala.gestionStagiaires.DTOs.stats.DureesEtTaux(global, Map.of(), Map.of("Informatique", global)));
        when(statistiqueService.profil(2L)).thenReturn(new com.vala.gestionStagiaires.DTOs.stats.ProfilStagiaires(
                3, ordonne("FEMME", 2L), Map.of(), Map.of(), Map.of(), Map.of()));

        mockMvc.perform(get("/statistiques/stages/durees-et-taux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.global.dureeMoyenneJours").value(20.5))
                .andExpect(jsonPath("$.parDepartement.Informatique.tauxAchevement").value(66.7));
        mockMvc.perform(get("/statistiques/stagiaires/profil").param("departementId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parSexe.FEMME").value(2));
    }
}
