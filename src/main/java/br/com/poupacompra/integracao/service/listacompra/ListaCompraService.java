package br.com.poupacompra.integracao.service.listacompra;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.common.exception.ConflitoException;
import br.com.poupacompra.integracao.common.exception.RecursoNaoEncontradoException;
import br.com.poupacompra.integracao.common.exception.RegraNegocioException;
import br.com.poupacompra.integracao.dto.listacompra.AdicionarItemListaRequest;
import br.com.poupacompra.integracao.dto.listacompra.AtualizarItemListaRequest;
import br.com.poupacompra.integracao.dto.listacompra.CompartilhamentoListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.ItemListaCompraResponse;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompraResponse;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompraResumoResponse;
import br.com.poupacompra.integracao.dto.listacompra.NotaOrigemListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.PaginaResponse;
import br.com.poupacompra.integracao.dto.listacompra.PerfilAcessoLista;
import br.com.poupacompra.integracao.dto.listacompra.SalvarItemListaCompraRequest;
import br.com.poupacompra.integracao.dto.listacompra.SalvarListaCompraRequest;
import br.com.poupacompra.integracao.dto.listacompra.TipoEventoLista;
import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.listacompra.ItemListaCompra;
import br.com.poupacompra.integracao.model.listacompra.ListaCompra;
import br.com.poupacompra.integracao.model.listacompra.Produto;
import br.com.poupacompra.integracao.model.listacompra.UnidadeMedida;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.repository.CompartilhamentoListaCompraRepository;
import br.com.poupacompra.integracao.repository.ItemListaCompraRepository;
import br.com.poupacompra.integracao.repository.ListaCompraRepository;
import br.com.poupacompra.integracao.repository.NotaRepository;
import br.com.poupacompra.integracao.repository.ProdutoRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;
import br.com.poupacompra.integracao.service.nota.NotaService;

@Service
public class ListaCompraService {
    private final ListaCompraRepository listaRepository;
    private final ItemListaCompraRepository itemRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotaRepository notaRepository;
    private final NotaService notaService;
    private final CompartilhamentoListaCompraRepository compartilhamentoRepository;
    private final ListaCompraAcessoService acessoService;
    private final ListaCompraEventoPublisher eventoPublisher;

    public ListaCompraService(ListaCompraRepository listaRepository, ItemListaCompraRepository itemRepository,
            ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository, NotaRepository notaRepository,
            NotaService notaService, CompartilhamentoListaCompraRepository compartilhamentoRepository,
            ListaCompraAcessoService acessoService, ListaCompraEventoPublisher eventoPublisher) {
        this.listaRepository = listaRepository;
        this.itemRepository = itemRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notaRepository = notaRepository;
        this.notaService = notaService;
        this.compartilhamentoRepository = compartilhamentoRepository;
        this.acessoService = acessoService;
        this.eventoPublisher = eventoPublisher;
    }

    @Transactional
    public ListaCompraResponse criar(String email, SalvarListaCompraRequest request) {
        Usuario usuario = buscarUsuario(email);
        String nome = request.nome().trim();
        validarNomeDisponivel(usuario.getId(), nome, null);
        validarIdsAusentesNaCriacao(request.itens());
        Map<Long, Produto> produtos = buscarEValidarProdutos(request.itens());
        try {
            ListaCompra lista = listaRepository.saveAndFlush(new ListaCompra(usuario, nome));
            itemRepository.saveAll(criarItens(lista, request.itens(), produtos));
            itemRepository.flush();
            return montarDetalhe(lista);
        } catch (DataIntegrityViolationException exception) {
            throw traduzirConflito();
        }
    }

