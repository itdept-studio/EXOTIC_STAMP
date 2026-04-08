-- ============================================================
-- V5 — Monetization module
-- Tables: advertisements, ad_impressions, affiliate_banners, affiliate_banner_clicks
-- Dependency: V2 (stations), V3 (campaigns), V4 (partners)
-- ============================================================

-- ── advertisements ───────────────────────────────────────────
-- Pre-stamp banner (3s), full-screen, sponsored stamp
-- total_impressions / total_clicks là denormalized counter —
-- increment bằng background job, không phải per-request UPDATE
CREATE TABLE advertisements (
    id                  SERIAL          PRIMARY KEY,
    partner_id          INT             NOT NULL,
    campaign_id         INT,
    title               VARCHAR(100)    NOT NULL,
    image_url           VARCHAR(255)    NOT NULL,
    target_url          VARCHAR(500),
    ad_type             VARCHAR(20)     NOT NULL,       -- 'PRE_STAMP' | 'FULL_SCREEN' | 'SPONSORED_STAMP'
    duration_seconds    INT             NOT NULL DEFAULT 3,
    start_date          TIMESTAMP       NOT NULL,
    end_date            TIMESTAMP       NOT NULL,
    budget_cap          DECIMAL(12, 2),
    total_impressions   INT             NOT NULL DEFAULT 0,
    total_clicks        INT             NOT NULL DEFAULT 0,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by          VARCHAR(255),
    updated_at          TIMESTAMP,

    CONSTRAINT fk_ads_partner_id
        FOREIGN KEY (partner_id) REFERENCES partners (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_ads_campaign_id
        FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
        ON DELETE SET NULL,

    CONSTRAINT chk_ads_dates
        CHECK (end_date > start_date),

    CONSTRAINT chk_ads_type
        CHECK (ad_type IN ('PRE_STAMP', 'FULL_SCREEN', 'SPONSORED_STAMP'))
);

-- Ad slot selection query tại scan time: lấy ad đang active cho station
CREATE INDEX idx_ads_type_active    ON advertisements (ad_type, is_active);
CREATE INDEX idx_ads_dates          ON advertisements (start_date, end_date);
CREATE INDEX idx_ads_partner_id     ON advertisements (partner_id);
CREATE INDEX idx_ads_campaign_id    ON advertisements (campaign_id) WHERE campaign_id IS NOT NULL;

-- ── ad_impressions ────────────────────────────────────────────
-- HIGH VOLUME — mỗi lần user scan stamp = 1 impression record
-- user_id nullable: anonymous impression vẫn được track
-- Partition by month trên impression_at khi data lớn sau 6 tháng launch
CREATE TABLE ad_impressions (
    id                  BIGSERIAL       PRIMARY KEY,
    advertisement_id    INT             NOT NULL,
    user_id             UUID,                           -- NULL = anonymous
    session_id          VARCHAR(100),
    station_id          INT,
    is_clicked          BOOLEAN         NOT NULL DEFAULT FALSE,
    impression_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    ip_address          VARCHAR(45),
    device_type         VARCHAR(10),                    -- 'ANDROID' | 'IOS'

    CONSTRAINT fk_ai_advertisement_id
        FOREIGN KEY (advertisement_id) REFERENCES advertisements (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_station_id
        FOREIGN KEY (station_id) REFERENCES stations (id)
        ON DELETE SET NULL,

    CONSTRAINT chk_ai_device_type
        CHECK (device_type IN ('ANDROID', 'IOS'))
);

-- CPM aggregation query — quan trọng nhất trên bảng này
CREATE INDEX idx_ai_ad_time         ON ad_impressions (advertisement_id, impression_at);
CREATE INDEX idx_ai_user_id         ON ad_impressions (user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_ai_impression_at   ON ad_impressions (impression_at);

-- ── affiliate_banners ─────────────────────────────────────────
-- Swiper banner carousel trên home screen
-- display_order quyết định thứ tự hiển thị
-- Giai đoạn đầu: FREE để thu hút partner.
-- Sau 10.000 MAU: chuyển sang FLAT_FEE hoặc REVENUE_SHARE
CREATE TABLE affiliate_banners (
    id              SERIAL          PRIMARY KEY,
    partner_id      INT             NOT NULL,
    image_url       VARCHAR(255)    NOT NULL,
    target_url      VARCHAR(500)    NOT NULL,
    title           VARCHAR(100),
    display_order   INT             NOT NULL DEFAULT 0,
    pricing_model   VARCHAR(20)     NOT NULL DEFAULT 'FREE', -- 'FREE' | 'FLAT_FEE' | 'REVENUE_SHARE'
    flat_fee_monthly    DECIMAL(10, 2),
    commission_rate     DECIMAL(5, 2),                  -- % cho REVENUE_SHARE
    start_date      TIMESTAMP       NOT NULL,
    end_date        TIMESTAMP       NOT NULL,
    total_clicks    INT             NOT NULL DEFAULT 0,  -- denormalized, batch update nightly
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by      VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255),
    updated_at      TIMESTAMP,

    CONSTRAINT fk_ab_partner_id
        FOREIGN KEY (partner_id) REFERENCES partners (id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_ab_dates
        CHECK (end_date > start_date),

    CONSTRAINT chk_ab_pricing_model
        CHECK (pricing_model IN ('FREE', 'FLAT_FEE', 'REVENUE_SHARE'))
);

-- Home screen carousel query: lấy banner active sắp xếp theo display_order
CREATE INDEX idx_ab_active_order    ON affiliate_banners (is_active, display_order);
CREATE INDEX idx_ab_dates           ON affiliate_banners (start_date, end_date);
CREATE INDEX idx_ab_partner_id      ON affiliate_banners (partner_id);

-- ── affiliate_banner_clicks ───────────────────────────────────
-- HIGH VOLUME — batch aggregate nightly vào affiliate_banners.total_clicks
-- user_id nullable: click từ non-authenticated user vẫn track được
CREATE TABLE affiliate_banner_clicks (
    id          BIGSERIAL       PRIMARY KEY,
    banner_id   INT             NOT NULL,
    user_id     UUID,                                   -- NULL = anonymous click
    clicked_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    ip_address  VARCHAR(45),
    device_type VARCHAR(10),                            -- 'ANDROID' | 'IOS'
    utm_source  VARCHAR(100),

    CONSTRAINT fk_abc_banner_id
        FOREIGN KEY (banner_id) REFERENCES affiliate_banners (id)
        ON DELETE CASCADE,

    CONSTRAINT chk_abc_device_type
        CHECK (device_type IN ('ANDROID', 'IOS'))
);

-- Aggregation query: đếm click theo banner trong khoảng thời gian
CREATE INDEX idx_abc_banner_time    ON affiliate_banner_clicks (banner_id, clicked_at);
CREATE INDEX idx_abc_user_id        ON affiliate_banner_clicks (user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_abc_clicked_at     ON affiliate_banner_clicks (clicked_at);