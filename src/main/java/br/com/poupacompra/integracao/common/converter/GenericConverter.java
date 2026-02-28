package br.com.poupacompra.integracao.common.converter;

/**
 * Interface genérica para conversão entre tipos de objetos.
 *
 * @param <D> DTO 
 * @param <E> Entity
 */
public interface GenericConverter<D, E> {
    /**
     * Converte um objeto do tipo S para o tipo D.
     *
     * @param source Objeto de origem
     * @return Objeto convertido do tipo D
     */
    E dtoToEntity(D source);

    D entityToDto(E source);
}
