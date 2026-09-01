CREATE TABLE public.lista_compra_compartilhamento (
    id BIGSERIAL NOT NULL,
    lista_compra_id BIGINT NOT NULL,
    convidado_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_lista_compra_compartilhamento PRIMARY KEY (id),
    CONSTRAINT fk_lista_compartilhamento_lista FOREIGN KEY (lista_compra_id)
        REFERENCES public.lista_compra (id) ON DELETE CASCADE,
    CONSTRAINT fk_lista_compartilhamento_convidado FOREIGN KEY (convidado_id)
        REFERENCES public.usuario (id) ON DELETE CASCADE,
    CONSTRAINT uk_lista_compartilhamento_lista UNIQUE (lista_compra_id),
    CONSTRAINT ck_lista_compartilhamento_status CHECK (status IN ('PENDENTE', 'ACEITO'))
);

CREATE INDEX idx_lista_compartilhamento_convidado_status
    ON public.lista_compra_compartilhamento (convidado_id, status);