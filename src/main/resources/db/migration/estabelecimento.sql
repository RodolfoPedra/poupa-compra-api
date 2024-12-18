-- notas.estabelecimento definição

-- Drop table

-- DROP TABLE notas.estabelecimento;

CREATE TABLE notas.estabelecimento (
	id bigserial NOT NULL,
	nome text NULL,
	cnpj text NOT NULL,
	endereco text NULL,
	nota_id int8 NOT NULL,
	CONSTRAINT estab_cnpj_unique UNIQUE (cnpj),
	CONSTRAINT estabelecimento_pk PRIMARY KEY (id)
);


-- notas.estabelecimento chaves estrangeiras

ALTER TABLE notas.estabelecimento ADD CONSTRAINT estabelecimento_nota_fk FOREIGN KEY (nota_id) REFERENCES notas.nota(id);