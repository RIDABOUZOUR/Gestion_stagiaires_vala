package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.candidature.FichierCv;
import com.vala.gestionStagiaires.exceptions.FichierInvalideException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;

/**
 * Contrôle d'un CV envoyé : PDF uniquement, 5 Mo au plus. Le format est vérifié
 * sur le contenu réel du fichier (signature « %PDF- »), pas sur son extension ni
 * sur le type annoncé par le navigateur, qui peuvent être falsifiés.
 */
public final class CvValidator {

    public static final long TAILLE_MAX = 5L * 1024 * 1024;
    private static final byte[] SIGNATURE_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final int LONGUEUR_MAX_NOM = 100;

    private CvValidator() {
    }

    public static FichierCv valider(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new FichierInvalideException("Le CV est vide.");
        }
        if (fichier.getSize() > TAILLE_MAX) {
            throw new FichierInvalideException("Le CV dépasse 5 Mo.");
        }
        byte[] contenu;
        try {
            contenu = fichier.getBytes();
        } catch (IOException e) {
            throw new FichierInvalideException("Le CV n'a pas pu être lu.");
        }
        return valider(fichier.getOriginalFilename(), contenu);
    }

    public static FichierCv valider(String nomOrigine, byte[] contenu) {
        if (contenu == null || contenu.length == 0) {
            throw new FichierInvalideException("Le CV est vide.");
        }
        if (contenu.length > TAILLE_MAX) {
            throw new FichierInvalideException("Le CV dépasse 5 Mo.");
        }
        if (contenu.length < SIGNATURE_PDF.length
                || !Arrays.equals(Arrays.copyOf(contenu, SIGNATURE_PDF.length), SIGNATURE_PDF)) {
            throw new FichierInvalideException("Le CV doit être un fichier PDF.");
        }
        return new FichierCv(nomAssaini(nomOrigine), contenu);
    }

    /**
     * Nom sûr pour l'en-tête Content-Disposition : sans répertoire, sans accent,
     * limité aux lettres, chiffres, « . », « _ » et « - », terminé par « .pdf ».
     */
    static String nomAssaini(String nomOrigine) {
        String nom = nomOrigine == null ? "" : nomOrigine;
        nom = nom.substring(Math.max(nom.lastIndexOf('/'), nom.lastIndexOf('\\')) + 1);
        nom = Normalizer.normalize(nom, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        nom = nom.replaceAll("[^A-Za-z0-9._-]+", "_").replaceAll("_+", "_").replaceAll("^[._]+", "");
        if (nom.toLowerCase().endsWith(".pdf")) {
            nom = nom.substring(0, nom.length() - 4);
        }
        if (nom.length() > LONGUEUR_MAX_NOM) {
            nom = nom.substring(0, LONGUEUR_MAX_NOM);
        }
        return (nom.isBlank() ? "cv" : nom) + ".pdf";
    }
}
