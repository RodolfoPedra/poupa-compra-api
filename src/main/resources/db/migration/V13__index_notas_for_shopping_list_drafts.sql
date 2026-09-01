CREATE INDEX idx_geral_nota_usuario_estabelecimento_id
    ON public.geral_nota (usuario_id, estabelecimento_id, id DESC);

CREATE INDEX idx_itens_nota_nota_codigo_id
    ON public.itens_nota (nota_id, codigo_item, id DESC);