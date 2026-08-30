CREATE TABLE public.geral_nota (
    id BIGSERIAL NOT NULL,
    quantidade_itens INTEGER NOT NULL,
    valor_total DOUBLE PRECISION NOT NULL,
    numero_cfe INTEGER,
    uf_cfe VARCHAR(2) NOT NULL,
    data_hora_emissao VARCHAR(255),
    url_cfe TEXT NOT NULL,
    chave_acesso TEXT NOT NULL,
    estabelecimento_id BIGINT NOT NULL,
    CONSTRAINT pk_geral_nota PRIMARY KEY (id),
    CONSTRAINT fk_geral_nota_estab FOREIGN KEY (estabelecimento_id)
        REFERENCES public.estabelecimento (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);