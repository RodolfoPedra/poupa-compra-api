# Changelog

## 2026-08-30

- Quantidade e unidade dos itens de listas de compras passaram a ser opcionais e independentes no frontend, API e banco de dados.
- Adicionada a migration `V11` para remover a obrigatoriedade dessas colunas sem alterar migrations já aplicadas.

## 2026-08-29

- Criado catálogo global com 32 categorias e 435 produtos carregados por migrations Flyway.
- Adicionada busca paginada de produtos e categorias com `pg_trgm` e índices GIN.
- Criadas listas de compras privadas, com nomes únicos por usuário e itens persistidos.
- Adicionados itens de catálogo e personalizados com quantidade por unidade ou quilograma.
- Adicionados check persistente, edição de quantidade/unidade e exclusão de itens.
- Adicionados endpoints autenticados com proteção contra acesso a listas de outros usuários.
- Implementada a tela responsiva de listas de compras integrada à API.
- Adicionados testes de integração para fluxo, validações, catálogo e isolamento entre usuários.
- Corrigida a busca inicial do catálogo no PostgreSQL, removendo parâmetros nulos que eram inferidos como `bytea` pelo driver.
- Corrigido o botão de criação de listas que indicava carregamento quando o nome ainda não havia sido informado.
- Adicionado timeout às chamadas autenticadas do frontend para evitar espera indefinida quando a API não responder.
- Alterada a edição de listas para rascunho local com persistência somente no botão `Salvar lista`.
- Substituídos os endpoints granulares de nome e itens por criação e atualização agregadas em uma única transação.
- Adicionada detecção de conflito por `updatedAt`, com bloqueio da lista durante a reconciliação e resposta HTTP 409 para versões desatualizadas.
- Adicionados aviso de alterações não salvas e diálogo para salvar, descartar ou cancelar ao trocar de lista.

## 2026-08-26

- Adicionada base de usuários com e-mail único, papéis `USER` e `ADMIN` e status de conta.
- Adicionados cadastro, login, verificação de e-mail, refresh e logout com JWT Bearer.
- Adicionados reenvio de verificação e recuperação de senha com tokens temporários.
- Adicionado armazenamento por hash de tokens de refresh e verificação.
- Adicionada configuração de CORS e chaves RSA por ambiente.
- Notas associadas ao usuário autenticado e endpoints migrados para `/api/v1/notas`.
- Criada documentação inicial de segurança, usuários, OAuth e ambientes.
- Adicionados clients OAuth Google/Apple, validação OIDC por JWKS e vinculação por provider+subject.
- Documentados os endpoints de autenticação, OAuth e notas no Swagger/OpenAPI com Bearer JWT.
- Liberados os caminhos do Swagger UI e OpenAPI na cadeia de segurança sem autenticação.
- Preparado o login Google no frontend com Google Identity Services e fluxo popup server-side.
- Corrigida a desserialização da resposta Google que contém `access_token` além de `id_token`.
- Corrigida a desserialização da resposta Google que também pode conter `refresh_token`.