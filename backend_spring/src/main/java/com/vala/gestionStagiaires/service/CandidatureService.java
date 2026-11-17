package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.candidature.AcceptationRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureDto;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureStatistiques;
import org.springframework.data.domain.Pageable;

public interface CandidatureService {

    CandidatureDto soumettre(CandidatureRequest request);

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
