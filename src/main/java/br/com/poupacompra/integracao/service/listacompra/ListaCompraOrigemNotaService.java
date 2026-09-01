package br.com.poupacompra.integracao.service.listacompra;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.common.exception.RecursoNaoEncontradoException;
import br.com.poupacompra.integracao.common.exception.RegraNegocioException;
import br.com.poupacompra.integracao.dto.listacompra.EstabelecimentoNotaResponse;
import br.com.poupacompra.integracao.dto.listacompra.ItemRascunhoListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.NotaOrigemListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.PaginaResponse;
import br.com.poupacompra.integracao.dto.listacompra.RascunhoListaNotasResponse;
import br.com.poupacompra.integracao.repository.ItemNotaRepository;
import br.com.poupacompra.integracao.repository.NotaRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;

@Service
public class ListaCompraOrigemNotaService {
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int TAMANHO_MAXIMO_NOME = 120;

    private final NotaRepository notaRepository;
    private final ItemNotaRepository itemNotaRepository;
    private final UsuarioRepository usuarioRepository;

    public ListaCompraOrigemNotaService(NotaRepository notaRepository, ItemNotaRepository itemNotaRepository,
            UsuarioRepository usuarioRepository) {
        this.notaRepository = notaRepository;
        this.itemNotaRepository = itemNotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<EstabelecimentoNotaResponse> listarEstabelecimentos(String email) {
        Long usuarioId = buscarUsuarioId(email);
        return notaRepository.listarEstabelecimentosDoUsuario(usuarioId).stream()
                .map(EstabelecimentoNotaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<NotaOrigemListaResponse> listarNotas(String email, Long estabelecimentoId,
            int pagina, int tamanho) {
        Long usuarioId = buscarUsuarioId(email);
        if (!notaRepository.existsByUsuarioIdAndEstabelecimentoId(usuarioId, estabelecimentoId)) {
            throw new RecursoNaoEncontradoException("Estabelecimento não encontrado");
        }
        return PaginaResponse.from(notaRepository
                .listarPorUsuarioEEstabelecimento(usuarioId, estabelecimentoId, PageRequest.of(pagina, tamanho))
                .map(NotaOrigemListaResponse::from));
    }

    @Transactional(readOnly = true)
    public RascunhoListaNotasResponse gerarRascunho(String email, List<Long> notaIds) {
        Long usuarioId = buscarUsuarioId(email);
        List<Long> idsUnicos = notaIds.stream().distinct().toList();
        if (idsUnicos.size() != notaIds.size()) {
            throw new RegraNegocioException("Não repita notas na seleção");
        }

        var notas = notaRepository.buscarSelecionadasDoUsuario(usuarioId, idsUnicos);
        if (notas.size() != idsUnicos.size()) {
            throw new RecursoNaoEncontradoException("Uma ou mais notas não foram encontradas");
        }
        if (new HashSet<>(notas.stream().map(nota -> nota.getEstabelecimentoId()).toList()).size() != 1) {
            throw new RegraNegocioException("As notas devem pertencer ao mesmo estabelecimento");
        }

        var descricoesPorCodigo = new LinkedHashMap<Long, String>();
        itemNotaRepository.listarParaRascunho(usuarioId, idsUnicos)
                .forEach(item -> descricoesPorCodigo.putIfAbsent(item.getCodigoItem(), item.getDescricao().trim()));
        List<String> descricoes = List.copyOf(descricoesPorCodigo.values());
        List<ItemRascunhoListaResponse> itens = IntStream.range(0, descricoes.size())
            .mapToObj(ordem -> ItemRascunhoListaResponse.from(descricoes.get(ordem), ordem))
                .toList();

        return new RascunhoListaNotasResponse(criarNome(notas.getFirst().getEstabelecimentoNome()), itens);
    }

    private Long buscarUsuarioId(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"))
                .getId();
    }

    private String criarNome(String estabelecimento) {
        String prefixo = "Compras - ";
        String sufixo = " - " + LocalDate.now().format(FORMATO_DATA);
        int limiteEstabelecimento = TAMANHO_MAXIMO_NOME - prefixo.length() - sufixo.length();
        String nomeEstabelecimento = estabelecimento.length() > limiteEstabelecimento
                ? estabelecimento.substring(0, limiteEstabelecimento).trim()
                : estabelecimento;
        return prefixo + nomeEstabelecimento + sufixo;
    }
}