package br.com.poupacompra.integracao;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

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
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NotaIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

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

        ResponseEntity<String> response = restTemplate.postForEntity("/salvar-nota", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
