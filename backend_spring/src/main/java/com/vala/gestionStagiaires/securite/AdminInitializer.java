package com.vala.gestionStagiaires.securite;

import com.vala.gestionStagiaires.entities.Utilisateur;
import com.vala.gestionStagiaires.enums.Role;
import com.vala.gestionStagiaires.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Au premier démarrage (aucun utilisateur en base), crée le compte administrateur
 * défini par ADMIN_EMAIL et ADMIN_PASSWORD. Sans ces variables, signale qu'aucune
 * connexion ne sera possible.
 */
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);
    static final int LONGUEUR_MIN_MOT_DE_PASSE = 8;

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecuriteProperties proprietes;

    @Override
    public void run(ApplicationArguments args) {
        if (utilisateurRepository.count() > 0) {
            return;
        }
        String email = proprietes.adminEmail();
        String motDePasse = proprietes.adminMotDePasse();
        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            log.warn("Aucun utilisateur en base : définissez ADMIN_EMAIL et ADMIN_PASSWORD pour créer le compte administrateur.");
            return;
        }
        if (motDePasse.length() < LONGUEUR_MIN_MOT_DE_PASSE) {
            log.warn("ADMIN_PASSWORD doit contenir au moins {} caractères : compte administrateur non créé.", LONGUEUR_MIN_MOT_DE_PASSE);
            return;
        }
        utilisateurRepository.save(Utilisateur.builder()
                .email(email.trim())
                .motDePasse(passwordEncoder.encode(motDePasse))
                .role(Role.ADMIN)
                .actif(true)
                .build());
        log.info("Compte administrateur initial créé : {}", email.trim());
    }
}
