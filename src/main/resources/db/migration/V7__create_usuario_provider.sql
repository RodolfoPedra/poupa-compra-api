CREATE TABLE public.usuario_provider (
    id BIGSERIAL NOT NULL,
    usuario_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    provider_email VARCHAR(320),
    CONSTRAINT pk_usuario_provider PRIMARY KEY (id),
    CONSTRAINT uk_usuario_provider_subject UNIQUE (provider, subject),
    CONSTRAINT fk_usuario_provider_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) ON DELETE CASCADE
);

CREATE INDEX idx_usuario_provider_usuario_id ON public.usuario_provider (usuario_id);