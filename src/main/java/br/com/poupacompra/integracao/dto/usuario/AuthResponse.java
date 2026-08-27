package br.com.poupacompra.integracao.dto.usuario;

public record AuthResponse(String accessToken, String refreshToken, UserResponse usuario) {
}