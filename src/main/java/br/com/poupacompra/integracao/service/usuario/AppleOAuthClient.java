package br.com.poupacompra.integracao.service.usuario;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.poupacompra.integracao.common.exception.TokenInvalidoException;

@Component
public class AppleOAuthClient implements OAuthProviderClient {
    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public AppleOAuthClient(RestClient.Builder builder,
            @Value("${security.oauth.apple.client-id:}") String clientId,
            @Value("${security.oauth.apple.client-secret:}") String clientSecret) {
        this.restClient = builder.baseUrl("https://appleid.apple.com").build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public OAuthTokenResponse exchangeCode(String authorizationCode, String redirectUri, String codeVerifier) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new TokenInvalidoException("OAuth Apple não configurado");
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", authorizationCode);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            form.add("code_verifier", codeVerifier);
        }
        AppleTokenResponse response = restClient.post().uri("/auth/token").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
                .retrieve().body(AppleTokenResponse.class);
        if (response == null || response.idToken() == null) {
            throw new TokenInvalidoException("Resposta OAuth Apple inválida");
        }
        return new OAuthTokenResponse(response.idToken());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AppleTokenResponse(@JsonProperty("id_token") String idToken) {
    }
}