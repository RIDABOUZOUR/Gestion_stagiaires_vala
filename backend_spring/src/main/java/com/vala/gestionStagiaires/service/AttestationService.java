package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.evaluation.AttestationPdf;

public interface AttestationService {

    /**
     * Génère le PDF de l'attestation du stagiaire (stage TERMINE uniquement).
     * Le numéro est attribué à la première génération, puis réutilisé.
     */
    AttestationPdf generer(Long stagiaireId);
}
