package br.com.poupacompra.integracao;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import br.com.poupacompra.integracao.dto.listacompra.ItemListaCompraResponse;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompraResponse;
import br.com.poupacompra.integracao.dto.listacompra.RascunhoListaNotasResponse;
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
class ListaCompraIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ListaCompraRepository listaRepository;
    @Autowired
    private ItemListaCompraRepository itemRepository;
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
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void prepararUsuarios() {
        itemRepository.deleteAll();
        listaRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM produto");
        jdbcTemplate.update("DELETE FROM categoria_produto");
        notaRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        usuarioProviderRepository.deleteAll();
        usuarioRepository.deleteAll();
        criarUsuario("Usuário Um", "usuario1@poupacompra.com", "senha-segura");
        criarUsuario("Usuário Dois", "usuario2@poupacompra.com", "senha-segura");
    }

    @Test
        void deveCriarESalvarListaCompleta() {
        String token = autenticar("usuario1@poupacompra.com");
        ListaCompraResponse lista = criarLista(token, "Compras do mês", """
            [{"descricao":"Sabão artesanal","quantidade":2,"unidade":"UNIDADE","selecionado":false,"ordem":0}]
            """);
        Long itemId = lista.itens().getFirst().id();

        ResponseEntity<ListaCompraResponse> salvarResponse = restTemplate.exchange(
            "/api/v1/listas/" + lista.id(), HttpMethod.PUT,
            json(token, """
                {"nome":"Compras mensais","updatedAt":"%s","itens":[
                  {"id":%d,"descricao":"Sabão artesanal","quantidade":3,"unidade":"UNIDADE","selecionado":true,"ordem":0},
                  {"descricao":"Banana","quantidade":1.250,"unidade":"QUILOGRAMA","selecionado":false,"ordem":1}
                ]}
                """.formatted(lista.updatedAt(), itemId)),
            ListaCompraResponse.class);

        assertThat(salvarResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(salvarResponse.getBody()).isNotNull();
        assertThat(salvarResponse.getBody().nome()).isEqualTo("Compras mensais");
        assertThat(salvarResponse.getBody().itens()).hasSize(2);
        assertThat(salvarResponse.getBody().itens().getFirst().quantidade()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(salvarResponse.getBody().itens().getFirst().selecionado()).isTrue();

        ItemListaCompraResponse itemNovo = salvarResponse.getBody().itens().get(1);
        ResponseEntity<ListaCompraResponse> removerResponse = restTemplate.exchange(
            "/api/v1/listas/" + lista.id(), HttpMethod.PUT,
            json(token, """
                {"nome":"Compras mensais","updatedAt":"%s","itens":[
                  {"id":%d,"descricao":"Banana","quantidade":1.250,"unidade":"QUILOGRAMA","selecionado":false,"ordem":0}
                ]}
                """.formatted(salvarResponse.getBody().updatedAt(), itemNovo.id())),
            ListaCompraResponse.class);

        assertThat(removerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(removerResponse.getBody().itens()).extracting(ItemListaCompraResponse::id).containsExactly(itemNovo.id());

        ResponseEntity<ListaCompraResponse> detalheResponse = restTemplate.exchange(
                "/api/v1/listas/" + lista.id(), HttpMethod.GET, autorizado(token), ListaCompraResponse.class);
        assertThat(detalheResponse.getBody().itens()).hasSize(1);

        ResponseEntity<String> resumosResponse = restTemplate.exchange(
            "/api/v1/listas", HttpMethod.GET, autorizado(token), String.class);
        assertThat(resumosResponse.getBody()).contains("quantidadeItens\":1", "quantidadeSelecionados\":0");
    }

    @Test
    void deveRecusarNomeDuplicadoParaMesmoUsuario() {
        String token = autenticar("usuario1@poupacompra.com");
        criarLista(token, "Semanal");

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/listas", HttpMethod.POST,
            json(token, "{\"nome\":\"semanal\",\"itens\":[]}"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deveOcultarListaDeOutroUsuario() {
        Long listaId = criarLista(autenticar("usuario1@poupacompra.com"), "Privada").id();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/listas/" + listaId, HttpMethod.GET,
                autorizado(autenticar("usuario2@poupacompra.com")), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deveRecusarQuantidadeFracionadaParaUnidade() {
        String token = autenticar("usuario1@poupacompra.com");

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/listas",
                HttpMethod.POST,
                json(token, """
                {"nome":"Feira","itens":[
                  {"descricao":"Abacate","quantidade":1.5,"unidade":"UNIDADE","selecionado":false,"ordem":0}
                ]}
                        """),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void devePermitirQuantidadeEUnidadeOpcionais() {
        String token = autenticar("usuario1@poupacompra.com");

        ListaCompraResponse lista = criarLista(token, "Itens opcionais", """
            [
              {"descricao":"Sem medida","selecionado":false,"ordem":0},
              {"descricao":"Somente quantidade","quantidade":2.5,"selecionado":false,"ordem":1},
              {"descricao":"Somente unidade","unidade":"QUILOGRAMA","selecionado":false,"ordem":2},
              {"descricao":"Remover medida","quantidade":3,"unidade":"UNIDADE","selecionado":false,"ordem":3}
            ]
            """);

        assertThat(lista.itens()).hasSize(4);
        assertThat(lista.itens().get(0).quantidade()).isNull();
        assertThat(lista.itens().get(0).unidade()).isNull();
        assertThat(lista.itens().get(1).quantidade()).isEqualByComparingTo("2.5");
        assertThat(lista.itens().get(1).unidade()).isNull();
        assertThat(lista.itens().get(2).quantidade()).isNull();
        assertThat(lista.itens().get(2).unidade()).isEqualTo(br.com.poupacompra.integracao.model.listacompra.UnidadeMedida.QUILOGRAMA);

        ItemListaCompraResponse itemComMedida = lista.itens().get(3);
        ResponseEntity<ListaCompraResponse> response = restTemplate.exchange(
            "/api/v1/listas/" + lista.id(), HttpMethod.PUT,
            json(token, """
                {"nome":"Itens opcionais","updatedAt":"%s","itens":[
                  {"id":%d,"descricao":"Remover medida","selecionado":false,"ordem":0}
                ]}
                """.formatted(lista.updatedAt(), itemComMedida.id())),
            ListaCompraResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().itens()).singleElement().satisfies(item -> {
            assertThat(item.quantidade()).isNull();
            assertThat(item.unidade()).isNull();
        });
    }

        @Test
        void deveGerarRascunhoPaginadoEDeduplicadoAPartirDeNotas() {
        String token = autenticar("usuario1@poupacompra.com");
        Long usuarioId = usuarioRepository.findByEmailIgnoreCase("usuario1@poupacompra.com").orElseThrow().getId();
        Long estabelecimentoId = criarEstabelecimento("Mercado Central", "11111111111111");
        Long notaAntigaId = criarNota(usuarioId, estabelecimentoId, "nota-antiga", 3);
        criarItemNota(notaAntigaId, 10L, "Descrição antiga");
        criarItemNota(notaAntigaId, 10L, "Descrição antiga corrigida");
        criarItemNota(notaAntigaId, 20L, "Produto B");
        Long notaNovaId = criarNota(usuarioId, estabelecimentoId, "nota-nova", 2);
        criarItemNota(notaNovaId, 10L, "Descrição mais recente");
        criarItemNota(notaNovaId, 30L, "Produto C");

        ResponseEntity<String> estabelecimentos = restTemplate.exchange(
            "/api/v1/listas/origem-notas/estabelecimentos", HttpMethod.GET, autorizado(token), String.class);
        assertThat(estabelecimentos.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(estabelecimentos.getBody()).contains("Mercado Central", "11111111111111");

        ResponseEntity<String> pagina = restTemplate.exchange(
            "/api/v1/listas/origem-notas/estabelecimentos/" + estabelecimentoId + "/notas?pagina=0&tamanho=1",
            HttpMethod.GET, autorizado(token), String.class);
        assertThat(pagina.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pagina.getBody()).contains("\"totalElementos\":2", "\"totalPaginas\":2",
            "\"id\":" + notaNovaId).doesNotContain("\"id\":" + notaAntigaId);

        ResponseEntity<RascunhoListaNotasResponse> response = restTemplate.exchange(
            "/api/v1/listas/origem-notas/rascunho", HttpMethod.POST,
            json(token, "{\"notaIds\":[" + notaAntigaId + "," + notaNovaId + "]}"),
            RascunhoListaNotasResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().nome()).isEqualTo("Compras - Mercado Central - "
            + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        assertThat(response.getBody().itens()).extracting(item -> item.descricao())
            .containsExactly("Produto C", "Descrição mais recente", "Produto B");
        assertThat(response.getBody().itens()).allSatisfy(item -> {
            assertThat(item.produtoId()).isNull();
            assertThat(item.quantidade()).isNull();
            assertThat(item.unidade()).isNull();
            assertThat(item.selecionado()).isFalse();
        });
        assertThat(listaRepository.count()).isZero();
        }

        @Test
        void deveRecusarSalvamentoComVersaoDesatualizada() {
        String token = autenticar("usuario1@poupacompra.com");
        ListaCompraResponse lista = criarLista(token, "Feira");
        String corpo = """
            {"nome":"Feira atualizada","updatedAt":"%s","itens":[]}
            """.formatted(lista.updatedAt());

        ResponseEntity<ListaCompraResponse> primeiraAtualizacao = restTemplate.exchange(
            "/api/v1/listas/" + lista.id(), HttpMethod.PUT, json(token, corpo), ListaCompraResponse.class);
        ResponseEntity<String> segundaAtualizacao = restTemplate.exchange(
            "/api/v1/listas/" + lista.id(), HttpMethod.PUT, json(token, corpo), String.class);

        assertThat(primeiraAtualizacao.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(segundaAtualizacao.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

    @Test
    void devePesquisarProdutoPorNomeECategoria() {
        jdbcTemplate.update("INSERT INTO categoria_produto (nome, ativo, created_at) VALUES (?, TRUE, CURRENT_TIMESTAMP)",
            "Grãos e Cereais");
        Long categoriaId = jdbcTemplate.queryForObject("SELECT id FROM categoria_produto WHERE nome = ?", Long.class,
            "Grãos e Cereais");
        jdbcTemplate.update("INSERT INTO produto (categoria_id, nome, ativo, created_at) VALUES (?, ?, TRUE, CURRENT_TIMESTAMP)",
            categoriaId, "Arroz integral");

        String token = autenticar("usuario1@poupacompra.com");
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/produtos?busca=arroz&categoriaId=" + categoriaId,
            HttpMethod.GET, autorizado(token), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Arroz integral", "Grãos e Cereais", "totalElementos\":1");
        }

        @Test
        void deveListarProdutosSemFiltros() {
        jdbcTemplate.update("INSERT INTO categoria_produto (nome, ativo, created_at) VALUES (?, TRUE, CURRENT_TIMESTAMP)",
            "Hortifruti");
        Long categoriaId = jdbcTemplate.queryForObject("SELECT id FROM categoria_produto WHERE nome = ?", Long.class,
            "Hortifruti");
        jdbcTemplate.update("INSERT INTO produto (categoria_id, nome, ativo, created_at) VALUES (?, ?, TRUE, CURRENT_TIMESTAMP)",
            categoriaId, "Banana");

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/v1/produtos", HttpMethod.GET,
            autorizado(autenticar("usuario1@poupacompra.com")), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Banana", "totalElementos\":1");
        }

    private void criarUsuario(String nome, String email, String senha) {
        Usuario usuario = new Usuario(nome, email, passwordEncoder.encode(senha));
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
    }

    private Long criarEstabelecimento(String nome, String cpfCnpj) {
        jdbcTemplate.update("INSERT INTO estabelecimento (nome_estabelecimento, cpf_cnpj, endereco) VALUES (?, ?, ?)",
                nome, cpfCnpj, "Rua de Teste");
        return jdbcTemplate.queryForObject("SELECT id FROM estabelecimento WHERE cpf_cnpj = ?", Long.class, cpfCnpj);
    }

    private Long criarNota(Long usuarioId, Long estabelecimentoId, String chave, int quantidadeItens) {
        jdbcTemplate.update("""
                INSERT INTO geral_nota
                    (quantidade_itens, valor_total, usuario_id, uf_cfe, url_cfe, chave_acesso, estabelecimento_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, quantidadeItens, 10.0, usuarioId, "SP", "https://nota/" + chave, chave, estabelecimentoId);
        return jdbcTemplate.queryForObject("SELECT id FROM geral_nota WHERE chave_acesso = ?", Long.class, chave);
    }

    private void criarItemNota(Long notaId, Long codigoItem, String descricao) {
        jdbcTemplate.update("""
                INSERT INTO itens_nota
                    (descricao, quantidade, tipo_unidade, valor_unitario, valor_total, nota_id, codigo_item)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, descricao, 1.0, "UNID", 10.0, 10.0, notaId, codigoItem);
    }

    private String autenticar(String email) {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                json(null, "{\"email\":\"" + email + "\",\"senha\":\"senha-segura\"}"),
                AuthResponse.class);
        return response.getBody().accessToken();
    }

    private ListaCompraResponse criarLista(String token, String nome) {
        return criarLista(token, nome, "[]");
        }

        private ListaCompraResponse criarLista(String token, String nome, String itens) {
        ResponseEntity<ListaCompraResponse> response = restTemplate.exchange(
                "/api/v1/listas", HttpMethod.POST,
            json(token, "{\"nome\":\"" + nome + "\",\"itens\":" + itens + "}"), ListaCompraResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private HttpEntity<String> json(String token, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> autorizado(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}