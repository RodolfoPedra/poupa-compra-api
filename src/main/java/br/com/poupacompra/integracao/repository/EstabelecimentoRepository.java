package br.com.poupacompra.integracao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.poupacompra.integracao.model.Estabelecimento;


public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long>{

  Optional<Estabelecimento> findByCpfCnpj(String cpfCnpj);
}
