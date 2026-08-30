ALTER TABLE public.itens_nota
    ALTER COLUMN codigo_item DROP DEFAULT,
    ALTER COLUMN codigo_item SET NOT NULL;