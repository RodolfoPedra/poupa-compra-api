package br.com.poupacompra.integracao.service.usuario;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import br.com.poupacompra.integracao.common.exception.EmailJaCadastradoException;
import br.com.poupacompra.integracao.dto.usuario.AuthResponse;
import br.com.poupacompra.integracao.model.usuario.AuthProvider;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.model.usuario.UsuarioProvider;
import br.com.poupacompra.integracao.repository.UsuarioProviderRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;

class OAuthIdentityServiceTest {
    private final UsuarioProviderRepository providerRepository = mock(UsuarioProviderRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final AuthService authService = mock(AuthService.class);
    private final OAuthProviderClient googleClient = mock(OAuthProviderClient.class);
    private final OAuthProviderClient appleClient = mock(OAuthProviderClient.class);
    private final JwtDecoder googleDecoder = mock(JwtDecoder.class);
    private final JwtDecoder appleDecoder = mock(JwtDecoder.class);
    private OAuthIdentityService service;

    @BeforeEach
    void setUp() {
        service = new OAuthIdentityService(providerRepository, usuarioRepository, authService, googleClient, appleClient,
                googleDecoder, appleDecoder, "google-client", "apple-client");
    }

    @Test
    void deveEmitirTokensParaIdentidadeJaVinculada() {
        Usuario usuario = usuario("existente@example.com");
        when(googleClient.exchangeCode("code", "http://localhost/callback")).thenReturn(new OAuthTokenResponse("id-token"));
        when(googleDecoder.decode("id-token")).thenReturn(jwt("subject-1", "existente@example.com", "Pessoa"));
        when(providerRepository.findByProviderAndSubject(AuthProvider.GOOGLE, "subject-1"))
                .thenReturn(Optional.of(new UsuarioProvider(usuario, AuthProvider.GOOGLE, "subject-1", usuario.getEmail())));
        AuthResponse response = new AuthResponse("access", "refresh", null);
        when(authService.loginSocial(usuario)).thenReturn(response);

        AuthResponse result = service.autenticar(AuthProvider.GOOGLE, "code", "http://localhost/callback");

        org.assertj.core.api.Assertions.assertThat(result).isSameAs(response);
        verify(authService).loginSocial(usuario);
    }

    @Test
    void deveExigirVinculacaoExplicitaQuandoEmailJaExiste() {
        when(googleClient.exchangeCode(any(), any())).thenReturn(new OAuthTokenResponse("id-token"));
        when(googleDecoder.decode("id-token")).thenReturn(jwt("subject-1", "existente@example.com", "Pessoa"));
        when(providerRepository.findByProviderAndSubject(AuthProvider.GOOGLE, "subject-1")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByEmailIgnoreCase("existente@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.autenticar(AuthProvider.GOOGLE, "code", "callback"))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    void deveCriarUsuarioVerificadoQuandoIdentidadeAindaNaoExiste() {
        when(appleClient.exchangeCode("code", "callback")).thenReturn(new OAuthTokenResponse("id-token"));
        when(appleDecoder.decode("id-token")).thenReturn(jwt("apple-sub", "novo@example.com", "Nova Pessoa"));
        when(providerRepository.findByProviderAndSubject(AuthProvider.APPLE, "apple-sub")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByEmailIgnoreCase("novo@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authService.loginSocial(any(Usuario.class))).thenReturn(new AuthResponse("access", "refresh", null));

        service.autenticar(AuthProvider.APPLE, "code", "callback");

        verify(usuarioRepository).save(any(Usuario.class));
        verify(providerRepository).save(any(UsuarioProvider.class));
    }

    private Usuario usuario(String email) {
        Usuario usuario = new Usuario("Pessoa", email, "hash");
        usuario.setEmailVerificado(true);
        return usuario;
    }

    private Jwt jwt(String subject, String email, String name) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("iss", "https://accounts.google.com")
                .claim("aud", List.of("google-client"))
                .claim("email", email)
                .claim("name", name)
                .subject(subject)
                .build();
    }
}