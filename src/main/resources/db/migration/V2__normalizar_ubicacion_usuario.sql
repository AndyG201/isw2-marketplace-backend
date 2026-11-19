CREATE TABLE pais (
    id_pais UUID PRIMARY KEY,
    codigo_iso2 VARCHAR(2) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE ciudad (
    id_ciudad UUID PRIMARY KEY,
    id_pais UUID NOT NULL REFERENCES pais(id_pais),
    nombre VARCHAR(150) NOT NULL,
    CONSTRAINT uq_ciudad_pais_nombre UNIQUE (id_pais, nombre)
);

ALTER TABLE usuario ADD COLUMN id_ciudad UUID;
ALTER TABLE usuario
    ADD CONSTRAINT fk_usuario_ciudad FOREIGN KEY (id_ciudad) REFERENCES ciudad(id_ciudad);

CREATE INDEX idx_ciudad_pais ON ciudad(id_pais);
