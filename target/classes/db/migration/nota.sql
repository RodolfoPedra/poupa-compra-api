-- notas.nota definição

-- Drop table

-- DROP TABLE notas.nota;

CREATE TABLE notas.nota (
	id bigserial NOT NULL,
	uf text NOT NULL,
	chave_acesso text NOT NULL,
	numero_nota int4 NOT NULL,
	nome_consumidor text NULL,
	cpf_cnpj_consumidor text NULL,
	valor_total numeric(10, 2) NOT NULL,
	total_itens int2 NULL,
	CONSTRAINT nota_pk PRIMARY KEY (id)
);