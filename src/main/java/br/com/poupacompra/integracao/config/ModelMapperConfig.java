package br.com.poupacompra.integracao.config;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  public static <S, T> List<T> modelMapperList(List<S> source, Class<T> targetClass) {
    return source
        .stream()
        .map(element -> new ModelMapper().map(element, targetClass))
        .collect(Collectors.toList());
  }
}
