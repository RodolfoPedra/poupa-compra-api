# Changelog

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