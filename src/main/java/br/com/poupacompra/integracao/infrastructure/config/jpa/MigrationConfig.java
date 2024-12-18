package br.com.poupacompra.integracao.infrastructure.config.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@Configuration(proxyBeanMethods = false)
public class MigrationConfig {

  @Bean("migrationService")
  InicializacaoDb inicializacaoDb() {
    log.info("Utilizando instância padrão de inicialização. Sem ação");
    return new InicializacaoDb() {
    };
  }
}