    @Transactional(readOnly = true)
    public List<ListaCompraResumoResponse> listar(String email) {
        Long usuarioId = buscarUsuario(email).getId();
        return listaRepository.listarResumos(usuarioId).stream().map(ListaCompraResumoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ListaCompraResponse buscar(String email, Long listaId) {
        AcessoListaCompra acesso = acessoService.buscarParaLeitura(email, listaId);
        return montarDetalhe(acesso);
    }

    @Transactional
    public ListaCompraResponse salvar(String email, Long listaId, SalvarListaCompraRequest request) {
        Long usuarioId = buscarUsuario(email).getId();
        ListaCompra lista = listaRepository.findByIdAndUsuarioIdForUpdate(listaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lista de compras não encontrada"));
        if (compartilhamentoRepository.existsByListaId(listaId)) {
            throw new ConflitoException("A lista está em modo colaborativo e deve ser alterada por operações específicas");
        }
        validarVersao(request.updatedAt(), lista.getUpdatedAt());
        String nome = request.nome().trim();
        validarNomeDisponivel(lista.getUsuario().getId(), nome, listaId);
        Map<Long, ItemListaCompra> existentes = itemRepository.findAllByListaId(listaId).stream()
                .collect(Collectors.toMap(ItemListaCompra::getId, Function.identity()));
        validarIdsDosItens(request.itens(), existentes.keySet());
        Map<Long, Produto> produtos = buscarEValidarProdutos(request.itens());

        lista.setNome(nome);
        Set<Long> mantidos = new HashSet<>();
        List<ItemListaCompra> novos = new ArrayList<>();
        for (SalvarItemListaCompraRequest requestItem : request.itens()) {
            Produto produto = requestItem.produtoId() == null ? null : produtos.get(requestItem.produtoId());
            String descricao = produto == null ? normalizarDescricao(requestItem.descricao()) : produto.getNome();
            validarQuantidade(requestItem.quantidade(), requestItem.unidade());
            if (requestItem.id() == null) {
                ItemListaCompra novo = new ItemListaCompra(lista, produto, descricao, requestItem.quantidade(),
                        requestItem.unidade(), requestItem.ordem());
                novo.setSelecionado(requestItem.selecionado());
                novos.add(novo);
            } else {
                ItemListaCompra item = existentes.get(requestItem.id());
                Long produtoAtualId = item.getProduto() == null ? null : item.getProduto().getId();
                if (!Objects.equals(produtoAtualId, requestItem.produtoId())) {
                    throw new RegraNegocioException("O produto de um item existente não pode ser alterado");
                }
                item.setDescricao(descricao);
                item.setQuantidade(requestItem.quantidade());
                item.setUnidade(requestItem.unidade());
                item.setSelecionado(requestItem.selecionado());
                item.setOrdem(requestItem.ordem());
                mantidos.add(item.getId());
            }
        }
        existentes.values().stream().filter(item -> !mantidos.contains(item.getId())).forEach(itemRepository::delete);
        itemRepository.saveAll(novos);
        lista.touch();
        try {
            itemRepository.flush();
            listaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw traduzirConflito();
        }
        return montarDetalhe(lista);
    }

    @Transactional
    public ListaCompraResponse atualizarSelecao(String email, Long listaId, Long itemId, boolean selecionado,
            Instant updatedAt) {
        AcessoListaCompra acesso = acessoService.buscarParaColaboracao(email, listaId);
        ItemListaCompra item = buscarItemParaAtualizacao(listaId, itemId);
        validarVersao(updatedAt, item.getUpdatedAt());
        item.setSelecionado(selecionado);
        acesso.lista().touch();
        itemRepository.flush();
        listaRepository.flush();
        eventoPublisher.publicar(TipoEventoLista.ITEM_SELECAO_ALTERADA, acesso.lista(), item);
        return montarDetalhe(acesso);
    }

    @Transactional
    public ListaCompraResponse adicionarItem(String email, Long listaId, AdicionarItemListaRequest request) {
        AcessoListaCompra acesso = acessoService.buscarParaColaboracao(email, listaId);
        Produto produto = null;
        if (request.produtoId() != null) {
            produto = produtoRepository.findById(request.produtoId()).filter(Produto::isAtivo)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
            if (itemRepository.existsByListaIdAndProdutoId(listaId, request.produtoId())) {
                throw new ConflitoException("O produto já está presente na lista");
            }
        }
        String descricao = produto == null ? normalizarDescricao(request.descricao()) : produto.getNome();
        validarQuantidade(request.quantidade(), request.unidade());
        ItemListaCompra item = new ItemListaCompra(acesso.lista(), produto, descricao, request.quantidade(),
                request.unidade(), itemRepository.findMaiorOrdem(listaId) + 1);
        try {
            itemRepository.saveAndFlush(item);
            acesso.lista().touch();
            listaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw traduzirConflito();
        }
        eventoPublisher.publicar(TipoEventoLista.ITEM_ADICIONADO, acesso.lista(), item);
        return montarDetalhe(acesso);
    }

    @Transactional
    public ListaCompraResponse atualizarNome(String email, Long listaId, String nome, Instant updatedAt) {
        AcessoListaCompra acesso = acessoService.buscarOwnerEmColaboracao(email, listaId);
        validarVersao(updatedAt, acesso.lista().getUpdatedAt());
        String nomeNormalizado = nome.trim();
        validarNomeDisponivel(acesso.lista().getUsuario().getId(), nomeNormalizado, listaId);
        acesso.lista().setNome(nomeNormalizado);
        acesso.lista().touch();
        listaRepository.flush();
        eventoPublisher.publicar(TipoEventoLista.LISTA_ATUALIZADA, acesso.lista(), null);
        return montarDetalhe(acesso);
    }

    @Transactional
    public ListaCompraResponse atualizarItem(String email, Long listaId, Long itemId,
            AtualizarItemListaRequest request) {
        AcessoListaCompra acesso = acessoService.buscarOwnerEmColaboracao(email, listaId);
        ItemListaCompra item = buscarItemParaAtualizacao(listaId, itemId);
        validarVersao(request.updatedAt(), item.getUpdatedAt());
        validarQuantidade(request.quantidade(), request.unidade());
        if (item.getProduto() == null) {
            item.setDescricao(normalizarDescricao(request.descricao()));
        }
        item.setQuantidade(request.quantidade());
        item.setUnidade(request.unidade());
        acesso.lista().touch();
        itemRepository.flush();
        listaRepository.flush();
        eventoPublisher.publicar(TipoEventoLista.ITEM_ATUALIZADO, acesso.lista(), item);
        return montarDetalhe(acesso);
    }

    @Transactional
    public ListaCompraResponse removerItem(String email, Long listaId, Long itemId, Instant updatedAt) {
        AcessoListaCompra acesso = acessoService.buscarOwnerEmColaboracao(email, listaId);
        ItemListaCompra item = buscarItemParaAtualizacao(listaId, itemId);
        validarVersao(updatedAt, item.getUpdatedAt());
        itemRepository.delete(item);
        acesso.lista().touch();
        itemRepository.flush();
        listaRepository.flush();
        eventoPublisher.publicarRemocao(acesso.lista(), itemId);
        return montarDetalhe(acesso);
    }

    @Transactional
    public void excluir(String email, Long listaId) {
        listaRepository.delete(Objects.requireNonNull(buscarListaDoUsuario(email, listaId)));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<NotaOrigemListaResponse> listarNotasDisponiveis(String email, Long listaId,
            int pagina, int tamanho) {
        Long usuarioId = buscarUsuario(email).getId();
        if (!listaRepository.existsByIdAndUsuarioId(listaId, usuarioId)) {
            throw new RecursoNaoEncontradoException("Lista de compras não encontrada");
        }
        return PaginaResponse.from(notaRepository
                .listarDisponiveisParaLista(usuarioId, listaId, PageRequest.of(pagina, tamanho))
                .map(NotaOrigemListaResponse::from));
    }

    @Transactional
    public ListaCompraResponse atualizarNota(String email, Long listaId, Long notaId, Instant updatedAt) {
        Usuario usuario = buscarUsuario(email);
        ListaCompra lista = buscarListaParaAtualizacao(listaId, usuario.getId(), updatedAt);
        if (notaId == null) {
            lista.setNota(null);
        } else {
            var nota = notaRepository.findByIdAndUsuarioId(notaId, usuario.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Nota fiscal não encontrada"));
            if (listaRepository.existsByNotaIdAndIdNot(notaId, listaId)) {
                throw new ConflitoException("A nota fiscal já está vinculada a outra lista");
            }
            lista.setNota(nota);
        }
        return salvarVinculo(lista);
    }

    @Transactional
    public ListaCompraResponse cadastrarNotaVinculada(String email, Long listaId, Instant updatedAt,
            NotaCompletaDTO notaRequest) {
        Usuario usuario = buscarUsuario(email);
        ListaCompra lista = buscarListaParaAtualizacao(listaId, usuario.getId(), updatedAt);
        lista.setNota(notaService.salvarNota(notaRequest));
        return salvarVinculo(lista);
    }

    private ListaCompra buscarListaParaAtualizacao(Long listaId, Long usuarioId, Instant updatedAt) {
        ListaCompra lista = listaRepository.findByIdAndUsuarioIdForUpdate(listaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lista de compras não encontrada"));
        validarVersao(updatedAt, lista.getUpdatedAt());
        return lista;
    }

    private ListaCompraResponse salvarVinculo(ListaCompra lista) {
        lista.touch();
        try {
            listaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoException("A nota fiscal já está vinculada a outra lista");
        }
        var compartilhamento = compartilhamentoRepository.findByListaId(lista.getId()).orElse(null);
        if (compartilhamento != null) {
            eventoPublisher.publicar(TipoEventoLista.LISTA_ATUALIZADA, lista, null);
        }
        List<ItemListaCompraResponse> itens = itemRepository
            .findAllByListaIdOrderByOrdemAscIdAsc(lista.getId()).stream()
            .map(ItemListaCompraResponse::from).toList();
        return ListaCompraResponse.from(lista, itens, PerfilAcessoLista.OWNER,
            CompartilhamentoListaResponse.from(compartilhamento));
    }

    private ListaCompraResponse montarDetalhe(ListaCompra lista) {
        List<ItemListaCompraResponse> itens = itemRepository.findAllByListaIdOrderByOrdemAscIdAsc(lista.getId()).stream()
                .map(ItemListaCompraResponse::from)
                .toList();
        return ListaCompraResponse.from(lista, itens);
    }

    private ListaCompraResponse montarDetalhe(AcessoListaCompra acesso) {
        List<ItemListaCompraResponse> itens = itemRepository
                .findAllByListaIdOrderByOrdemAscIdAsc(acesso.lista().getId()).stream()
                .map(ItemListaCompraResponse::from).toList();
        return ListaCompraResponse.from(acesso.lista(), itens, acesso.perfil(),
            CompartilhamentoListaResponse.from(acesso.compartilhamento()));
    }

    private ItemListaCompra buscarItemParaAtualizacao(Long listaId, Long itemId) {
        return itemRepository.findByIdAndListaId(itemId, listaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado"));
    }

    private ListaCompra buscarListaDoUsuario(String email, Long listaId) {
        Long usuarioId = buscarUsuario(email).getId();
        return listaRepository.findByIdAndUsuarioId(listaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lista de compras não encontrada"));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    private void validarNomeDisponivel(Long usuarioId, String nome, Long listaId) {
        boolean existe = listaId == null
                ? listaRepository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, nome)
                : listaRepository.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(usuarioId, nome, listaId);
        if (existe) {
            throw new ConflitoException("Já existe uma lista com este nome");
        }
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Descrição é obrigatória para item personalizado");
        }
        return descricao.trim();
    }

    private void validarQuantidade(BigDecimal quantidade, UnidadeMedida unidade) {
        if (quantidade != null && unidade == UnidadeMedida.UNIDADE
                && quantidade.stripTrailingZeros().scale() > 0) {
            throw new RegraNegocioException("Quantidade por unidade deve ser um número inteiro");
        }
    }

    private void validarVersao(Instant recebida, Instant atual) {
        if (recebida == null || !recebida.truncatedTo(ChronoUnit.MILLIS).equals(atual.truncatedTo(ChronoUnit.MILLIS))) {
            throw new ConflitoException("A lista foi alterada em outra sessão. Recarregue antes de salvar");
        }
    }

    private void validarIdsAusentesNaCriacao(List<SalvarItemListaCompraRequest> itens) {
        if (itens.stream().anyMatch(item -> item.id() != null)) {
            throw new RegraNegocioException("Itens de uma nova lista não podem possuir identificador");
        }
    }

    private void validarIdsDosItens(List<SalvarItemListaCompraRequest> itens, Set<Long> idsExistentes) {
        Set<Long> idsRecebidos = new HashSet<>();
        for (SalvarItemListaCompraRequest item : itens) {
            if (item.id() != null && (!idsExistentes.contains(item.id()) || !idsRecebidos.add(item.id()))) {
                throw new RegraNegocioException("Item inválido para esta lista");
            }
        }
    }

    private Map<Long, Produto> buscarEValidarProdutos(List<SalvarItemListaCompraRequest> itens) {
        List<Long> idsInformados = itens.stream().map(SalvarItemListaCompraRequest::produtoId)
                .filter(Objects::nonNull).toList();
        Set<Long> idsUnicos = new HashSet<>(idsInformados);
        if (idsUnicos.size() != idsInformados.size()) {
            throw new ConflitoException("O mesmo produto não pode aparecer mais de uma vez na lista");
        }
        Map<Long, Produto> produtos = produtoRepository.findAllById(idsUnicos).stream()
                .filter(Produto::isAtivo)
                .collect(Collectors.toMap(Produto::getId, Function.identity()));
        if (produtos.size() != idsUnicos.size()) {
            throw new RecursoNaoEncontradoException("Produto não encontrado");
        }
        return produtos;
    }

    private List<ItemListaCompra> criarItens(ListaCompra lista, List<SalvarItemListaCompraRequest> requests,
            Map<Long, Produto> produtos) {
        return requests.stream().map(request -> {
            Produto produto = request.produtoId() == null ? null : produtos.get(request.produtoId());
            String descricao = produto == null ? normalizarDescricao(request.descricao()) : produto.getNome();
            validarQuantidade(request.quantidade(), request.unidade());
            ItemListaCompra item = new ItemListaCompra(lista, produto, descricao, request.quantidade(), request.unidade(),
                    request.ordem());
            item.setSelecionado(request.selecionado());
            return item;
        }).toList();
    }

    private ConflitoException traduzirConflito() {
        return new ConflitoException("Não foi possível salvar: nome ou produto duplicado na lista");
    }
}