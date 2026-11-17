package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.PresenceDto;
import com.vala.gestionStagiaires.DTOs.suivi.PresenceRequest;
import com.vala.gestionStagiaires.entities.Presence;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.enums.StatutPresence;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.repository.PresenceRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PresenceServiceImpl")
class PresenceServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 15);

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private StagiaireRepository stagiaireRepository;

    private PresenceServiceImpl service;

    private Stagiaire stagiaire;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new PresenceServiceImpl(presenceRepository, stagiaireRepository, clock);
        Stage stage = Stage.builder().id(1L).status(StageStatus.EN_COURS)
                .dateDebut(LocalDate.of(2026, 3, 1)).dateFin(LocalDate.of(2026, 6, 30)).build();
        stagiaire = Stagiaire.builder().id(5L).stage(stage).build();
    }

    private void stagiaireExiste() {
        when(stagiaireRepository.findById(5L)).thenReturn(Optional.of(stagiaire));
    }

    @Test
    @DisplayName("enregistrer() crée la présence du jour")
    void enregistrer_cree() {
        stagiaireExiste();
        LocalDate jour = LocalDate.of(2026, 3, 10);
        when(presenceRepository.findByStagiaireIdAndDate(5L, jour)).thenReturn(Optional.empty());
        when(presenceRepository.save(any(Presence.class))).thenAnswer(inv -> inv.getArgument(0));

        PresenceDto dto = service.enregistrer(5L, jour, new PresenceRequest(StatutPresence.RETARD, "Bus"));

        assertThat(dto.stagiaireId()).isEqualTo(5L);
        assertThat(dto.date()).isEqualTo(jour);
        assertThat(dto.statut()).isEqualTo(StatutPresence.RETARD);
        assertThat(dto.commentaire()).isEqualTo("Bus");
    }

    @Test
    @DisplayName("enregistrer() remplace une présence existante (idempotent)")
    void enregistrer_remplace() {
        stagiaireExiste();
        LocalDate jour = LocalDate.of(2026, 3, 10);
        Presence existante = Presence.builder().id(9L).stagiaire(stagiaire).date(jour).statut(StatutPresence.ABSENT).build();
        when(presenceRepository.findByStagiaireIdAndDate(5L, jour)).thenReturn(Optional.of(existante));
        when(presenceRepository.save(existante)).thenReturn(existante);

        service.enregistrer(5L, jour, new PresenceRequest(StatutPresence.ABSENCE_JUSTIFIEE, "Certificat"));

        assertThat(existante.getId()).isEqualTo(9L);
        assertThat(existante.getStatut()).isEqualTo(StatutPresence.ABSENCE_JUSTIFIEE);
    }

    @Test
    @DisplayName("enregistrer() refuse une date hors de la période du stage")
    void enregistrer_horsPeriode() {
        stagiaireExiste();

        assertThatThrownBy(() -> service.enregistrer(5L, LocalDate.of(2026, 2, 28),
                new PresenceRequest(StatutPresence.PRESENT, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("en dehors de la période");
        verify(presenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("enregistrer() refuse une date future")
    void enregistrer_dateFuture() {
        stagiaireExiste();

        assertThatThrownBy(() -> service.enregistrer(5L, TODAY.plusDays(1),
                new PresenceRequest(StatutPresence.PRESENT, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("future");
    }

    @Test
    @DisplayName("enregistrer() refuse le suivi d'un stage annulé")
    void enregistrer_stageAnnule() {
        stagiaire.getStage().setStatus(StageStatus.ANNULE);
        stagiaireExiste();

        assertThatThrownBy(() -> service.enregistrer(5L, LocalDate.of(2026, 3, 10),
                new PresenceRequest(StatutPresence.PRESENT, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("annulé");
    }

    @Test
    @DisplayName("enregistrer() lève StagiaireNotFoundException si le stagiaire n'existe pas")
    void enregistrer_stagiaireInconnu() {
        when(stagiaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enregistrer(99L, TODAY, new PresenceRequest(StatutPresence.PRESENT, null)))
                .isInstanceOf(StagiaireNotFoundException.class);
    }

    @Test
    @DisplayName("lister() utilise par défaut toute la période du stage")
    void lister_periodeParDefaut() {
        stagiaireExiste();
        Presence p = Presence.builder().id(1L).stagiaire(stagiaire).date(LocalDate.of(2026, 3, 2))
                .statut(StatutPresence.PRESENT).build();
        when(presenceRepository.findByStagiaireIdAndDateBetweenOrderByDateAsc(
                5L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30))).thenReturn(List.of(p));

        assertThat(service.lister(5L, null, null)).extracting(PresenceDto::id).containsExactly(1L);
    }

    @Test
    @DisplayName("lister() renvoie une liste vide si du > au")
    void lister_intervalleInverse() {
        stagiaireExiste();

        assertThat(service.lister(5L, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 3, 1))).isEmpty();
    }

    @Test
    @DisplayName("supprimer() lève ResourceNotFoundException si rien n'est saisi ce jour-là")
    void supprimer_absente() {
        when(presenceRepository.findByStagiaireIdAndDate(5L, TODAY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.supprimer(5L, TODAY)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("supprimer() supprime la présence")
    void supprimer() {
        Presence p = Presence.builder().id(1L).stagiaire(stagiaire).date(TODAY).statut(StatutPresence.PRESENT).build();
        when(presenceRepository.findByStagiaireIdAndDate(5L, TODAY)).thenReturn(Optional.of(p));

        service.supprimer(5L, TODAY);

        verify(presenceRepository).delete(p);
    }
}
