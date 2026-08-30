# ADR: Catálogo e listas de compras

## Contexto

O sistema precisa armazenar listas privadas por usuário e oferecer um catálogo pesquisável de categorias e produtos. Também deve aceitar itens que ainda não existam no catálogo.

## Decisão

- Usar identificadores numéricos imutáveis para listas e manter o nome como atributo editável.
- Garantir unicidade case-insensitive do nome dentro de cada usuário.
- Consultar listas sempre por identificador e usuário autenticado. Recursos de outro usuário são respondidos como não encontrados.
- Manter o catálogo global e somente leitura nesta entrega.
- Salvar a descrição no item como snapshot. Para produto do catálogo, o servidor copia o nome cadastrado; para item personalizado, `produto_id` permanece nulo.
- Usar `NUMERIC(12,3)` para quantidade e restringir unidades a `UNIDADE` e `QUILOGRAMA` quando informadas. Quantidade e unidade são opcionais e independentes.
- Usar `pg_trgm` com índice GIN em nomes de produtos e categorias para buscas por substring.
- Impedir por índice parcial que o mesmo produto do catálogo apareça duas vezes na mesma lista.
- Editar a lista inteira como rascunho no frontend e persistir nome e itens somente por ação explícita de salvamento.
- Reconciliar itens em uma única transação: IDs existentes são atualizados, itens sem ID são criados e IDs omitidos são removidos.
- Usar `updatedAt` como token de concorrência, na precisão de milissegundos do contrato JSON, e bloquear a lista durante a reconciliação. Uma versão desatualizada recebe HTTP 409.
- Manter a identidade do produto imutável em itens existentes. Trocar um produto exige remover o item e adicionar outro.

## Consequências

- Renomear uma lista não altera seu identificador nem suas URLs.
- Usuários diferentes podem usar o mesmo nome de lista.
- Renomear um produto no catálogo não altera itens já adicionados.
- Itens personalizados com descrições iguais continuam permitidos.
- Itens podem ser salvos sem quantidade, sem unidade ou sem ambos; as validações de valor positivo e quantidade inteira são aplicadas somente aos dados informados.
- A migration do catálogo depende da extensão PostgreSQL `pg_trgm`.
- Testes específicos das migrations e dos índices precisam de PostgreSQL; o perfil H2 cobre contratos JPA e HTTP, mas não substitui essa validação.
- Alterações locais podem ser descartadas sem gerar escrita parcial no banco.
- A troca de lista com rascunho pendente exige salvar, descartar ou cancelar; fechar ou recarregar a aba aciona o aviso nativo do navegador.
- Excluir uma lista persistida continua sendo uma operação imediata e independente do salvamento do rascunho.

## Endpoints

- `GET /api/v1/categorias`
- `GET /api/v1/produtos`
- `POST /api/v1/listas`
- `GET /api/v1/listas`
- `GET /api/v1/listas/{listaId}`
- `PUT /api/v1/listas/{listaId}`
- `DELETE /api/v1/listas/{listaId}`