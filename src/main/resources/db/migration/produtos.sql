-- notas.produtos definição

-- Drop table

-- DROP TABLE notas.produtos;

CREATE TABLE notas.produtos (
	id bigserial NOT NULL,
	nome text NOT NULL,
	valor_unitario numeric(10, 2) NOT NULL,
	quantidade int2 NOT NULL,
	tipo_unidade text NULL,
	valor_total numeric(10, 2) NOT NULL,
	nota_id int8 NOT NULL,
	CONSTRAINT produtos_pk PRIMARY KEY (id)
);


-- notas.produtos chaves estrangeiras

ALTER TABLE notas.produtos ADD CONSTRAINT produtos_nota_fk FOREIGN KEY (nota_id) REFERENCES notas.nota(id);