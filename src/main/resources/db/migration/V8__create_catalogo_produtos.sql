CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE public.categoria_produto (
    id BIGSERIAL NOT NULL,
    nome VARCHAR(120) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_categoria_produto PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_categoria_produto_nome
    ON public.categoria_produto (LOWER(nome));

CREATE INDEX idx_categoria_produto_nome_trgm
    ON public.categoria_produto USING GIN (LOWER(nome) gin_trgm_ops);

CREATE TABLE public.produto (
    id BIGSERIAL NOT NULL,
    categoria_id BIGINT NOT NULL,
    nome VARCHAR(160) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_produto PRIMARY KEY (id),
    CONSTRAINT fk_produto_categoria FOREIGN KEY (categoria_id)
        REFERENCES public.categoria_produto (id)
);

CREATE UNIQUE INDEX uk_produto_categoria_nome
    ON public.produto (categoria_id, LOWER(nome));

CREATE INDEX idx_produto_categoria_id
    ON public.produto (categoria_id);

CREATE INDEX idx_produto_nome_trgm
    ON public.produto USING GIN (LOWER(nome) gin_trgm_ops);