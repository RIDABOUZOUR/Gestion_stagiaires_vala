package com.vala.gestionStagiaires.DTOs.candidature;

/** CV validé, prêt à être enregistré ou renvoyé au navigateur. */
public record FichierCv(String nomFichier, byte[] contenu) {

    public long taille() {
        return contenu.length;
    }

    /** Pas de contenu binaire dans les journaux (aspect de journalisation en DEBUG). */
    @Override
    public String toString() {
        return "FichierCv[nomFichier=" + nomFichier + ", taille=" + contenu.length + " octets]";
    }
}
