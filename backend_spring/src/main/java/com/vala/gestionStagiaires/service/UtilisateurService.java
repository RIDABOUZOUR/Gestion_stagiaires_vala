package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.auth.UtilisateurDto;
import com.vala.gestionStagiaires.DTOs.auth.UtilisateurModification;
import com.vala.gestionStagiaires.DTOs.auth.UtilisateurRequest;

import java.util.List;

public interface UtilisateurService {

    List<UtilisateurDto> lister();

    UtilisateurDto creer(UtilisateurRequest request);

    UtilisateurDto modifier(Long id, UtilisateurModification modification);

    void changerMotDePasse(Long id, String motDePasse);

    /** {@code emailCourant} : un administrateur ne peut pas supprimer son propre compte. */
    void supprimer(Long id, String emailCourant);
}
