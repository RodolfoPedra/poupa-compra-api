ALTER TABLE public.item_lista_compra
    ALTER COLUMN quantidade DROP NOT NULL,
    ALTER COLUMN unidade DROP NOT NULL;