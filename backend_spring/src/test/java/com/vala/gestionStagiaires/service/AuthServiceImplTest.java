package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.auth.LoginRequest;
import com.vala.gestionStagiaires.DTOs.auth.LoginResponse;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Utilisateur;
import com.vala.gestionStagiaires.enums.Role;
import com.vala.gestionStagiaires.repository.UtilisateurRepository;
import com.vala.gestionStagiaires.securite.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    /** Coût minimal : tests rapides, même algorithme. */
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(utilisateurRepository, encoder, jwtService);
    }

    private Utilisateur utilisateur(boolean actif) {
        return Utilisateur.builder().id(3L).email("paul@vala.ma").motDePasse(encoder.encode("secret123"))
                .role(Role.ENCADRANT).encadrant(Encadrant.builder().id(9L).build()).actif(actif).build();
    }

    @Test
    @DisplayName("identifiants corrects : jeton, rôle et encadrant renvoyés")
    void connexion() {
        Utilisateur u = utilisateur(true);
        when(utilisateurRepository.findByEmailIgnoreCase("paul@vala.ma")).thenReturn(Optional.of(u));
        when(jwtService.emettre(u)).thenReturn(new JwtService.JetonEmis("jeton", Instant.parse("2026-03-15T18:00:00Z")));

        LoginResponse r = service.connecter(new LoginRequest(" paul@vala.ma ", "secret123"));

        assertThat(r.token()).isEqualTo("jeton");
        assertThat(r.role()).isEqualTo(Role.ENCADRANT);
        assertThat(r.encadrantId()).isEqualTo(9L);
        assertThat(r.toString()).doesNotContain("jeton");
    }

    @Test
    @DisplayName("mauvais mot de passe, email inconnu ou compte désactivé : même message générique")
    void refus() {
        when(utilisateurRepository.findByEmailIgnoreCase("paul@vala.ma")).thenReturn(Optional.of(utilisateur(true)));
        assertThatThrownBy(() -> service.connecter(new LoginRequest("paul@vala.ma", "mauvais")))
                .isInstanceOf(BadCredentialsException.class).hasMessage("Email ou mot de passe incorrect.");

        when(utilisateurRepository.findByEmailIgnoreCase("inconnu@vala.ma")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.connecter(new LoginRequest("inconnu@vala.ma", "secret123")))
                .isInstanceOf(BadCredentialsException.class).hasMessage("Email ou mot de passe incorrect.");

        when(utilisateurRepository.findByEmailIgnoreCase("ancien@vala.ma")).thenReturn(Optional.of(utilisateur(false)));
        assertThatThrownBy(() -> service.connecter(new LoginRequest("ancien@vala.ma", "secret123")))
                .isInstanceOf(BadCredentialsException.class).hasMessage("Email ou mot de passe incorrect.");

        verify(jwtService, never()).emettre(any());
    }

    @Test
    @DisplayName("le mot de passe n'apparaît pas dans toString() (journaux de l'aspect)")
    void motDePasseMasque() {
        assertThat(new LoginRequest("a@b.c", "secret123").toString()).doesNotContain("secret123");
    }
}
