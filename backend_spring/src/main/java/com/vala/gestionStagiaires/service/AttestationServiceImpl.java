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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class AttestationServiceImpl implements AttestationService {

    private final AttestationRepository attestationRepository;
    private final StagiaireRepository stagiaireRepository;
    private final AttestationPdfGenerator generator;
    private final Clock clock;

    @Override
    public AttestationPdf generer(Long stagiaireId) {
        Stagiaire stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new StagiaireNotFoundException(stagiaireId));
        Stage stage = stagiaire.getStage();
        StageStatus status = stage != null ? stage.getStatus() : null;
        if (status == StageStatus.ANNULE) {
            throw new BusinessRuleException("Le stage a été annulé : aucune attestation ne peut être délivrée.");
        }
        if (status != StageStatus.TERMINE) {
            throw new BusinessRuleException("L'attestation n'est disponible qu'une fois le stage terminé (statut actuel : "
                    + status + ").");
        }

        Attestation attestation = attestationRepository.findByStagiaireId(stagiaireId)
                .orElseGet(() -> emettre(stagiaire));
        byte[] pdf = generator.generer(attestation, stagiaire);
        return new AttestationPdf(attestation.getNumero(), nomFichier(attestation, stagiaire), pdf);
    }

    /** Attribue le numéro suivant de l'année (ATT-2026-0001, ATT-2026-0002…). */
    private Attestation emettre(Stagiaire stagiaire) {
        LocalDate today = LocalDate.now(clock);
        int annee = today.getYear();
        int rang = attestationRepository.dernierRang(annee) + 1;
        return attestationRepository.save(Attestation.builder()
                .stagiaire(stagiaire)
                .annee(annee)
                .sequence(rang)
                .numero(String.format("ATT-%d-%04d", annee, rang))
                .dateEmission(today)
                .build());
    }

    /** Ex. {@code attestation-ATT-2026-0001-alami-sara.pdf} (ASCII, sans accents ni espaces). */
    static String nomFichier(Attestation attestation, Stagiaire stagiaire) {
        String nom = ((stagiaire.getNom() == null ? "" : stagiaire.getNom()) + " "
                + (stagiaire.getPrenom() == null ? "" : stagiaire.getPrenom()));
        String slug = Normalizer.normalize(nom, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return "attestation-" + attestation.getNumero() + (slug.isEmpty() ? "" : "-" + slug) + ".pdf";
    }
}
