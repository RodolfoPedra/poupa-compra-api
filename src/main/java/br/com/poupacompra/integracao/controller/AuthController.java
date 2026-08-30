package br.com.poupacompra.integracao.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.integracao.dto.usuario.AuthResponse;
import br.com.poupacompra.integracao.dto.usuario.ForgotPasswordRequest;
import br.com.poupacompra.integracao.dto.usuario.LoginRequest;
import br.com.poupacompra.integracao.dto.usuario.OAuthLoginRequest;
import br.com.poupacompra.integracao.dto.usuario.RefreshRequest;
import br.com.poupacompra.integracao.dto.usuario.RegisterRequest;
import br.com.poupacompra.integracao.dto.usuario.ResendVerificationRequest;
import br.com.poupacompra.integracao.dto.usuario.ResetPasswordRequest;
import br.com.poupacompra.integracao.dto.usuario.UserResponse;
import br.com.poupacompra.integracao.dto.usuario.VerifyEmailRequest;
import br.com.poupacompra.integracao.model.usuario.AuthProvider;
import br.com.poupacompra.integracao.service.usuario.AuthService;
import br.com.poupacompra.integracao.service.usuario.OAuthIdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Cadastro, autenticação, sessões e identidades externas")
public class AuthController {
    private final AuthService authService;
    private final OAuthIdentityService oauthIdentityService;

    public AuthController(AuthService authService, OAuthIdentityService oauthIdentityService) {
        this.authService = authService;
        this.oauthIdentityService = oauthIdentityService;
    }

    @PostMapping("/register")
        @Operation(summary = "Cadastrar usuário", description = "Cria uma conta local e registra um token de verificação de e-mail.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado")
        })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(request));
    }

    @PostMapping("/login")
        @Operation(summary = "Autenticar usuário", description = "Autentica uma conta local verificada e emite access e refresh tokens.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas ou e-mail não verificado")
        })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/oauth/google")
        @Operation(summary = "Autenticar com Google", description = "Troca um authorization code Google, valida o id_token via OIDC e emite tokens da aplicação.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação Google realizada"),
            @ApiResponse(responseCode = "400", description = "Código ou identidade Google inválida")
        })
    public AuthResponse google(@Valid @RequestBody OAuthLoginRequest request) {
        return oauthIdentityService.autenticar(AuthProvider.GOOGLE, request.authorizationCode(), request.redirectUri(), request.codeVerifier());
    }

    @PostMapping("/oauth/apple")
        @Operation(summary = "Autenticar com Apple", description = "Troca um authorization code Apple, valida o id_token via OIDC e emite tokens da aplicação.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação Apple realizada"),
            @ApiResponse(responseCode = "400", description = "Código ou identidade Apple inválida")
        })
    public AuthResponse apple(@Valid @RequestBody OAuthLoginRequest request) {
        return oauthIdentityService.autenticar(AuthProvider.APPLE, request.authorizationCode(), request.redirectUri(), request.codeVerifier());
    }

    @PostMapping("/verify-email")
        @Operation(summary = "Verificar e-mail", description = "Confirma o e-mail usando o token temporário registrado no log local durante o desenvolvimento.")
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "E-mail verificado"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
        })
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verificarEmail(request.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Reenviar verificação", description = "Gera um novo token de verificação para uma conta local ainda não verificada.")
    @ApiResponse(responseCode = "204", description = "Solicitação processada")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.reenviarVerificacao(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperação de senha", description = "Registra um token temporário sem revelar se o e-mail existe.")
    @ApiResponse(responseCode = "204", description = "Solicitação processada")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.solicitarRedefinicao(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
        @Operation(summary = "Redefinir senha", description = "Altera a senha usando um token de recuperação válido e revoga as sessões ativas.")
        @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha redefinida"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
        })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.redefinirSenha(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
        @Operation(summary = "Renovar sessão", description = "Revoga o refresh token atual e emite um novo par de tokens.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessão renovada"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido, expirado ou revogado")
        })
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.renovar(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerrar sessão", description = "Revoga o refresh token informado.")
    @ApiResponse(responseCode = "204", description = "Sessão encerrada")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
        @Operation(summary = "Consultar usuário autenticado", description = "Retorna os dados públicos do usuário associado ao access token.")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário retornado"),
            @ApiResponse(responseCode = "401", description = "Access token ausente ou inválido")
        })
        @SecurityRequirement(name = "bearerAuth")
    public UserResponse me(@AuthenticationPrincipal UserDetails details) {
        return UserResponse.from(authService.buscarUsuario(details.getUsername()));
    }
}