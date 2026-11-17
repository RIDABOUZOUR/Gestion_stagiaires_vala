package com.vala.gestionStagiaires.export;

import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.repository.CandidatureRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportServiceImpl")
@SuppressWarnings("unchecked")
class ExportServiceImplTest {

    @Mock
    private StagiaireRepository stagiaireRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private CandidatureRepository candidatureRepository;

    private ExportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExportServiceImpl(stagiaireRepository, stageRepository, candidatureRepository,
                Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneOffset.UTC));
    }

    private static String texte(ExportCsv export) {
        return new String(export.contenu(), StandardCharsets.UTF_8).substring(1); // sans le BOM
    }

    @Test
    @DisplayName("exporterStagiaires() : une ligne par stagiaire, tri transmis, nom de fichier daté")
    void exporterStagiaires() {
        Encadrant martin = Encadrant.builder().nom("Martin").prenom("Paul").build();
        Stage stage = Stage.builder().sujet("IA").status(StageStatus.EN_COURS)
                .dateDebut(LocalDate.of(2026, 3, 2)).dateFin(LocalDate.of(2026, 6, 30)).encadrant(martin).build();
        Stagiaire s = Stagiaire.builder().nom("Alami").prenom("Sara").email("sara@mail.com").sexe(Sexe.FEMME)
                .organisme("ENSA").departement(Departement.builder().nom("Informatique").build()).stage(stage).build();
        Sort tri = Sort.by("nom");
        when(stagiaireRepository.count(any(Specification.class))).thenReturn(1L);
        when(stagiaireRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(s)));

        ExportCsv export = service.exporterStagiaires(new StagiaireFiltre("ali", null, null, null, null), tri);

        assertThat(export.nomFichier()).isEqualTo("stagiaires-2026-03-15.csv");
        assertThat(export.nombreLignes()).isEqualTo(1);
        assertThat(texte(export).split("\r\n")).containsExactly(
                "Nom;Prénom;Email;Téléphone;Sexe;Date de naissance;Organisme;Filière;Niveau;Département;Stage;"
                        + "Statut du stage;Début du stage;Fin du stage;Encadrant",
                "Alami;Sara;sara@mail.com;;FEMME;;ENSA;;;Informatique;IA;EN_COURS;02/03/2026;30/06/2026;Martin Paul");
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(stagiaireRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort()).isEqualTo(tri);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("exporterStages() calcule la durée en jours, bornes incluses")
    void exporterStages() {
        Stage stage = Stage.builder().sujet("Web").status(StageStatus.TERMINE)
                .dateDebut(LocalDate.of(2026, 3, 1)).dateFin(LocalDate.of(2026, 3, 31))
                .encadrant(Encadrant.builder().nom("Martin").prenom("Paul").email("p@vala.ma").build()).build();
        when(stageRepository.count(any(Specification.class))).thenReturn(1L);
        when(stageRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(stage)));

        ExportCsv export = service.exporterStages(new StageFiltre(null, null, null), Sort.unsorted());

        assertThat(texte(export)).endsWith("Web;TERMINE;01/03/2026;31/03/2026;31;Martin Paul;p@vala.ma\r\n");
    }

    @Test
    @DisplayName("sans résultat : seulement l'en-tête, sans requête de chargement")
    void exportVide() {
        when(stageRepository.count(any(Specification.class))).thenReturn(0L);

        ExportCsv export = service.exporterStages(new StageFiltre(null, null, null), Sort.unsorted());

        assertThat(export.nombreLignes()).isZero();
        assertThat(texte(export)).isEqualTo("Sujet;Statut;Début;Fin;Durée (jours);Encadrant;Email de l'encadrant\r\n");
        verify(stageRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("au-delà de 10 000 lignes, l'export est refusé")
    void tropDeLignes() {
        when(candidatureRepository.count(any(Specification.class))).thenReturn(10_001L);

        assertThatThrownBy(() -> service.exporterCandidatures(null, Sort.unsorted()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("10001")
                .hasMessageContaining("affinez");
    }
}
