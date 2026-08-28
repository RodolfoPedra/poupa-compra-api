package br.com.poupacompra.integracao.service.nota.impl;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.poupacompra.integracao.common.converter.impl.EstabelecimentoConverter;
import br.com.poupacompra.integracao.common.converter.impl.GeralNotaConverter;
import br.com.poupacompra.integracao.common.converter.impl.ItensNotaConverter;
import br.com.poupacompra.integracao.common.exception.NotaNaoEncontradaException;
import br.com.poupacompra.integracao.dto.nota.NotaCompletaDTO;
import br.com.poupacompra.integracao.model.nota.Estabelecimento;
import br.com.poupacompra.integracao.model.nota.GeralNota;
import br.com.poupacompra.integracao.model.nota.ItensNota;
import br.com.poupacompra.integracao.model.usuario.Usuario;
import br.com.poupacompra.integracao.repository.EstabelecimentoRepository;
import br.com.poupacompra.integracao.repository.NotaRepository;
import br.com.poupacompra.integracao.repository.UsuarioRepository;
import br.com.poupacompra.integracao.service.nota.NotaService;
import br.com.poupacompra.integracao.service.nota.validation.NotaValidation;

@Service
public class NotaServiceImpl implements NotaService {

  private final NotaRepository notaRepository;

  private final EstabelecimentoRepository estabelecimentoRepository;

  private final GeralNotaConverter geralNotaConverter;

  private final EstabelecimentoConverter estabelecimentoConverter;

  private final ItensNotaConverter itensNotaConverter;

  private final NotaValidation notaValidation;
  private final UsuarioRepository usuarioRepository;

  public NotaServiceImpl(NotaRepository notaRepository, EstabelecimentoRepository estabelecimentoRepository, GeralNotaConverter geralNotaConverter, EstabelecimentoConverter estabelecimentoConverter, ItensNotaConverter itensNotaConverter, NotaValidation notaValidation, UsuarioRepository usuarioRepository) {
    this.notaRepository = notaRepository;
    this.estabelecimentoRepository = estabelecimentoRepository;
    this.geralNotaConverter = geralNotaConverter;
    this.estabelecimentoConverter = estabelecimentoConverter;
    this.itensNotaConverter = itensNotaConverter;
    this.notaValidation = notaValidation;
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  @Transactional
  public GeralNota salvarNota(NotaCompletaDTO notaDTO) {

    notaValidation.validarNotaExistente(notaDTO.getNota().getChaveAcesso());

    GeralNota geralNota = geralNotaConverter.dtoToEntity(notaDTO.getNota());
    geralNota.setUsuario(usuarioRepository.findByEmailIgnoreCase(SecurityContextHolder.getContext().getAuthentication().getName())
      .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado")));
    Estabelecimento estabelecimento = estabelecimentoConverter.dtoToEntity(notaDTO.getEstabelecimento());
    List<ItensNota> itensNota =  itensNotaConverter.dtoToEntity(notaDTO.getItensNota());

    var optEst = estabelecimentoRepository.findByCpfCnpj(estabelecimento.getCpfCnpj());
    Estabelecimento currentEstabelecimento;
    if (optEst.isPresent()) {
      currentEstabelecimento = optEst.get();
    } else {
      currentEstabelecimento = estabelecimentoRepository.save(estabelecimento);
    }

    geralNota.setEstabelecimento(currentEstabelecimento);
    geralNota.setItensNotas(itensNota);

    return notaRepository.save(geralNota);
  }

  @Override
  public List<GeralNota> listarNotas() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (isAdmin(authentication)) {
      return notaRepository.findAll();
    }
    return notaRepository.findByUsuarioId(usuarioAutenticado(authentication).getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ItensNota> listarItensDaNota(Long notaId) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    GeralNota nota = notaRepository.findById(notaId)
        .orElseThrow(() -> new NotaNaoEncontradaException("Nota não encontrada"));

    if (!isAdmin(authentication) && !nota.getUsuario().getId().equals(usuarioAutenticado(authentication).getId())) {
      throw new AccessDeniedException("Nota pertence a outro usuário");
    }

    return List.copyOf(nota.getItensNotas());
  }

  private Usuario usuarioAutenticado(Authentication authentication) {
    return usuarioRepository.findByEmailIgnoreCase(authentication.getName())
        .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
  }

}
