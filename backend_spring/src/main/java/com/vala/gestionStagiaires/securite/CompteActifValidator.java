package com.vala.gestionStagiaires.securite;

import com.vala.gestionStagiaires.repository.UtilisateurRepository;
import com.vala.gestionStagiaires.repository.projection.EtatCompte;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;
import java.util.Optional;

/**
 * Refuse un jeton encore valide dont le compte a été supprimé, désactivé,
 * ou dont le rôle / l'encadrant rattaché a changé depuis l'émission.
 * Sans ce contrôle, ces changements ne prendraient effet qu'à l'expiration du jeton.
 */
public class CompteActifValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error COMPTE_INVALIDE = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
            "Le compte associé au jeton n'est plus valide.", null);

    private final UtilisateurRepository utilisateurRepository;

    public CompteActifValidator(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Long uid = enLong(jwt.getClaim(JwtService.CLAIM_UTILISATEUR));
        Optional<EtatCompte> etat = uid == null ? Optional.empty() : utilisateurRepository.etatCompte(uid);
        boolean valide = etat.isPresent()
                && etat.get().actif()
                && etat.get().role().name().equals(jwt.getClaimAsString(JwtService.CLAIM_ROLE))
                && Objects.equals(etat.get().encadrantId(), enLong(jwt.getClaim(JwtService.CLAIM_ENCADRANT)));
        return valide ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(COMPTE_INVALIDE);
    }

    private static Long enLong(Object valeur) {
        return valeur instanceof Number n ? n.longValue() : null;
    }
}
