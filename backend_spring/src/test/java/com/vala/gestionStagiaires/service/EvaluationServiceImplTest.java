package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationDto;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationRequest;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationStatistiques;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Evaluation;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.EvaluationRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluationServiceImpl")
class EvaluationServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 2);

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private StagiaireRepository stagiaireRepository;

    private EvaluationServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new EvaluationServiceImpl(evaluationRepository, stagiaireRepository, clock);
    }

    private static Stagiaire stagiaire(long id, StageStatus status) {
        return Stagiaire.builder().id(id).stage(Stage.builder().id(id).status(status).build()).build();
    }

    private static EvaluationRequest requete(String note) {
        return new EvaluationRequest(new BigDecimal(note), 4, 5, 3, 4, "Très bon stage", true);
    }

    @Test
    @DisplayName("enregistrer() crée l'évaluation d'un stage terminé")
    void enregistrer_cree() {
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(stagiaire(1, StageStatus.TERMINE)));
        when(evaluationRepository.findByStagiaireId(1L)).thenReturn(Optional.empty());
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(inv -> inv.getArgument(0));

        EvaluationDto dto = service.enregistrer(1L, requete("15.5"));

        assertThat(dto.stagiaireId()).isEqualTo(1L);
        assertThat(dto.noteGlobale()).isEqualByComparingTo("15.5");
        assertThat(dto.communication()).isEqualTo(5);
        assertThat(dto.recommande()).isTrue();
        assertThat(dto.dateEvaluation()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("enregistrer() met à jour l'évaluation existante")
    void enregistrer_metAJour() {
        Stagiaire s = stagiaire(1, StageStatus.TERMINE);
        Evaluation existante = Evaluation.builder().id(9L).stagiaire(s).noteGlobale(BigDecimal.TEN)
                .competenceTechnique(1).communication(1).autonomie(1).assiduite(1)
                .dateEvaluation(TODAY.minusDays(10)).build();
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(s));
        when(evaluationRepository.findByStagiaireId(1L)).thenReturn(Optional.of(existante));
        when(evaluationRepository.save(existante)).thenReturn(existante);

        service.enregistrer(1L, requete("17"));

        assertThat(existante.getId()).isEqualTo(9L);
        assertThat(existante.getNoteGlobale()).isEqualByComparingTo("17");
        assertThat(existante.getDateEvaluation()).isEqualTo(TODAY);
    }

    @ParameterizedTest(name = "stage {0} : évaluation refusée")
    @EnumSource(value = StageStatus.class, names = {"PLANIFIE", "EN_COURS", "ANNULE"})
    void enregistrer_stageNonTermine(StageStatus status) {
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(stagiaire(1, status)));

        assertThatThrownBy(() -> service.enregistrer(1L, requete("12")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining(status.name());
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    @DisplayName("getEvaluation() lève ResourceNotFoundException si le stagiaire n'est pas évalué")
    void getEvaluation_absente() {
        when(evaluationRepository.findByStagiaireId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEvaluation(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest(name = "note {0} → tranche « {1} »")
    @CsvSource({"0, < 10", "9.9, < 10", "10, 10 – 12", "11.9, 10 – 12", "12, 12 – 14", "15.9, 14 – 16", "16, 16 et plus", "20, 16 et plus"})
    void tranche(String note, String attendue) {
        assertThat(EvaluationServiceImpl.tranche(new BigDecimal(note))).isEqualTo(attendue);
    }

    @Test
    @DisplayName("statistiques() calcule moyennes, tranches, recommandations et regroupements")
    void statistiques() {
        Departement info = Departement.builder().id(1L).nom("Informatique").build();
        Departement rh = Departement.builder().id(2L).nom("RH").build();
        Encadrant martin1 = Encadrant.builder().id(1L).nom("Martin").prenom("Paul").build();
        Encadrant martin2 = Encadrant.builder().id(2L).nom("Martin").prenom("Paul").build();
        when(evaluationRepository.findAll()).thenReturn(List.of(
                evaluation("16", 5, true, info, "Génie Info", martin1),
                evaluation("14", 3, true, info, "Génie Info", martin1),
                evaluation("9", 2, false, rh, null, martin2)));
        when(evaluationRepository.compterStagiairesSansEvaluation(StageStatus.TERMINE)).thenReturn(4L);

        EvaluationStatistiques stats = service.statistiques();

        assertThat(stats.nombreEvaluations()).isEqualTo(3);
        assertThat(stats.noteMoyenne()).isEqualTo(13.0);
        assertThat(stats.tauxRecommandation()).isEqualTo(66.7);
        assertThat(stats.stagiairesTerminesSansEvaluation()).isEqualTo(4);
        assertThat(stats.moyennesCompetences()).containsEntry("Autonomie", 3.33);
        assertThat(stats.repartitionNotes()).containsExactly(
                entry("< 10", 1L), entry("10 – 12", 0L), entry("12 – 14", 0L), entry("14 – 16", 1L), entry("16 et plus", 1L));
        assertThat(stats.moyenneParDepartement()).containsExactly(entry("Informatique", 15.0), entry("RH", 9.0));
        assertThat(stats.moyenneParEncadrant()).containsExactly(entry("Martin Paul (#1)", 15.0), entry("Martin Paul (#2)", 9.0));
        assertThat(stats.moyenneParFiliere()).containsExactly(entry("Génie Info", 15.0), entry(StatistiquesUtils.NON_RENSEIGNE, 9.0));
    }

    @Test
    @DisplayName("statistiques() sans évaluation : moyennes nulles, tranches à 0")
    void statistiques_vide() {
        when(evaluationRepository.findAll()).thenReturn(List.of());

        EvaluationStatistiques stats = service.statistiques();

        assertThat(stats.noteMoyenne()).isNull();
        assertThat(stats.tauxRecommandation()).isNull();
        assertThat(stats.repartitionNotes()).hasSize(5).allSatisfy((k, v) -> assertThat(v).isZero());
    }

    private static Evaluation evaluation(String note, int autonomie, boolean recommande, Departement dep,
                                         String filiere, Encadrant encadrant) {
        Stagiaire s = Stagiaire.builder().departement(dep).filiere(filiere)
                .stage(Stage.builder().encadrant(encadrant).status(StageStatus.TERMINE).build()).build();
        return Evaluation.builder().stagiaire(s).noteGlobale(new BigDecimal(note))
                .competenceTechnique(4).communication(4).autonomie(autonomie).assiduite(4)
                .recommande(recommande).dateEvaluation(TODAY).build();
    }
}
