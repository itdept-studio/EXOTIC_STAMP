-- RBAC: metadata, optimistic locking, system roles, permission catalog seed (idempotent).

ALTER TABLE roles ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE roles ADD COLUMN IF NOT EXISTS system_role BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE permissions ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'roles'
          AND column_name = 'role'
          AND character_maximum_length IS NOT NULL
          AND character_maximum_length < 64
    ) THEN
        ALTER TABLE roles ALTER COLUMN role TYPE VARCHAR(64);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'permissions'
          AND column_name = 'permission'
          AND character_maximum_length IS NOT NULL
          AND character_maximum_length < 80
    ) THEN
        ALTER TABLE permissions ALTER COLUMN permission TYPE VARCHAR(80);
    END IF;
END $$;

UPDATE roles SET system_role = TRUE WHERE role = 'ADMIN';

INSERT INTO permissions (permission, description, version)
VALUES ('RBAC_ADMIN', 'Administrative RBAC management APIs', 0)
ON CONFLICT (permission) DO UPDATE SET description = EXCLUDED.description;

INSERT INTO permissions (permission, description, version)
VALUES
    ('READ_SALES', NULL, 0),
    ('UPLOAD_SALES', NULL, 0),
    ('READ_CUSTOMER', NULL, 0),
    ('UPLOAD_CUSTOMER', NULL, 0),
    ('READ_SERVICE', NULL, 0),
    ('UPLOAD_SERVICE', NULL, 0),
    ('READ_BOOKING', NULL, 0),
    ('UPLOAD_BOOKING', NULL, 0),
    ('READ_REALTIME', NULL, 0),
    ('MANAGE_USER', NULL, 0),
    ('ASSIGN_ROLE', NULL, 0),
    ('UPLOAD_APP_USAGE', NULL, 0)
ON CONFLICT (permission) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.role = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);
