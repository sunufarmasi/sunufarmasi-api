# 🧪 GUIDE DE TEST COMPLET - API SUNUFARMASI

## 📋 Table des matières

1. [Configuration initiale](#1-configuration-initiale)
2. [ÉTAPE 1 - Localisation](#étape-1---localisation-régions-départements-communes)
3. [ÉTAPE 2 - Authentification & Users](#étape-2---authentification--users)
4. [ÉTAPE 3 - Syndicat](#étape-3---syndicat)
5. [ÉTAPE 4 - Pharmacien](#étape-4---pharmacien)
6. [ÉTAPE 5 - Pharmacie](#étape-5---pharmacie)
7. [ÉTAPE 6 - Employés](#étape-6---employés)
8. [ÉTAPE 7 - Patients](#étape-7---patients)
9. [ÉTAPE 8 - Produits & Stock](#étape-8---produits--stock)
10. [ÉTAPE 9 - Fournisseurs & Commandes](#étape-9---fournisseurs--commandes)
11. [ÉTAPE 10 - Ventes](#étape-10---ventes)
12. [ÉTAPE 11 - Mutuelles](#étape-11---mutuelles)
13. [ÉTAPE 12 - Gardes](#étape-12---gardes)
14. [ÉTAPE 13 - Notifications](#étape-13---notifications)
15. [ÉTAPE 14 - Abonnements & Paiements](#étape-14---abonnements--paiements)
16. [ÉTAPE 15 - Dashboard](#étape-15---dashboard)
17. [Scénarios de test complets](#scénarios-de-test-complets)

---

## 1. Configuration initiale

### 🔧 Variables d'environnement Postman/Insomnia

```json
{
  "BASE_URL": "http://localhost:8080/api/v1",
  "TOKEN": "",
  "ADMIN_TOKEN": "",
  "REGION_ID": "",
  "DEPARTEMENT_ID": "",
  "COMMUNE_ID": "",
  "SYNDICAT_ID": "",
  "PHARMACIEN_ID": "",
  "PHARMACIE_ID": "",
  "EMPLOYE_ID": "",
  "PATIENT_ID": "",
  "PRODUIT_ID": "",
  "FOURNISSEUR_ID": "",
  "COMMANDE_ID": "",
  "VENTE_ID": "",
  "MUTUELLE_ID": "",
  "PLANNING_ID": "",
  "GARDE_ID": ""
}
```

### 🗄️ Base de données

```sql
-- Vider la base pour les tests (ATTENTION: environnement de test uniquement!)
-- Exécuter dans l'ordre inverse des dépendances

TRUNCATE TABLE notifications CASCADE;
TRUNCATE TABLE gardes CASCADE;
TRUNCATE TABLE plannings_garde CASCADE;
TRUNCATE TABLE lignes_vente CASCADE;
TRUNCATE TABLE ventes CASCADE;
TRUNCATE TABLE lignes_commande CASCADE;
TRUNCATE TABLE commandes CASCADE;
TRUNCATE TABLE mouvements_stock CASCADE;
TRUNCATE TABLE produits_pharmacie CASCADE;
TRUNCATE TABLE produits CASCADE;
TRUNCATE TABLE employes CASCADE;
TRUNCATE TABLE affectations_pharmacie CASCADE;
TRUNCATE TABLE adhesions_syndicat CASCADE;
TRUNCATE TABLE membres_syndicat CASCADE;
TRUNCATE TABLE pharmacies CASCADE;
TRUNCATE TABLE pharmaciens CASCADE;
TRUNCATE TABLE patients CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE syndicats CASCADE;
TRUNCATE TABLE communes CASCADE;
TRUNCATE TABLE departements CASCADE;
TRUNCATE TABLE regions CASCADE;
```

---

## ÉTAPE 1 - Localisation (Régions, Départements, Communes)

### 📍 Ordre d'exécution
```
Région → Département → Commune
```

### 1.1 Créer les Régions

**POST** `{{BASE_URL}}/regions`

```json
// Région 1 - Dakar
{
  "code": "DKR",
  "nom": "Dakar"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "code": "DKR",
  "nom": "Dakar",
  "createdAt": "2025-01-15T10:00:00"
}
```

➡️ **Sauvegarder:** `REGION_ID = id`

```json
// Région 2 - Thiès
{
  "code": "THS",
  "nom": "Thiès"
}
```

```json
// Région 3 - Saint-Louis
{
  "code": "STL",
  "nom": "Saint-Louis"
}
```

### 1.2 Créer les Départements

**POST** `{{BASE_URL}}/departements`

```json
// Département 1 - Dakar (dans région Dakar)
{
  "code": "DKR-DKR",
  "nom": "Dakar",
  "regionId": "{{REGION_ID}}"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "code": "DKR-DKR",
  "nom": "Dakar",
  "region": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "nom": "Dakar"
  }
}
```

➡️ **Sauvegarder:** `DEPARTEMENT_ID = id`

```json
// Département 2 - Pikine
{
  "code": "DKR-PIK",
  "nom": "Pikine",
  "regionId": "{{REGION_ID}}"
}
```

```json
// Département 3 - Guédiawaye
{
  "code": "DKR-GUE",
  "nom": "Guédiawaye",
  "regionId": "{{REGION_ID}}"
}
```

```json
// Département 4 - Rufisque
{
  "code": "DKR-RUF",
  "nom": "Rufisque",
  "regionId": "{{REGION_ID}}"
}
```

### 1.3 Créer les Communes

**POST** `{{BASE_URL}}/communes`

```json
// Commune 1 - Plateau
{
  "code": "DKR-PLT",
  "nom": "Plateau",
  "departementId": "{{DEPARTEMENT_ID}}"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440020",
  "code": "DKR-PLT",
  "nom": "Plateau",
  "departement": {
    "id": "550e8400-e29b-41d4-a716-446655440010",
    "nom": "Dakar"
  }
}
```

➡️ **Sauvegarder:** `COMMUNE_ID = id`

```json
// Commune 2 - Médina
{
  "code": "DKR-MED",
  "nom": "Médina",
  "departementId": "{{DEPARTEMENT_ID}}"
}
```

```json
// Commune 3 - Grand Dakar
{
  "code": "DKR-GRD",
  "nom": "Grand Dakar",
  "departementId": "{{DEPARTEMENT_ID}}"
}
```

```json
// Commune 4 - Parcelles Assainies
{
  "code": "DKR-PAR",
  "nom": "Parcelles Assainies",
  "departementId": "{{DEPARTEMENT_ID}}"
}
```

```json
// Commune 5 - Yoff
{
  "code": "DKR-YOF",
  "nom": "Yoff",
  "departementId": "{{DEPARTEMENT_ID}}"
}
```

```json
// Commune 6 - Ouakam
{
  "code": "DKR-OUA",
  "nom": "Ouakam",
  "departementId": "{{DEPARTEMENT_ID}}"
}
```

### 1.4 Vérifier les données

**GET** `{{BASE_URL}}/regions`

**GET** `{{BASE_URL}}/regions/{{REGION_ID}}/departements`

**GET** `{{BASE_URL}}/departements/{{DEPARTEMENT_ID}}/communes`

---

## ÉTAPE 2 - Authentification & Users

### 2.1 Créer un Super Admin

**POST** `{{BASE_URL}}/auth/register/admin`

```json
{
  "email": "admin@sunufarmasi.sn",
  "password": "Admin@123456",
  "prenom": "Moussa",
  "nom": "DIOP",
  "telephone": "+221770001000",
  "role": "SUPER_ADMIN"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440100",
  "email": "admin@sunufarmasi.sn",
  "prenom": "Moussa",
  "nom": "DIOP",
  "role": "SUPER_ADMIN",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

➡️ **Sauvegarder:** `ADMIN_TOKEN = token`

### 2.2 Connexion Admin

**POST** `{{BASE_URL}}/auth/login`

```json
{
  "email": "admin@sunufarmasi.sn",
  "password": "Admin@123456"
}
```

**Réponse attendue (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
  "expiresIn": 86400,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440100",
    "email": "admin@sunufarmasi.sn",
    "nomComplet": "Moussa DIOP",
    "role": "SUPER_ADMIN"
  }
}
```

### 2.3 Headers pour les requêtes authentifiées

```
Authorization: Bearer {{ADMIN_TOKEN}}
Content-Type: application/json
```

---

## ÉTAPE 3 - Syndicat

### 3.1 Créer un Syndicat

**POST** `{{BASE_URL}}/syndicats`
**Headers:** `Authorization: Bearer {{ADMIN_TOKEN}}`

```json
{
  "code": "SYN-DKR",
  "nom": "Syndicat des Pharmaciens Privés de Dakar",
  "sigle": "SPPD",
  "description": "Syndicat regroupant les pharmaciens d'officine de la région de Dakar",
  "regionId": "{{REGION_ID}}",
  "adresse": "25 Avenue Léopold Sédar Senghor, Dakar Plateau",
  "telephone": "+221338231000",
  "telephoneSecondaire": "+221776001000",
  "email": "contact@sppd.sn",
  "siteWeb": "https://www.sppd.sn",
  "numeroEnregistrement": "SYN-2010-DKR-001",
  "dateCreationOfficielle": "2010-05-15",
  "ninea": "005678901",
  "cotisationAnnuelle": 150000,
  "delaiPaiementCotisation": 30,
  "emailNotificationGarde": "gardes@sppd.sn",
  "smsGardeActif": true
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440200",
  "code": "SYN-DKR",
  "nom": "Syndicat des Pharmaciens Privés de Dakar",
  "sigle": "SPPD",
  "region": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "nom": "Dakar"
  },
  "cotisationAnnuelle": 150000,
  "estActif": true,
  "createdAt": "2025-01-15T10:30:00"
}
```

➡️ **Sauvegarder:** `SYNDICAT_ID = id`

### 3.2 Créer les Membres du Bureau

**POST** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}/membres`

```json
// Président du Syndicat
{
  "email": "president@sppd.sn",
  "password": "President@123",
  "prenom": "Amadou",
  "nom": "FALL",
  "telephone": "+221776100001",
  "fonction": "PRESIDENT",
  "datePriseFonction": "2023-01-01",
  "dateFinMandat": "2026-12-31",
  "numeroOrdre": "PH-DKR-1985-001",
  "biographie": "Pharmacien titulaire depuis 1985, fondateur du syndicat"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440201",
  "nomComplet": "Amadou FALL",
  "fonction": "PRESIDENT",
  "role": "PRESIDENT_SYNDICAT",
  "peutGererGardes": true,
  "peutValiderAdhesions": true,
  "peutPublier": true,
  "peutGererFinances": true,
  "mandatActif": true
}
```

```json
// Vice-Président
{
  "email": "vp@sppd.sn",
  "password": "VicePresident@123",
  "prenom": "Fatou",
  "nom": "NDIAYE",
  "telephone": "+221776100002",
  "fonction": "VICE_PRESIDENT",
  "datePriseFonction": "2023-01-01",
  "dateFinMandat": "2026-12-31",
  "numeroOrdre": "PH-DKR-1990-015"
}
```

```json
// Secrétaire Général
{
  "email": "sg@sppd.sn",
  "password": "SecretaireG@123",
  "prenom": "Ibrahima",
  "nom": "SARR",
  "telephone": "+221776100003",
  "fonction": "SECRETAIRE_GENERAL",
  "datePriseFonction": "2023-01-01",
  "dateFinMandat": "2026-12-31",
  "numeroOrdre": "PH-DKR-1995-042"
}
```

```json
// Trésorier
{
  "email": "tresorier@sppd.sn",
  "password": "Tresorier@123",
  "prenom": "Mariama",
  "nom": "BA",
  "telephone": "+221776100004",
  "fonction": "TRESORIER",
  "datePriseFonction": "2023-01-01",
  "dateFinMandat": "2026-12-31",
  "numeroOrdre": "PH-DKR-2000-078"
}
```

```json
// Responsable des Gardes
{
  "email": "gardes@sppd.sn",
  "password": "Gardes@123",
  "prenom": "Ousmane",
  "nom": "DIALLO",
  "telephone": "+221776100005",
  "fonction": "RESPONSABLE_GARDES",
  "datePriseFonction": "2023-01-01",
  "dateFinMandat": "2026-12-31",
  "numeroOrdre": "PH-DKR-2005-112"
}
```

### 3.3 Vérifier le Syndicat

**GET** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}`

**GET** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}/membres`

---

## ÉTAPE 4 - Pharmacien

### 4.1 Créer des Pharmaciens Titulaires

**POST** `{{BASE_URL}}/pharmaciens`
**Headers:** `Authorization: Bearer {{ADMIN_TOKEN}}`

```json
// Pharmacien Titulaire 1
{
  "email": "dr.diop@pharmacie-centrale.sn",
  "password": "Pharmacien@123",
  "prenom": "Abdoulaye",
  "nom": "DIOP",
  "telephone": "+221776200001",
  "numeroOrdre": "PH-DKR-2008-156",
  "dateInscriptionOrdre": "2008-06-15",
  "specialite": "OFFICINE",
  "diplome": "Doctorat en Pharmacie",
  "universite": "Université Cheikh Anta Diop de Dakar",
  "anneeDiplome": 2007,
  "paysDiplome": "Sénégal",
  "estTitulaire": true,
  "cotisationOrdreJour": true,
  "dateFinCotisation": "2025-12-31"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440300",
  "email": "dr.diop@pharmacie-centrale.sn",
  "nomComplet": "Abdoulaye DIOP",
  "numeroOrdre": "PH-DKR-2008-156",
  "specialite": "OFFICINE",
  "estTitulaire": true,
  "role": "PHARMACIEN_TITULAIRE",
  "peutExercer": true,
  "cotisationValide": true
}
```

➡️ **Sauvegarder:** `PHARMACIEN_ID = id`

```json
// Pharmacien Titulaire 2
{
  "email": "dr.sow@pharmacie-medina.sn",
  "password": "Pharmacien@123",
  "prenom": "Aissatou",
  "nom": "SOW",
  "telephone": "+221776200002",
  "numeroOrdre": "PH-DKR-2010-189",
  "dateInscriptionOrdre": "2010-03-20",
  "specialite": "OFFICINE",
  "diplome": "Doctorat en Pharmacie",
  "universite": "Université Cheikh Anta Diop de Dakar",
  "anneeDiplome": 2009,
  "paysDiplome": "Sénégal",
  "estTitulaire": true,
  "cotisationOrdreJour": true,
  "dateFinCotisation": "2025-12-31"
}
```

```json
// Pharmacien Titulaire 3
{
  "email": "dr.gueye@pharmacie-yoff.sn",
  "password": "Pharmacien@123",
  "prenom": "Mamadou",
  "nom": "GUEYE",
  "telephone": "+221776200003",
  "numeroOrdre": "PH-DKR-2012-234",
  "dateInscriptionOrdre": "2012-09-10",
  "specialite": "OFFICINE",
  "diplome": "Doctorat en Pharmacie",
  "universite": "Université de Bordeaux",
  "anneeDiplome": 2011,
  "paysDiplome": "France",
  "estTitulaire": true,
  "cotisationOrdreJour": true,
  "dateFinCotisation": "2025-12-31"
}
```

### 4.2 Créer des Pharmaciens Assistants

```json
// Pharmacien Assistant 1
{
  "email": "assistant1@pharmacie.sn",
  "password": "Assistant@123",
  "prenom": "Cheikh",
  "nom": "MBAYE",
  "telephone": "+221776200010",
  "numeroOrdre": "PH-DKR-2018-345",
  "dateInscriptionOrdre": "2018-07-01",
  "specialite": "OFFICINE",
  "diplome": "Doctorat en Pharmacie",
  "universite": "Université Cheikh Anta Diop de Dakar",
  "anneeDiplome": 2017,
  "paysDiplome": "Sénégal",
  "estTitulaire": false,
  "cotisationOrdreJour": true,
  "dateFinCotisation": "2025-12-31"
}
```

```json
// Pharmacien Assistant 2
{
  "email": "assistant2@pharmacie.sn",
  "password": "Assistant@123",
  "prenom": "Ndèye",
  "nom": "DIAGNE",
  "telephone": "+221776200011",
  "numeroOrdre": "PH-DKR-2020-412",
  "dateInscriptionOrdre": "2020-02-15",
  "specialite": "OFFICINE",
  "diplome": "Doctorat en Pharmacie",
  "universite": "Université Gaston Berger de Saint-Louis",
  "anneeDiplome": 2019,
  "paysDiplome": "Sénégal",
  "estTitulaire": false,
  "cotisationOrdreJour": true,
  "dateFinCotisation": "2025-12-31"
}
```

### 4.3 Vérifier les Pharmaciens

**GET** `{{BASE_URL}}/pharmaciens`

**GET** `{{BASE_URL}}/pharmaciens/titulaires`

**GET** `{{BASE_URL}}/pharmaciens/assistants`

**GET** `{{BASE_URL}}/pharmaciens/{{PHARMACIEN_ID}}`

---

## ÉTAPE 5 - Pharmacie

### 5.1 Créer des Pharmacies

**POST** `{{BASE_URL}}/pharmacies`
**Headers:** `Authorization: Bearer {{ADMIN_TOKEN}}`

```json
// Pharmacie 1 - Pharmacie Centrale du Plateau
{
  "code": "PH-DKR-001",
  "nom": "Pharmacie Centrale du Plateau",
  "titulaireId": "{{PHARMACIEN_ID}}",
  "syndicatId": "{{SYNDICAT_ID}}",
  "communeId": "{{COMMUNE_ID}}",
  "adresse": "15 Avenue Léopold Sédar Senghor",
  "quartier": "Plateau",
  "latitude": 14.6937,
  "longitude": -17.4441,
  "zone": "ZONE-A",
  "telephone": "+221338212345",
  "telephoneFixe": "+221338212346",
  "email": "contact@pharmacie-centrale.sn",
  "whatsapp": "+221776200001",
  "numeroAutorisation": "AUTH-2010-DKR-001",
  "dateAutorisation": "2010-01-15",
  "ninea": "001234567",
  "registreCommerce": "SN-DKR-2010-B-12345",
  "dateOuverture": "2010-03-01",
  "heureOuverture": "08:00",
  "heureFermeture": "22:00",
  "joursOuverture": "LUN,MAR,MER,JEU,VEN,SAM",
  "ouvert24h": false,
  "livraisonDisponible": true,
  "fraisLivraison": 1500,
  "rayonLivraisonKm": 10,
  "commandeEnLigne": true,
  "participeGardes": true,
  "servicesSpeciaux": "[\"Vaccination\", \"Prise de tension\", \"Conseil nutritionnel\"]"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440400",
  "code": "PH-DKR-001",
  "nom": "Pharmacie Centrale du Plateau",
  "titulaire": {
    "id": "550e8400-e29b-41d4-a716-446655440300",
    "nomComplet": "Abdoulaye DIOP"
  },
  "syndicat": {
    "id": "550e8400-e29b-41d4-a716-446655440200",
    "nom": "SPPD"
  },
  "commune": {
    "nom": "Plateau"
  },
  "statut": "ACTIVE",
  "participeGardes": true,
  "livraisonDisponible": true
}
```

➡️ **Sauvegarder:** `PHARMACIE_ID = id`

```json
// Pharmacie 2 - Pharmacie de la Médina
{
  "code": "PH-DKR-002",
  "nom": "Pharmacie de la Médina",
  "titulaireId": "<ID_PHARMACIEN_2>",
  "syndicatId": "{{SYNDICAT_ID}}",
  "communeId": "<ID_COMMUNE_MEDINA>",
  "adresse": "42 Rue Blanchot",
  "quartier": "Médina",
  "latitude": 14.6872,
  "longitude": -17.4456,
  "zone": "ZONE-A",
  "telephone": "+221338234567",
  "email": "contact@pharmacie-medina.sn",
  "numeroAutorisation": "AUTH-2012-DKR-015",
  "dateAutorisation": "2012-05-20",
  "dateOuverture": "2012-07-01",
  "heureOuverture": "08:00",
  "heureFermeture": "21:00",
  "joursOuverture": "LUN,MAR,MER,JEU,VEN,SAM,DIM",
  "participeGardes": true
}
```

```json
// Pharmacie 3 - Pharmacie Yoff Océan
{
  "code": "PH-DKR-003",
  "nom": "Pharmacie Yoff Océan",
  "titulaireId": "<ID_PHARMACIEN_3>",
  "syndicatId": "{{SYNDICAT_ID}}",
  "communeId": "<ID_COMMUNE_YOFF>",
  "adresse": "Route de l'Aéroport, Yoff",
  "quartier": "Yoff Village",
  "latitude": 14.7645,
  "longitude": -17.4901,
  "zone": "ZONE-B",
  "telephone": "+221338201234",
  "email": "contact@pharmacie-yoff.sn",
  "numeroAutorisation": "AUTH-2015-DKR-089",
  "dateAutorisation": "2015-11-10",
  "dateOuverture": "2016-01-15",
  "heureOuverture": "07:30",
  "heureFermeture": "23:00",
  "ouvert24h": false,
  "participeGardes": true
}
```

```json
// Pharmacie 4 - Pharmacie 24h Grand Dakar
{
  "code": "PH-DKR-004",
  "nom": "Pharmacie 24h Grand Dakar",
  "titulaireId": "<ID_PHARMACIEN_4>",
  "syndicatId": "{{SYNDICAT_ID}}",
  "communeId": "<ID_COMMUNE_GRAND_DAKAR>",
  "adresse": "Boulevard du Général de Gaulle",
  "quartier": "Grand Dakar",
  "latitude": 14.7012,
  "longitude": -17.4523,
  "zone": "ZONE-A",
  "telephone": "+221338256789",
  "numeroAutorisation": "AUTH-2018-DKR-156",
  "dateAutorisation": "2018-03-25",
  "dateOuverture": "2018-06-01",
  "ouvert24h": true,
  "participeGardes": true
}
```

### 5.2 Affecter des Pharmaciens Assistants

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/assistants`

```json
{
  "pharmacienId": "<ID_ASSISTANT_1>"
}
```

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/assistants`

```json
{
  "pharmacienId": "<ID_ASSISTANT_2>"
}
```

### 5.3 Adhésion au Syndicat

**POST** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}/adhesions`

```json
{
  "pharmacieId": "{{PHARMACIE_ID}}",
  "annee": 2025,
  "lettreDemandeUrl": "https://docs.sunufarmasi.sn/adhesion/lettre-ph001.pdf",
  "attestationOrdreUrl": "https://docs.sunufarmasi.sn/adhesion/ordre-ph001.pdf",
  "autorisationOuvertureUrl": "https://docs.sunufarmasi.sn/adhesion/auth-ph001.pdf",
  "notes": "Première adhésion au syndicat"
}
```

**Réponse attendue:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440450",
  "numeroReference": "ADH-SYN-DKR-2025-1234",
  "pharmacie": {
    "nom": "Pharmacie Centrale du Plateau"
  },
  "annee": 2025,
  "statut": "EN_ATTENTE",
  "montantCotisation": 150000,
  "cotisationPayee": false
}
```

### 5.4 Valider l'Adhésion (Président/SG)

**PUT** `{{BASE_URL}}/adhesions/<ID_ADHESION>/valider`
**Headers:** `Authorization: Bearer <TOKEN_PRESIDENT>`

```json
{
  "notes": "Dossier complet, adhésion validée"
}
```

### 5.5 Payer la Cotisation

**PUT** `{{BASE_URL}}/adhesions/<ID_ADHESION>/paiement`

```json
{
  "reference": "PAY-WAVE-20250115-001",
  "mode": "WAVE"
}
```

### 5.6 Vérifier les Pharmacies

**GET** `{{BASE_URL}}/pharmacies`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}`

**GET** `{{BASE_URL}}/pharmacies/proximite?lat=14.6937&lon=-17.4441&rayonKm=5`

**GET** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}/pharmacies`

---

## ÉTAPE 6 - Employés

### 6.1 Connexion Pharmacien Titulaire

**POST** `{{BASE_URL}}/auth/login`

```json
{
  "email": "dr.diop@pharmacie-centrale.sn",
  "password": "Pharmacien@123"
}
```

➡️ **Sauvegarder:** `TOKEN = token` (token du pharmacien)

### 6.2 Créer des Employés

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/employes`
**Headers:** `Authorization: Bearer {{TOKEN}}`

```json
// Employé 1 - Préparateur
{
  "nom": "NIANG",
  "prenom": "Moustapha",
  "telephone": "+221776300001",
  "email": "m.niang@pharmacie-centrale.sn",
  "adresse": "Parcelles Assainies U15",
  "dateNaissance": "1990-05-20",
  "numeroCni": "1900519901234",
  "poste": "PREPARATEUR",
  "dateEmbauche": "2020-03-01",
  "typeContrat": "CDI",
  "salaireBase": 250000,
  "numeroSecuriteSociale": "1234567890",
  "numeroCompteBancaire": "SN08 0001 0000 0000 1234 5678 901",
  "banque": "CBAO",
  "contactUrgenceNom": "Fatou NIANG",
  "contactUrgenceTelephone": "+221776300010",
  "contactUrgenceRelation": "Épouse"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440500",
  "matricule": "PH--12345",
  "nomComplet": "Moustapha NIANG",
  "poste": "PREPARATEUR",
  "pharmacie": {
    "id": "550e8400-e29b-41d4-a716-446655440400",
    "nom": "Pharmacie Centrale du Plateau"
  },
  "statut": "ACTIF",
  "ancienneteAnnees": 4,
  "creePar": {
    "nomComplet": "Abdoulaye DIOP"
  }
}
```

➡️ **Sauvegarder:** `EMPLOYE_ID = id`

```json
// Employé 2 - Vendeur
{
  "nom": "FAYE",
  "prenom": "Aminata",
  "telephone": "+221776300002",
  "email": "a.faye@pharmacie-centrale.sn",
  "dateNaissance": "1995-08-12",
  "poste": "VENDEUR",
  "dateEmbauche": "2022-01-15",
  "typeContrat": "CDI",
  "salaireBase": 150000
}
```

```json
// Employé 3 - Caissier
{
  "nom": "SECK",
  "prenom": "Ibrahima",
  "telephone": "+221776300003",
  "email": "i.seck@pharmacie-centrale.sn",
  "dateNaissance": "1988-12-03",
  "poste": "CAISSIER",
  "dateEmbauche": "2019-06-01",
  "typeContrat": "CDI",
  "salaireBase": 180000
}
```

```json
// Employé 4 - Comptable
{
  "nom": "THIAM",
  "prenom": "Rokhaya",
  "telephone": "+221776300004",
  "email": "r.thiam@pharmacie-centrale.sn",
  "dateNaissance": "1992-03-25",
  "poste": "COMPTABLE",
  "dateEmbauche": "2021-09-01",
  "typeContrat": "CDI",
  "salaireBase": 300000
}
```

```json
// Employé 5 - Livreur
{
  "nom": "DIENG",
  "prenom": "Pape",
  "telephone": "+221776300005",
  "dateNaissance": "1998-07-18",
  "poste": "LIVREUR",
  "dateEmbauche": "2023-02-01",
  "typeContrat": "CDD",
  "dateFinContrat": "2025-01-31",
  "salaireBase": 120000
}
```

```json
// Employé 6 - Stagiaire
{
  "nom": "DIOUF",
  "prenom": "Mame Diarra",
  "telephone": "+221776300006",
  "dateNaissance": "2000-11-30",
  "poste": "VENDEUR",
  "dateEmbauche": "2024-10-01",
  "typeContrat": "STAGE",
  "dateFinContrat": "2025-03-31",
  "salaireBase": 75000
}
```

### 6.3 Vérifier les Employés

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/employes`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/employes?statut=ACTIF`

**GET** `{{BASE_URL}}/employes/{{EMPLOYE_ID}}`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/employes/stats`

---

## ÉTAPE 7 - Patients

### 7.1 Créer des Patients

**POST** `{{BASE_URL}}/patients`
**Headers:** `Authorization: Bearer {{TOKEN}}`

```json
// Patient 1
{
  "email": "patient1@email.sn",
  "password": "Patient@123",
  "prenom": "Oumar",
  "nom": "KANE",
  "telephone": "+221776400001",
  "dateNaissance": "1975-04-10",
  "sexe": "MASCULIN",
  "numeroCni": "1750410751234",
  "adresse": "Médina, Rue 12 x 13",
  "communeId": "{{COMMUNE_ID}}",
  "groupeSanguin": "O_POSITIF",
  "allergies": "Pénicilline, Aspirine",
  "antecedents": "Hypertension artérielle",
  "maladiesChroniques": "Diabète type 2",
  "contactUrgenceNom": "Fatou KANE",
  "contactUrgenceTelephone": "+221776400010",
  "contactUrgenceRelation": "Épouse",
  "medecinTraitant": "Dr. Amadou MBAYE",
  "medecinTelephone": "+221338501234",
  "consentementRgpd": true
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440600",
  "numeroFiche": "PAT-12345678",
  "nomComplet": "Oumar KANE",
  "age": 49,
  "sexe": "MASCULIN",
  "groupeSanguin": "O_POSITIF",
  "hasAllergies": true,
  "hasAntecedents": true,
  "estMajeur": true
}
```

➡️ **Sauvegarder:** `PATIENT_ID = id`

```json
// Patient 2
{
  "email": "patient2@email.sn",
  "password": "Patient@123",
  "prenom": "Aïda",
  "nom": "SY",
  "telephone": "+221776400002",
  "dateNaissance": "1988-09-22",
  "sexe": "FEMININ",
  "adresse": "Grand Dakar",
  "communeId": "{{COMMUNE_ID}}",
  "groupeSanguin": "A_POSITIF",
  "consentementRgpd": true
}
```

```json
// Patient 3 - Mineur
{
  "prenom": "Modou",
  "nom": "DIALLO",
  "telephone": "+221776400003",
  "dateNaissance": "2015-02-14",
  "sexe": "MASCULIN",
  "adresse": "Parcelles Assainies U20",
  "communeId": "{{COMMUNE_ID}}",
  "groupeSanguin": "B_POSITIF",
  "allergies": "Arachides",
  "contactUrgenceNom": "Mamadou DIALLO",
  "contactUrgenceTelephone": "+221776400030",
  "contactUrgenceRelation": "Père",
  "consentementRgpd": true
}
```

### 7.2 Vérifier les Patients

**GET** `{{BASE_URL}}/patients`

**GET** `{{BASE_URL}}/patients/{{PATIENT_ID}}`

**GET** `{{BASE_URL}}/patients/search?q=KANE`

---

## ÉTAPE 8 - Produits & Stock

### 8.1 Créer des Catégories

**POST** `{{BASE_URL}}/categories`

```json
// Catégorie 1
{
  "code": "MEDIC",
  "nom": "Médicaments",
  "description": "Médicaments et spécialités pharmaceutiques"
}
```

```json
// Catégorie 2
{
  "code": "PARA",
  "nom": "Parapharmacie",
  "description": "Produits de parapharmacie et cosmétiques"
}
```

```json
// Catégorie 3
{
  "code": "MATER",
  "nom": "Matériel médical",
  "description": "Équipements et matériel médical"
}
```

### 8.2 Créer des Produits (Catalogue national)

**POST** `{{BASE_URL}}/produits`
**Headers:** `Authorization: Bearer {{ADMIN_TOKEN}}`

```json
// Produit 1 - Doliprane 1000mg
{
  "code": "DOL-1000-BT30",
  "codeBarres": "3400936459830",
  "designation": "Doliprane 1000mg",
  "dci": "Paracétamol",
  "forme": "COMPRIME",
  "dosage": "1000mg",
  "conditionnement": "Boîte de 30 comprimés",
  "laboratoire": "Sanofi",
  "paysOrigine": "France",
  "categorieId": "<ID_CATEGORIE_MEDIC>",
  "prixPublicConseille": 3500,
  "tauxTva": 0,
  "ordonnanceRequise": false,
  "estGenerique": false,
  "classeTherapeutique": "Antalgique, Antipyrétique",
  "indicationsTherapeutiques": "Douleurs légères à modérées, Fièvre",
  "contreIndications": "Insuffisance hépatique sévère",
  "posologie": "1 comprimé toutes les 6 heures, maximum 4 par jour",
  "effetsSecondaires": "Rares: réactions allergiques cutanées",
  "estActif": true
}
```

➡️ **Sauvegarder:** `PRODUIT_ID = id`

```json
// Produit 2 - Amoxicilline 500mg
{
  "code": "AMOX-500-BT24",
  "designation": "Amoxicilline 500mg",
  "dci": "Amoxicilline",
  "forme": "GELULE",
  "dosage": "500mg",
  "conditionnement": "Boîte de 24 gélules",
  "laboratoire": "Sandoz",
  "prixPublicConseille": 2800,
  "tauxTva": 0,
  "ordonnanceRequise": true,
  "estGenerique": true,
  "classeTherapeutique": "Antibiotique",
  "dureeValiditeJours": 730
}
```

```json
// Produit 3 - Ventoline spray
{
  "code": "VENT-100-FL1",
  "designation": "Ventoline 100µg/dose",
  "dci": "Salbutamol",
  "forme": "SPRAY",
  "dosage": "100µg/dose",
  "conditionnement": "Flacon 200 doses",
  "laboratoire": "GlaxoSmithKline",
  "prixPublicConseille": 4500,
  "ordonnanceRequise": true,
  "classeTherapeutique": "Bronchodilatateur"
}
```

```json
// Produit 4 - Efferalgan Vitamine C
{
  "code": "EFF-VIT-BT20",
  "designation": "Efferalgan Vitamine C",
  "dci": "Paracétamol + Acide ascorbique",
  "forme": "COMPRIMES_EFFERVESCENTS",
  "dosage": "500mg/200mg",
  "conditionnement": "Boîte de 20 comprimés effervescents",
  "laboratoire": "UPSA",
  "prixPublicConseille": 4200,
  "ordonnanceRequise": false
}
```

```json
// Produit 5 - Sérum physiologique
{
  "code": "SERUM-PHYSIO-BT30",
  "designation": "Sérum Physiologique unidoses",
  "forme": "SOLUTION",
  "conditionnement": "Boîte de 30 unidoses 5ml",
  "categorieId": "<ID_CATEGORIE_PARA>",
  "prixPublicConseille": 2500,
  "ordonnanceRequise": false
}
```

```json
// Produit 6 - Tensomètre électronique
{
  "code": "TENS-ELEC-001",
  "designation": "Tensomètre électronique bras",
  "forme": "DISPOSITIF_MEDICAL",
  "conditionnement": "1 appareil + brassard",
  "laboratoire": "Omron",
  "categorieId": "<ID_CATEGORIE_MATER>",
  "prixPublicConseille": 45000,
  "ordonnanceRequise": false,
  "tauxTva": 18
}
```

### 8.3 Ajouter des Produits au Stock de la Pharmacie

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/produits`
**Headers:** `Authorization: Bearer {{TOKEN}}`

```json
// Stock Doliprane
{
  "produitId": "{{PRODUIT_ID}}",
  "prixAchat": 2500,
  "prixVente": 3500,
  "quantiteStock": 150,
  "quantiteMinimum": 20,
  "quantiteOptimale": 100,
  "emplacement": "Rayon A - Étagère 2",
  "numeroLot": "LOT-2024-12-001",
  "datePeremption": "2026-12-31",
  "dateDerniereEntree": "2025-01-10"
}
```

```json
// Stock Amoxicilline
{
  "produitId": "<ID_AMOXICILLINE>",
  "prixAchat": 1800,
  "prixVente": 2800,
  "quantiteStock": 80,
  "quantiteMinimum": 15,
  "quantiteOptimale": 60,
  "emplacement": "Rayon B - Étagère 1",
  "numeroLot": "LOT-2024-11-045",
  "datePeremption": "2026-06-30"
}
```

```json
// Stock Ventoline
{
  "produitId": "<ID_VENTOLINE>",
  "prixAchat": 3200,
  "prixVente": 4500,
  "quantiteStock": 25,
  "quantiteMinimum": 5,
  "quantiteOptimale": 20,
  "emplacement": "Rayon C - Étagère 3",
  "numeroLot": "LOT-2025-01-012",
  "datePeremption": "2027-01-15"
}
```

### 8.4 Enregistrer un Mouvement de Stock

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/produits/<ID_PRODUIT_PHARMACIE>/mouvements`

```json
// Entrée de stock (réception commande)
{
  "type": "ENTREE",
  "quantite": 50,
  "motif": "RECEPTION_COMMANDE",
  "numeroLot": "LOT-2025-01-NEW",
  "datePeremption": "2027-06-30",
  "prixUnitaire": 2500,
  "referenceDocument": "CMD-2025-001",
  "notes": "Réception commande fournisseur LABOREX"
}
```

```json
// Sortie de stock (vente)
{
  "type": "SORTIE",
  "quantite": 5,
  "motif": "VENTE",
  "referenceDocument": "VTE-2025-0001",
  "notes": "Vente comptoir"
}
```

```json
// Ajustement d'inventaire
{
  "type": "AJUSTEMENT",
  "quantite": -3,
  "motif": "INVENTAIRE",
  "notes": "Écart constaté lors de l'inventaire mensuel"
}
```

```json
// Perte (péremption)
{
  "type": "SORTIE",
  "quantite": 10,
  "motif": "PEREMPTION",
  "notes": "Produits périmés retirés du stock"
}
```

### 8.5 Vérifier le Stock

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/produits`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/produits/rupture`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/produits/peremption?joursAvant=90`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/produits/<ID>/mouvements`

---

## ÉTAPE 9 - Fournisseurs & Commandes

### 9.1 Créer des Fournisseurs

**POST** `{{BASE_URL}}/fournisseurs`

```json
// Fournisseur 1 - LABOREX
{
  "code": "FRN-LABOREX",
  "raisonSociale": "LABOREX Sénégal",
  "formeJuridique": "SA",
  "sigle": "LABOREX",
  "adresse": "Zone Industrielle de Dakar",
  "telephone": "+221338590000",
  "email": "commandes@laborex.sn",
  "siteWeb": "https://www.laborex.sn",
  "ninea": "005000001",
  "registreCommerce": "SN-DKR-1990-A-00001",
  "delaiLivraisonJours": 2,
  "minimumCommande": 50000,
  "contactNom": "Amadou DIAGNE",
  "contactTelephone": "+221776590001",
  "contactEmail": "a.diagne@laborex.sn",
  "estActif": true
}
```

➡️ **Sauvegarder:** `FOURNISSEUR_ID = id`

```json
// Fournisseur 2 - SODIPHARM
{
  "code": "FRN-SODIPHARM",
  "raisonSociale": "SODIPHARM SA",
  "adresse": "Route de Rufisque, Dakar",
  "telephone": "+221338320000",
  "email": "contact@sodipharm.sn",
  "delaiLivraisonJours": 3,
  "minimumCommande": 75000
}
```

```json
// Fournisseur 3 - DUOPHARM
{
  "code": "FRN-DUOPHARM",
  "raisonSociale": "DUOPHARM Sénégal",
  "adresse": "Zone Franche de Dakar",
  "telephone": "+221338450000",
  "email": "commande@duopharm.sn",
  "delaiLivraisonJours": 1,
  "minimumCommande": 25000
}
```

### 9.2 Créer une Commande

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/commandes`
**Headers:** `Authorization: Bearer {{TOKEN}}`

```json
{
  "fournisseurId": "{{FOURNISSEUR_ID}}",
  "dateLivraisonSouhaitee": "2025-01-20",
  "notes": "Commande urgente - rupture de stock Doliprane",
  "lignes": [
    {
      "produitId": "{{PRODUIT_ID}}",
      "quantiteCommandee": 100,
      "prixUnitaire": 2500,
      "notes": "Urgent"
    },
    {
      "produitId": "<ID_AMOXICILLINE>",
      "quantiteCommandee": 50,
      "prixUnitaire": 1800
    },
    {
      "produitId": "<ID_VENTOLINE>",
      "quantiteCommandee": 20,
      "prixUnitaire": 3200
    }
  ]
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440700",
  "numeroCommande": "CMD-PH-DKR-001-20250115-001",
  "fournisseur": {
    "raisonSociale": "LABOREX Sénégal"
  },
  "statut": "BROUILLON",
  "nombreLignes": 3,
  "montantTotal": 404000,
  "dateLivraisonSouhaitee": "2025-01-20"
}
```

➡️ **Sauvegarder:** `COMMANDE_ID = id`

### 9.3 Workflow de Commande

**PUT** `{{BASE_URL}}/commandes/{{COMMANDE_ID}}/valider`

```json
{
  "notes": "Commande validée par le titulaire"
}
```

**PUT** `{{BASE_URL}}/commandes/{{COMMANDE_ID}}/envoyer`

```json
{
  "notes": "Commande envoyée au fournisseur par email"
}
```

**PUT** `{{BASE_URL}}/commandes/{{COMMANDE_ID}}/confirmer`
(Fait par le fournisseur ou admin)

```json
{
  "dateLivraisonPrevue": "2025-01-18",
  "notes": "Livraison confirmée pour le 18/01"
}
```

**PUT** `{{BASE_URL}}/commandes/{{COMMANDE_ID}}/livrer`

```json
{
  "dateLivraisonEffective": "2025-01-18",
  "lignesRecues": [
    {
      "ligneId": "<ID_LIGNE_1>",
      "quantiteRecue": 100,
      "numeroLot": "LOT-2025-01-100",
      "datePeremption": "2027-12-31"
    },
    {
      "ligneId": "<ID_LIGNE_2>",
      "quantiteRecue": 48,
      "numeroLot": "LOT-2025-01-101",
      "datePeremption": "2027-06-30",
      "notes": "2 unités manquantes"
    },
    {
      "ligneId": "<ID_LIGNE_3>",
      "quantiteRecue": 20,
      "numeroLot": "LOT-2025-01-102",
      "datePeremption": "2028-01-15"
    }
  ],
  "notes": "Réception partielle - 2 Amoxicilline manquants"
}
```

### 9.4 Vérifier les Commandes

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/commandes`

**GET** `{{BASE_URL}}/commandes/{{COMMANDE_ID}}`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/commandes?statut=EN_ATTENTE_LIVRAISON`

---

## ÉTAPE 10 - Ventes

### 10.1 Créer une Vente Simple (Comptoir)

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/ventes`
**Headers:** `Authorization: Bearer {{TOKEN}}`

```json
{
  "typeVente": "COMPTOIR",
  "lignes": [
    {
      "produitPharmacieId": "<ID_PRODUIT_PHARMACIE_DOLIPRANE>",
      "quantite": 2,
      "prixUnitaire": 3500
    }
  ],
  "modePaiement": "ESPECES",
  "montantRecu": 10000,
  "notes": "Vente comptoir"
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440800",
  "numeroVente": "VTE-PH-DKR-001-20250115-001",
  "typeVente": "COMPTOIR",
  "statut": "TERMINEE",
  "montantTotal": 7000,
  "montantTva": 0,
  "montantNet": 7000,
  "montantRecu": 10000,
  "montantRendu": 3000,
  "modePaiement": "ESPECES",
  "vendeur": {
    "nomComplet": "Moustapha NIANG"
  }
}
```

➡️ **Sauvegarder:** `VENTE_ID = id`

### 10.2 Créer une Vente avec Ordonnance

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/ventes`

```json
{
  "typeVente": "ORDONNANCE",
  "patientId": "{{PATIENT_ID}}",
  "numeroOrdonnance": "ORD-2025-0001",
  "medecinPrescripteur": "Dr. Amadou MBAYE",
  "dateOrdonnance": "2025-01-14",
  "lignes": [
    {
      "produitPharmacieId": "<ID_AMOXICILLINE>",
      "quantite": 1,
      "prixUnitaire": 2800
    },
    {
      "produitPharmacieId": "<ID_DOLIPRANE>",
      "quantite": 1,
      "prixUnitaire": 3500
    }
  ],
  "modePaiement": "WAVE",
  "referenceTransaction": "WAVE-20250115-12345",
  "notes": "Ordonnance validée, traitement complet"
}
```

### 10.3 Créer une Vente avec Mutuelle

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/ventes`

```json
{
  "typeVente": "MUTUELLE",
  "patientId": "{{PATIENT_ID}}",
  "mutuelleId": "{{MUTUELLE_ID}}",
  "numeroAdherent": "MUT-2025-0001234",
  "tauxPriseEnCharge": 80,
  "lignes": [
    {
      "produitPharmacieId": "<ID_VENTOLINE>",
      "quantite": 1,
      "prixUnitaire": 4500
    }
  ],
  "montantMutuelle": 3600,
  "montantPatient": 900,
  "modePaiement": "ESPECES",
  "montantRecu": 1000,
  "notes": "Prise en charge mutuelle 80%"
}
```

### 10.4 Créer une Vente avec Livraison

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/ventes`

```json
{
  "typeVente": "LIVRAISON",
  "patientId": "{{PATIENT_ID}}",
  "lignes": [
    {
      "produitPharmacieId": "<ID_PRODUIT_1>",
      "quantite": 3,
      "prixUnitaire": 3500
    },
    {
      "produitPharmacieId": "<ID_PRODUIT_2>",
      "quantite": 1,
      "prixUnitaire": 45000
    }
  ],
  "adresseLivraison": "Médina, Rue 12 x 13",
  "telephoneLivraison": "+221776400001",
  "fraisLivraison": 1500,
  "modePaiement": "ORANGE_MONEY",
  "referenceTransaction": "OM-20250115-67890",
  "notes": "Livraison prévue dans les 2 heures"
}
```

### 10.5 Annuler une Vente

**PUT** `{{BASE_URL}}/ventes/<ID_VENTE>/annuler`

```json
{
  "motif": "Erreur de saisie - doublon de vente"
}
```

### 10.6 Vérifier les Ventes

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/ventes`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/ventes?date=2025-01-15`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/ventes/stats/jour?date=2025-01-15`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/ventes/caisse`

---

## ÉTAPE 11 - Mutuelles

### 11.1 Créer des Mutuelles

**POST** `{{BASE_URL}}/mutuelles`
**Headers:** `Authorization: Bearer {{ADMIN_TOKEN}}`

```json
// Mutuelle 1 - IPM Sonatel
{
  "code": "MUT-SONATEL",
  "nom": "IPM Sonatel",
  "sigle": "IPM-SON",
  "type": "IPM",
  "adresse": "46 Boulevard de la République, Dakar",
  "telephone": "+221338394000",
  "email": "ipm@sonatel.sn",
  "siteWeb": "https://www.sonatel.sn/ipm",
  "tauxPriseEnChargeDefaut": 80,
  "plafondAnnuel": 5000000,
  "delaiRemboursement": 15,
  "contactNom": "Mme Ndèye DIOP",
  "contactTelephone": "+221776394001",
  "contactEmail": "n.diop@sonatel.sn",
  "estActif": true
}
```

➡️ **Sauvegarder:** `MUTUELLE_ID = id`

```json
// Mutuelle 2 - IPM Port Autonome
{
  "code": "MUT-PAD",
  "nom": "IPM Port Autonome de Dakar",
  "sigle": "IPM-PAD",
  "type": "IPM",
  "adresse": "21 Boulevard de la Libération, Dakar",
  "telephone": "+221338493000",
  "email": "ipm@portdakar.sn",
  "tauxPriseEnChargeDefaut": 85,
  "plafondAnnuel": 3000000,
  "estActif": true
}
```

```json
// Mutuelle 3 - Mutuelle de Santé
{
  "code": "MUT-SANTE",
  "nom": "Mutuelle de Santé du Sénégal",
  "sigle": "MSS",
  "type": "MUTUELLE_SANTE",
  "adresse": "Avenue Cheikh Anta Diop, Dakar",
  "telephone": "+221338220000",
  "email": "contact@mss.sn",
  "tauxPriseEnChargeDefaut": 70,
  "plafondAnnuel": 2000000,
  "estActif": true
}
```

### 11.2 Créer un Contrat avec la Pharmacie

**POST** `{{BASE_URL}}/mutuelles/{{MUTUELLE_ID}}/contrats`

```json
{
  "pharmacieId": "{{PHARMACIE_ID}}",
  "dateDebut": "2025-01-01",
  "dateFin": "2025-12-31",
  "tauxPriseEnCharge": 80,
  "plafondMensuel": 500000,
  "delaiPaiement": 30,
  "notes": "Contrat annuel renouvelable"
}
```

### 11.3 Créer des Adhérents

**POST** `{{BASE_URL}}/mutuelles/{{MUTUELLE_ID}}/adherents`

```json
{
  "patientId": "{{PATIENT_ID}}",
  "numeroAdherent": "SON-2025-001234",
  "dateAdhesion": "2020-03-15",
  "typeAdherent": "TITULAIRE",
  "plafondIndividuel": 1000000,
  "tauxIndividuel": 80,
  "estActif": true
}
```

### 11.4 Créer une Demande de Remboursement

**POST** `{{BASE_URL}}/mutuelles/{{MUTUELLE_ID}}/remboursements`

```json
{
  "pharmacieId": "{{PHARMACIE_ID}}",
  "venteId": "<ID_VENTE_MUTUELLE>",
  "adherentId": "<ID_ADHERENT>",
  "montantDemande": 3600,
  "datePrestation": "2025-01-15",
  "documentsJustificatifs": ["facture.pdf", "ordonnance.pdf"]
}
```

### 11.5 Traiter le Remboursement

**PUT** `{{BASE_URL}}/remboursements/<ID>/valider`

```json
{
  "montantAccorde": 3600,
  "notes": "Remboursement validé"
}
```

**PUT** `{{BASE_URL}}/remboursements/<ID>/payer`

```json
{
  "referenceVirement": "VIR-2025-01-001",
  "dateVirement": "2025-01-20",
  "notes": "Paiement effectué"
}
```

### 11.6 Vérifier les Mutuelles

**GET** `{{BASE_URL}}/mutuelles`

**GET** `{{BASE_URL}}/mutuelles/{{MUTUELLE_ID}}`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/mutuelles`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/remboursements?statut=EN_ATTENTE`

---

## ÉTAPE 12 - Gardes

### 12.1 Créer un Planning de Garde

**POST** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}/plannings`
**Headers:** `Authorization: Bearer <TOKEN_RESPONSABLE_GARDES>`

```json
{
  "titre": "Planning Gardes Janvier 2025 - Semaine 4",
  "description": "Planning des gardes pour la semaine du 20 au 26 janvier 2025",
  "dateDebut": "2025-01-20",
  "dateFin": "2025-01-26",
  "publicationAuto": true,
  "datePublicationPrevue": "2025-01-18T18:00:00",
  "notifierPharmacies": true,
  "notifierSms": true,
  "notifierEmail": true
}
```

**Réponse attendue (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440900",
  "titre": "Planning Gardes Janvier 2025 - Semaine 4",
  "dateDebut": "2025-01-20",
  "dateFin": "2025-01-26",
  "statut": "BROUILLON",
  "nombreJours": 7,
  "nombreGardes": 0,
  "publicationAuto": true,
  "creeParNom": "Ousmane DIALLO"
}
```

➡️ **Sauvegarder:** `PLANNING_ID = id`

### 12.2 Ajouter des Gardes au Planning

**POST** `{{BASE_URL}}/plannings/{{PLANNING_ID}}/gardes`

```json
// Garde de jour - Lundi
{
  "pharmacieId": "{{PHARMACIE_ID}}",
  "dateGarde": "2025-01-20",
  "typeGarde": "JOUR",
  "heureDebut": "08:00",
  "heureFin": "20:00",
  "zone": "ZONE-A",
  "notes": "Garde de jour"
}
```

➡️ **Sauvegarder:** `GARDE_ID = id`

```json
// Garde de nuit - Lundi
{
  "pharmacieId": "<ID_PHARMACIE_2>",
  "dateGarde": "2025-01-20",
  "typeGarde": "NUIT",
  "heureDebut": "20:00",
  "heureFin": "08:00",
  "zone": "ZONE-A"
}
```

```json
// Garde 24h - Mardi
{
  "pharmacieId": "<ID_PHARMACIE_3>",
  "dateGarde": "2025-01-21",
  "typeGarde": "JOUR_24H",
  "zone": "ZONE-B"
}
```

### 12.3 Ajouter des Gardes en Lot

**POST** `{{BASE_URL}}/plannings/{{PLANNING_ID}}/gardes/batch`

```json
{
  "gardes": [
    {
      "pharmacieId": "{{PHARMACIE_ID}}",
      "dateGarde": "2025-01-22",
      "typeGarde": "JOUR",
      "zone": "ZONE-A"
    },
    {
      "pharmacieId": "<ID_PHARMACIE_2>",
      "dateGarde": "2025-01-22",
      "typeGarde": "NUIT",
      "zone": "ZONE-A"
    },
    {
      "pharmacieId": "<ID_PHARMACIE_4>",
      "dateGarde": "2025-01-23",
      "typeGarde": "JOUR_24H",
      "zone": "ZONE-A"
    },
    {
      "pharmacieId": "{{PHARMACIE_ID}}",
      "dateGarde": "2025-01-25",
      "typeGarde": "WEEKEND",
      "zone": "ZONE-A"
    },
    {
      "pharmacieId": "<ID_PHARMACIE_3>",
      "dateGarde": "2025-01-26",
      "typeGarde": "WEEKEND",
      "zone": "ZONE-B"
    }
  ]
}
```

### 12.4 Workflow du Planning

**POST** `{{BASE_URL}}/plannings/{{PLANNING_ID}}/soumettre`

```json
{}
```

**Réponse:** Statut passe à `EN_VALIDATION`

**POST** `{{BASE_URL}}/plannings/{{PLANNING_ID}}/valider`
**Headers:** `Authorization: Bearer <TOKEN_PRESIDENT>`

```json
{}
```

**Réponse:** Statut passe à `VALIDE`

**POST** `{{BASE_URL}}/plannings/{{PLANNING_ID}}/publier`

```json
{}
```

**Réponse:** Statut passe à `PUBLIE`, notifications envoyées

### 12.5 Confirmer une Garde (par la Pharmacie)

**POST** `{{BASE_URL}}/gardes/{{GARDE_ID}}/confirmer`
**Headers:** `Authorization: Bearer {{TOKEN}}` (token pharmacien titulaire)

```json
{}
```

### 12.6 Rechercher les Pharmacies de Garde

**GET** `{{BASE_URL}}/gardes/aujourd-hui`

**GET** `{{BASE_URL}}/gardes?date=2025-01-20`

**GET** `{{BASE_URL}}/gardes?date=2025-01-20&type=NUIT`

**GET** `{{BASE_URL}}/gardes/proximite?date=2025-01-20&latitude=14.6937&longitude=-17.4441&rayonKm=5`

### 12.7 Vérifier les Gardes

**GET** `{{BASE_URL}}/plannings/{{PLANNING_ID}}`

**GET** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}/plannings`

**GET** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}/plannings/actuel`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/gardes`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/gardes/historique`

---

## ÉTAPE 13 - Notifications

### 13.1 Configurer les Notifications pour une Pharmacie

**POST** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/notifications/config`

```json
{
  "emailActif": true,
  "smsActif": true,
  "pushActif": true,
  "notifierRuptureStock": true,
  "notifierPeremption": true,
  "notifierGarde": true,
  "notifierCommande": true,
  "joursAvantPeremption": 90,
  "emailDestinataires": ["dr.diop@pharmacie-centrale.sn", "comptable@pharmacie-centrale.sn"],
  "telephoneSms": "+221776200001"
}
```

### 13.2 Envoyer une Notification Manuelle

**POST** `{{BASE_URL}}/notifications`
**Headers:** `Authorization: Bearer {{ADMIN_TOKEN}}`

```json
{
  "type": "INFO",
  "destinataireType": "PHARMACIE",
  "destinataireId": "{{PHARMACIE_ID}}",
  "titre": "Mise à jour du système",
  "message": "Une maintenance est prévue ce soir de 23h à 1h. L'application sera indisponible.",
  "canaux": ["EMAIL", "PUSH"],
  "priorite": "NORMALE"
}
```

### 13.3 Notification de Garde

**POST** `{{BASE_URL}}/notifications/garde`

```json
{
  "pharmacieId": "{{PHARMACIE_ID}}",
  "gardeId": "{{GARDE_ID}}",
  "type": "RAPPEL_GARDE",
  "message": "Rappel: Vous êtes de garde demain 20/01/2025 de 08h à 20h"
}
```

### 13.4 Consulter les Notifications

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/notifications`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/notifications?lu=false`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/notifications/count`

### 13.5 Marquer comme Lu

**PUT** `{{BASE_URL}}/notifications/<ID>/lu`

**PUT** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/notifications/marquer-tout-lu`

---

## ÉTAPE 14 - Abonnements & Paiements

### 14.1 Créer des Plans d'Abonnement

**POST** `{{BASE_URL}}/abonnements/plans`
**Headers:** `Authorization: Bearer {{ADMIN_TOKEN}}`

```json
// Plan Gratuit
{
  "code": "PLAN-GRATUIT",
  "nom": "Gratuit",
  "description": "Accès basique à l'application",
  "prix": 0,
  "dureeJours": 365,
  "type": "GRATUIT",
  "maxConsultations": 5,
  "remiseMedicaments": 0,
  "livraisonGratuite": false,
  "features": "[\"Recherche pharmacies\", \"Horaires de garde\"]"
}
```

```json
// Plan Basic
{
  "code": "PLAN-BASIC",
  "nom": "Basic",
  "description": "Accès standard avec remises",
  "prix": 5000,
  "dureeJours": 30,
  "type": "BASIC",
  "maxConsultations": 20,
  "remiseMedicaments": 5,
  "livraisonGratuite": false,
  "features": "[\"Recherche pharmacies\", \"Horaires de garde\", \"Historique achats\", \"5% de remise\"]"
}
```

```json
// Plan Premium
{
  "code": "PLAN-PREMIUM",
  "nom": "Premium",
  "description": "Accès complet avec tous les avantages",
  "prix": 15000,
  "dureeJours": 30,
  "type": "PREMIUM",
  "maxConsultations": -1,
  "remiseMedicaments": 15,
  "livraisonGratuite": true,
  "features": "[\"Tout Basic\", \"Livraison gratuite\", \"15% de remise\", \"Conseils personnalisés\", \"Priorité SAV\"]"
}
```

### 14.2 Souscrire à un Abonnement

**POST** `{{BASE_URL}}/patients/{{PATIENT_ID}}/abonnements`

```json
{
  "planCode": "PLAN-BASIC",
  "autoRenouvellement": true,
  "methodePaiement": "ORANGE_MONEY"
}
```

### 14.3 Effectuer un Paiement

**POST** `{{BASE_URL}}/paiements`

```json
{
  "patientId": "{{PATIENT_ID}}",
  "subscriptionId": "<ID_ABONNEMENT>",
  "montant": 5000,
  "methode": "ORANGE_MONEY",
  "referenceExterne": "OM-20250115-ABCD1234"
}
```

### 14.4 Confirmer le Paiement (Webhook ou manuel)

**PUT** `{{BASE_URL}}/paiements/<ID>/confirmer`

```json
{
  "referenceTransaction": "OM-CONF-20250115-ABCD1234",
  "statut": "SUCCESS"
}
```

### 14.5 Vérifier les Abonnements

**GET** `{{BASE_URL}}/abonnements/plans`

**GET** `{{BASE_URL}}/patients/{{PATIENT_ID}}/abonnement`

**GET** `{{BASE_URL}}/patients/{{PATIENT_ID}}/paiements`

---

## ÉTAPE 15 - Dashboard

### 15.1 Dashboard Pharmacie

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/dashboard`

**Réponse attendue:**
```json
{
  "pharmacie": {
    "nom": "Pharmacie Centrale du Plateau",
    "statut": "ACTIVE"
  },
  "ventesAujourdhui": {
    "nombre": 45,
    "montantTotal": 567000,
    "montantEspeces": 320000,
    "montantMobileMoney": 247000
  },
  "stockAlerts": {
    "rupturesStock": 3,
    "stockFaible": 12,
    "peremptionProche": 8
  },
  "commandesEnCours": 2,
  "employesActifs": 6,
  "prochaineGarde": {
    "date": "2025-01-20",
    "type": "JOUR"
  },
  "chiffreAffairesMois": 12500000,
  "evolutionCA": [
    {"jour": "2025-01-01", "montant": 450000},
    {"jour": "2025-01-02", "montant": 380000}
  ]
}
```

### 15.2 Dashboard Syndicat

**GET** `{{BASE_URL}}/syndicats/{{SYNDICAT_ID}}/dashboard`

**Réponse attendue:**
```json
{
  "syndicat": {
    "nom": "SPPD",
    "region": "Dakar"
  },
  "pharmacies": {
    "total": 150,
    "actives": 145,
    "suspendues": 3,
    "fermees": 2
  },
  "adhesions": {
    "total2025": 140,
    "enAttente": 5,
    "cotisationsPayees": 130,
    "montantCotisations": 19500000
  },
  "gardes": {
    "planningActuel": "Semaine 4 Janvier 2025",
    "gardesAujourdhui": 8,
    "prochainPlanning": "Semaine 5 Janvier 2025"
  },
  "membres": {
    "total": 12,
    "actifs": 10
  }
}
```

### 15.3 Statistiques Détaillées

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/stats/ventes?periode=MOIS&date=2025-01`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/stats/produits?top=10`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/stats/clients?top=20`

**GET** `{{BASE_URL}}/pharmacies/{{PHARMACIE_ID}}/stats/employes`

---

## Scénarios de Test Complets

### 🎬 Scénario 1: Parcours Patient Complet

```
1. Patient s'inscrit (POST /auth/register)
2. Patient se connecte (POST /auth/login)
3. Patient recherche pharmacies de garde (GET /gardes/aujourd-hui)
4. Patient recherche pharmacie proche (GET /pharmacies/proximite)
5. Patient consulte les produits disponibles (GET /pharmacies/{id}/produits)
6. Patient souscrit abonnement (POST /patients/{id}/abonnements)
7. Patient effectue paiement (POST /paiements)
8. Patient commande avec livraison (POST /pharmacies/{id}/ventes)
9. Patient consulte historique (GET /patients/{id}/achats)
```

### 🎬 Scénario 2: Journée Type Pharmacien

```
1. Pharmacien se connecte (POST /auth/login)
2. Consulte dashboard (GET /pharmacies/{id}/dashboard)
3. Vérifie alertes stock (GET /pharmacies/{id}/produits/rupture)
4. Passe commande fournisseur (POST /pharmacies/{id}/commandes)
5. Reçoit livraison (PUT /commandes/{id}/livrer)
6. Effectue ventes de la journée (POST /pharmacies/{id}/ventes)
7. Vérifie caisse (GET /pharmacies/{id}/ventes/caisse)
8. Consulte prochaine garde (GET /pharmacies/{id}/gardes)
9. Confirme la garde (POST /gardes/{id}/confirmer)
```

### 🎬 Scénario 3: Gestion Syndicat

```
1. Responsable Gardes se connecte
2. Crée planning mensuel (POST /syndicats/{id}/plannings)
3. Ajoute gardes en lot (POST /plannings/{id}/gardes/batch)
4. Soumet pour validation (POST /plannings/{id}/soumettre)
5. Président valide (POST /plannings/{id}/valider)
6. Publication automatique ou manuelle (POST /plannings/{id}/publier)
7. Notifications envoyées automatiquement
8. Pharmacies confirment leurs gardes
9. Consulte statistiques (GET /syndicats/{id}/dashboard)
```

### 🎬 Scénario 4: Prise en Charge Mutuelle

```
1. Vérifier convention pharmacie-mutuelle (GET /pharmacies/{id}/mutuelles)
2. Vérifier adhérent (GET /mutuelles/{id}/adherents/{numero})
3. Vérifier plafond disponible (GET /adherents/{id}/plafond)
4. Créer vente avec mutuelle (POST /pharmacies/{id}/ventes)
5. Créer demande remboursement (POST /mutuelles/{id}/remboursements)
6. Mutuelle valide (PUT /remboursements/{id}/valider)
7. Mutuelle paie (PUT /remboursements/{id}/payer)
8. Pharmacie reçoit virement
```

---

## 📝 Notes Importantes

### Codes d'erreur courants

| Code | Signification |
|------|--------------|
| 200 | Succès |
| 201 | Créé avec succès |
| 400 | Données invalides |
| 401 | Non authentifié |
| 403 | Non autorisé (permissions) |
| 404 | Ressource non trouvée |
| 409 | Conflit (doublon, etc.) |
| 422 | Erreur validation métier |
| 500 | Erreur serveur |

### Formats de dates

- **Date:** `YYYY-MM-DD` (ex: `2025-01-15`)
- **DateTime:** `YYYY-MM-DDTHH:mm:ss` (ex: `2025-01-15T14:30:00`)
- **Heure:** `HH:mm` (ex: `08:00`)

### Pagination

```
GET /pharmacies?page=0&size=20&sort=nom,asc
```

Réponse:
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

---

## ✅ Checklist de Test

- [ ] Localisation (Régions, Départements, Communes)
- [ ] Authentification (Register, Login, Refresh Token)
- [ ] Syndicat + Membres Bureau
- [ ] Pharmaciens (Titulaires + Assistants)
- [ ] Pharmacies + Adhésions
- [ ] Employés
- [ ] Patients
- [ ] Produits + Stock + Mouvements
- [ ] Fournisseurs + Commandes
- [ ] Ventes (Comptoir, Ordonnance, Mutuelle, Livraison)
- [ ] Mutuelles + Contrats + Remboursements
- [ ] Plannings de Garde + Gardes
- [ ] Notifications
- [ ] Abonnements + Paiements
- [ ] Dashboard

---

**Document créé pour SunuFarmasi API v1.0**
**Auteur: WeCan**
**Date: Janvier 2025**
