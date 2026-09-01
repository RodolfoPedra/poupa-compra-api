package br.com.poupacompra.integracao;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.poupacompra.integracao.dto.nota.ItensNotaDTO;
import br.com.poupacompra.integracao.dto.nota.NotaDTO;
import br.com.poupacompra.integracao.dto.usuario.AuthResponse;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.repository.EmailVerificationTokenRepository;
import br.com.poupacompra.integracao.repository.ItemListaCompraRepository;
import br.com.poupacompra.integracao.repository.ListaCompraRepository;
import br.com.poupacompra.integracao.repository.NotaRepository;
import br.com.poupacompra.integracao.repository.PasswordResetTokenRepository;
import br.com.poupacompra.integracao.repository.RefreshTokenRepository;
import br.com.poupacompra.integracao.repository.UsuarioProviderRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NotaIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ItemListaCompraRepository itemListaCompraRepository;

    @Autowired
    private ListaCompraRepository listaCompraRepository;

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UsuarioProviderRepository usuarioProviderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuario() {
        // ordem necessária: filhos antes do usuário, pois o schema de teste não possui ON DELETE CASCADE.
        itemListaCompraRepository.deleteAll();
        listaCompraRepository.deleteAll();
        notaRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        usuarioProviderRepository.deleteAll();
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
        assertEquals(0, listaCompraRepository.count());
    }

    @Test
    public void deveRetornarBadRequestQuandoCodigoItemNaoForInformado() throws Exception {
        ClassPathResource resource = new ClassPathResource("payload-nota.json");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = (ObjectNode) mapper.readTree(resource.getInputStream());
        ((ObjectNode) root.withArray("itensNota").get(0)).remove("codigoItem");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(autenticar("teste@poupacompra.com", "senha-segura"));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/notas",
                new HttpEntity<>(mapper.writeValueAsString(root), headers),
                String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0, notaRepository.count());
    }

    @Test
    public void deveListarItensDaNotaCadastrada() throws Exception {
        String bearerToken = autenticar("teste@poupacompra.com", "senha-segura");
        Long notaId = cadastrarNotaERetornarId(bearerToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        ResponseEntity<List<ItensNotaDTO>> response = restTemplate.exchange(
                "/api/v1/notas/" + notaId + "/itens",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<ItensNotaDTO>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6, response.getBody().size());
    }

    @Test
    public void deveRetornarNotFoundParaNotaInexistente() {
        String bearerToken = autenticar("teste@poupacompra.com", "senha-segura");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/notas/999999/itens", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void deveRetornarForbiddenParaNotaDeOutroUsuario() throws Exception {
        Long notaId = cadastrarNotaERetornarId(autenticar("teste@poupacompra.com", "senha-segura"));

        Usuario outroUsuario = new Usuario("Outro Usuário", "outro@poupacompra.com", passwordEncoder.encode("outra-senha"));
        outroUsuario.setEmailVerificado(true);
        usuarioRepository.save(outroUsuario);
        String tokenOutroUsuario = autenticar("outro@poupacompra.com", "outra-senha");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenOutroUsuario);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/notas/" + notaId + "/itens", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private String autenticar(String email, String senha) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login",
                new HttpEntity<>("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}", headers),
                AuthResponse.class);
        return loginResponse.getBody().accessToken();
    }

    private Long cadastrarNotaERetornarId(String bearerToken) throws Exception {
        ClassPathResource resource = new ClassPathResource("payload-nota.json");
        String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = (ObjectNode) mapper.readTree(json);
        String unique = "-test-" + System.nanoTime();
        ObjectNode nota = (ObjectNode) root.get("nota");
        nota.put("urlCfe", nota.get("urlCfe").asText() + unique);
        nota.put("chaveAcesso", nota.get("chaveAcesso").asText() + unique);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(bearerToken);
        HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(root), headers);

        ResponseEntity<NotaDTO> response = restTemplate.postForEntity("/api/v1/notas", request, NotaDTO.class);
        return response.getBody().getId();
    }
}

