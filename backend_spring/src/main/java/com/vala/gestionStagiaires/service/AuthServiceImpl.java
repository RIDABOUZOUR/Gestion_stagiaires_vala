package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.auth.LoginRequest;
import com.vala.gestionStagiaires.DTOs.auth.LoginResponse;
import com.vala.gestionStagiaires.entities.Utilisateur;
import com.vala.gestionStagiaires.repository.UtilisateurRepository;
import com.vala.gestionStagiaires.securite.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    /** Message unique : ne révèle pas si l'email existe ou si le compte est désactivé. */
    static final String IDENTIFIANTS_INCORRECTS = "Email ou mot de passe incorrect.";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Empreinte factice : un email inconnu coûte le même temps de vérification qu'un email connu. */
    private final String empreinteFactice;

    public AuthServiceImpl(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.empreinteFactice = passwordEncoder.encode("mot-de-passe-factice-" + System.nanoTime());
    }

    @Override
    public LoginResponse connecter(LoginRequest request) {
        Optional<Utilisateur> trouve = utilisateurRepository.findByEmailIgnoreCase(request.email().trim());
        boolean motDePasseValide = passwordEncoder.matches(request.motDePasse(),
                trouve.map(Utilisateur::getMotDePasse).orElse(empreinteFactice));
        Utilisateur utilisateur = trouve.filter(u -> motDePasseValide && u.isActif())
                .orElseThrow(() -> new BadCredentialsException(IDENTIFIANTS_INCORRECTS));

        JwtService.JetonEmis jeton = jwtService.emettre(utilisateur);
        return new LoginResponse(jeton.valeur(), jeton.expiration(), utilisateur.getEmail(), utilisateur.getRole(),
                utilisateur.getEncadrant() != null ? utilisateur.getEncadrant().getId() : null);
    }
}
