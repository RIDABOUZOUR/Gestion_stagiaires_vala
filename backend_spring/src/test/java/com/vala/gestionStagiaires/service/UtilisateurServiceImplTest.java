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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UtilisateurServiceImpl")
class UtilisateurServiceImplTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private EncadrantRepository encadrantRepository;

    private UtilisateurServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UtilisateurServiceImpl(utilisateurRepository, encadrantRepository, encoder);
    }

    private static Utilisateur compte(long id, String email, Role role, boolean actif) {
        return Utilisateur.builder().id(id).email(email).motDePasse("x").role(role).actif(actif).build();
    }

    @Test
    @DisplayName("creer() hache le mot de passe et lie l'encadrant d'un compte ENCADRANT")
    void creerEncadrant() {
        Encadrant martin = Encadrant.builder().id(9L).nom("Martin").prenom("Paul").build();
        when(encadrantRepository.findById(9L)).thenReturn(Optional.of(martin));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        UtilisateurDto dto = service.creer(new UtilisateurRequest(" paul@vala.ma ", "secret123", Role.ENCADRANT, 9L));

        ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(captor.capture());
        assertThat(captor.getValue().getMotDePasse()).isNotEqualTo("secret123");
        assertThat(encoder.matches("secret123", captor.getValue().getMotDePasse())).isTrue();
        assertThat(dto.email()).isEqualTo("paul@vala.ma");
        assertThat(dto.encadrantNom()).isEqualTo("Martin Paul");
        assertThat(dto.actif()).isTrue();
    }

    @Test
    @DisplayName("creer() : email déjà pris, encadrant manquant, déjà lié ou interdit pour RH")
    void creerRefus() {
        when(utilisateurRepository.existsByEmailIgnoreCase("pris@vala.ma")).thenReturn(true);
        assertThatThrownBy(() -> service.creer(new UtilisateurRequest("pris@vala.ma", "secret123", Role.RH, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("existe déjà");

        assertThatThrownBy(() -> service.creer(new UtilisateurRequest("a@vala.ma", "secret123", Role.ENCADRANT, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("doit être lié");

        when(utilisateurRepository.existsByEncadrantId(9L)).thenReturn(true);
        assertThatThrownBy(() -> service.creer(new UtilisateurRequest("b@vala.ma", "secret123", Role.ENCADRANT, 9L)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("déjà un compte");

        assertThatThrownBy(() -> service.creer(new UtilisateurRequest("c@vala.ma", "secret123", Role.RH, 9L)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("Seul un compte ENCADRANT");

        when(encadrantRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.creer(new UtilisateurRequest("d@vala.ma", "secret123", Role.ENCADRANT, 5L)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    @DisplayName("modifier() refuse de rétrograder ou désactiver le dernier administrateur actif")
    void dernierAdmin() {
        Utilisateur admin = compte(1L, "admin@vala.ma", Role.ADMIN, true);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(utilisateurRepository.countByRoleAndActifTrue(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.modifier(1L, new UtilisateurModification(Role.RH, null, true)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("dernier");
        assertThatThrownBy(() -> service.modifier(1L, new UtilisateurModification(Role.ADMIN, null, false)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("modifier() rétrograde un administrateur s'il en reste un autre")
    void retrograderAdmin() {
        Utilisateur admin = compte(1L, "admin@vala.ma", Role.ADMIN, true);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(utilisateurRepository.countByRoleAndActifTrue(Role.ADMIN)).thenReturn(2L);
        when(utilisateurRepository.save(admin)).thenReturn(admin);

        service.modifier(1L, new UtilisateurModification(Role.RH, null, true));

        assertThat(admin.getRole()).isEqualTo(Role.RH);
    }

    @Test
    @DisplayName("supprimer() refuse la suppression de son propre compte et du dernier administrateur")
    void supprimer() {
        Utilisateur admin = compte(1L, "admin@vala.ma", Role.ADMIN, true);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertThatThrownBy(() -> service.supprimer(1L, "ADMIN@vala.ma"))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("propre compte");

        when(utilisateurRepository.countByRoleAndActifTrue(Role.ADMIN)).thenReturn(1L);
        assertThatThrownBy(() -> service.supprimer(1L, "autre@vala.ma")).isInstanceOf(BusinessRuleException.class);

        Utilisateur rh = compte(2L, "rh@vala.ma", Role.RH, true);
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(rh));
        service.supprimer(2L, "admin@vala.ma");
        verify(utilisateurRepository).delete(rh);
    }

    @Test
    @DisplayName("changerMotDePasse() enregistre une nouvelle empreinte")
    void changerMotDePasse() {
        Utilisateur rh = compte(2L, "rh@vala.ma", Role.RH, true);
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(rh));

        service.changerMotDePasse(2L, "nouveau-secret");

        assertThat(encoder.matches("nouveau-secret", rh.getMotDePasse())).isTrue();
        verify(utilisateurRepository).save(rh);
    }
}
