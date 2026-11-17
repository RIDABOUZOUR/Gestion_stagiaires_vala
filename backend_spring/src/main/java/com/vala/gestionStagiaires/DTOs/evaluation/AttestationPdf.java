package com.vala.gestionStagiaires.DTOs.evaluation;

/** Attestation générée, prête à être téléchargée. */
public record AttestationPdf(String numero, String nomFichier, byte[] contenu) {
}
