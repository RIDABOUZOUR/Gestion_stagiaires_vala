package com.vala.gestionStagiaires.export;

/** Fichier CSV prêt à être téléchargé. */
public record ExportCsv(String nomFichier, byte[] contenu, int nombreLignes) {
}
