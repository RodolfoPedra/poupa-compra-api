# OAuth Google e Apple

## Estado atual

A API já possui troca de authorization code, validação de `id_token` por JWKS e associação por `provider + subject`. As credenciais continuam sendo obrigatórias por ambiente.

## Fluxo planejado

React/mobile autenticam o usuário com Authorization Code + PKCE. O cliente envia o code para a API, que troca o code com o provedor, valida o `id_token`, identifica a conta por `provedor + subject` e emite os JWTs próprios da aplicação.

Endpoints planejados:

```text
POST /api/v1/auth/oauth/google
POST /api/v1/auth/oauth/apple
```

O corpo dos dois endpoints é:

```json
{
	"authorizationCode": "code-retornado-pelo-provedor",
	"redirectUri": "http://localhost:3000/oauth/callback"
}
```

Se o e-mail já existir em uma conta local, o usuário deverá autenticar explicitamente a conta existente antes da vinculação. Não haverá vinculação automática apenas pelo e-mail.

## Configuração futura

As credenciais serão fornecidas por ambiente, nunca pelo código:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
APPLE_CLIENT_ID
APPLE_CLIENT_SECRET
```

Para Apple, `APPLE_CLIENT_SECRET` deve ser um client secret JWT assinado conforme as credenciais do Apple Developer (`team_id`, `key_id` e a chave privada). A API não aceita segredos enviados pelo aplicativo.

Os testes usarão mocks dos provedores e não farão chamadas externas.