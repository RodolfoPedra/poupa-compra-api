ALTER TABLE public.geral_nota
    ADD COLUMN usuario_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_geral_nota_usuario FOREIGN KEY (usuario_id)
        REFERENCES public.usuario (id)
        ON DELETE CASCADE;

CREATE INDEX idx_geral_nota_usuario_id ON public.geral_nota (usuario_id);