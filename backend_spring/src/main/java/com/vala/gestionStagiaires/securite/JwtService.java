package com.vala.gestionStagiaires.securite;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.vala.gestionStagiaires.entities.Utilisateur;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;

/** Émet les jetons JWT (HS256) remis à la connexion. */
@Service
public class JwtService {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_UTILISATEUR = "uid";
    public static final String CLAIM_ENCADRANT = "encadrantId";

    private final JwtEncoder encoder;
    private final SecuriteProperties proprietes;
    private final Clock clock;

    public JwtService(SecretKey cleJwt, SecuriteProperties proprietes, Clock clock) {
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(cleJwt));
        this.proprietes = proprietes;
        this.clock = clock;
    }

    public JetonEmis emettre(Utilisateur utilisateur) {
        Instant maintenant = Instant.now(clock);
        Instant expiration = maintenant.plus(proprietes.jwtDuree());
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(proprietes.emetteur())
                .subject(utilisateur.getEmail())
                .issuedAt(maintenant)
                .expiresAt(expiration)
                .claim(CLAIM_ROLE, utilisateur.getRole().name())
                .claim(CLAIM_UTILISATEUR, utilisateur.getId());
        if (utilisateur.getEncadrant() != null) {
            claims.claim(CLAIM_ENCADRANT, utilisateur.getEncadrant().getId());
        }
        String jeton = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
        return new JetonEmis(jeton, expiration);
    }

    public record JetonEmis(String valeur, Instant expiration) {
    }
}
