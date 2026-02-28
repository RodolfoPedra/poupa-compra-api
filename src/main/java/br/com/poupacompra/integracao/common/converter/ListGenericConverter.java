package br.com.poupacompra.integracao.common.converter;

import java.util.Collection;
import java.util.List;

public interface ListGenericConverter<S, D> extends GenericConverter<S, D> {

    /**
     * Converte uma lista de objetos do tipo S para uma lista de objetos do tipo D.
     *
     * @param sourceList Lista de objetos de origem
     * @return Lista de objetos convertidos do tipo D
     */
    default List<D> dtoToEntity(Collection<S> sourceList) {
        return sourceList.stream()
                .map(this::dtoToEntity)
                .toList();
    }
}
