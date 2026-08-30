# Usuários e autorização

## Papéis

- `USER`: acessa os próprios recursos.
- `ADMIN`: administra recursos de todos os usuários.

O primeiro administrador será criado pelo fluxo normal de cadastro e promovido manualmente no banco:

```sql
UPDATE usuario SET role = 'ADMIN' WHERE email = 'admin@exemplo.com';
```

Nenhuma senha deve ser inserida em migration ou script versionado.

## Propriedade das notas

As notas passaram a possuir relacionamento JPA com `Usuario`. O valor `nota.usuario` recebido no JSON não é usado para decidir o proprietário; a associação é obtida do usuário autenticado.

Rotas atuais:

```text
POST /api/v1/notas
GET  /api/v1/notas
```

Usuários comuns recebem apenas suas próprias notas. Administradores podem listar todas. A migration `V5__relate_geral_nota_to_usuario.sql` remove o campo legado e recria a foreign key, pois o banco local foi autorizado a ser reinicializado.

## Arquivos principais

- `model/usuario/Usuario.java`: identidade, papel, status e verificação.
- `model/nota/GeralNota.java`: relacionamento obrigatório com usuário.
- `service/nota/impl/NotaServiceImpl.java`: associação e filtro por usuário autenticado.
- `repository/NotaRepository.java`: consulta por `usuario_id`.
- `db/migration/V4__create_usuario_and_auth_tokens.sql`.
- `db/migration/V5__relate_geral_nota_to_usuario.sql`.