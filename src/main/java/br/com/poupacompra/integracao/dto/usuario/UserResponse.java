package br.com.poupacompra.integracao.dto.usuario;

import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.model.usuario.UsuarioRole;

public record UserResponse(Long id, String nome, String email, UsuarioRole role, boolean emailVerificado) {
    public static UserResponse from(Usuario usuario) {
        return new UserResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole(), usuario.isEmailVerificado());
    }
}