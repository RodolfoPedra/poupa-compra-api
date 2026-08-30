CREATE TABLE public.lista_compra (
    id BIGSERIAL NOT NULL,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_lista_compra PRIMARY KEY (id),
    CONSTRAINT fk_lista_compra_usuario FOREIGN KEY (usuario_id)
        REFERENCES public.usuario (id) ON DELETE CASCADE,
    CONSTRAINT ck_lista_compra_nome CHECK (BTRIM(nome) <> '')
);

CREATE UNIQUE INDEX uk_lista_compra_usuario_nome
    ON public.lista_compra (usuario_id, LOWER(nome));

CREATE INDEX idx_lista_compra_usuario_updated_at
    ON public.lista_compra (usuario_id, updated_at DESC);

CREATE TABLE public.item_lista_compra (
    id BIGSERIAL NOT NULL,
    lista_compra_id BIGINT NOT NULL,
    produto_id BIGINT,
    descricao VARCHAR(160) NOT NULL,
    quantidade NUMERIC(12, 3) NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    selecionado BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_item_lista_compra PRIMARY KEY (id),
    CONSTRAINT fk_item_lista_compra_lista FOREIGN KEY (lista_compra_id)
        REFERENCES public.lista_compra (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_lista_compra_produto FOREIGN KEY (produto_id)
        REFERENCES public.produto (id),
    CONSTRAINT ck_item_lista_descricao CHECK (BTRIM(descricao) <> ''),
    CONSTRAINT ck_item_lista_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_item_lista_unidade CHECK (unidade IN ('UNIDADE', 'QUILOGRAMA')),
    CONSTRAINT ck_item_lista_quantidade_unidade CHECK (
        unidade <> 'UNIDADE' OR quantidade = TRUNC(quantidade)
    ),
    CONSTRAINT ck_item_lista_ordem CHECK (ordem >= 0)
);

CREATE INDEX idx_item_lista_compra_lista_ordem
    ON public.item_lista_compra (lista_compra_id, ordem, id);

CREATE UNIQUE INDEX uk_item_lista_compra_produto
    ON public.item_lista_compra (lista_compra_id, produto_id)
    WHERE produto_id IS NOT NULL;