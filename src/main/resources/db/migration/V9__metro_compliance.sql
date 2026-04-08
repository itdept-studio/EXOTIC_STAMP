-- Metro: sequence unique per line; INTERNAL permission for service-to-service APIs.
-- Idempotent where possible.

ALTER TABLE stations
    ADD CONSTRAINT uq_stations_line_sequence UNIQUE (line_id, sequence);

INSERT INTO permissions (permission, description, version)
VALUES ('INTERNAL', 'Internal service APIs (e.g. metro collector increment)', 0)
ON CONFLICT (permission) DO UPDATE SET description = EXCLUDED.description;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.role = 'ADMIN'
  AND p.permission = 'INTERNAL'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);
