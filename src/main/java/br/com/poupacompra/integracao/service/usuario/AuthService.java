package br.com.poupacompra.integracao.service.usuario;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.common.exception.EmailJaCadastradoException;
import br.com.poupacompra.integracao.common.exception.TokenInvalidoException;
import br.com.poupacompra.integracao.dto.usuario.AuthResponse;
import br.com.poupacompra.integracao.dto.usuario.ForgotPasswordRequest;
import br.com.poupacompra.integracao.dto.usuario.LoginRequest;
import br.com.poupacompra.integracao.dto.usuario.RefreshRequest;
import br.com.poupacompra.integracao.dto.usuario.RegisterRequest;
import br.com.poupacompra.integracao.dto.usuario.ResetPasswordRequest;
import br.com.poupacompra.integracao.dto.usuario.UserResponse;
import br.com.poupacompra.integracao.model.usuario.EmailVerificationToken;
import br.com.poupacompra.integracao.model.usuario.PasswordResetToken;
import br.com.poupacompra.integracao.model.usuario.RefreshToken;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.repository.EmailVerificationTokenRepository;
import br.com.poupacompra.integracao.repository.PasswordResetTokenRepository;
import br.com.poupacompra.integracao.repository.RefreshTokenRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final EmailVerificationTokenRepository verificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService detailsService;
    private final JwtService jwtService;
    private final TokenGenerator tokenGenerator;
    private final Duration refreshTokenTtl;

    public AuthService(UsuarioRepository usuarioRepository,
            EmailVerificationTokenRepository verificationRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UsuarioDetailsService detailsService,
            JwtService jwtService,
            TokenGenerator tokenGenerator,
            @Value("${security.jwt.refresh-token-ttl:P30D}") Duration refreshTokenTtl) {
        this.usuarioRepository = usuarioRepository;
        this.verificationRepository = verificationRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.detailsService = detailsService;
        this.jwtService = jwtService;
        this.tokenGenerator = tokenGenerator;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public UserResponse cadastrar(RegisterRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailJaCadastradoException("E-mail já cadastrado");
        }
        Usuario usuario = new Usuario(request.nome().trim(), email, passwordEncoder.encode(request.senha()));
        usuario = usuarioRepository.save(usuario);
        emitirTokenVerificacao(usuario);
        log.info("auth action=register userId={}", usuario.getId());
        return UserResponse.from(usuario);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = buscarUsuario(request.email());
        if (!usuario.isEmailVerificado()) {
            throw new TokenInvalidoException("E-mail ainda não verificado");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usuario.getEmail(), request.senha()));
        usuario.setLastLoginAt(Instant.now());
        usuario.setUpdatedAt(Instant.now());
        log.info("auth action=login userId={}", usuario.getId());
        return emitirTokens(usuario);
    }

    @Transactional
    public AuthResponse loginSocial(Usuario usuario) {
        usuario.setLastLoginAt(Instant.now());
        usuario.setUpdatedAt(Instant.now());
        log.info("auth action=socialLogin userId={}", usuario.getId());
        return emitirTokens(usuario);
    }

    @Transactional
    public void verificarEmail(String rawToken) {
        EmailVerificationToken token = verificationRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new TokenInvalidoException("Token de verificação inválido ou expirado"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenInvalidoException("Token de verificação inválido ou expirado");
        }
        Usuario usuario = token.getUsuario();
        usuario.setEmailVerificado(true);
        usuario.setUpdatedAt(Instant.now());
        verificationRepository.delete(token);
        log.info("auth action=emailVerified userId={}", usuario.getId());
    }

    @Transactional
    public void reenviarVerificacao(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(email)).orElse(null);
        if (usuario != null && !usuario.isEmailVerificado()) {
            verificationRepository.deleteByUsuarioId(usuario.getId());
            emitirTokenVerificacao(usuario);
        }
    }

    @Transactional
    public void solicitarRedefinicao(ForgotPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(request.email())).orElse(null);
        if (usuario == null || usuario.getSenhaHash() == null) {
            return;
        }
        passwordResetTokenRepository.deleteByUsuarioId(usuario.getId());
        String rawToken = tokenGenerator.gerar();
        passwordResetTokenRepository.save(new PasswordResetToken(usuario, tokenGenerator.hash(rawToken), Instant.now().plus(Duration.ofHours(1))));
        log.info("auth action=passwordResetRequested userId={}", usuario.getId());
    }

    @Transactional
    public void redefinirSenha(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenGenerator.hash(request.token()))
                .orElseThrow(() -> new TokenInvalidoException("Token de recuperação inválido ou expirado"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenInvalidoException("Token de recuperação inválido ou expirado");
        }
        Usuario usuario = token.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuario.setUpdatedAt(Instant.now());
        refreshTokenRepository.findAll().stream()
                .filter(refresh -> refresh.getUsuario().getId().equals(usuario.getId()) && refresh.getRevokedAt() == null)
                .forEach(refresh -> refresh.setRevokedAt(Instant.now()));
        passwordResetTokenRepository.delete(token);
        log.info("auth action=passwordReset userId={}", usuario.getId());
    }

    @Transactional
    public AuthResponse renovar(RefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenGenerator.hash(request.refreshToken()))
                .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido"));
        if (!token.isValid(Instant.now())) {
            throw new TokenInvalidoException("Refresh token expirado ou revogado");
        }
        token.setRevokedAt(Instant.now());
        log.info("auth action=tokenRefreshed userId={}", token.getUsuario().getId());
        return emitirTokens(token.getUsuario());
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByTokenHash(tokenGenerator.hash(request.refreshToken()))
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    log.info("auth action=logout userId={}", token.getUsuario().getId());
                });
    }

    public Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(normalizarEmail(email))
                .orElseThrow(() -> new TokenInvalidoException("Credenciais inválidas"));
    }

    private AuthResponse emitirTokens(Usuario usuario) {
        UserDetails details = detailsService.loadUserByUsername(usuario.getEmail());
        String rawRefreshToken = tokenGenerator.gerar();
        refreshTokenRepository.save(new RefreshToken(usuario, tokenGenerator.hash(rawRefreshToken), Instant.now().plus(refreshTokenTtl)));
        return new AuthResponse(jwtService.gerarAccessToken(details), rawRefreshToken, UserResponse.from(usuario));
    }

    private void emitirTokenVerificacao(Usuario usuario) {
        String rawToken = tokenGenerator.gerar();
        verificationRepository.save(new EmailVerificationToken(usuario, tokenGenerator.hash(rawToken), Instant.now().plus(Duration.ofHours(24))));
        log.info("auth action=emailVerificationIssued userId={}", usuario.getId());
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}