# Configuração de ambientes

## API local

```text
CORS_ALLOWED_ORIGINS=http://localhost:3000
JWT_ACCESS_TOKEN_TTL=PT15M
JWT_REFRESH_TOKEN_TTL=P30D
GOOGLE_CLIENT_ID=seu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=segredo-fornecido-apenas-ao-backend
GOOGLE_REDIRECT_URI=postmessage
API_BASE_URL=http://localhost:8182/integracao-poupa-compra
NEXT_PUBLIC_WS_URL=ws://localhost:8182/integracao-poupa-compra/ws
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

O frontend usa `NEXT_PUBLIC_WS_URL` para conectar ao canal STOMP. Em produção, use `wss://` quando o painel for servido por HTTPS. Sem essa variável, o navegador usa o hostname atual com a porta `8182`.

O proxy reverso deve encaminhar `/integracao-poupa-compra/ws` para a API preservando os headers `Upgrade` e `Connection`. A origem do painel também precisa estar em `CORS_ALLOWED_ORIGINS` para que o handshake seja aceito.

O broker simples configurado na aplicação mantém assinaturas apenas na instância que recebeu a conexão. Uma implantação com múltiplas instâncias requer um broker compartilhado com STOMP broker relay.