-- =====================================================
-- DATA.SQL - Données initiales pour PharmaGo
-- Sénégal : Pays, Régions, Départements, Communes principales
-- =====================================================

-- =====================================================
-- PAYS
-- =====================================================
INSERT INTO pays (id, code, nom, devise, fuseau_horaire, indicatif_telephonique, actif, created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440000', 'SN', 'Sénégal', 'FCFA', 'Africa/Dakar', '+221', true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- RÉGIONS DU SÉNÉGAL (14 régions)
-- =====================================================
INSERT INTO regions (id, code, nom, pays_id, created_at, updated_at)
VALUES
-- Dakar
('550e8400-e29b-41d4-a716-446655440001', 'DK', 'Dakar', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Thiès
('550e8400-e29b-41d4-a716-446655440002', 'TH', 'Thiès', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Saint-Louis
('550e8400-e29b-41d4-a716-446655440003', 'SL', 'Saint-Louis', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Kaolack
('550e8400-e29b-41d4-a716-446655440004', 'KL', 'Kaolack', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Diourbel
('550e8400-e29b-41d4-a716-446655440005', 'DB', 'Diourbel', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Ziguinchor
('550e8400-e29b-41d4-a716-446655440006', 'ZG', 'Ziguinchor', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Louga
('550e8400-e29b-41d4-a716-446655440007', 'LG', 'Louga', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Fatick
('550e8400-e29b-41d4-a716-446655440008', 'FK', 'Fatick', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Kolda
('550e8400-e29b-41d4-a716-446655440009', 'KD', 'Kolda', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Matam
('550e8400-e29b-41d4-a716-446655440010', 'MT', 'Matam', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Tambacounda
('550e8400-e29b-41d4-a716-446655440011', 'TC', 'Tambacounda', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Kaffrine
('550e8400-e29b-41d4-a716-446655440012', 'KF', 'Kaffrine', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Kédougou
('550e8400-e29b-41d4-a716-446655440013', 'KE', 'Kédougou', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- Sédhiou
('550e8400-e29b-41d4-a716-446655440014', 'SE', 'Sédhiou', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- DÉPARTEMENTS DE DAKAR (4 départements)
-- =====================================================
INSERT INTO departements (id, code, nom, region_id, created_at, updated_at)
VALUES
('650e8400-e29b-41d4-a716-446655440001', 'DK-DAK', 'Dakar', '550e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440002', 'DK-PIK', 'Pikine', '550e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440003', 'DK-GUE', 'Guédiawaye', '550e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440004', 'DK-RUF', 'Rufisque', '550e8400-e29b-41d4-a716-446655440001', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- COMMUNES PRINCIPALES DE DAKAR (avec coordonnées GPS)
-- =====================================================
INSERT INTO communes (id, code, nom, latitude, longitude, departement_id, created_at, updated_at)
VALUES
-- Département de Dakar
('750e8400-e29b-41d4-a716-446655440001', 'DK-PLAT', 'Plateau', 14.6928, -17.4467, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440002', 'DK-MED', 'Médina', 14.6800, -17.4550, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440003', 'DK-PAA', 'Parcelles Assainies', 14.7500, -17.4300, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440004', 'DK-ALM', 'Almadies', 14.7453, -17.5114, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440005', 'DK-GTA', 'Gueule Tapée', 14.7000, -17.4600, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440006', 'DK-FAD', 'Fass-Delorme', 14.7100, -17.4500, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440007', 'DK-GRA', 'Grand Dakar', 14.7200, -17.4650, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440008', 'DK-DRK', 'Dieuppeul-Derklé', 14.7300, -17.4800, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440009', 'DK-YOF', 'Yoff', 14.7500, -17.4950, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440010', 'DK-OUA', 'Ouakam', 14.7200, -17.4900, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440011', 'DK-NIA', 'Ngor', 14.7550, -17.5150, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440012', 'DK-MAM', 'Mermoz-Sacré-Cœur', 14.7150, -17.4700, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440013', 'DK-SIC', 'Sicap-Liberté', 14.7250, -17.4600, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440014', 'DK-PTM', 'Point E', 14.7050, -17.4550, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440015', 'DK-HLM', 'HLM', 14.7400, -17.4550, '650e8400-e29b-41d4-a716-446655440001', NOW(), NOW()),

-- Département de Pikine
('750e8400-e29b-41d4-a716-446655440016', 'PIK-PIKG', 'Pikine Guédiawaye', 14.7600, -17.4100, '650e8400-e29b-41d4-a716-446655440002', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440017', 'PIK-THIA', 'Thiaroye', 14.7800, -17.3800, '650e8400-e29b-41d4-a716-446655440002', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440018', 'PIK-DIE', 'Diamaguène', 14.7700, -17.4000, '650e8400-e29b-41d4-a716-446655440002', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440019', 'PIK-KEU', 'Keur Massar', 14.7900, -17.3200, '650e8400-e29b-41d4-a716-446655440002', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440020', 'PIK-GUE', 'Guinaw Rail', 14.7650, -17.4050, '650e8400-e29b-41d4-a716-446655440002', NOW(), NOW()),

-- Département de Guédiawaye
('750e8400-e29b-41d4-a716-446655440021', 'GUE-GOLF', 'Golf Sud', 14.7800, -17.4200, '650e8400-e29b-41d4-a716-446655440003', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440022', 'GUE-SAM', 'Sam Notaire', 14.7850, -17.4250, '650e8400-e29b-41d4-a716-446655440003', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440023', 'GUE-MEL', 'Médina Gounass', 14.7900, -17.4300, '650e8400-e29b-41d4-a716-446655440003', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440024', 'GUE-WAK', 'Wakhinane Nimzatt', 14.7950, -17.4350, '650e8400-e29b-41d4-a716-446655440003', NOW(), NOW()),

-- Département de Rufisque
('750e8400-e29b-41d4-a716-446655440025', 'RUF-RUF', 'Rufisque', 14.7167, -17.2667, '650e8400-e29b-41d4-a716-446655440004', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440026', 'RUF-BAR', 'Bargny', 14.7333, -17.2167, '650e8400-e29b-41d4-a716-446655440004', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440027', 'RUF-DIE', 'Diamniadio', 14.7200, -17.1900, '650e8400-e29b-41d4-a716-446655440004', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440028', 'RUF-SEB', 'Sebikotane', 14.7400, -17.1500, '650e8400-e29b-41d4-a716-446655440004', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- AUTRES COMMUNES IMPORTANTES (Thiès, Saint-Louis, etc.)
-- =====================================================

-- Départements de Thiès
INSERT INTO departements (id, code, nom, region_id, created_at, updated_at)
VALUES
('650e8400-e29b-41d4-a716-446655440005', 'TH-THI', 'Thiès', '550e8400-e29b-41d4-a716-446655440002', NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440006', 'TH-MBO', 'Mbour', '550e8400-e29b-41d4-a716-446655440002', NOW(), NOW()),
('650e8400-e29b-41d4-a716-446655440007', 'TH-TIV', 'Tivaouane', '550e8400-e29b-41d4-a716-446655440002', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Communes de Thiès
INSERT INTO communes (id, code, nom, latitude, longitude, departement_id, created_at, updated_at)
VALUES
('750e8400-e29b-41d4-a716-446655440029', 'TH-THIE', 'Thiès', 14.7889, -16.9316, '650e8400-e29b-41d4-a716-446655440005', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440030', 'TH-MBOU', 'Mbour', 14.4167, -16.9667, '650e8400-e29b-41d4-a716-446655440006', NOW(), NOW()),
('750e8400-e29b-41d4-a716-446655440031', 'TH-TIVA', 'Tivaouane', 14.9500, -16.8167, '650e8400-e29b-41d4-a716-446655440007', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Saint-Louis
INSERT INTO departements (id, code, nom, region_id, created_at, updated_at)
VALUES
('650e8400-e29b-41d4-a716-446655440008', 'SL-STL', 'Saint-Louis', '550e8400-e29b-41d4-a716-446655440003', NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO communes (id, code, nom, latitude, longitude, departement_id, created_at, updated_at)
VALUES
('750e8400-e29b-41d4-a716-446655440032', 'SL-STLO', 'Saint-Louis', 16.0179, -16.4897, '650e8400-e29b-41d4-a716-446655440008', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- FIN DU SCRIPT
-- =====================================================