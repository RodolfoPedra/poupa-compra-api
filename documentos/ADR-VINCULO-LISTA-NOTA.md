# ADR: vínculo entre lista de compras e nota fiscal

## Contexto

Após concluir uma compra, o usuário pode associar à lista utilizada a NFC-e correspondente. O cadastro direto de notas deve continuar independente de listas, e uma nota não pode comprovar mais de uma lista.

## Decisão

- `lista_compra.nota_id` é opcional e referencia `geral_nota.id`.
- Uma lista possui no máximo uma nota e uma nota pode estar em no máximo uma lista.
- Um índice único parcial protege a exclusividade de notas vinculadas sem impedir múltiplas listas sem nota.
- O serviço valida que lista e nota pertencem ao usuário autenticado, sem acesso administrativo especial nesse fluxo.
- Troca e desvínculo exigem a versão atual da lista por meio de `updatedAt`.
- O cadastro de uma nova nota pela lista e a criação do vínculo ocorrem na mesma transação.
- O endpoint geral `POST /api/v1/notas` permanece sem vínculo automático.

## Consequências

- Notas vinculadas a outras listas não aparecem entre as opções disponíveis.
- Ao trocar ou remover o vínculo, a nota anterior volta a ficar disponível.
- Rascunhos de lista precisam ser salvos antes de receber uma NFC-e.
- Uma falha no vínculo reverte também a nova nota criada pelo fluxo da lista.