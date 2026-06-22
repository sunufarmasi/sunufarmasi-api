-- ═══════════════════════════════════════════════════════════
-- SCRIPT D'INITIALISATION DES PLANS D'ABONNEMENT
-- SunuFarmasi - Plans tarifaires
-- ═══════════════════════════════════════════════════════════

-- Supprimer les plans existants (pour réinitialiser)
TRUNCATE TABLE subscription_plans CASCADE;

-- ═══════════════════════════════════════════════════════════
-- PLAN 1 : ESSAI GRATUIT (15 jours)
-- ═══════════════════════════════════════════════════════════
INSERT INTO subscription_plans (
    id,
    code,
    nom,
    description,
    prix,
    duree_jours,
    avec_publicite,
    actif,
    ordre,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'FREE_TRIAL',
    'Essai Gratuit',
    'Profitez de 15 jours d''essai gratuit pour découvrir toutes les fonctionnalités de SunuFarmasi. Aucun paiement requis !',
    0,
    15,
    true,
    true,
    1,
    NOW(),
    NOW()
);

-- ═══════════════════════════════════════════════════════════
-- PLAN 2 : MENSUEL (750 FCFA/mois avec pub)
-- ═══════════════════════════════════════════════════════════
INSERT INTO subscription_plans (
    id,
    code,
    nom,
    description,
    prix,
    duree_jours,
    avec_publicite,
    actif,
    ordre,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'MONTHLY',
    'Abonnement Mensuel',
    'Accédez à toutes les fonctionnalités de SunuFarmasi pour 750 FCFA par mois. Renouvelable automatiquement.',
    750,
    30,
    true,
    true,
    2,
    NOW(),
    NOW()
);

-- ═══════════════════════════════════════════════════════════
-- PLAN 3 : ANNUEL (7500 FCFA/an SANS pub)
-- ═══════════════════════════════════════════════════════════
INSERT INTO subscription_plans (
    id,
    code,
    nom,
    description,
    prix,
    duree_jours,
    avec_publicite,
    actif,
    ordre,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'ANNUAL',
    'Abonnement Annuel',
    'La meilleure offre ! Seulement 7 500 FCFA pour 12 mois d''accès complet SANS PUBLICITÉ. Économisez 1 500 FCFA par rapport au mensuel (2 mois offerts).',
    7500,
    365,
    false,
    true,
    3,
    NOW(),
    NOW()
);

-- ═══════════════════════════════════════════════════════════
-- VÉRIFICATION
-- ═══════════════════════════════════════════════════════════
SELECT
    code,
    nom,
    prix || ' FCFA' as prix,
    duree_jours || ' jours' as duree,
    CASE WHEN avec_publicite THEN 'Avec pub' ELSE 'Sans pub' END as publicite,
    CASE WHEN actif THEN 'Actif' ELSE 'Inactif' END as statut
FROM subscription_plans
ORDER BY ordre;

-- ═══════════════════════════════════════════════════════════
-- RÉSULTAT ATTENDU :
-- ═══════════════════════════════════════════════════════════
-- | code        | nom                  | prix        | duree    | publicite | statut |
-- |-------------|----------------------|-------------|----------|-----------|--------|
-- | FREE_TRIAL  | Essai Gratuit        | 0 FCFA      | 15 jours | Avec pub  | Actif  |
-- | MONTHLY     | Abonnement Mensuel   | 750 FCFA    | 30 jours | Avec pub  | Actif  |
-- | ANNUAL      | Abonnement Annuel    | 2500 FCFA   | 365 jours| Sans pub  | Actif  |
-- ═══════════════════════════════════════════════════════════