package br.com.poupacompra.integracao.service.usuario;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GoogleOAuthClientTest {

    @Test
    void deveAceitarRespostaDoGoogleComAccessTokenEIdToken() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GoogleOAuthClient client = new GoogleOAuthClient(builder, "client-id", "client-secret");

        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("code=authorization-code&client_id=client-id&client_secret=client-secret&redirect_uri=postmessage&grant_type=authorization_code"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "google-access-token",
                          "refresh_token": "google-refresh-token",
                          "expires_in": 3599,
                          "scope": "openid email profile",
                          "token_type": "Bearer",
                          "id_token": "google-id-token"
                        }
                        """, MediaType.APPLICATION_JSON));

        OAuthTokenResponse response = client.exchangeCode("authorization-code", "postmessage", null);

        assertThat(response.idToken()).isEqualTo("google-id-token");
        server.verify();
    }
}
