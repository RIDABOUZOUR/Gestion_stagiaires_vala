package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.DTOs.candidature.AcceptationRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureDto;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureStatistiques;
import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.CandidatureRepository;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatutCandidature;
import com.vala.gestionStagiaires.repository.projection.DelaiDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CandidatureServiceImpl")
class CandidatureServiceImplTest {

    private static final Instant MAINTENANT = Instant.parse("2026-03-15T10:00:00Z");

    @Mock
    private CandidatureRepository candidatureRepository;

    @Mock
    private DepartementRepository departementRepository;

    @Mock
    private StagiaireRepository stagiaireRepository;

    @Mock
    private StagiaireService stagiaireService;

    private CandidatureServiceImpl service;

    private final Departement info = Departement.builder().id(2L).nom("Informatique").build();

    @BeforeEach
    void setUp() {
        service = new CandidatureServiceImpl(candidatureRepository, departementRepository, stagiaireRepository,
                stagiaireService, Clock.fixed(MAINTENANT, ZoneOffset.UTC));
    }

    private static CandidatureRequest requete(Long departementId) {
        return new CandidatureRequest(" Alami ", "Sara", "sara@mail.com", "0612345678", LocalDate.of(2003, 5, 14),
                Sexe.FEMME, "ENSA", "Bac+5", "Génie Info", departementId,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 7, 31), "Motivée");
    }

    private Candidature candidature(CandidatureStatus statut) {
        return Candidature.builder().id(1L).nom("Alami").prenom("Sara").email("sara@mail.com").sexe(Sexe.FEMME)
                .organisme("ENSA").niveau("Bac+5").filiere("Génie Info").dateNaissance(LocalDate.of(2003, 5, 14))
                .departementSouhaite(info).statut(statut).dateSoumission(MAINTENANT.minusSeconds(86400)).build();
    }

    private void existe(Candidature c) {
        when(candidatureRepository.findById(1L)).thenReturn(Optional.of(c));
    }

    @Nested
    @DisplayName("soumettre() et modifier()")
    class Depot {

        @Test
        @DisplayName("soumettre() crée une candidature SOUMISE datée, avec le département souhaité")
        void soumettre() {
            when(departementRepository.findById(2L)).thenReturn(Optional.of(info));
            when(candidatureRepository.save(any(Candidature.class))).thenAnswer(inv -> inv.getArgument(0));

            CandidatureDto dto = service.soumettre(requete(2L));

            assertThat(dto.statut()).isEqualTo(CandidatureStatus.SOUMISE);
            assertThat(dto.dateSoumission()).isEqualTo(MAINTENANT);
            assertThat(dto.nom()).isEqualTo("Alami");
            assertThat(dto.departementSouhaiteNom()).isEqualTo("Informatique");
        }

        @Test
        @DisplayName("soumettre() refuse une 2e candidature active pour le même email")
        void soumettre_doublon() {
            when(candidatureRepository.existsByEmailIgnoreCaseAndStatutIn("sara@mail.com", CandidatureStatus.ACTIVES))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.soumettre(requete(null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("déjà en cours");
            verify(candidatureRepository, never()).save(any());
        }

        @Test
        @DisplayName("soumettre() lève ResourceNotFoundException pour un département inconnu")
        void soumettre_departementInconnu() {
            when(departementRepository.findById(9L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.soumettre(requete(9L))).isInstanceOf(ResourceNotFoundException.class);
        }

        @ParameterizedTest(name = "candidature {0} : modification refusée")
        @EnumSource(value = CandidatureStatus.class, names = {"ACCEPTEE", "REFUSEE", "RETIREE"})
        void modifier_candidatureTraitee(CandidatureStatus statut) {
            existe(candidature(statut));

            assertThatThrownBy(() -> service.modifier(1L, requete(null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining(statut.name());
        }

        @Test
        @DisplayName("modifier() refuse l'email d'une autre candidature active")
        void modifier_emailPris() {
            existe(candidature(CandidatureStatus.SOUMISE));
            when(candidatureRepository.existsByEmailIgnoreCaseAndStatutInAndIdNot("sara@mail.com", CandidatureStatus.ACTIVES, 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.modifier(1L, requete(null))).isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("supprimer() refuse une candidature acceptée")
        void supprimer_acceptee() {
            existe(candidature(CandidatureStatus.ACCEPTEE));

            assertThatThrownBy(() -> service.supprimer(1L)).isInstanceOf(BusinessRuleException.class);
            verify(candidatureRepository, never()).delete(any(Candidature.class));
        }

        @Test
        @DisplayName("supprimer() supprime une candidature refusée")
        void supprimer_refusee() {
            Candidature c = candidature(CandidatureStatus.REFUSEE);
            existe(c);

            service.supprimer(1L);

            verify(candidatureRepository).delete(c);
        }
    }

    @Nested
    @DisplayName("Workflow")
    class Workflow {

        @ParameterizedTest(name = "{0} → {1} autorisé")
        @CsvSource({"SOUMISE, EN_ETUDE", "SOUMISE, REFUSEE", "SOUMISE, RETIREE", "EN_ETUDE, REFUSEE", "EN_ETUDE, RETIREE"})
        void transitionsValides(CandidatureStatus depart, CandidatureStatus cible) {
            Candidature c = candidature(depart);
            existe(c);
            when(candidatureRepository.save(c)).thenReturn(c);

            switch (cible) {
                case EN_ETUDE -> service.etudier(1L);
                case REFUSEE -> service.refuser(1L, "Profil incomplet");
                case RETIREE -> service.retirer(1L);
                default -> throw new IllegalArgumentException();
            }

            assertThat(c.getStatut()).isEqualTo(cible);
            assertThat(c.getDateDecision()).isEqualTo(cible == CandidatureStatus.EN_ETUDE ? null : MAINTENANT);
        }

        @ParameterizedTest(name = "{0} → EN_ETUDE refusé")
        @EnumSource(value = CandidatureStatus.class, names = {"EN_ETUDE", "ACCEPTEE", "REFUSEE", "RETIREE"})
        void etudier_interdit(CandidatureStatus depart) {
            existe(candidature(depart));

            assertThatThrownBy(() -> service.etudier(1L)).isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("refuser() exige un motif et l'enregistre")
        void refuser() {
            assertThatThrownBy(() -> service.refuser(1L, " ")).isInstanceOf(BusinessRuleException.class);

            Candidature c = candidature(CandidatureStatus.EN_ETUDE);
            existe(c);
            when(candidatureRepository.save(c)).thenReturn(c);
            service.refuser(1L, " Période indisponible ");
            assertThat(c.getMotifRefus()).isEqualTo("Période indisponible");
        }
    }

    @Nested
    @DisplayName("accepter()")
    class Accepter {

        @Test
        @DisplayName("crée le stagiaire à partir de la candidature et l'y lie")
        void accepter() {
            Candidature c = candidature(CandidatureStatus.EN_ETUDE);
            existe(c);
            StagiaireDto cree = new StagiaireDto();
            cree.setId(40L);
            when(stagiaireService.addStagiare(any(StagiaireDto.class))).thenReturn(cree);
            Stagiaire reference = Stagiaire.builder().id(40L).build();
            when(stagiaireRepository.getReferenceById(40L)).thenReturn(reference);
            when(candidatureRepository.save(c)).thenReturn(c);

            CandidatureDto dto = service.accepter(1L, new AcceptationRequest(7L, null));

            ArgumentCaptor<StagiaireDto> captor = ArgumentCaptor.forClass(StagiaireDto.class);
            verify(stagiaireService).addStagiare(captor.capture());
            StagiaireDto envoye = captor.getValue();
            assertThat(envoye.getNom()).isEqualTo("Alami");
            assertThat(envoye.getEmail()).isEqualTo("sara@mail.com");
            assertThat(envoye.getSexe()).isEqualTo(Sexe.FEMME);
            assertThat(envoye.getFiliere()).isEqualTo("Génie Info");
            assertThat(envoye.getStage().getId()).isEqualTo(7L);
            assertThat(envoye.getDepartement().getId()).isEqualTo(2L); // département souhaité par défaut

            assertThat(dto.statut()).isEqualTo(CandidatureStatus.ACCEPTEE);
            assertThat(dto.stagiaireId()).isEqualTo(40L);
            assertThat(c.getDateDecision()).isEqualTo(MAINTENANT);
        }

        @Test
        @DisplayName("le département indiqué remplace le département souhaité")
        void accepter_autreDepartement() {
            existe(candidature(CandidatureStatus.EN_ETUDE));
            StagiaireDto cree = new StagiaireDto();
            cree.setId(40L);
            when(stagiaireService.addStagiare(any(StagiaireDto.class))).thenReturn(cree);
            when(candidatureRepository.save(any(Candidature.class))).thenAnswer(inv -> inv.getArgument(0));

            service.accepter(1L, new AcceptationRequest(7L, 5L));

            ArgumentCaptor<StagiaireDto> captor = ArgumentCaptor.forClass(StagiaireDto.class);
            verify(stagiaireService).addStagiare(captor.capture());
            assertThat(captor.getValue().getDepartement().getId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("refuse une candidature qui n'a pas été mise à l'étude")
        void accepter_soumise() {
            existe(candidature(CandidatureStatus.SOUMISE));

            assertThatThrownBy(() -> service.accepter(1L, new AcceptationRequest(7L, null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("mise à l'étude");
            verify(stagiaireService, never()).addStagiare(any());
        }

        @Test
        @DisplayName("exige un département si le candidat n'en a pas souhaité")
        void accepter_sansDepartement() {
            Candidature c = candidature(CandidatureStatus.EN_ETUDE);
            c.setDepartementSouhaite(null);
            existe(c);

            assertThatThrownBy(() -> service.accepter(1L, new AcceptationRequest(7L, null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("département");
        }

        @Test
        @DisplayName("propage le refus des règles du stagiaire (ex. stage terminé) sans lier de stagiaire")
        void accepter_regleStagiaire() {
            Candidature c = candidature(CandidatureStatus.EN_ETUDE);
            existe(c);
            when(stagiaireService.addStagiare(any(StagiaireDto.class)))
                    .thenThrow(new BusinessRuleException("Impossible d'affecter un stagiaire à un stage TERMINE."));

            assertThatThrownBy(() -> service.accepter(1L, new AcceptationRequest(7L, null)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("TERMINE");
            verify(stagiaireRepository, never()).getReferenceById(anyLong());
            verify(candidatureRepository, never()).save(any());
            assertThat(c.getStatut()).isEqualTo(CandidatureStatus.EN_ETUDE);
            assertThat(c.getDateDecision()).isNull();
        }
    }

    @Test
    @DisplayName("statistiques() : statuts, 12 mois, taux d'acceptation, délai moyen, organismes")
    void statistiques() {
        when(candidatureRepository.compterParStatut()).thenReturn(List.of(
                new ComptageParStatutCandidature(CandidatureStatus.SOUMISE, 4L),
                new ComptageParStatutCandidature(CandidatureStatus.ACCEPTEE, 3L),
                new ComptageParStatutCandidature(CandidatureStatus.REFUSEE, 1L)));
        when(candidatureRepository.datesSoumissionDepuis(Instant.parse("2025-04-01T00:00:00Z"))).thenReturn(List.of(
                Instant.parse("2025-04-01T00:00:00Z"), Instant.parse("2026-03-01T08:00:00Z"),
                Instant.parse("2026-03-14T08:00:00Z")));
        when(candidatureRepository.delaisDecision(any())).thenReturn(List.of(
                new DelaiDecision(Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-03-03T00:00:00Z")),
                new DelaiDecision(Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-03-08T00:00:00Z"))));
        when(candidatureRepository.compterParOrganisme()).thenReturn(List.of(
                new ComptageParLibelle("EMSI", 2L), new ComptageParLibelle("ENSA", 5L), new ComptageParLibelle(null, 1L)));

        CandidatureStatistiques stats = service.statistiques();

        assertThat(stats.total()).isEqualTo(8);
        assertThat(stats.parStatut()).containsEntry(CandidatureStatus.EN_ETUDE, 0L).containsEntry(CandidatureStatus.SOUMISE, 4L);
        assertThat(stats.parMois()).hasSize(12);
        assertThat(stats.parMois().keySet()).first().isEqualTo("2025-04");
        assertThat(stats.parMois()).containsEntry("2025-04", 1L).containsEntry("2026-03", 2L).containsEntry("2025-10", 0L);
        assertThat(stats.tauxAcceptation()).isEqualTo(75.0);
        assertThat(stats.delaiMoyenDecisionJours()).isEqualTo(4.5);
        assertThat(stats.parOrganisme()).containsExactly(entry("ENSA", 5L), entry("EMSI", 2L), entry("Non renseigné", 1L));
    }

    @Test
    @DisplayName("statistiques() sans décision : taux et délai nuls")
    void statistiques_sansDecision() {
        when(candidatureRepository.compterParStatut()).thenReturn(List.of());
        when(candidatureRepository.datesSoumissionDepuis(any())).thenReturn(List.of());
        when(candidatureRepository.delaisDecision(any())).thenReturn(List.of());
        when(candidatureRepository.compterParOrganisme()).thenReturn(List.of());

        CandidatureStatistiques stats = service.statistiques();

        assertThat(stats.total()).isZero();
        assertThat(stats.tauxAcceptation()).isNull();
        assertThat(stats.delaiMoyenDecisionJours()).isNull();
    }
}
