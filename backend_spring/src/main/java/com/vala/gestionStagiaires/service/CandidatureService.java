package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.candidature.AcceptationRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureDto;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureStatistiques;
import com.vala.gestionStagiaires.DTOs.candidature.DepartementPublic;
import com.vala.gestionStagiaires.DTOs.candidature.FichierCv;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CandidatureService {

    /** Enregistre la candidature et, s'il est fourni, son CV (déjà validé), dans la même transaction. */
    CandidatureDto soumettre(CandidatureRequest request, FichierCv cv);

    /** Ajoute ou remplace le CV d'une candidature encore en cours de traitement. */
    CandidatureDto remplacerCv(Long id, FichierCv cv);

    /** CV de la candidature ; 404 s'il n'y en a pas. */
    FichierCv cv(Long id);

    /** Départements proposés dans le formulaire public, triés par nom. */
    List<DepartementPublic> departementsPublics();

    CandidatureDto getCandidature(Long id);

    PageResponse<CandidatureDto> rechercher(CandidatureFiltre filtre, Pageable pageable);

    /** Modifiable tant qu'elle est en cours de traitement (SOUMISE ou EN_ETUDE). */
    CandidatureDto modifier(Long id, CandidatureRequest request);

    /** Une candidature acceptée ne peut pas être supprimée (elle est liée au stagiaire). */
    void supprimer(Long id);

    CandidatureDto etudier(Long id);

    CandidatureDto refuser(Long id, String motif);

    CandidatureDto retirer(Long id);

    /** Crée le stagiaire (mêmes règles qu'une création directe) et l'affecte au stage. */
    CandidatureDto accepter(Long id, AcceptationRequest request);

    CandidatureStatistiques statistiques();
}
