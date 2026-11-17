package com.vala.gestionStagiaires.export;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Écrit un fichier CSV lisible directement par Excel en français :
 * BOM UTF-8, séparateur « ; », fins de ligne CRLF, dates jj/mm/aaaa, décimales à virgule.
 *
 * <p>Protection contre l'injection de formules (CSV injection) : une cellule texte
 * qui commence par {@code = + - @}, une tabulation ou un retour chariot est
 * préfixée d'une apostrophe, pour que le tableur l'affiche comme du texte au lieu
 * de l'exécuter.
 */
public class CsvWriter {

    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final char SEPARATEUR = ';';
    private static final String FIN_DE_LIGNE = "\r\n";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final StringBuilder contenu = new StringBuilder();
    private final int nombreColonnes;
    private final ZoneId zone;
    private int lignes;

    public CsvWriter(List<String> entetes, ZoneId zone) {
        this.nombreColonnes = entetes.size();
        this.zone = zone;
        ecrire(entetes.toArray());
    }

    /** Ajoute une ligne ; il doit y avoir autant de valeurs que d'en-têtes. */
    public CsvWriter ligne(Object... valeurs) {
        if (valeurs.length != nombreColonnes) {
            throw new IllegalArgumentException("Ligne de " + valeurs.length + " valeurs pour " + nombreColonnes + " colonnes");
        }
        ecrire(valeurs);
        lignes++;
        return this;
    }

    /** Nombre de lignes de données (sans l'en-tête). */
    public int nombreLignes() {
        return lignes;
    }

    public byte[] toBytes() {
        byte[] texte = contenu.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream(BOM.length + texte.length);
        out.writeBytes(BOM);
        out.writeBytes(texte);
        return out.toByteArray();
    }

    // ------------------------------------------------------------------

    private void ecrire(Object[] valeurs) {
        for (int i = 0; i < valeurs.length; i++) {
            if (i > 0) {
                contenu.append(SEPARATEUR);
            }
            contenu.append(cellule(valeurs[i]));
        }
        contenu.append(FIN_DE_LIGNE);
    }

    String cellule(Object valeur) {
        if (valeur == null) {
            return "";
        }
        String texte;
        if (valeur instanceof LocalDate date) {
            texte = DATE.format(date);
        } else if (valeur instanceof Instant instant) {
            texte = DATE_HEURE.format(instant.atZone(zone));
        } else if (valeur instanceof BigDecimal || valeur instanceof Double || valeur instanceof Float) {
            texte = valeur.toString().replace('.', ',');
        } else if (valeur instanceof Number || valeur instanceof Boolean) {
            texte = valeur.toString();
        } else {
            texte = neutraliser(valeur.toString());
        }
        return echapper(texte);
    }

    /** Empêche le tableur d'interpréter une cellule texte comme une formule. */
    static String neutraliser(String texte) {
        if (!texte.isEmpty() && "=+-@\t\r".indexOf(texte.charAt(0)) >= 0) {
            return "'" + texte;
        }
        return texte;
    }

    /** Entoure de guillemets (en doublant ceux du texte) si la cellule contient un caractère spécial. */
    static String echapper(String texte) {
        if (texte.indexOf(SEPARATEUR) >= 0 || texte.indexOf('"') >= 0 || texte.indexOf('\n') >= 0 || texte.indexOf('\r') >= 0) {
            return '"' + texte.replace("\"", "\"\"") + '"';
        }
        return texte;
    }
}
