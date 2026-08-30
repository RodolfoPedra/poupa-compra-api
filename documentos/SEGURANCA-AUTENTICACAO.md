# Segurança e autenticação

## Escopo desta entrega

Foi criada a primeira fatia de segurança da API:

- autenticação stateless com Bearer JWT;
- access token com TTL padrão de 15 minutos;
- refresh token rotativo com TTL padrão de 30 dias;
- senha armazenada com BCrypt;
- cadastro local com e-mail único;
- verificação de e-mail obrigatória para contas locais;
- recuperação de senha e reenvio de verificação com tokens temporários;
- tokens persistidos somente como hash SHA-256;
- CORS configurável, com `http://localhost:3000` como padrão;
- respostas de autenticação para rotas protegidas sem redirecionamento HTML.
- documentação OpenAPI dos endpoints de autenticação, OAuth e notas com esquema Bearer JWT.

## Arquivos principais

- `config/SecurityConfig.java`: cadeia stateless, CORS e autorização.
- `config/JwtAuthenticationFilter.java`: leitura do header `Authorization: Bearer`.
- `service/usuario/JwtService.java`: assinatura e validação dos JWTs RSA.
- `service/usuario/AuthService.java`: cadastro, login, verificação, refresh e logout.
- `service/usuario/TokenGenerator.java`: tokens aleatórios e seus hashes.
- `controller/AuthController.java`: endpoints versionados de autenticação.
- `model/usuario/`: entidades de usuário, refresh e verificação.
- `repository/`: persistência dos recursos de identidade.

## Endpoints disponíveis

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/verify-email
POST /api/v1/auth/resend-verification
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
GET  /api/v1/auth/me
```

O token de verificação é exibido no log local após o cadastro. Em produção, esse comportamento deve ser substituído por um provedor de e-mail.

## Swagger/OpenAPI

Novos endpoints devem receber `@Operation` e `@ApiResponse` no controller. Rotas protegidas também devem declarar `@SecurityRequirement(name = "bearerAuth")`; o esquema Bearer JWT é registrado em `IntegracaoApplication`. Os caminhos do Swagger UI, recursos auxiliares e documentos OpenAPI são públicos na `SecurityFilterChain`, para que a documentação seja acessível sem token.

Com a aplicação em execução na porta `8182`, a documentação pode ser consultada em:

```text
/swagger-ui/index.html
/v3/api-docs
```

## Chaves JWT

A API aceita chaves RSA em Base64 por ambiente:

```text
JWT_PRIVATE_KEY_BASE64
JWT_PUBLIC_KEY_BASE64
```

Na ausência das chaves, o ambiente de desenvolvimento/teste gera um par efêmero na inicialização. Esse fallback não deve ser usado em produção, pois invalida tokens após cada reinício.

Exemplo para gerar as chaves localmente:

```bash
openssl genrsa 2048 > jwt-private.pem
openssl rsa -in jwt-private.pem -pubout > jwt-public.pem
base64 -w 0 jwt-private.pem
base64 -w 0 jwt-public.pem
```

As chaves não devem ser commitadas.

## Decisões pendentes

- recuperação de senha e reenvio de verificação;
- associação com Google e Apple;
- gerenciamento administrativo de usuários;
- rotação de chaves com `kid`;
- armazenamento de credenciais em secret manager no deploy.