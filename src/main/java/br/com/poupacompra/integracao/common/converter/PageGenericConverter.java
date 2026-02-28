package br.com.poupacompra.integracao.common.converter;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface PageGenericConverter<S, D> extends ListGenericConverter<S, D> {

  default Page<D> convertPage(Page<S> sourcePage, Pageable pageable) {
    
    List<D> convertedList = dtoToEntity(sourcePage.getContent());

    return new PageImpl<>(convertedList, pageable, sourcePage.getTotalElements());
  }

  default Page<D> convertPage(List<S> sourcePage, Pageable pageable) {
    
    List<D> convertedList = dtoToEntity(sourcePage);

    return new PageImpl<>(convertedList, pageable, sourcePage.size());
  }

}
