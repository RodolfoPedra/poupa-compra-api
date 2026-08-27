# Configuração de ambientes

## API local

```text
CORS_ALLOWED_ORIGINS=http://localhost:3000
JWT_ACCESS_TOKEN_TTL=PT15M
JWT_REFRESH_TOKEN_TTL=P30D
```

O frontend Next.js fica em `poupa-compra-web` e o backend local usa a porta `8182`.

## Produção

Defina `CORS_ALLOWED_ORIGINS` com a origem real do painel publicado. `localhost` no navegador representa a máquina do usuário e não deve ser usado como substituto do domínio do servidor.

As chaves JWT RSA devem ser injetadas por secret manager ou mecanismo equivalente:

```text
JWT_PRIVATE_KEY_BASE64
JWT_PUBLIC_KEY_BASE64
```

Não adicione arquivos `.env` com segredos ao repositório.