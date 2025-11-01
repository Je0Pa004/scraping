-- Backfill quotas for abonnement
-- Create columns if not exist (PostgreSQL)
ALTER TABLE IF EXISTS abonnement ADD COLUMN IF NOT EXISTS quota_total integer;
ALTER TABLE IF EXISTS abonnement ADD COLUMN IF NOT EXISTS quota_utilise integer;

-- Initialize totals from type_abonnement for rows where missing
UPDATE abonnement a
SET quota_total = t.nombre_scraping_max
FROM type_abonnement t
WHERE a.type_abonnement_id = t.id
  AND a.quota_total IS NULL;

-- Initialize used to 0 where missing
UPDATE abonnement SET quota_utilise = 0 WHERE quota_utilise IS NULL;

-- Set default for future inserts
ALTER TABLE abonnement ALTER COLUMN quota_utilise SET DEFAULT 0;
