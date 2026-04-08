-- Server-side access token invalidation: compare JWT claim to authoritative column.
ALTER TABLE users ADD COLUMN IF NOT EXISTS token_version BIGINT NOT NULL DEFAULT 0;
