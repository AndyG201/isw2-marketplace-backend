ALTER TABLE alojamiento
    ADD COLUMN habitaciones INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN camas INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN banos INTEGER NOT NULL DEFAULT 1;

ALTER TABLE alojamiento
    ADD CONSTRAINT ck_alojamiento_habitaciones CHECK (habitaciones BETWEEN 1 AND 50),
    ADD CONSTRAINT ck_alojamiento_camas CHECK (camas BETWEEN 1 AND 50),
    ADD CONSTRAINT ck_alojamiento_banos CHECK (banos BETWEEN 1 AND 50);
