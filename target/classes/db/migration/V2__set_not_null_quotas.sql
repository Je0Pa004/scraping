-- After backfill, enforce NOT NULL and defaults for quotas
-- Ensure no NULLs remain
UPDATE abonnement SET quota_total = 0 WHERE quota_total IS NULL;
UPDATE abonnement SET quota_utilise = 0 WHERE quota_utilise IS NULL;

-- Set NOT NULL and defaults
ALTER TABLE abonnement ALTER COLUMN quota_total SET NOT NULL;
ALTER TABLE abonnement ALTER COLUMN quota_utilise SET NOT NULL;
ALTER TABLE abonnement ALTER COLUMN quota_total SET DEFAULT 0;
ALTER TABLE abonnement ALTER COLUMN quota_utilise SET DEFAULT 0;
