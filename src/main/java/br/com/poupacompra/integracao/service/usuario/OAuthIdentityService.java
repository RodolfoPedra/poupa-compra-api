package br.com.poupacompra.integracao.service.usuario;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.common.exception.EmailJaCadastradoException;
import br.com.poupacompra.integracao.common.exception.TokenInvalidoException;
import br.com.poupacompra.integracao.dto.usuario.AuthResponse;
import br.com.poupacompra.integracao.model.usuario.AuthProvider;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.model.usuario.UsuarioProvider;
import br.com.poupacompra.integracao.repository.UsuarioProviderRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;

@Service
public class OAuthIdentityService {
    private final UsuarioProviderRepository providerRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final OAuthProviderClient googleClient;
    private final OAuthProviderClient appleClient;
    private final JwtDecoder googleDecoder;
    private final JwtDecoder appleDecoder;
    private final String googleClientId;
    private final String appleClientId;

    @Autowired
    public OAuthIdentityService(UsuarioProviderRepository providerRepository, UsuarioRepository usuarioRepository,
            AuthService authService, GoogleOAuthClient googleClient, AppleOAuthClient appleClient,
            @Value("${security.oauth.google.client-id:}") String googleClientId,
            @Value("${security.oauth.apple.client-id:}") String appleClientId) {
        this.providerRepository = providerRepository;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.googleClient = googleClient;
        this.appleClient = appleClient;
        this.googleClientId = googleClientId;
        this.appleClientId = appleClientId;
        this.googleDecoder = decoder("https://accounts.google.com", "https://www.googleapis.com/oauth2/v3/certs", googleClientId);
        this.appleDecoder = decoder("https://appleid.apple.com", "https://appleid.apple.com/auth/keys", appleClientId);
    }

    OAuthIdentityService(UsuarioProviderRepository providerRepository, UsuarioRepository usuarioRepository,
            AuthService authService, OAuthProviderClient googleClient, OAuthProviderClient appleClient,
            JwtDecoder googleDecoder, JwtDecoder appleDecoder, String googleClientId, String appleClientId) {
        this.providerRepository = providerRepository;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.googleClient = googleClient;
        this.appleClient = appleClient;
        this.googleDecoder = googleDecoder;
        this.appleDecoder = appleDecoder;
        this.googleClientId = googleClientId;
        this.appleClientId = appleClientId;
    }

    @Transactional
    public AuthResponse autenticar(AuthProvider provider, String authorizationCode, String redirectUri) {
        OAuthProviderClient client = provider == AuthProvider.GOOGLE ? googleClient : appleClient;
        String clientId = provider == AuthProvider.GOOGLE ? googleClientId : appleClientId;
        if (clientId.isBlank()) {
            throw new TokenInvalidoException("OAuth " + provider + " não configurado");
        }
        Jwt identity = decode(provider, client.exchangeCode(authorizationCode, redirectUri).idToken());
        String subject = identity.getSubject();
        String email = identity.getClaimAsString("email");
        if (subject == null || email == null || email.isBlank()) {
            throw new TokenInvalidoException("Identidade OAuth sem subject ou e-mail");
        }
        email = email.trim().toLowerCase(java.util.Locale.ROOT);
        var linked = providerRepository.findByProviderAndSubject(provider, subject);
        if (linked.isPresent()) {
            return authService.loginSocial(linked.get().getUsuario());
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailJaCadastradoException("E-mail já cadastrado; autentique a conta existente antes de vincular o provedor");
        }
        String name = identity.getClaimAsString("name");
        Usuario usuario = new Usuario(name == null || name.isBlank() ? email : name, email, null);
        usuario.setEmailVerificado(true);
        usuario = usuarioRepository.save(usuario);
        providerRepository.save(new UsuarioProvider(usuario, provider, subject, email));
        return authService.loginSocial(usuario);
    }

    private Jwt decode(AuthProvider provider, String idToken) {
        try {
            return (provider == AuthProvider.GOOGLE ? googleDecoder : appleDecoder).decode(idToken);
        } catch (RuntimeException exception) {
            throw new TokenInvalidoException("id_token OAuth inválido");
        }
    }

    private JwtDecoder decoder(String issuer, String jwkSetUri, String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>("aud",
                audience -> audience != null && audience.contains(clientId));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        return decoder;
    }
}