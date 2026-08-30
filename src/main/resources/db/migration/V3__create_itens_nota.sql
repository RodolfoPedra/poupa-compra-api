CREATE TABLE public.itens_nota (
    id BIGSERIAL NOT NULL,
    descricao VARCHAR(50) NOT NULL,
    quantidade DOUBLE PRECISION NOT NULL,
    tipo_unidade VARCHAR(10),
    valor_unitario DOUBLE PRECISION NOT NULL,
    valor_total DOUBLE PRECISION NOT NULL,
    nota_id BIGINT NOT NULL,
    codigo_item BIGINT DEFAULT 0,
    CONSTRAINT pk_itens_nota PRIMARY KEY (id),
    CONSTRAINT fk_itens_nota_nota FOREIGN KEY (nota_id)
        REFERENCES public.geral_nota (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);