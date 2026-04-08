-- ============================================================
-- V3 — Collection module
-- Tables: campaigns, campaign_stations, stamp_designs, user_stamps
-- Dependency: V2 (stations)
-- ============================================================

-- ── campaigns ────────────────────────────────────────────────
-- CampaignID = NULL trên child tables = permanent (không theo mùa)
-- partner_id để NULL trước, FK sang partners sẽ được thêm ở V4
-- sau khi bảng partners được tạo
CREATE TABLE campaigns (
                           id          SERIAL          PRIMARY KEY,
                           partner_id  INT,
                           code        VARCHAR(30)     NOT NULL,
                           name        VARCHAR(100)    NOT NULL,
                           description VARCHAR(500),
                           banner_url  VARCHAR(255),
                           start_date  TIMESTAMP       NOT NULL,
                           end_date    TIMESTAMP       NOT NULL,
                           is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
                           created_by  VARCHAR(255),
                           created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
                           updated_by  VARCHAR(255),
                           updated_at  TIMESTAMP,

                           CONSTRAINT uq_campaigns_code   UNIQUE (code),
                           CONSTRAINT chk_campaigns_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_campaigns_active_dates ON campaigns (is_active, start_date, end_date);
CREATE INDEX idx_campaigns_partner_id   ON campaigns (partner_id) WHERE partner_id IS NOT NULL;

-- ── campaign_stations ────────────────────────────────────────
CREATE TABLE campaign_stations (
                                   id          SERIAL  PRIMARY KEY,
                                   campaign_id INT     NOT NULL,
                                   station_id  INT     NOT NULL,

                                   CONSTRAINT fk_cs_campaign_id
                                       FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_cs_station_id
                                       FOREIGN KEY (station_id) REFERENCES stations (id)
                                           ON DELETE RESTRICT,

                                   CONSTRAINT uq_campaign_stations UNIQUE (campaign_id, station_id)
);

CREATE INDEX idx_cs_campaign_id ON campaign_stations (campaign_id);
CREATE INDEX idx_cs_station_id  ON campaign_stations (station_id);

-- ── stamp_designs ─────────────────────────────────────────────
-- Tách artwork khỏi user_stamps để update design mà không đụng collection records
CREATE TABLE stamp_designs (
                               id              SERIAL          PRIMARY KEY,
                               station_id      INT,
                               campaign_id     INT,
                               name            VARCHAR(100)    NOT NULL,
                               artwork_url     VARCHAR(255)    NOT NULL,
                               animation_url   VARCHAR(255),
                               sound_url       VARCHAR(255),
                               is_limited      BOOLEAN         NOT NULL DEFAULT FALSE,
                               is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
                               created_by      VARCHAR(255),
                               created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                               updated_by      VARCHAR(255),
                               updated_at      TIMESTAMP,

                               CONSTRAINT fk_sd_station_id
                                   FOREIGN KEY (station_id) REFERENCES stations (id)
                                       ON DELETE SET NULL,

                               CONSTRAINT fk_sd_campaign_id
                                   FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
                                       ON DELETE SET NULL
);

CREATE INDEX idx_sd_station_id  ON stamp_designs (station_id)  WHERE station_id IS NOT NULL;
CREATE INDEX idx_sd_campaign_id ON stamp_designs (campaign_id) WHERE campaign_id IS NOT NULL;
CREATE INDEX idx_sd_is_active   ON stamp_designs (is_active);

-- ── user_stamps ───────────────────────────────────────────────
-- Bảng quan trọng nhất hệ thống
-- BIGSERIAL thay UUID vì high-volume insert
-- user_id là plain UUID không FK — cross-module boundary rule
CREATE TABLE user_stamps (
                             id              BIGSERIAL       PRIMARY KEY,
                             user_id         UUID            NOT NULL,
                             station_id      INT             NOT NULL,
                             stamp_design_id INT             NOT NULL,
                             campaign_id     INT,
                             collected_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
                             latitude        DECIMAL(9, 6),
                             longitude       DECIMAL(9, 6),
                             gps_verified    BOOLEAN         NOT NULL DEFAULT FALSE,
                             scan_method     VARCHAR(5)      NOT NULL,
                             device_id       VARCHAR(255),

                             CONSTRAINT fk_us_station_id
                                 FOREIGN KEY (station_id) REFERENCES stations (id)
                                     ON DELETE RESTRICT,

                             CONSTRAINT fk_us_stamp_design_id
                                 FOREIGN KEY (stamp_design_id) REFERENCES stamp_designs (id)
                                     ON DELETE RESTRICT,

                             CONSTRAINT fk_us_campaign_id
                                 FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
                                     ON DELETE RESTRICT,

    -- ANTI-CHEAT: một user chỉ collect 1 stamp / station / campaign
    -- NULLS NOT DISTINCT: NULL campaign_id (permanent stamp) cũng bị unique check
    -- Yêu cầu PostgreSQL 15+. Nếu dùng PG < 15 thì báo để dùng partial index thay thế
                             CONSTRAINT uq_user_stamps_collect
                                 UNIQUE NULLS NOT DISTINCT (user_id, station_id, campaign_id)
);

CREATE INDEX idx_us_user_id      ON user_stamps (user_id);
CREATE INDEX idx_us_user_time    ON user_stamps (user_id, collected_at DESC);
CREATE INDEX idx_us_station_id   ON user_stamps (station_id);
CREATE INDEX idx_us_campaign_id  ON user_stamps (campaign_id) WHERE campaign_id IS NOT NULL;