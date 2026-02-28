-- ============================================
-- Tabela: estabelecimento
-- ============================================
CREATE TABLE estabelecimento (
    id                  BIGSERIAL       NOT NULL,
    nome_estabelecimento VARCHAR(150)   NOT NULL,
    cpf_cnpj            VARCHAR(14)     NOT NULL,
    endereco            VARCHAR(200)    NOT NULL,

    CONSTRAINT pk_estabelecimento PRIMARY KEY (id)
);

-- ============================================
-- Tabela: geral_nota
-- ============================================
CREATE TABLE geral_nota (
    id                  BIGSERIAL       NOT NULL,
    quantidade_itens    INTEGER         NOT NULL,
    valor_total         FLOAT           NOT NULL,
    usuario_id          BIGINT          NOT NULL,
    numero_cfe          INTEGER,
    uf_cfe              VARCHAR(2)      NOT NULL,
    data_hora_emissao   VARCHAR(255),
    url_cfe             TEXT            NOT NULL,
    chave_acesso        TEXT            NOT NULL,
    estabelecimento_id  BIGINT          NOT NULL,

    CONSTRAINT pk_geral_nota            PRIMARY KEY (id),
    CONSTRAINT fk_geral_nota_estab      FOREIGN KEY (estabelecimento_id)
        REFERENCES estabelecimento(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ============================================
-- Tabela: itens_nota
-- ============================================
CREATE TABLE itens_nota (
    id              BIGSERIAL       NOT NULL,
    descricao       VARCHAR(50)     NOT NULL,
    quantidade      FLOAT           NOT NULL,
    tipo_unidade    VARCHAR(3),
    valor_unitario  FLOAT           NOT NULL,
    valor_total     FLOAT           NOT NULL,
    nota_id         BIGINT          NOT NULL,

    CONSTRAINT pk_itens_nota        PRIMARY KEY (id),
    CONSTRAINT fk_itens_nota_nota   FOREIGN KEY (nota_id)
        REFERENCES geral_nota(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ============================================
-- Índices recomendados
-- ============================================
CREATE INDEX idx_geral_nota_estabelecimento ON geral_nota(estabelecimento_id);
CREATE INDEX idx_geral_nota_usuario         ON geral_nota(usuario_id);
CREATE INDEX idx_itens_nota_nota            ON itens_nota(nota_id);