package br.com.poupacompra.integracao;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.poupacompra.integracao.dto.usuario.AuthResponse;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.repository.UsuarioRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NotaIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuario() {
        usuarioRepository.deleteAll();
        Usuario usuario = new Usuario("Usuário de Teste", "teste@poupacompra.com", passwordEncoder.encode("senha-segura"));
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
    }

    @Test
    public void deveSalvarNotaERetornarCreated() throws Exception {
        ClassPathResource resource = new ClassPathResource("payload-nota.json");
        String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = (ObjectNode) mapper.readTree(json);
        String unique = "-test-" + System.currentTimeMillis();
        if (root.has("nota")) {
            ObjectNode nota = (ObjectNode) root.get("nota");
            if (nota.has("urlCfe")) {
                nota.put("urlCfe", nota.get("urlCfe").asText() + unique);
            }
            if (nota.has("chaveAcesso")) {
                nota.put("chaveAcesso", nota.get("chaveAcesso").asText() + unique);
            }
        }

        String uniqueJson = mapper.writeValueAsString(root);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(uniqueJson, headers);

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
            "/api/v1/auth/login",
            new HttpEntity<>("{\"email\":\"teste@poupacompra.com\",\"senha\":\"senha-segura\"}", headers),
            AuthResponse.class);
        headers.setBearerAuth(loginResponse.getBody().accessToken());

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/notas", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
