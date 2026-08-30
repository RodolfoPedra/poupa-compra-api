package br.com.poupacompra.integracao.service.usuario;

public interface OAuthProviderClient {
    OAuthTokenResponse exchangeCode(String authorizationCode, String redirectUri, String codeVerifier);
}