ALTER TABLE public.lista_compra
    ADD COLUMN nota_id BIGINT,
    ADD CONSTRAINT fk_lista_compra_nota FOREIGN KEY (nota_id)
        REFERENCES public.geral_nota (id);

CREATE UNIQUE INDEX uk_lista_compra_nota
    ON public.lista_compra (nota_id)
    WHERE nota_id IS NOT NULL;