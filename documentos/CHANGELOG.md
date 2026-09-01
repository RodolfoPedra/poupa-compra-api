# Changelog

## 2026-09-01

- Simplificados os logs do backend para registrar uma linha por requisição, com identificador de correlação, método, rota, status e duração.
- Adicionados logs estruturados para fluxos de autenticação, listas de compras, compartilhamento e eventos WebSocket, usando somente identificadores técnicos.
- Centralizado o registro de erros de negócio, validação e exceções inesperadas, com stack trace somente para falhas internas.
- Removida a exposição de tokens de verificação e recuperação de senha nos logs.
- Desabilitados logs SQL e Hibernate verbosos nos perfis local e de teste.

## 2026-08-31

- Adicionado compartilhamento de lista de compras com um convidado e aceite obrigatório.
- Adicionados endpoints granulares para persistência concorrente de nome, itens e seleção no modo colaborativo.
- Bloqueado o salvamento integral de listas compartilhadas para evitar perda de atualizações.
- Adicionadas autenticação JWT, autorização por lista e notificações pós-commit via WebSocket/STOMP.
- Restringidas ao proprietário as operações de exclusão, compartilhamento e vínculo com NFC-e.
- Adicionadas telas de convites e listas recebidas, atualização em tempo real e estado de conexão no frontend.
- Adicionada a migration `V15` com unicidade por lista e índice para consulta de convites.
- Adicionados testes de integração para concorrência, permissões, ciclo do convite e segurança STOMP.

## 2026-08-30

- Quantidade e unidade dos itens de listas de compras passaram a ser opcionais e independentes no frontend, API e banco de dados.
- Adicionada a migration `V11` para remover a obrigatoriedade dessas colunas sem alterar migrations já aplicadas.
- Tornado obrigatório o `codigo_item` dos itens de notas fiscais na API e no banco de dados.
- Adicionada a migration `V12`, que remove o valor padrão `0` de `codigo_item` e impede valores nulos.
- Corrigido o build local para não depender de um diretório `.mvn` inexistente.
- Adicionada a geração de rascunhos de listas de compras a partir de uma a cinco NFC-e do mesmo estabelecimento.
- Adicionadas seleção paginada de notas próprias, deduplicação por `codigo_item` e preferência pela descrição da nota mais recente.
- Adicionada a migration `V13` com índices para consulta de notas por usuário/estabelecimento e leitura ordenada dos itens.
- Integrada à tela de listas a seleção de estabelecimento e notas, mantendo o rascunho local até o salvamento explícito.
- Adicionado vínculo opcional e exclusivo entre lista de compras e NFC-e, com suporte a troca e desvínculo.
- Adicionadas seleção paginada de notas disponíveis e criação transacional de NFC-e vinculada à lista.
- Mantido o cadastro direto de NFC-e sem associação automática a listas de compras.
- Adicionada a migration `V14` com chave estrangeira e índice único parcial para impedir o reuso da mesma nota.

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