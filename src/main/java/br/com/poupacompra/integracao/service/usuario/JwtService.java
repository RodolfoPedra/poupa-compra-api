package br.com.poupacompra.integracao.service.usuario;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class JwtService {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final Duration accessTokenTtl;

    public JwtService(@Value("${security.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl,
            @Value("${security.jwt.private-key-base64:}") String privateKeyBase64,
            @Value("${security.jwt.public-key-base64:}") String publicKeyBase64) {
        this.accessTokenTtl = accessTokenTtl;
        try {
            if (!privateKeyBase64.isBlank() && !publicKeyBase64.isBlank()) {
                KeyFactory factory = KeyFactory.getInstance("RSA");
                this.privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)));
                this.publicKey = factory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)));
            } else {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();
                this.privateKey = keyPair.getPrivate();
                this.publicKey = keyPair.getPublic();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível inicializar as chaves JWT", exception);
        }
    }

    public String gerarAccessToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getAuthorities().stream().map(authority -> authority.getAuthority()).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String extrairEmail(String token) {
        return claims(token).getSubject();
    }

    public boolean isValido(String token, UserDetails user) {
        try {
            return user.getUsername().equals(extrairEmail(token)) && !claims(token).getExpiration().before(new Date());
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
    }
}