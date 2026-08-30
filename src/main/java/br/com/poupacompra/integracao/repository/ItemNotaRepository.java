package br.com.poupacompra.integracao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.poupacompra.integracao.model.nota.ItensNota;

public interface ItemNotaRepository extends JpaRepository<ItensNota, Long>{

  @Query("""
	  SELECT item.codigoItem AS codigoItem, item.descricao AS descricao
	  FROM ItensNota item
	  JOIN item.nota nota
	  WHERE nota.usuario.id = :usuarioId AND nota.id IN :notaIds
	  ORDER BY nota.id DESC, item.id DESC
	  """)
  List<ItemNotaRascunhoProjection> listarParaRascunho(@Param("usuarioId") Long usuarioId,
	  @Param("notaIds") List<Long> notaIds);

}
