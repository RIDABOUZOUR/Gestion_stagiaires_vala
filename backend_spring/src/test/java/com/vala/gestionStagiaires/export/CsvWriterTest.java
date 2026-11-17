package com.vala.gestionStagiaires.export;

import com.vala.gestionStagiaires.enums.StageStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CsvWriter")
class CsvWriterTest {

    private static String texte(byte[] csv) {
        return new String(Arrays.copyOfRange(csv, 3, csv.length), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("commence par le BOM UTF-8, sépare par « ; » et termine les lignes par CRLF")
    void format() {
        byte[] csv = new CsvWriter(List.of("Nom", "Prénom"), ZoneOffset.UTC).ligne("Alami", "Sara").toBytes();

        assertThat(Arrays.copyOfRange(csv, 0, 3)).containsExactly(0xEF, 0xBB, 0xBF);
        assertThat(texte(csv)).isEqualTo("Nom;Prénom\r\nAlami;Sara\r\n");
    }

    @Test
    @DisplayName("formate dates, instants, décimales, énumérations et valeurs nulles")
    void formatageDesValeurs() {
        CsvWriter csv = new CsvWriter(List.of("a", "b", "c", "d", "e", "f"), ZoneOffset.UTC)
                .ligne(LocalDate.of(2026, 3, 2), Instant.parse("2026-03-02T14:05:00Z"), new BigDecimal("15.5"),
                        122L, StageStatus.EN_COURS, null);

        assertThat(texte(csv.toBytes())).endsWith("02/03/2026;02/03/2026 14:05;15,5;122;EN_COURS;\r\n");
        assertThat(csv.nombreLignes()).isEqualTo(1);
    }

    @Test
    @DisplayName("entoure de guillemets les cellules contenant « ; », des guillemets ou un saut de ligne")
    void echappement() {
        byte[] csv = new CsvWriter(List.of("a", "b", "c"), ZoneOffset.UTC)
                .ligne("Rabat; Maroc", "Le \"meilleur\"", "ligne 1\nligne 2").toBytes();

        assertThat(texte(csv)).endsWith("\"Rabat; Maroc\";\"Le \"\"meilleur\"\"\";\"ligne 1\nligne 2\"\r\n");
    }

    @ParameterizedTest(name = "« {0} » est neutralisée")
    @ValueSource(strings = {"=CMD()", "+33 6 12", "-2+3", "@SUM(A1)", "\tcache", "\rcache"})
    void injectionDeFormules(String dangereux) {
        assertThat(CsvWriter.neutraliser(dangereux)).isEqualTo("'" + dangereux);
    }

    @Test
    @DisplayName("une formule est neutralisée puis échappée si besoin ; un nombre négatif n'est pas modifié")
    void injectionDansUneLigne() {
        byte[] csv = new CsvWriter(List.of("a", "b", "c"), ZoneOffset.UTC)
                .ligne("=HYPERLINK(\"http://x\";\"clic\")", -5, "texte normal").toBytes();

        assertThat(texte(csv)).endsWith("\"'=HYPERLINK(\"\"http://x\"\";\"\"clic\"\")\";-5;texte normal\r\n");
    }

    @Test
    @DisplayName("refuse une ligne qui n'a pas le bon nombre de colonnes")
    void nombreDeColonnes() {
        CsvWriter csv = new CsvWriter(List.of("a", "b"), ZoneOffset.UTC);

        assertThatThrownBy(() -> csv.ligne("seul")).isInstanceOf(IllegalArgumentException.class);
    }
}
