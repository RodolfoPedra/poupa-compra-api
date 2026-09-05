package br.com.poupacompra.integracao;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.poupacompra.integracao.dto.listacompra.CompartilhamentoListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.EventoListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompraResponse;
import br.com.poupacompra.integracao.dto.usuario.AuthResponse;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.repository.CompartilhamentoListaCompraRepository;
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
class ListaCompraCompartilhamentoIntegrationTest {
        @LocalServerPort
        private int serverPort;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CompartilhamentoListaCompraRepository compartilhamentoRepository;
    @Autowired
    private ItemListaCompraRepository itemRepository;
    @Autowired
    private ListaCompraRepository listaRepository;
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
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
        @Autowired
        private ObjectMapper objectMapper;
        @Autowired
        private WebSocketSubscribeProbe webSocketSubscribeProbe;

    @BeforeEach
    void prepararUsuarios() {
        compartilhamentoRepository.deleteAll();
        itemRepository.deleteAll();
        listaRepository.deleteAll();
        notaRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        usuarioProviderRepository.deleteAll();
        usuarioRepository.deleteAll();
        criarUsuario("Proprietário", "owner@poupacompra.com");
        criarUsuario("Convidado", "guest@poupacompra.com");
        criarUsuario("Terceiro", "other@poupacompra.com");
    }

