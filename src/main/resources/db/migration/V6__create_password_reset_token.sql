CREATE TABLE public.password_reset_token (
    id BIGSERIAL NOT NULL,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_password_reset_token PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_usuario UNIQUE (usuario_id),
    CONSTRAINT uk_password_reset_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) ON DELETE CASCADE
);