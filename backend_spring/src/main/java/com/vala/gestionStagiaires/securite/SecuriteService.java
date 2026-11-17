package com.vala.gestionStagiaires.securite;

import com.vala.gestionStagiaires.repository.RapportHebdomadaireRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Contrôles d'accès « à la ligne » pour les encadrants, utilisés dans les
 * {@code @PreAuthorize("@securite.peutAccederStagiaire(#id)")} des contrôleurs.
 * ADMIN et RH ne sont pas restreints. Un encadrant qui demande une ressource
 * qui n'est pas la sienne (ou qui n'existe pas) reçoit un 403.
 */
@Component("securite")
@RequiredArgsConstructor
public class SecuriteService {

    /** Identifiant qui ne correspond à aucun encadrant : compte ENCADRANT mal configuré = aucun accès. */
    private static final long AUCUN_ENCADRANT = -1L;

    private final StagiaireRepository stagiaireRepository;
    private final StageRepository stageRepository;
    private final RapportHebdomadaireRepository rapportRepository;

    /** Si l'utilisateur courant est un encadrant, l'identifiant de sa fiche ; vide sinon (accès complet). */
    public Optional<Long> encadrantRestreint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> "ROLE_ENCADRANT".equals(a.getAuthority()))) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof Jwt jwt && jwt.getClaim(JwtService.CLAIM_ENCADRANT) instanceof Number id) {
            return Optional.of(id.longValue());
        }
        return Optional.of(AUCUN_ENCADRANT);
    }

    public boolean peutAccederStagiaire(Long stagiaireId) {
        return encadrantRestreint()
                .map(enc -> stagiaireRepository.existsByIdAndStageEncadrantId(stagiaireId, enc))
                .orElse(true);
    }

    public boolean peutAccederStage(Long stageId) {
        return encadrantRestreint()
                .map(enc -> stageRepository.existsByIdAndEncadrantId(stageId, enc))
                .orElse(true);
    }

    public boolean peutAccederRapport(Long rapportId) {
        return encadrantRestreint()
                .map(enc -> rapportRepository.existsByIdAndStagiaireStageEncadrantId(rapportId, enc))
                .orElse(true);
    }

    /** Nom (email) de l'utilisateur authentifié, ou {@code null} (tâche planifiée, appel interne). */
    public static String utilisateurCourant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt ? auth.getName() : null;
    }
}
