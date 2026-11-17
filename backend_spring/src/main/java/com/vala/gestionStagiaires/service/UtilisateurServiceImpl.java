package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.auth.UtilisateurDto;
import com.vala.gestionStagiaires.DTOs.auth.UtilisateurModification;
import com.vala.gestionStagiaires.DTOs.auth.UtilisateurRequest;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Utilisateur;
import com.vala.gestionStagiaires.enums.Role;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final EncadrantRepository encadrantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurDto> lister() {
        return utilisateurRepository.findAllByOrderByEmailAsc().stream().map(UtilisateurDto::of).toList();
    }

    @Override
    public UtilisateurDto creer(UtilisateurRequest request) {
        String email = request.email().trim();
        if (utilisateurRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Un compte existe déjà pour l'email " + email + ".");
        }
        Utilisateur utilisateur = Utilisateur.builder()
                .email(email)
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .role(request.role())
                .encadrant(encadrantPourRole(request.role(), request.encadrantId(), null))
                .actif(true)
                .build();
        return UtilisateurDto.of(utilisateurRepository.save(utilisateur));
    }

    @Override
    public UtilisateurDto modifier(Long id, UtilisateurModification modification) {
        Utilisateur utilisateur = findUtilisateur(id);
        boolean resteAdminActif = modification.role() == Role.ADMIN && Boolean.TRUE.equals(modification.actif());
        if (estDernierAdminActif(utilisateur) && !resteAdminActif) {
            throw new BusinessRuleException("Impossible : c'est le dernier compte administrateur actif.");
        }
        utilisateur.setRole(modification.role());
        utilisateur.setEncadrant(encadrantPourRole(modification.role(), modification.encadrantId(), id));
        utilisateur.setActif(Boolean.TRUE.equals(modification.actif()));
        return UtilisateurDto.of(utilisateurRepository.save(utilisateur));
    }

    @Override
    public void changerMotDePasse(Long id, String motDePasse) {
        Utilisateur utilisateur = findUtilisateur(id);
        utilisateur.setMotDePasse(passwordEncoder.encode(motDePasse));
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public void supprimer(Long id, String emailCourant) {
        Utilisateur utilisateur = findUtilisateur(id);
        if (utilisateur.getEmail().equalsIgnoreCase(emailCourant)) {
            throw new BusinessRuleException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        if (estDernierAdminActif(utilisateur)) {
            throw new BusinessRuleException("Impossible : c'est le dernier compte administrateur actif.");
        }
        utilisateurRepository.delete(utilisateur);
    }

    // ------------------------------------------------------------------

    private boolean estDernierAdminActif(Utilisateur u) {
        return u.getRole() == Role.ADMIN && u.isActif() && utilisateurRepository.countByRoleAndActifTrue(Role.ADMIN) <= 1;
    }

    /** Un compte ENCADRANT est lié à une fiche encadrant (une seule par compte) ; les autres rôles, à aucune. */
    private Encadrant encadrantPourRole(Role role, Long encadrantId, Long idCourant) {
        if (role != Role.ENCADRANT) {
            if (encadrantId != null) {
                throw new BusinessRuleException("Seul un compte ENCADRANT peut être lié à une fiche encadrant.");
            }
            return null;
        }
        if (encadrantId == null) {
            throw new BusinessRuleException("Un compte ENCADRANT doit être lié à une fiche encadrant.");
        }
        boolean dejaLie = idCourant == null
                ? utilisateurRepository.existsByEncadrantId(encadrantId)
                : utilisateurRepository.existsByEncadrantIdAndIdNot(encadrantId, idCourant);
        if (dejaLie) {
            throw new BusinessRuleException("Cet encadrant a déjà un compte utilisateur.");
        }
        return encadrantRepository.findById(encadrantId)
                .orElseThrow(() -> new ResourceNotFoundException("Encadrant", encadrantId));
    }

    private Utilisateur findUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }
}
