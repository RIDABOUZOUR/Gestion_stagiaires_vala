package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.candidature.FichierCv;
import com.vala.gestionStagiaires.exceptions.FichierInvalideException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CvValidator")
class CvValidatorTest {

    private static final byte[] PDF = "%PDF-1.7\n1 0 obj".getBytes();

    @Test
    @DisplayName("accepte un vrai PDF et conserve son contenu")
    void pdfValide() {
        FichierCv cv = CvValidator.valider(new MockMultipartFile("cv", "cv-sara.pdf", "application/pdf", PDF));

        assertThat(cv.nomFichier()).isEqualTo("cv-sara.pdf");
        assertThat(cv.contenu()).isEqualTo(PDF);
    }

    @Test
    @DisplayName("refuse un fichier nommé .pdf et annoncé PDF dont le contenu n'en est pas un")
    void fauxPdf() {
        MockMultipartFile faux = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "<html>piège</html>".getBytes());

        assertThatThrownBy(() -> CvValidator.valider(faux))
                .isInstanceOf(FichierInvalideException.class)
                .hasMessage("Le CV doit être un fichier PDF.");
    }

    @Test
    @DisplayName("refuse un fichier vide ou absent")
    void vide() {
        assertThatThrownBy(() -> CvValidator.valider(new MockMultipartFile("cv", "cv.pdf", "application/pdf", new byte[0])))
                .isInstanceOf(FichierInvalideException.class).hasMessage("Le CV est vide.");
        assertThatThrownBy(() -> CvValidator.valider((org.springframework.web.multipart.MultipartFile) null))
                .isInstanceOf(FichierInvalideException.class);
    }

    @Test
    @DisplayName("refuse un fichier de plus de 5 Mo")
    void tropLourd() {
        byte[] lourd = Arrays.copyOf(PDF, (int) CvValidator.TAILLE_MAX + 1);

        assertThatThrownBy(() -> CvValidator.valider(new MockMultipartFile("cv", "cv.pdf", "application/pdf", lourd)))
                .isInstanceOf(FichierInvalideException.class).hasMessage("Le CV dépasse 5 Mo.");
    }

    @ParameterizedTest(name = "« {0} » → « {1} »")
    @CsvSource(delimiter = '|', value = {
            "../../etc/passwd.pdf|passwd.pdf",
            "C:\\Users\\sara\\Mon CV été.PDF|Mon_CV_ete.pdf",
            "cv\";.pdf|cv_.pdf",
            ".htaccess|htaccess.pdf",
            "''|cv.pdf",
            "rapport.docx|rapport.docx.pdf"})
    void nomAssaini(String origine, String attendu) {
        assertThat(CvValidator.nomAssaini(origine)).isEqualTo(attendu);
    }
}
