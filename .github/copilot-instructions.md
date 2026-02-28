# Diretrizes do Projeto — Poupa Compra API

## Estilo de Código

- **Java 21** com **Spring Boot 3.5.0** e **Maven**
- **Nomes em português:** classes, métodos, variáveis, exceções e tabelas (ex: `salvarNota`, `NotaJaCadastradaException`)
- **Lombok** obrigatório: `@Data`/`@Builder`/`@AllArgsConstructor`/`@NoArgsConstructor` em DTOs; `@Getter`/`@Setter`/`@Builder` em entidades JPA (evitar `@Data` em `@Entity`)
- Injeção de dependência via **construtor** nos services (sem `@Autowired`); nos controllers usa-se `@Autowired` explícito
- Referência de estilo: [NotaServiceImpl.java](src/main/java/br/com/poupacompra/integracao/service/nota/impl/NotaServiceImpl.java)

## Arquitetura

- **Camadas:** Controller → Service (interface + impl) → Repository
- **Pacote base:** `br.com.poupacompra.integracao`
- **Converters manuais** com hierarquia genérica (`GenericConverter` → `ListGenericConverter` → `PageGenericConverter`) em `common/converter/`. Novas conversões devem seguir esse padrão — ver [GeralNotaConverter.java](src/main/java/br/com/poupacompra/integracao/common/converter/impl/GeralNotaConverter.java)
- **Validações de negócio** isoladas em classes `*Validation` (`@Component`) no pacote `service/*/validation/` — ver [NotaValidation.java](src/main/java/br/com/poupacompra/integracao/service/nota/validation/NotaValidation.java)
- **Exceções customizadas** em `common/exception/`, tratadas via `@RestControllerAdvice` em [ControllerAdvice.java](src/main/java/br/com/poupacompra/integracao/common/exception/advice/ControllerAdvice.java)
- **DTOs** agrupados por domínio em `dto/` (ex: `dto/nota/`). Wrapper DTOs como `NotaCompletaDTO` agregam sub-DTOs
- Métodos de serviço transacionais com `@Transactional`

## Build e Testes

```bash
# Compilar e gerar JAR
./mvnw clean package

# Rodar testes
./mvnw test

# Deploy (Docker Swarm)
bash deploy.sh
```

- **PostgreSQL** em produção/dev, **H2 em memória** nos testes (perfil `test`)
- Testes de integração usam `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` + `@ActiveProfiles("test")`
- Para evitar conflitos de unicidade nos testes, adicionar sufixo único aos campos restritivos (ex: `urlCfe`, `chaveAcesso`)
- Referência de teste: [NotaIntegrationTest.java](src/test/java/br/com/poupacompra/integracao/NotaIntegrationTest.java)

## Convenções do Projeto

- **Context path:** `/integracao-poupa-compra` — todos os endpoints são relativos a ele
- **Porta padrão:** `8181`
- **Swagger/OpenAPI:** anotar controllers com `@OpenAPIDefinition`, `@Tag`, `@Operation`, `@ApiResponse` (lib `springdoc-openapi-ui`)
- **IDs:** tipo `Long` com `GenerationType.IDENTITY`
- **Relacionamentos JPA:** `@ManyToOne(fetch = LAZY)`, `@OneToMany(cascade = ALL, orphanRemoval = true)`
- **Jackson restritivo:** `fail-on-unknown-properties: true` globalmente. Usar `@JsonIgnoreProperties(ignoreUnknown = true)` apenas quando necessário no DTO
- **Flyway** presente mas desabilitado — DDL gerenciado externamente
- Perfis de ambiente: `local`, `dev`, `hmg`, `prd`, `test`

## Integrações e Infraestrutura

- **PostgreSQL** configurado via variáveis de ambiente (`ds.integracao.pc.url`, `ds.integracao.pc.user`, `ds.integracao.pc.password`)
- **Spring Actuator** habilitado — healthcheck em `/integracao-poupa-compra/actuator/health`
- Limites de recursos do container: 0.8 CPU / 512M RAM
