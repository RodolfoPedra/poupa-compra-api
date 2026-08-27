-- DROP SCHEMA public;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

-- DROP SEQUENCE public.estabelecimento_id_seq;

CREATE SEQUENCE public.estabelecimento_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE public.geral_nota_id_seq;

CREATE SEQUENCE public.geral_nota_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;
-- DROP SEQUENCE public.itens_nota_id_seq;

CREATE SEQUENCE public.itens_nota_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;-- public.estabelecimento definition

-- Drop table

-- DROP TABLE public.estabelecimento;

CREATE TABLE public.estabelecimento (
	id bigserial NOT NULL,
	nome_estabelecimento varchar(150) NOT NULL,
	cpf_cnpj varchar(14) NOT NULL,
	endereco varchar(200) NOT NULL,
	CONSTRAINT estabelecimento_cpf_cnpj_not_null NOT NULL cpf_cnpj,
	CONSTRAINT estabelecimento_cpf_cnpj_unique UNIQUE (cpf_cnpj),
	CONSTRAINT estabelecimento_endereco_not_null NOT NULL endereco,
	CONSTRAINT estabelecimento_id_not_null NOT NULL id,
	CONSTRAINT estabelecimento_nome_estabelecimento_not_null NOT NULL nome_estabelecimento,
	CONSTRAINT pk_estabelecimento PRIMARY KEY (id)
);


-- public.geral_nota definition

-- Drop table

-- DROP TABLE public.geral_nota;

CREATE TABLE public.geral_nota (
	id bigserial NOT NULL,
	quantidade_itens int4 NOT NULL,
	valor_total float8 NOT NULL,
	usuario_id int8 NOT NULL,
	numero_cfe int4 NULL,
	uf_cfe varchar(2) NOT NULL,
	data_hora_emissao varchar(255) NULL,
	url_cfe text NOT NULL,
	chave_acesso text NOT NULL,
	estabelecimento_id int8 NOT NULL,
	CONSTRAINT geral_nota_chave_acesso_not_null NOT NULL chave_acesso,
	CONSTRAINT geral_nota_estabelecimento_id_not_null NOT NULL estabelecimento_id,
	CONSTRAINT geral_nota_id_not_null NOT NULL id,
	CONSTRAINT geral_nota_quantidade_itens_not_null NOT NULL quantidade_itens,
	CONSTRAINT geral_nota_uf_cfe_not_null NOT NULL uf_cfe,
	CONSTRAINT geral_nota_url_cfe_not_null NOT NULL url_cfe,
	CONSTRAINT geral_nota_usuario_id_not_null NOT NULL usuario_id,
	CONSTRAINT geral_nota_valor_total_not_null NOT NULL valor_total,
	CONSTRAINT pk_geral_nota PRIMARY KEY (id),
	CONSTRAINT fk_geral_nota_estab FOREIGN KEY (estabelecimento_id) REFERENCES public.estabelecimento(id) ON DELETE CASCADE ON UPDATE CASCADE
);


-- public.itens_nota definition

-- Drop table

-- DROP TABLE public.itens_nota;

CREATE TABLE public.itens_nota (
	id bigserial NOT NULL,
	descricao varchar(50) NOT NULL,
	quantidade float8 NOT NULL,
	tipo_unidade varchar(10) NULL,
	valor_unitario float8 NOT NULL,
	valor_total float8 NOT NULL,
	nota_id int8 NOT NULL,
	codigo_item int8 DEFAULT 0 NULL,
	CONSTRAINT itens_nota_descricao_not_null NOT NULL descricao,
	CONSTRAINT itens_nota_id_not_null NOT NULL id,
	CONSTRAINT itens_nota_nota_id_not_null NOT NULL nota_id,
	CONSTRAINT itens_nota_quantidade_not_null NOT NULL quantidade,
	CONSTRAINT itens_nota_valor_total_not_null NOT NULL valor_total,
	CONSTRAINT itens_nota_valor_unitario_not_null NOT NULL valor_unitario,
	CONSTRAINT pk_itens_nota PRIMARY KEY (id),
	CONSTRAINT fk_itens_nota_nota FOREIGN KEY (nota_id) REFERENCES public.geral_nota(id) ON DELETE CASCADE ON UPDATE CASCADE
);