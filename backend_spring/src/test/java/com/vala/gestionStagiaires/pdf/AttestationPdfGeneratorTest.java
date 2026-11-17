package com.vala.gestionStagiaires.pdf;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.vala.gestionStagiaires.config.OrganisationProperties;
import com.vala.gestionStagiaires.entities.Attestation;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Génère un vrai PDF et vérifie son contenu textuel. */
@DisplayName("AttestationPdfGenerator")
class AttestationPdfGeneratorTest {

    private final Attestation attestation = Attestation.builder().numero("ATT-2026-0001")
            .annee(2026).sequence(1).dateEmission(LocalDate.of(2026, 7, 2)).build();

    private static Stagiaire stagiaire(Sexe sexe) {
        Stage stage = Stage.builder().sujet("Plateforme de suivi").status(StageStatus.TERMINE)
                .dateDebut(LocalDate.of(2026, 3, 2)).dateFin(LocalDate.of(2026, 6, 30))
                .encadrant(Encadrant.builder().nom("Martin").prenom("Paul").build()).build();
        return Stagiaire.builder().nom("Alami").prenom("Sara").sexe(sexe)
                .dateNaissance(LocalDate.of(2003, 5, 14)).organisme("ENSA").filiere("Génie Informatique").niveau("Bac+5")
                .departement(Departement.builder().nom("Informatique").build()).stage(stage).build();
    }

    private static String texte(byte[] pdf) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            StringBuilder sb = new StringBuilder();
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                sb.append(extractor.getTextFromPage(page)).append('\n');
            }
            return sb.toString().replaceAll("\\s+", " ");
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("produit un PDF d'une page avec toutes les informations du stage")
    void contenuComplet() throws IOException {
        AttestationPdfGenerator generator = new AttestationPdfGenerator(
                new OrganisationProperties("Vala SARL", "Rabat", "Nadia Bennani", "Directrice des Ressources Humaines"));

        byte[] pdf = generator.generer(attestation, stagiaire(Sexe.FEMME));

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(new PdfReader(pdf).getNumberOfPages()).isEqualTo(1);
        String texte = texte(pdf);
        assertThat(texte)
                .contains("ATTESTATION DE STAGE")
                .contains("N° ATT-2026-0001")
                .contains("Je soussigné(e), Nadia Bennani, Directrice des Ressources Humaines de Vala SARL")
                .contains("Madame Sara ALAMI")
                .contains("née le 14/05/2003")
                .contains("étudiante à ENSA en Génie Informatique (Bac+5)")
                .contains("au département Informatique")
                .contains("du 02/03/2026 au 30/06/2026")
                .contains("« Plateforme de suivi »")
                .contains("Encadrée par : Paul Martin")
                .contains("l'intéressée")
                .contains("Fait à Rabat, le 02/07/2026");
    }

    @Test
    @DisplayName("accorde au masculin et parle au nom de la fonction sans signataire nommé")
    void masculinSansSignataire() {
        AttestationPdfGenerator generator = new AttestationPdfGenerator(
                new OrganisationProperties("Vala SARL", "Rabat", "", "Directeur des Ressources Humaines"));

        String corps = generator.corps(stagiaire(Sexe.HOMME));

        assertThat(corps)
                .startsWith("Le Directeur des Ressources Humaines de Vala SARL atteste que Monsieur Sara ALAMI, né le")
                .contains("étudiant à ENSA");
    }

    @Test
    @DisplayName("omet les informations absentes")
    void informationsAbsentes() {
        AttestationPdfGenerator generator = new AttestationPdfGenerator(
                new OrganisationProperties("Vala SARL", "Rabat", "", "Directeur"));
        Stagiaire s = stagiaire(null);
        s.setDateNaissance(null);
        s.setOrganisme(null);
        s.setDepartement(null);

        assertThat(generator.corps(s)).isEqualTo(
                "Le Directeur de Vala SARL atteste que Sara ALAMI, a effectué un stage au sein de notre organisme, "
                        + "du 02/03/2026 au 30/06/2026.");
    }
}
