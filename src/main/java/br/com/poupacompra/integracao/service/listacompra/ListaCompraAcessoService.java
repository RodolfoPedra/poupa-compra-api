package br.com.poupacompra.integracao.service.listacompra;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.common.exception.ConflitoException;
import br.com.poupacompra.integracao.common.exception.RecursoNaoEncontradoException;
import br.com.poupacompra.integracao.dto.listacompra.PerfilAcessoLista;
import br.com.poupacompra.integracao.model.listacompra.CompartilhamentoListaCompra;
import br.com.poupacompra.integracao.model.listacompra.ListaCompra;
import br.com.poupacompra.integracao.model.listacompra.StatusCompartilhamentoLista;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.repository.CompartilhamentoListaCompraRepository;
import br.com.poupacompra.integracao.repository.ListaCompraRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;

@Service
public class ListaCompraAcessoService {
    private final UsuarioRepository usuarioRepository;
    private final ListaCompraRepository listaRepository;
    private final CompartilhamentoListaCompraRepository compartilhamentoRepository;

    public ListaCompraAcessoService(UsuarioRepository usuarioRepository, ListaCompraRepository listaRepository,
            CompartilhamentoListaCompraRepository compartilhamentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.listaRepository = listaRepository;
        this.compartilhamentoRepository = compartilhamentoRepository;
    }

    public Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    @Transactional(readOnly = true)
    public AcessoListaCompra buscarParaLeitura(String email, Long listaId) {
        Usuario usuario = buscarUsuario(email);
        ListaCompra lista = listaRepository.findById(listaId).orElseThrow(this::listaNaoEncontrada);
        CompartilhamentoListaCompra compartilhamento = compartilhamentoRepository.findByListaId(listaId).orElse(null);
        if (lista.getUsuario().getId().equals(usuario.getId())) {
            return new AcessoListaCompra(lista, PerfilAcessoLista.OWNER, compartilhamento);
        }
        if (compartilhamento != null && compartilhamento.getConvidado().getId().equals(usuario.getId())
                && compartilhamento.getStatus() == StatusCompartilhamentoLista.ACEITO) {
            return new AcessoListaCompra(lista, PerfilAcessoLista.CONVIDADO, compartilhamento);
        }
        throw listaNaoEncontrada();
    }

    public AcessoListaCompra buscarParaColaboracao(String email, Long listaId) {
        Usuario usuario = buscarUsuario(email);
        ListaCompra lista = listaRepository.findByIdForUpdate(listaId).orElseThrow(this::listaNaoEncontrada);
        CompartilhamentoListaCompra compartilhamento = compartilhamentoRepository.findByListaId(listaId).orElse(null);
        if (compartilhamento == null) {
            throw new ConflitoException("A lista não está em modo colaborativo");
        }
        if (lista.getUsuario().getId().equals(usuario.getId())) {
            return new AcessoListaCompra(lista, PerfilAcessoLista.OWNER, compartilhamento);
        }
        if (compartilhamento.getStatus() == StatusCompartilhamentoLista.ACEITO
                && compartilhamento.getConvidado().getId().equals(usuario.getId())) {
            return new AcessoListaCompra(lista, PerfilAcessoLista.CONVIDADO, compartilhamento);
        }
        throw listaNaoEncontrada();
    }

    public AcessoListaCompra buscarOwnerEmColaboracao(String email, Long listaId) {
        AcessoListaCompra acesso = buscarParaColaboracao(email, listaId);
        if (acesso.perfil() != PerfilAcessoLista.OWNER) {
            throw listaNaoEncontrada();
        }
        return acesso;
    }

    private RecursoNaoEncontradoException listaNaoEncontrada() {
        return new RecursoNaoEncontradoException("Lista de compras não encontrada");
    }
}