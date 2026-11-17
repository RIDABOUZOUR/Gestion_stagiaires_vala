package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.entities.CvCandidature;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.Sexe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/** Stockage du CV en base (H2), dont un fichier proche de la taille maximale. */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Stockage des CV")
class CvCandidatureRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CvCandidatureRepository repository;

    @Test
    @DisplayName("enregistre un CV de 5 Mo, le relit à l'identique puis le supprime avec la candidature")
    void cycleDeVie() {
        Candidature candidature = em.persist(Candidature.builder().nom("Alami").prenom("Sara").email("sara@mail.com")
                .sexe(Sexe.FEMME).statut(CandidatureStatus.SOUMISE).dateSoumission(Instant.now()).cvFourni(true).build());
        byte[] contenu = Arrays.copyOf("%PDF-1.7".getBytes(), 5 * 1024 * 1024);
        contenu[contenu.length - 1] = 42;
        em.persist(CvCandidature.builder().candidature(candidature).nomFichier("cv.pdf").taille(contenu.length)
                .contenu(contenu).dateDepot(Instant.now()).build());
        em.flush();
        em.clear();

        CvCandidature relu = repository.findByCandidatureId(candidature.getId()).orElseThrow();
        assertThat(relu.getContenu()).hasSize(5 * 1024 * 1024);
        assertThat(relu.getContenu()[contenu.length - 1]).isEqualTo((byte) 42);

        repository.supprimerParCandidature(candidature.getId());
        assertThat(repository.findByCandidatureId(candidature.getId())).isEmpty();
    }
}
