package br.com.poupacompra.integracao.service.listacompra;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.common.exception.ConflitoException;
import br.com.poupacompra.integracao.common.exception.RecursoNaoEncontradoException;
import br.com.poupacompra.integracao.common.exception.RegraNegocioException;
import br.com.poupacompra.integracao.dto.listacompra.CompartilhamentoListaResponse;
import br.com.poupacompra.integracao.dto.listacompra.ListaCompartilhadaResumoResponse;
import br.com.poupacompra.integracao.dto.listacompra.TipoEventoLista;
import br.com.poupacompra.integracao.model.listacompra.CompartilhamentoListaCompra;
import br.com.poupacompra.integracao.model.listacompra.ListaCompra;
import br.com.poupacompra.integracao.model.listacompra.StatusCompartilhamentoLista;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.model.usuario.UsuarioStatus;
import br.com.poupacompra.integracao.repository.CompartilhamentoListaCompraRepository;
import br.com.poupacompra.integracao.repository.ListaCompraRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ListaCompraCompartilhamentoService {
    private final ListaCompraAcessoService acessoService;
    private final ListaCompraRepository listaRepository;
    private final CompartilhamentoListaCompraRepository compartilhamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ListaCompraEventoPublisher eventoPublisher;

    public ListaCompraCompartilhamentoService(ListaCompraAcessoService acessoService,
            ListaCompraRepository listaRepository,
            CompartilhamentoListaCompraRepository compartilhamentoRepository,
            UsuarioRepository usuarioRepository, ListaCompraEventoPublisher eventoPublisher) {
        this.acessoService = acessoService;
        this.listaRepository = listaRepository;
        this.compartilhamentoRepository = compartilhamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoPublisher = eventoPublisher;
    }

    @Transactional
    public CompartilhamentoListaResponse convidar(String emailOwner, Long listaId, String emailConvidado,
            Instant updatedAt) {
        Usuario owner = acessoService.buscarUsuario(emailOwner);
        ListaCompra lista = buscarListaDoOwnerParaAtualizacao(listaId, owner.getId());
        validarVersao(updatedAt, lista.getUpdatedAt());
        if (compartilhamentoRepository.existsByListaId(listaId)) {
            throw new ConflitoException("A lista já possui um convite");
        }
        Usuario convidado = usuarioRepository.findByEmailIgnoreCase(emailConvidado.trim())
                .filter(usuario -> usuario.getStatus() == UsuarioStatus.ACTIVE)
                .orElseThrow(() -> new RegraNegocioException("Não foi possível convidar o usuário informado"));
        if (owner.getId().equals(convidado.getId())) {
            throw new RegraNegocioException("O proprietário não pode convidar a si mesmo");
        }
        try {
            CompartilhamentoListaCompra compartilhamento = compartilhamentoRepository
                    .saveAndFlush(new CompartilhamentoListaCompra(lista, convidado));
            lista.touch();
            listaRepository.flush();
            eventoPublisher.publicar(TipoEventoLista.LISTA_ATUALIZADA, lista, null);
            log.info("listSharing action=invited listaId={} ownerId={} guestId={}", listaId, owner.getId(), convidado.getId());
            return CompartilhamentoListaResponse.from(compartilhamento);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflitoException("A lista já possui um convite");
        }
    }

    @Transactional
    public void cancelar(String emailOwner, Long listaId) {
        Usuario owner = acessoService.buscarUsuario(emailOwner);
        ListaCompra lista = buscarListaDoOwnerParaAtualizacao(listaId, owner.getId());
        CompartilhamentoListaCompra compartilhamento = compartilhamentoRepository.findByListaId(listaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convite não encontrado"));
        compartilhamentoRepository.delete(compartilhamento);
        lista.touch();
        compartilhamentoRepository.flush();
        listaRepository.flush();
        eventoPublisher.publicar(TipoEventoLista.CONVITE_CANCELADO, listaId);
        log.info("listSharing action=cancelled listaId={} ownerId={}", listaId, owner.getId());
    }

    @Transactional(readOnly = true)
    public List<CompartilhamentoListaResponse> listarConvites(String emailConvidado) {
        Long convidadoId = acessoService.buscarUsuario(emailConvidado).getId();
        return compartilhamentoRepository
                .findAllByConvidadoIdAndStatusOrderByCreatedAtDesc(convidadoId, StatusCompartilhamentoLista.PENDENTE)
                .stream().map(CompartilhamentoListaResponse::from).toList();
    }

    @Transactional
    public CompartilhamentoListaResponse aceitar(String emailConvidado, Long conviteId) {
        Usuario convidado = acessoService.buscarUsuario(emailConvidado);
        CompartilhamentoListaCompra compartilhamento = buscarConvitePendente(conviteId, convidado.getId());
        ListaCompra lista = listaRepository.findByIdForUpdate(compartilhamento.getLista().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convite não encontrado"));
        compartilhamento.aceitar();
        lista.touch();
        compartilhamentoRepository.flush();
        listaRepository.flush();
        eventoPublisher.publicar(TipoEventoLista.LISTA_ATUALIZADA, lista, null);
        log.info("listSharing action=accepted listaId={} guestId={}", lista.getId(), convidado.getId());
        return CompartilhamentoListaResponse.from(compartilhamento);
    }

    @Transactional
    public void recusar(String emailConvidado, Long conviteId) {
        Usuario convidado = acessoService.buscarUsuario(emailConvidado);
        CompartilhamentoListaCompra compartilhamento = buscarConvitePendente(conviteId, convidado.getId());
        ListaCompra lista = listaRepository.findByIdForUpdate(compartilhamento.getLista().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convite não encontrado"));
        compartilhamentoRepository.delete(compartilhamento);
        lista.touch();
        compartilhamentoRepository.flush();
        listaRepository.flush();
        eventoPublisher.publicar(TipoEventoLista.CONVITE_CANCELADO, lista.getId());
        log.info("listSharing action=rejected listaId={} guestId={}", lista.getId(), convidado.getId());
    }

    @Transactional(readOnly = true)
    public List<ListaCompartilhadaResumoResponse> listarCompartilhadas(String emailConvidado) {
        Long convidadoId = acessoService.buscarUsuario(emailConvidado).getId();
        return listaRepository.listarCompartilhadas(convidadoId).stream()
                .map(ListaCompartilhadaResumoResponse::from).toList();
    }

    @Transactional
    public void sair(String emailConvidado, Long listaId) {
        Usuario convidado = acessoService.buscarUsuario(emailConvidado);
        ListaCompra lista = listaRepository.findByIdForUpdate(listaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lista de compras não encontrada"));
        CompartilhamentoListaCompra compartilhamento = compartilhamentoRepository
                .findByListaIdAndConvidadoIdAndStatus(listaId, convidado.getId(), StatusCompartilhamentoLista.ACEITO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lista de compras não encontrada"));
        compartilhamentoRepository.delete(compartilhamento);
        lista.touch();
        compartilhamentoRepository.flush();
        listaRepository.flush();
        eventoPublisher.publicar(TipoEventoLista.PARTICIPACAO_ENCERRADA, listaId);
        log.info("listSharing action=left listaId={} guestId={}", listaId, convidado.getId());
    }

    private CompartilhamentoListaCompra buscarConvitePendente(Long conviteId, Long convidadoId) {
        CompartilhamentoListaCompra compartilhamento = compartilhamentoRepository
                .findByIdAndConvidadoId(conviteId, convidadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convite não encontrado"));
        if (compartilhamento.getStatus() != StatusCompartilhamentoLista.PENDENTE) {
            throw new ConflitoException("O convite já foi respondido");
        }
        return compartilhamento;
    }

    private ListaCompra buscarListaDoOwnerParaAtualizacao(Long listaId, Long ownerId) {
        return listaRepository.findByIdAndUsuarioIdForUpdate(listaId, ownerId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lista de compras não encontrada"));
    }

    private void validarVersao(Instant recebida, Instant atual) {
        if (recebida == null || !recebida.truncatedTo(ChronoUnit.MILLIS).equals(atual.truncatedTo(ChronoUnit.MILLIS))) {
            throw new ConflitoException("A lista foi alterada em outra sessão. Recarregue antes de compartilhar");
        }
    }
}