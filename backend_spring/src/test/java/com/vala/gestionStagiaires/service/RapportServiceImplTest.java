package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.RapportDto;
import com.vala.gestionStagiaires.DTOs.suivi.RapportRequest;
import com.vala.gestionStagiaires.entities.RapportHebdomadaire;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.enums.StatutRapport;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.RapportHebdomadaireRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RapportServiceImpl")
class RapportServiceImplTest {

    /** Dimanche 15 mars 2026 : la semaine courante commence le lundi 9 mars. */
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);

    @Mock
    private RapportHebdomadaireRepository rapportRepository;

    @Mock
    private StagiaireRepository stagiaireRepository;

    private RapportServiceImpl service;
    private Clock clock;
    private Stagiaire stagiaire;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new RapportServiceImpl(rapportRepository, stagiaireRepository, clock);
        Stage stage = Stage.builder().id(1L).status(StageStatus.EN_COURS)
                .dateDebut(LocalDate.of(2026, 3, 4)).dateFin(LocalDate.of(2026, 6, 30)).build();
        stagiaire = Stagiaire.builder().id(5L).stage(stage).build();
    }

    private static RapportRequest requete(LocalDate semaine) {
        return new RapportRequest(semaine, "  Mise en place de l'API  ", "Aucune", "Tests");
    }

    private RapportHebdomadaire rapport(StatutRapport statut) {
        return RapportHebdomadaire.builder().id(7L).stagiaire(stagiaire).semaine(LocalDate.of(2026, 3, 9))
                .travauxRealises("Ancien").statut(statut).dateDepot(Instant.EPOCH).build();
    }

    @Test
    @DisplayName("deposer() ramène la semaine au lundi et crée un rapport SOUMIS")
    void deposer() {
        when(stagiaireRepository.findById(5L)).thenReturn(Optional.of(stagiaire));
        when(rapportRepository.existsByStagiaireIdAndSemaine(5L, LocalDate.of(2026, 3, 9))).thenReturn(false);
        when(rapportRepository.save(any(RapportHebdomadaire.class))).thenAnswer(inv -> inv.getArgument(0));

        RapportDto dto = service.deposer(5L, requete(LocalDate.of(2026, 3, 12))); // un jeudi

        assertThat(dto.semaine()).isEqualTo(LocalDate.of(2026, 3, 9));
        assertThat(dto.statut()).isEqualTo(StatutRapport.SOUMIS);
        assertThat(dto.travauxRealises()).isEqualTo("Mise en place de l'API");
        assertThat(dto.dateDepot()).isEqualTo(clock.instant());
    }

    @Test
    @DisplayName("deposer() accepte la semaine du début du stage, même si le lundi la précède")
    void deposer_semaineDuDebut() {
        when(stagiaireRepository.findById(5L)).thenReturn(Optional.of(stagiaire));
        when(rapportRepository.save(any(RapportHebdomadaire.class))).thenAnswer(inv -> inv.getArgument(0));

        // Le stage commence le mercredi 4 mars ; sa semaine commence le lundi 2 mars.
        assertThat(service.deposer(5L, requete(LocalDate.of(2026, 3, 4))).semaine()).isEqualTo(LocalDate.of(2026, 3, 2));
    }

    @Test
    @DisplayName("deposer() refuse une semaine hors de la période du stage")
    void deposer_horsPeriode() {
        when(stagiaireRepository.findById(5L)).thenReturn(Optional.of(stagiaire));

        assertThatThrownBy(() -> service.deposer(5L, requete(LocalDate.of(2026, 2, 25))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ne fait pas partie de la période");
    }

    @Test
    @DisplayName("deposer() refuse une semaine future")
    void deposer_semaineFuture() {
        when(stagiaireRepository.findById(5L)).thenReturn(Optional.of(stagiaire));

        assertThatThrownBy(() -> service.deposer(5L, requete(LocalDate.of(2026, 3, 16))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("future");
    }

    @Test
    @DisplayName("deposer() refuse un second rapport pour la même semaine")
    void deposer_doublon() {
        when(stagiaireRepository.findById(5L)).thenReturn(Optional.of(stagiaire));
        when(rapportRepository.existsByStagiaireIdAndSemaine(5L, LocalDate.of(2026, 3, 9))).thenReturn(true);

        assertThatThrownBy(() -> service.deposer(5L, requete(LocalDate.of(2026, 3, 9))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("existe déjà");
        verify(rapportRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifier() repasse un rapport A_REVOIR à SOUMIS")
    void modifier_resoumission() {
        RapportHebdomadaire r = rapport(StatutRapport.A_REVOIR);
        when(rapportRepository.findById(7L)).thenReturn(Optional.of(r));
        when(rapportRepository.save(r)).thenReturn(r);

        service.modifier(7L, requete(LocalDate.of(2026, 3, 11)));

        assertThat(r.getStatut()).isEqualTo(StatutRapport.SOUMIS);
        assertThat(r.getTravauxRealises()).isEqualTo("Mise en place de l'API");
    }

    @Test
    @DisplayName("modifier() refuse un rapport validé")
    void modifier_valide() {
        when(rapportRepository.findById(7L)).thenReturn(Optional.of(rapport(StatutRapport.VALIDE)));

        assertThatThrownBy(() -> service.modifier(7L, requete(LocalDate.of(2026, 3, 9))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("validé");
    }

    @Test
    @DisplayName("modifier() refuse un changement de semaine")
    void modifier_changementSemaine() {
        when(rapportRepository.findById(7L)).thenReturn(Optional.of(rapport(StatutRapport.SOUMIS)));

        assertThatThrownBy(() -> service.modifier(7L, requete(LocalDate.of(2026, 3, 2))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("semaine");
    }

    @Test
    @DisplayName("valider() passe un rapport SOUMIS à VALIDE avec le commentaire")
    void valider() {
        RapportHebdomadaire r = rapport(StatutRapport.SOUMIS);
        when(rapportRepository.findById(7L)).thenReturn(Optional.of(r));
        when(rapportRepository.save(r)).thenReturn(r);

        service.valider(7L, " Bon travail ");

        assertThat(r.getStatut()).isEqualTo(StatutRapport.VALIDE);
        assertThat(r.getCommentaireEncadrant()).isEqualTo("Bon travail");
    }

    @ParameterizedTest(name = "un rapport {0} ne peut pas être examiné")
    @EnumSource(value = StatutRapport.class, names = {"VALIDE", "A_REVOIR"})
    void decider_statutNonSoumis(StatutRapport statut) {
        when(rapportRepository.findById(7L)).thenReturn(Optional.of(rapport(statut)));

        assertThatThrownBy(() -> service.valider(7L, null)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("demanderRevision() exige un commentaire")
    void demanderRevision_sansCommentaire() {
        assertThatThrownBy(() -> service.demanderRevision(7L, "  "))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("commentaire");
    }

    @Test
    @DisplayName("demanderRevision() passe le rapport à A_REVOIR")
    void demanderRevision() {
        RapportHebdomadaire r = rapport(StatutRapport.SOUMIS);
        when(rapportRepository.findById(7L)).thenReturn(Optional.of(r));
        when(rapportRepository.save(r)).thenReturn(r);

        service.demanderRevision(7L, "Détailler les tests");

        assertThat(r.getStatut()).isEqualTo(StatutRapport.A_REVOIR);
        assertThat(r.getCommentaireEncadrant()).isEqualTo("Détailler les tests");
    }

    @Test
    @DisplayName("getRapport() lève ResourceNotFoundException si absent")
    void getRapport_absent() {
        when(rapportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRapport(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
