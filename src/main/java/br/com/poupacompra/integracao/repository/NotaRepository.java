package br.com.poupacompra.integracao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.poupacompra.integracao.model.nota.GeralNota;

public interface NotaRepository extends JpaRepository<GeralNota, Long>{

  Optional<GeralNota> findByChaveAcesso(String chaveAcesso);

  List<GeralNota> findByUsuarioId(Long usuarioId);

  Optional<GeralNota> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("""
      SELECT DISTINCT estabelecimento.id AS id,
         estabelecimento.nomeEstabelecimento AS nome,
         estabelecimento.cpfCnpj AS cpfCnpj
      FROM GeralNota nota
      JOIN nota.estabelecimento estabelecimento
      WHERE nota.usuario.id = :usuarioId
      ORDER BY estabelecimento.nomeEstabelecimento, estabelecimento.id
      """)
    List<EstabelecimentoNotaProjection> listarEstabelecimentosDoUsuario(@Param("usuarioId") Long usuarioId);

    @Query(value = """
      SELECT nota.id AS id, nota.numeroCfe AS numeroCfe, nota.dataHoraEmissao AS dataHoraEmissao,
         nota.valorTotal AS valorTotal, nota.quantidadeItens AS quantidadeItens
      FROM GeralNota nota
      WHERE nota.usuario.id = :usuarioId AND nota.estabelecimento.id = :estabelecimentoId
      ORDER BY nota.id DESC
      """, countQuery = """
      SELECT COUNT(nota.id) FROM GeralNota nota
      WHERE nota.usuario.id = :usuarioId AND nota.estabelecimento.id = :estabelecimentoId
      """)
    Page<NotaOrigemListaProjection> listarPorUsuarioEEstabelecimento(@Param("usuarioId") Long usuarioId,
      @Param("estabelecimentoId") Long estabelecimentoId, Pageable pageable);

    boolean existsByUsuarioIdAndEstabelecimentoId(Long usuarioId, Long estabelecimentoId);

    @Query("""
      SELECT nota.id AS id, nota.estabelecimento.id AS estabelecimentoId,
         nota.estabelecimento.nomeEstabelecimento AS estabelecimentoNome
      FROM GeralNota nota
      WHERE nota.usuario.id = :usuarioId AND nota.id IN :notaIds
      """)
    List<NotaSelecionadaProjection> buscarSelecionadasDoUsuario(@Param("usuarioId") Long usuarioId,
      @Param("notaIds") List<Long> notaIds);

    @Query(value = """
      SELECT nota.id AS id, nota.numeroCfe AS numeroCfe, nota.dataHoraEmissao AS dataHoraEmissao,
         nota.valorTotal AS valorTotal, nota.quantidadeItens AS quantidadeItens
      FROM GeralNota nota
      LEFT JOIN ListaCompra listaVinculada ON listaVinculada.nota = nota
      WHERE nota.usuario.id = :usuarioId
        AND (listaVinculada.id IS NULL OR listaVinculada.id = :listaId)
      ORDER BY nota.id DESC
      """, countQuery = """
      SELECT COUNT(nota.id)
      FROM GeralNota nota
      LEFT JOIN ListaCompra listaVinculada ON listaVinculada.nota = nota
      WHERE nota.usuario.id = :usuarioId
        AND (listaVinculada.id IS NULL OR listaVinculada.id = :listaId)
      """)
    Page<NotaOrigemListaProjection> listarDisponiveisParaLista(@Param("usuarioId") Long usuarioId,
      @Param("listaId") Long listaId, Pageable pageable);

}
