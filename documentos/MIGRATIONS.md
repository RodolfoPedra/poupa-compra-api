# Migrations com Flyway

## Objetivo

O Flyway versiona a estrutura do banco de dados da aplicação. Cada alteração de schema deve ser registrada em um novo arquivo de migration, mantendo o histórico das mudanças.

As migrations ficam em:

```text
src/main/resources/db/migration/
```

## Migrations atuais

As migrations são executadas nesta ordem, conforme a versão do arquivo:

1. `V1__create_estabelecimento.sql`: cria a tabela `estabelecimento`.
2. `V2__create_geral_nota.sql`: cria a tabela `geral_nota` e o relacionamento com `estabelecimento`.
3. `V3__create_itens_nota.sql`: cria a tabela `itens_nota` e o relacionamento com `geral_nota`.
4. `V4__create_usuario_and_auth_tokens.sql`: cria usuários, refresh tokens e tokens de verificação.
5. `V5__relate_geral_nota_to_usuario.sql`: adiciona `usuario_id`, a foreign key para `usuario` e o índice de consulta.
6. `V6__create_password_reset_token.sql`: cria tokens temporários para recuperação de senha.
7. `V7__create_usuario_provider.sql`: cria as associações Google/Apple por `provider` e `subject`.
8. `V8__create_catalogo_produtos.sql`: cria categorias, produtos e índices GIN com `pg_trgm`.
9. `V9__seed_catalogo_produtos.sql`: carrega as categorias e produtos do catálogo inicial.
10. `V10__create_listas_compras.sql`: cria listas, itens, relacionamentos, constraints e índices.
11. `V11__allow_null_item_quantity_and_unit.sql`: torna quantidade e unidade opcionais nos itens das listas.
12. `V12__require_codigo_item_on_itens_nota.sql`: remove o valor padrão `0` e torna o código do item da nota obrigatório.

A numeração é importante porque o Flyway executa as migrations pela ordem crescente da versão.

## Quando executar

As migrations são executadas automaticamente durante a inicialização da aplicação, antes da criação do `EntityManagerFactory` do JPA.

No perfil local, o Flyway está habilitado em `application-local.yml`. Quando a aplicação é iniciada com esse perfil, ele:

1. conecta ao banco PostgreSQL configurado;
2. cria a tabela `flyway_schema_history`, caso ainda não exista;
3. identifica as migrations já aplicadas;
4. executa somente as migrations pendentes;
5. permite que o Hibernate valide o schema com `ddl-auto: validate`.

Exemplo de execução local:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Também é possível iniciar a aplicação pelo ambiente Docker local:

```bash
docker compose up -d --build
```

Nesse caso, a aplicação deve estar na mesma rede Docker do PostgreSQL e conseguir acessar o host `postgres`.

## Como criar uma nova migration

Para alterar o banco, crie um novo arquivo seguindo o padrão:

```text
V<versao>__<descricao>.sql
```

Exemplo:

```text
V4__add_data_atualizacao_to_geral_nota.sql
```

Depois, reinicie a aplicação com um perfil que tenha o Flyway habilitado. A nova migration será aplicada automaticamente se ainda não estiver registrada em `flyway_schema_history`.

## Regras importantes

- Nunca edite ou remova uma migration que já foi aplicada em algum ambiente.
- Para corrigir ou alterar uma estrutura existente, crie uma nova migration com a próxima versão.
- A migration deve ser revisada antes da execução, principalmente quando alterar ou remover dados.
- O banco deve estar acessível no momento do startup; caso contrário, a aplicação não inicia.
- Em bancos que já possuem tabelas criadas fora do Flyway, é necessário definir uma estratégia de baseline antes de habilitar as migrations.
- A migration `V8` requer permissão para executar `CREATE EXTENSION IF NOT EXISTS pg_trgm`. Em ambientes gerenciados, a extensão deve ser liberada ou provisionada previamente.

## Catálogo e listas de compras

O catálogo inicial possui 32 categorias e 435 produtos. A busca por nome de produto ou categoria usa `LOWER` e índices GIN com operadores trigram, adequados para termos no meio do texto.

As listas são privadas e pertencem a um usuário. O banco garante:

- nome de lista único por usuário, sem diferenciar maiúsculas e minúsculas;
- no máximo uma ocorrência do mesmo produto do catálogo por lista;
- quantidade e unidade opcionais e independentes;
- quantidade positiva, com até três casas decimais, quando informada;
- quantidade inteira quando quantidade e unidade `UNIDADE` forem informadas;
- remoção dos itens quando uma lista ou usuário for excluído.

Os itens das notas fiscais exigem `codigo_item`. A coluna não possui valor padrão, evitando que códigos ausentes sejam armazenados como `0`.
