package com.vala.gestionStagiaires.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.vala.gestionStagiaires.config.OrganisationProperties;
import com.vala.gestionStagiaires.entities.Attestation;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.Sexe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Met en page l'attestation de stage (A4, police Helvetica intégrée au PDF :
 * aucune police système n'est nécessaire, y compris dans l'image Docker).
 */
@Component
@RequiredArgsConstructor
public class AttestationPdfGenerator {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Font TITRE = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 12, Font.NORMAL);
    private static final Font GRAS = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font PETIT = new Font(Font.HELVETICA, 10, Font.ITALIC);

    private final OrganisationProperties organisation;

    public byte[] generer(Attestation attestation, Stagiaire stagiaire) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 72, 72, 72, 72);
        try {
            PdfWriter.getInstance(doc, out);
            doc.addTitle("Attestation de stage " + attestation.getNumero());
            doc.addAuthor(organisation.nom());
            doc.addCreator("Gestion des stagiaires");
            doc.open();

            doc.add(paragraphe(organisation.nom(), GRAS, Element.ALIGN_LEFT, 0, 50));
            doc.add(paragraphe("ATTESTATION DE STAGE", TITRE, Element.ALIGN_CENTER, 0, 6));
            doc.add(paragraphe("N° " + attestation.getNumero(), PETIT, Element.ALIGN_CENTER, 0, 40));

            Paragraph corps = paragraphe(corps(stagiaire), NORMAL, Element.ALIGN_JUSTIFIED, 0, 16);
            corps.setLeading(20);
            doc.add(corps);

            Stage stage = stagiaire.getStage();
            if (stage.getSujet() != null && !stage.getSujet().isBlank()) {
                doc.add(paragraphe("Sujet du stage : « " + stage.getSujet().trim() + " »", NORMAL, Element.ALIGN_LEFT, 0, 6));
            }
            String encadrant = nomComplet(stage.getEncadrant());
            if (encadrant != null) {
                doc.add(paragraphe(accord(stagiaire, "Encadré", "Encadrée") + " par : " + encadrant,
                        NORMAL, Element.ALIGN_LEFT, 0, 16));
            }

            doc.add(paragraphe("Cette attestation est délivrée à " + accord(stagiaire, "l'intéressé", "l'intéressée")
                    + " pour servir et valoir ce que de droit.", NORMAL, Element.ALIGN_JUSTIFIED, 10, 40));

            doc.add(paragraphe("Fait à " + organisation.ville() + ", le " + date(attestation.getDateEmission()),
                    NORMAL, Element.ALIGN_RIGHT, 0, 20));
            doc.add(paragraphe(organisation.fonction(), GRAS, Element.ALIGN_RIGHT, 0, 0));
            if (aSignataire()) {
                doc.add(paragraphe(organisation.signataire().trim(), NORMAL, Element.ALIGN_RIGHT, 0, 0));
            }
        } catch (DocumentException e) {
            throw new IllegalStateException("Impossible de générer l'attestation " + attestation.getNumero(), e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
        return out.toByteArray();
    }

    /** Phrase principale ; les informations absentes sont simplement omises. */
    String corps(Stagiaire s) {
        Stage stage = s.getStage();
        StringBuilder phrase = new StringBuilder();
        if (aSignataire()) {
            phrase.append("Je soussigné(e), ").append(organisation.signataire().trim()).append(", ")
                    .append(organisation.fonction()).append(" de ").append(organisation.nom()).append(", atteste que ");
        } else {
            phrase.append("Le ").append(organisation.fonction()).append(" de ").append(organisation.nom())
                    .append(" atteste que ");
        }

        String civilite = s.getSexe() == null ? "" : (s.getSexe() == Sexe.FEMME ? "Madame " : "Monsieur ");
        phrase.append(civilite).append(texte(s.getPrenom())).append(' ')
                .append(texte(s.getNom()).toUpperCase(Locale.FRENCH));

        if (s.getDateNaissance() != null) {
            phrase.append(", ").append(accord(s, "né", "née")).append(" le ").append(date(s.getDateNaissance()));
        }
        if (renseigne(s.getOrganisme())) {
            phrase.append(", ").append(accord(s, "étudiant", "étudiante")).append(" à ").append(s.getOrganisme().trim());
            if (renseigne(s.getFiliere())) {
                phrase.append(" en ").append(s.getFiliere().trim());
            }
            if (renseigne(s.getNiveau())) {
                phrase.append(" (").append(s.getNiveau().trim()).append(')');
            }
        }
        phrase.append(", a effectué un stage au sein de notre organisme");
        if (s.getDepartement() != null && renseigne(s.getDepartement().getNom())) {
            phrase.append(", au département ").append(s.getDepartement().getNom().trim());
        }
        phrase.append(", du ").append(date(stage.getDateDebut())).append(" au ").append(date(stage.getDateFin())).append('.');
        return phrase.toString();
    }

    // ------------------------------------------------------------------

    private boolean aSignataire() {
        return renseigne(organisation.signataire());
    }

    private static Paragraph paragraphe(String texte, Font police, int alignement, float avant, float apres) {
        Paragraph p = new Paragraph(texte, police);
        p.setAlignment(alignement);
        p.setSpacingBefore(avant);
        p.setSpacingAfter(apres);
        return p;
    }

    private static String accord(Stagiaire s, String masculin, String feminin) {
        return s.getSexe() == Sexe.FEMME ? feminin : masculin;
    }

    private static String nomComplet(Encadrant e) {
        if (e == null) {
            return null;
        }
        String nom = (texte(e.getPrenom()) + " " + texte(e.getNom())).trim();
        return nom.isEmpty() ? null : nom;
    }

    private static String date(LocalDate date) {
        return date == null ? "—" : FORMAT_DATE.format(date);
    }

    private static boolean renseigne(String valeur) {
        return valeur != null && !valeur.isBlank();
    }

    private static String texte(String valeur) {
        return valeur == null ? "" : valeur.trim();
    }
}