    @Test
    void deveExigirAceiteEBloquearPutIntegralNoModoColaborativo() {
        String ownerToken = autenticar("owner@poupacompra.com");
        String guestToken = autenticar("guest@poupacompra.com");
        ListaCompraResponse lista = criarLista(ownerToken);

        CompartilhamentoListaResponse convite = convidar(ownerToken, lista);
        assertThat(convite.listaId()).isEqualTo(lista.id());
        assertThat(convite.listaNome()).isEqualTo(lista.nome());

        ResponseEntity<String> acessoPendente = restTemplate.exchange(
                "/api/v1/listas/compartilhadas/" + lista.id(), HttpMethod.GET,
                autorizado(guestToken), String.class);
        assertThat(acessoPendente.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> put = restTemplate.exchange(
                "/api/v1/listas/" + lista.id(), HttpMethod.PUT,
                json(ownerToken, "{\"nome\":\"Alterada\",\"updatedAt\":\"" + lista.updatedAt()
                        + "\",\"itens\":[]}"),
                String.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<CompartilhamentoListaResponse> aceite = restTemplate.exchange(
                "/api/v1/listas/convites/" + convite.id() + "/aceite", HttpMethod.POST,
                autorizado(guestToken), CompartilhamentoListaResponse.class);
        assertThat(aceite.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(aceite.getBody().status().name()).isEqualTo("ACEITO");

        ResponseEntity<ListaCompraResponse> detalhe = restTemplate.exchange(
                "/api/v1/listas/compartilhadas/" + lista.id(), HttpMethod.GET,
                autorizado(guestToken), ListaCompraResponse.class);
        assertThat(detalhe.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detalhe.getBody().perfilAcesso().name()).isEqualTo("CONVIDADO");
        assertThat(detalhe.getBody().modoColaborativo()).isTrue();
    }

    @Test
    void convidadoDeveMarcarEAdicionarItensSemAcessarOperacoesDoOwner() {
        String ownerToken = autenticar("owner@poupacompra.com");
        String guestToken = autenticar("guest@poupacompra.com");
        ListaCompraResponse lista = criarLista(ownerToken);
        aceitar(guestToken, convidar(ownerToken, lista).id());

        ListaCompraResponse detalhe = buscarCompartilhada(guestToken, lista.id());
        var item = detalhe.itens().getFirst();
        ResponseEntity<ListaCompraResponse> selecao = restTemplate.exchange(
                "/api/v1/listas/" + lista.id() + "/itens/" + item.id() + "/selecao", HttpMethod.PATCH,
                json(guestToken, "{\"selecionado\":true,\"updatedAt\":\"" + item.updatedAt() + "\"}"),
                ListaCompraResponse.class);
        assertThat(selecao.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(selecao.getBody().itens().getFirst().selecionado()).isTrue();

        ResponseEntity<ListaCompraResponse> adicao = restTemplate.exchange(
                "/api/v1/listas/" + lista.id() + "/itens", HttpMethod.POST,
                json(guestToken, "{\"descricao\":\"Item do convidado\",\"quantidade\":2,\"unidade\":\"UNIDADE\"}"),
                ListaCompraResponse.class);
        assertThat(adicao.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(adicao.getBody().itens()).extracting(itemResponse -> itemResponse.descricao())
                .contains("Item do convidado");

        ResponseEntity<ListaCompraResponse> vinculoOwner = restTemplate.exchange(
                "/api/v1/listas/" + lista.id() + "/nota", HttpMethod.PATCH,
                json(ownerToken, "{\"notaId\":null,\"updatedAt\":\"" + adicao.getBody().updatedAt() + "\"}"),
                ListaCompraResponse.class);
        assertThat(vinculoOwner.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(vinculoOwner.getBody().modoColaborativo()).isTrue();
        assertThat(vinculoOwner.getBody().compartilhamento()).isNotNull();

        ResponseEntity<String> excluir = restTemplate.exchange(
                "/api/v1/listas/" + lista.id(), HttpMethod.DELETE, autorizado(guestToken), String.class);
        ResponseEntity<String> compartilhar = restTemplate.exchange(
                "/api/v1/listas/" + lista.id() + "/convite", HttpMethod.POST,
                json(guestToken, "{\"email\":\"other@poupacompra.com\",\"updatedAt\":\""
                        + adicao.getBody().updatedAt() + "\"}"), String.class);
        ResponseEntity<String> vincularNota = restTemplate.exchange(
                "/api/v1/listas/" + lista.id() + "/nota", HttpMethod.PATCH,
                json(guestToken, "{\"notaId\":null,\"updatedAt\":\"" + adicao.getBody().updatedAt() + "\"}"),
                String.class);

        assertThat(excluir.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(compartilhar.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(vincularNota.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

        @Test
        void deveRetornarBadRequestQuandoAdicaoColaborativaContemCampoDesconhecido() {
                String ownerToken = autenticar("owner@poupacompra.com");
                String guestToken = autenticar("guest@poupacompra.com");
                ListaCompraResponse lista = criarLista(ownerToken);
                aceitar(guestToken, convidar(ownerToken, lista).id());

                ResponseEntity<String> response = restTemplate.exchange(
                                "/api/v1/listas/" + lista.id() + "/itens", HttpMethod.POST,
                                json(guestToken, "{\"id\":-1,\"descricao\":\"Item inválido\",\"quantidade\":1,\"unidade\":\"UNIDADE\"}"),
                                String.class);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(response.getBody()).isEqualTo("JSON inválido");
        }

    @Test
    void deveRecusarAtualizacaoComVersaoAntigaDoMesmoItem() {
        String ownerToken = autenticar("owner@poupacompra.com");
        String guestToken = autenticar("guest@poupacompra.com");
        ListaCompraResponse lista = criarLista(ownerToken);
        aceitar(guestToken, convidar(ownerToken, lista).id());
        var item = buscarCompartilhada(guestToken, lista.id()).itens().getFirst();
        String payload = "{\"selecionado\":true,\"updatedAt\":\"" + item.updatedAt() + "\"}";

        ResponseEntity<String> primeira = restTemplate.exchange(
                "/api/v1/listas/" + lista.id() + "/itens/" + item.id() + "/selecao", HttpMethod.PATCH,
                json(ownerToken, payload), String.class);
        ResponseEntity<String> segunda = restTemplate.exchange(
                "/api/v1/listas/" + lista.id() + "/itens/" + item.id() + "/selecao", HttpMethod.PATCH,
                json(guestToken, payload), String.class);

        assertThat(primeira.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(segunda.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void deveValidarDestinatarioEImpedirRespostaPorOutroUsuario() {
        String ownerToken = autenticar("owner@poupacompra.com");
        String guestToken = autenticar("guest@poupacompra.com");
        String otherToken = autenticar("other@poupacompra.com");
        ListaCompraResponse lista = criarLista(ownerToken);

        ResponseEntity<String> autoConvite = convidar(ownerToken, lista, "owner@poupacompra.com");
        ResponseEntity<String> usuarioDesconhecido = convidar(ownerToken, lista, "unknown@poupacompra.com");
        CompartilhamentoListaResponse convite = convidar(ownerToken, lista);
        ListaCompraResponse listaAtualizada = buscarLista(ownerToken, lista.id());
        ResponseEntity<String> segundoConvite = convidar(ownerToken, listaAtualizada, "other@poupacompra.com");
        ResponseEntity<String> aceitePorTerceiro = restTemplate.exchange(
                "/api/v1/listas/convites/" + convite.id() + "/aceite", HttpMethod.POST,
                autorizado(otherToken), String.class);
        ResponseEntity<String> recusaPorTerceiro = restTemplate.exchange(
                "/api/v1/listas/convites/" + convite.id() + "/recusa", HttpMethod.POST,
                autorizado(otherToken), String.class);
        ResponseEntity<String> acessoAindaPendente = restTemplate.exchange(
                "/api/v1/listas/compartilhadas/" + lista.id(), HttpMethod.GET,
                autorizado(guestToken), String.class);

        assertThat(autoConvite.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(usuarioDesconhecido.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(segundoConvite.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(aceitePorTerceiro.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(recusaPorTerceiro.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(acessoAindaPendente.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deveRetornarAoModoIndividualAposRecusaESaidaDoConvidado() {
        String ownerToken = autenticar("owner@poupacompra.com");
        String guestToken = autenticar("guest@poupacompra.com");
        ListaCompraResponse lista = criarLista(ownerToken);
        CompartilhamentoListaResponse primeiroConvite = convidar(ownerToken, lista);

        ResponseEntity<Void> recusa = restTemplate.exchange(
                "/api/v1/listas/convites/" + primeiroConvite.id() + "/recusa", HttpMethod.POST,
                autorizado(guestToken), Void.class);
        ListaCompraResponse aposRecusa = buscarLista(ownerToken, lista.id());
        CompartilhamentoListaResponse segundoConvite = convidar(ownerToken, aposRecusa);
        aceitar(guestToken, segundoConvite.id());
        ResponseEntity<Void> saida = restTemplate.exchange(
                "/api/v1/listas/compartilhadas/" + lista.id() + "/participacao", HttpMethod.DELETE,
                autorizado(guestToken), Void.class);
        ListaCompraResponse aposSaida = buscarLista(ownerToken, lista.id());
        ResponseEntity<String> put = restTemplate.exchange(
                "/api/v1/listas/" + lista.id(), HttpMethod.PUT,
                json(ownerToken, "{\"nome\":\"Individual novamente\",\"updatedAt\":\""
                        + aposSaida.updatedAt() + "\",\"itens\":[]}"), String.class);

        assertThat(recusa.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(aposRecusa.modoColaborativo()).isFalse();
        assertThat(saida.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(aposSaida.modoColaborativo()).isFalse();
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

        @Test
        void devePublicarSelecaoConfirmadaNoWebSocket() throws Exception {
                String ownerToken = autenticar("owner@poupacompra.com");
                String guestToken = autenticar("guest@poupacompra.com");
                ListaCompraResponse lista = criarLista(ownerToken);
                aceitar(guestToken, convidar(ownerToken, lista).id());
                var item = buscarCompartilhada(guestToken, lista.id()).itens().getFirst();
                webSocketSubscribeProbe.reiniciar();

                WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
                CompletableFuture<Throwable> erroStomp = new CompletableFuture<>();
                StompSession session = conectarStomp(stompClient, guestToken, erroStomp);
                CompletableFuture<EventoListaResponse> eventoRecebido = new CompletableFuture<>();
                session.subscribe("/topic/listas/" + lista.id(), new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                                return byte[].class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                                try {
                                        EventoListaResponse recebido = objectMapper.readValue((byte[]) payload, EventoListaResponse.class);
                                        eventoRecebido.complete(recebido);
                                } catch (Exception exception) {
                                        eventoRecebido.completeExceptionally(exception);
                                }
                        }
                });
                webSocketSubscribeProbe.aguardar(erroStomp);

                        ResponseEntity<String> response = restTemplate.exchange(
                                "/api/v1/listas/" + lista.id() + "/itens/" + item.id() + "/selecao",
                                HttpMethod.PATCH,
                                json(ownerToken, "{\"selecionado\":true,\"updatedAt\":\"" + item.updatedAt() + "\"}"),
                                String.class);
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                EventoListaResponse evento = eventoRecebido.applyToEither(
                        erroStomp.thenCompose(exception -> CompletableFuture.failedFuture(exception)),
                        recebido -> recebido).get(5, TimeUnit.SECONDS);
                assertThat(evento.tipo().name()).isEqualTo("ITEM_SELECAO_ALTERADA");
                assertThat(evento.listaId()).isEqualTo(lista.id());
                assertThat(evento.item().id()).isEqualTo(item.id());
                assertThat(evento.item().selecionado()).isTrue();
                session.disconnect();
                stompClient.stop();
        }

        @Test
        void deveRejeitarAssinaturaDeUsuarioNaoParticipante() throws Exception {
                String ownerToken = autenticar("owner@poupacompra.com");
                String otherToken = autenticar("other@poupacompra.com");
                ListaCompraResponse lista = criarLista(ownerToken);
                WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
                CompletableFuture<Throwable> erroStomp = new CompletableFuture<>();
                StompSession session = conectarStomp(stompClient, otherToken, erroStomp);

                session.subscribe("/topic/listas/" + lista.id(), new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                                return byte[].class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                        }
                });

                assertThat(erroStomp.get(5, TimeUnit.SECONDS)).hasMessageContaining("Assinatura não autorizada");
                stompClient.stop();
        }

        @Test
        void deveRejeitarEnvioStompOriginadoNoCliente() throws Exception {
                String ownerToken = autenticar("owner@poupacompra.com");
                ListaCompraResponse lista = criarLista(ownerToken);
                WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
                CompletableFuture<Throwable> erroStomp = new CompletableFuture<>();
                StompSession session = conectarStomp(stompClient, ownerToken, erroStomp);

                session.send("/topic/listas/" + lista.id(), new byte[0]);

                assertThat(erroStomp.get(5, TimeUnit.SECONDS)).hasMessageContaining("Envio de mensagens não permitido");
                stompClient.stop();
        }

        @Test
        void deveRejeitarConexaoStompSemToken() throws Exception {
                WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
                CompletableFuture<Throwable> erroStomp = new CompletableFuture<>();

                stompClient.connectAsync("ws://localhost:" + serverPort + "/integracao-poupa-compra/ws",
                        new WebSocketHttpHeaders(), new StompHeaders(), capturarErroStomp(erroStomp));

                assertThat(erroStomp.get(5, TimeUnit.SECONDS)).hasMessageContaining("Token de acesso ausente");
                stompClient.stop();
        }

        @TestConfiguration
        static class WebSocketTestConfiguration {
                @Bean
                WebSocketSubscribeProbe webSocketSubscribeProbe() {
                        return new WebSocketSubscribeProbe();
                }
        }

        static class WebSocketSubscribeProbe {
                private CompletableFuture<Void> assinaturaProcessada = new CompletableFuture<>();

                synchronized void reiniciar() {
                        assinaturaProcessada = new CompletableFuture<>();
                }

                @EventListener
                void registrar(SessionSubscribeEvent event) {
                        assinaturaProcessada.complete(null);
                }

                void aguardar(CompletableFuture<Throwable> erroStomp) throws Exception {
                        assinaturaProcessada.applyToEither(
                                erroStomp.thenCompose(exception -> CompletableFuture.failedFuture(exception)),
                                resultado -> resultado).get(5, TimeUnit.SECONDS);
                }
        }

    private ListaCompraResponse criarLista(String token) {
        ResponseEntity<ListaCompraResponse> response = restTemplate.exchange(
                "/api/v1/listas", HttpMethod.POST,
                json(token, "{\"nome\":\"Compra compartilhada\",\"itens\":[{\"descricao\":\"Arroz\","
                        + "\"quantidade\":1,\"unidade\":\"UNIDADE\",\"selecionado\":false,\"ordem\":0}]}"),
                ListaCompraResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private CompartilhamentoListaResponse convidar(String token, ListaCompraResponse lista) {
        ResponseEntity<CompartilhamentoListaResponse> response = restTemplate.exchange(
                "/api/v1/listas/" + lista.id() + "/convite", HttpMethod.POST,
                json(token, "{\"email\":\"guest@poupacompra.com\",\"updatedAt\":\""
                        + lista.updatedAt() + "\"}"), CompartilhamentoListaResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

        private ResponseEntity<String> convidar(String token, ListaCompraResponse lista, String email) {
                return restTemplate.exchange("/api/v1/listas/" + lista.id() + "/convite", HttpMethod.POST,
                                json(token, "{\"email\":\"" + email + "\",\"updatedAt\":\""
                                                + lista.updatedAt() + "\"}"), String.class);
        }

    private void aceitar(String token, Long conviteId) {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/listas/convites/" + conviteId + "/aceite", HttpMethod.POST,
                autorizado(token), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ListaCompraResponse buscarCompartilhada(String token, Long listaId) {
        return restTemplate.exchange("/api/v1/listas/compartilhadas/" + listaId, HttpMethod.GET,
                autorizado(token), ListaCompraResponse.class).getBody();
    }

    private ListaCompraResponse buscarLista(String token, Long listaId) {
        return restTemplate.exchange("/api/v1/listas/" + listaId, HttpMethod.GET,
                autorizado(token), ListaCompraResponse.class).getBody();
    }

        private StompSession conectarStomp(WebSocketStompClient stompClient, String token,
                        CompletableFuture<Throwable> erroStomp) throws Exception {
                StompHeaders connectHeaders = new StompHeaders();
                connectHeaders.add("Authorization", "Bearer " + token);
                return stompClient.connectAsync("ws://localhost:" + serverPort + "/integracao-poupa-compra/ws",
                                new WebSocketHttpHeaders(), connectHeaders, capturarErroStomp(erroStomp)).get(5, TimeUnit.SECONDS);
        }

        private StompSessionHandlerAdapter capturarErroStomp(CompletableFuture<Throwable> erroStomp) {
                return new StompSessionHandlerAdapter() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                                return byte[].class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                                String corpo = payload == null ? "" : new String((byte[]) payload, StandardCharsets.UTF_8);
                                erroStomp.complete(new IllegalStateException(headers + " " + corpo));
                        }

                        @Override
                        public void handleException(StompSession session, StompCommand command, StompHeaders headers,
                                        byte[] payload, Throwable exception) {
                                erroStomp.complete(exception);
                        }

                        @Override
                        public void handleTransportError(StompSession session, Throwable exception) {
                                erroStomp.complete(exception);
                        }
                };
        }

    private void criarUsuario(String nome, String email) {
        Usuario usuario = new Usuario(nome, email, passwordEncoder.encode("senha-segura"));
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);
    }

    private String autenticar(String email) {
        return restTemplate.postForEntity("/api/v1/auth/login",
                json(null, "{\"email\":\"" + email + "\",\"senha\":\"senha-segura\"}"), AuthResponse.class)
                .getBody().accessToken();
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
