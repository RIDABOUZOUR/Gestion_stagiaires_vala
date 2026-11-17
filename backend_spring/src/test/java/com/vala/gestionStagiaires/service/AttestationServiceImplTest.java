package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.evaluation.AttestationPdf;
import com.vala.gestionStagiaires.entities.Attestation;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.pdf.AttestationPdfGenerator;
import com.vala.gestionStagiaires.repository.AttestationRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
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
@DisplayName("AttestationServiceImpl")
class AttestationServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 2);

    @Mock
    private AttestationRepository attestationRepository;

    @Mock
    private StagiaireRepository stagiaireRepository;

    @Mock
    private AttestationPdfGenerator generator;

    private AttestationServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new AttestationServiceImpl(attestationRepository, stagiaireRepository, generator, clock);
    }

    private static Stagiaire stagiaire(StageStatus status) {
        return Stagiaire.builder().id(1L).nom("Él Alami").prenom("Sâra")
                .stage(Stage.builder().id(1L).status(status).build()).build();
    }

    @Test
    @DisplayName("première génération : attribue le numéro suivant de l'année")
    void premiereGeneration() {
        Stagiaire s = stagiaire(StageStatus.TERMINE);
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(s));
        when(attestationRepository.findByStagiaireId(1L)).thenReturn(Optional.empty());
        when(attestationRepository.dernierRang(2026)).thenReturn(41);
        when(attestationRepository.save(any(Attestation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(generator.generer(any(Attestation.class), any(Stagiaire.class))).thenReturn(new byte[]{1, 2, 3});

        AttestationPdf pdf = service.generer(1L);

        ArgumentCaptor<Attestation> captor = ArgumentCaptor.forClass(Attestation.class);
        verify(attestationRepository).save(captor.capture());
        assertThat(captor.getValue().getNumero()).isEqualTo("ATT-2026-0042");
        assertThat(captor.getValue().getSequence()).isEqualTo(42);
        assertThat(captor.getValue().getDateEmission()).isEqualTo(TODAY);
        assertThat(pdf.numero()).isEqualTo("ATT-2026-0042");
        assertThat(pdf.nomFichier()).isEqualTo("attestation-ATT-2026-0042-el-alami-sara.pdf");
        assertThat(pdf.contenu()).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("régénération : réutilise l'attestation existante, sans nouveau numéro")
    void regeneration() {
        Stagiaire s = stagiaire(StageStatus.TERMINE);
        Attestation existante = Attestation.builder().id(5L).stagiaire(s).numero("ATT-2025-0007")
                .annee(2025).sequence(7).dateEmission(LocalDate.of(2025, 12, 1)).build();
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(s));
        when(attestationRepository.findByStagiaireId(1L)).thenReturn(Optional.of(existante));
        when(generator.generer(existante, s)).thenReturn(new byte[]{9});

        assertThat(service.generer(1L).numero()).isEqualTo("ATT-2025-0007");
        verify(attestationRepository, never()).save(any());
    }

    @Test
    @DisplayName("stage annulé : refus explicite")
    void stageAnnule() {
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(stagiaire(StageStatus.ANNULE)));

        assertThatThrownBy(() -> service.generer(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("annulé");
        verify(generator, never()).generer(any(), any());
    }

    @Test
    @DisplayName("stage en cours : refus avec le statut actuel")
    void stageEnCours() {
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(stagiaire(StageStatus.EN_COURS)));

        assertThatThrownBy(() -> service.generer(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("EN_COURS");
    }

    @Test
    @DisplayName("stagiaire inconnu : 404")
    void stagiaireInconnu() {
        when(stagiaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generer(99L)).isInstanceOf(StagiaireNotFoundException.class);
    }
}
