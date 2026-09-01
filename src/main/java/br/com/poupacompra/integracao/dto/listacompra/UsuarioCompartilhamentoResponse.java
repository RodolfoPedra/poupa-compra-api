package br.com.poupacompra.integracao.dto.listacompra;

import br.com.poupacompra.integracao.model.usuario.Usuario;

public record UsuarioCompartilhamentoResponse(Long id, String nome, String email) {
    public static UsuarioCompartilhamentoResponse from(Usuario usuario) {
        return new UsuarioCompartilhamentoResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}