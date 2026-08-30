CREATE TABLE public.estabelecimento (
    id BIGSERIAL NOT NULL,
    nome_estabelecimento VARCHAR(150) NOT NULL,
    cpf_cnpj VARCHAR(14) NOT NULL,
    endereco VARCHAR(200) NOT NULL,
    CONSTRAINT pk_estabelecimento PRIMARY KEY (id),
    CONSTRAINT estabelecimento_cpf_cnpj_unique UNIQUE (cpf_cnpj)
);