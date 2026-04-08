-- ============================================================
-- V2 — Metro Network module
-- Tables: lines, stations
-- Shared Kernel — reference data, seeded at startup by MetroLineSeeder
-- Dependency: none
-- ============================================================

-- ── lines ────────────────────────────────────────────────────
CREATE TABLE lines (
                       id              SERIAL          PRIMARY KEY,
                       code            VARCHAR(10)     NOT NULL,
                       name            VARCHAR(100)    NOT NULL,
                       color           VARCHAR(7),
                       total_stations  INT             NOT NULL DEFAULT 0,
                       is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
                       created_by      VARCHAR(255),
                       created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                       updated_by      VARCHAR(255),
                       updated_at      TIMESTAMP,

                       CONSTRAINT uq_lines_code UNIQUE (code)
);

CREATE INDEX idx_lines_is_active ON lines (is_active);

-- ── stations ─────────────────────────────────────────────────
-- HOT PATH: nfc_tag_id và qr_code_token là primary scan lookup keys
-- Cả hai phải có dedicated index riêng
-- StationCacheRepository cache theo 2 key này:
--   station:nfc:{nfc_tag_id}   → StationDetail JSON
--   station:qr:{qr_code_token} → StationDetail JSON
CREATE TABLE stations (
                          id              SERIAL          PRIMARY KEY,
                          line_id         INT             NOT NULL,
                          code            VARCHAR(20)     NOT NULL,
                          name            VARCHAR(100)    NOT NULL,
                          sequence        INT             NOT NULL,
                          description     VARCHAR(500),
                          historical_info TEXT,
                          image_url       VARCHAR(255),
                          latitude        DECIMAL(9, 6),
                          longitude       DECIMAL(9, 6),
                          nfc_tag_id      VARCHAR(100),
                          qr_code_token   VARCHAR(100),
                          collector_count INT             NOT NULL DEFAULT 0,
                          is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
                          created_by      VARCHAR(255),
                          created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                          updated_by      VARCHAR(255),
                          updated_at      TIMESTAMP,

                          CONSTRAINT fk_stations_line_id
                              FOREIGN KEY (line_id) REFERENCES lines (id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT uq_stations_code         UNIQUE (code),
                          CONSTRAINT uq_stations_nfc_tag_id   UNIQUE (nfc_tag_id),
                          CONSTRAINT uq_stations_qr_token     UNIQUE (qr_code_token)
);

-- HOT PATH indexes — scan lookup
CREATE INDEX idx_stations_nfc_tag_id
    ON stations (nfc_tag_id)
    WHERE nfc_tag_id IS NOT NULL;

CREATE INDEX idx_stations_qr_code_token
    ON stations (qr_code_token)
    WHERE qr_code_token IS NOT NULL;

-- Stamp Book query: tất cả station trên 1 tuyến theo thứ tự
CREATE INDEX idx_stations_line_seq  ON stations (line_id, sequence);
CREATE INDEX idx_stations_is_active ON stations (is_active);