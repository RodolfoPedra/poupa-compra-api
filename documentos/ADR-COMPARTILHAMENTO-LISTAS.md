# ADR: compartilhamento de listas de compras

## Contexto

Uma lista de compras precisa aceitar a colaboração de outro usuário sem permitir que alterações concorrentes sobrescrevam itens ou operações exclusivas do proprietário.

## Decisão

- Cada lista pode possuir um único convidado, com convite nos estados `PENDENTE` ou `ACEITO`.
- O convidado recebe acesso somente após aceitar o convite.
- Convite pendente ou aceito ativa o modo colaborativo e bloqueia o `PUT` integral da lista com HTTP 409.
- No modo colaborativo, nome e itens são persistidos por endpoints granulares. A versão da lista protege o nome, e a versão de cada item protege alterações concorrentes no mesmo item.
- Owner e convidado podem marcar itens e adicionar produtos do catálogo ou itens personalizados.
- Somente o owner pode renomear a lista, editar quantidade/unidade, remover itens, excluir ou compartilhar a lista e alterar o vínculo com NFC-e.
- O convidado pode consultar o resumo da NFC-e vinculada, sem executar operações sobre ela.
- Recursos de listas sem participação do usuário são respondidos como não encontrados para reduzir exposição por IDOR.
- Alterações confirmadas são notificadas após o commit em `/topic/listas/{listaId}`. Clientes usam REST para mutações e não podem enviar mensagens STOMP.
- O frame STOMP `CONNECT` exige o JWT no header `Authorization`, e cada `SUBSCRIBE` é autorizado para o owner ou convidado aceito.

## Consequências

- Listas individuais preservam o rascunho local e o salvamento integral já existente.
- Recusar, cancelar ou encerrar a participação remove o compartilhamento e devolve a lista ao modo individual.
- A migration garante no banco o limite de um compartilhamento por lista e indexa convites por convidado e status.
- O simple broker do Spring atende uma única instância da API. Múltiplas instâncias exigem broker compartilhado e configuração de relay.
- O cliente recarrega o estado confirmado pela API ao receber um evento, evitando tratar a notificação como fonte de verdade.

## Endpoints

- `POST /api/v1/listas/{listaId}/convite`
- `DELETE /api/v1/listas/{listaId}/convite`
- `GET /api/v1/listas/convites`
- `POST /api/v1/listas/convites/{conviteId}/aceite`
- `POST /api/v1/listas/convites/{conviteId}/recusa`
- `GET /api/v1/listas/compartilhadas`
- `GET /api/v1/listas/compartilhadas/{listaId}`
- `DELETE /api/v1/listas/compartilhadas/{listaId}/participacao`
- `PATCH /api/v1/listas/{listaId}/nome`
- `POST /api/v1/listas/{listaId}/itens`
- `PATCH /api/v1/listas/{listaId}/itens/{itemId}/selecao`
- `PATCH /api/v1/listas/{listaId}/itens/{itemId}`
- `DELETE /api/v1/listas/{listaId}/itens/{itemId}`

## Canal em tempo real

- Handshake WebSocket: `/integracao-poupa-compra/ws`
- Tópico STOMP: `/topic/listas/{listaId}`