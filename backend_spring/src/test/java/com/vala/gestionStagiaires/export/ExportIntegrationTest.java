package com.vala.gestionStagiaires.export;

import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.config.ClockConfig;
import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Export réel sur H2 : mêmes filtres et même tri que les recherches, relations chargées. */
@DataJpaTest
@ActiveProfiles("test")
@Import({ExportServiceImpl.class, ClockConfig.class})
@DisplayName("Export CSV (intégration)")
class ExportIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ExportService exportService;

    @BeforeEach
    void setUp() {
        Departement info = em.persist(Departement.builder().nom("Informatique").responsable("Durand").build());
        Encadrant enc = em.persist(Encadrant.builder().nom("Martin").prenom("Paul").email("p@vala.ma").build());
        Stage stage = em.persist(Stage.builder().sujet("IA").status(StageStatus.EN_COURS)
                .dateDebut(LocalDate.of(2026, 3, 2)).dateFin(LocalDate.of(2026, 6, 30)).encadrant(enc).build());
        em.persist(Stagiaire.builder().nom("Chraibi").prenom("Ali").email("ali@mail.com").sexe(Sexe.HOMME)
                .organisme("ENSA").stage(stage).departement(info).build());
        em.persist(Stagiaire.builder().nom("Alami").prenom("Sara").email("sara@mail.com").sexe(Sexe.FEMME)
                .organisme("=ENSA").stage(stage).departement(info).build());
        em.persist(Stagiaire.builder().nom("Benali").prenom("Omar").email("omar@mail.com").sexe(Sexe.HOMME)
                .organisme("EMSI").stage(stage).departement(info).build());
        em.persist(Candidature.builder().nom("Doukkali").prenom("Lina").email("lina@mail.com").sexe(Sexe.FEMME)
                .statut(CandidatureStatus.SOUMISE).dateSoumission(Instant.parse("2026-03-01T09:30:00Z"))
                .departementSouhaite(info).build());
        em.flush();
        em.clear();
    }

    private static String[] lignes(ExportCsv export) {
        return new String(export.contenu(), StandardCharsets.UTF_8).substring(1).split("\r\n");
    }

    @Test
    @DisplayName("stagiaires filtrés et triés, formule neutralisée, relations présentes")
    void stagiaires() {
        ExportCsv export = exportService.exporterStagiaires(
                new StagiaireFiltre(null, null, null, StageStatus.EN_COURS, null), Sort.by(Sort.Direction.DESC, "nom"));

        String[] lignes = lignes(export);
        assertThat(lignes).hasSize(4);
        assertThat(lignes[1]).startsWith("Chraibi;Ali;").endsWith(";Informatique;IA;EN_COURS;02/03/2026;30/06/2026;Martin Paul");
        assertThat(lignes[2]).startsWith("Benali;");
        assertThat(lignes[3]).startsWith("Alami;Sara;sara@mail.com;;FEMME;;'=ENSA;");
    }

    @Test
    @DisplayName("filtre par mot-clé : seules les lignes correspondantes")
    void stagiairesFiltres() {
        ExportCsv export = exportService.exporterStagiaires(
                new StagiaireFiltre("omar", null, null, null, null), Sort.by("nom"));

        assertThat(export.nombreLignes()).isEqualTo(1);
        assertThat(lignes(export)[1]).startsWith("Benali;Omar;");
    }

    @Test
    @DisplayName("candidatures avec le département souhaité")
    void candidatures() {
        ExportCsv export = exportService.exporterCandidatures(new CandidatureFiltre(null, null, null), Sort.by("nom"));

        assertThat(lignes(export)[1]).startsWith("Doukkali;Lina;lina@mail.com;;;;;Informatique;;;SOUMISE;");
    }
}
