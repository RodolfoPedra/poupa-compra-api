CREATE TABLE public.usuario (
    id BIGSERIAL NOT NULL,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL,
    senha_hash VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_usuario_status CHECK (status IN ('ACTIVE', 'BLOCKED'))
);

CREATE TABLE public.refresh_token (
    id BIGSERIAL NOT NULL,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) ON DELETE CASCADE
);

CREATE TABLE public.email_verification_token (
    id BIGSERIAL NOT NULL,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_email_verification_token PRIMARY KEY (id),
    CONSTRAINT uk_email_verification_usuario UNIQUE (usuario_id),
    CONSTRAINT uk_email_verification_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuario (id) ON DELETE CASCADE
);