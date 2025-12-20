# 🏥 SunuFarmasi - Système de Gestion de Pharmacie

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![License](https://img.shields.io/badge/license-Proprietary-red.svg)

**Solution complète de gestion pour les pharmacies au Sénégal**

[Fonctionnalités](#-fonctionnalités) • [Architecture](#-architecture) • [Installation](#-installation) • [API](#-api) • [Modules](#-modules)

</div>

---

## 📋 Description

**SunuFarmasi** est une solution SaaS complète de gestion de pharmacie développée spécifiquement pour le marché sénégalais. Elle intègre toutes les fonctionnalités essentielles : gestion des ventes, stock, commandes fournisseurs, mutuelles/tiers payant, gardes, et bien plus.

### 🎯 Objectifs

- ✅ Digitaliser la gestion quotidienne des pharmacies
- ✅ Gérer le tiers payant et les mutuelles (IPM, CMU, Assurances)
- ✅ Optimiser la gestion des stocks et commandes
- ✅ Automatiser les alertes (ruptures, péremptions, gardes)
- ✅ Fournir des tableaux de bord décisionnels
- ✅ Supporter les paiements mobiles sénégalais (Orange Money, Wave, Free Money)

---

## 📊 État d'avancement

### Vue globale : **95% Backend complété**

| Module | Fichiers | Statut | Description |
|--------|----------|--------|-------------|
| 🔐 Auth & Utilisateurs | ~15 | ✅ Terminé | JWT, Rôles, Permissions |
| 🗺️ Localisation | ~10 | ✅ Terminé | Régions, Départements, Communes Sénégal |
| 🏪 Pharmacie | ~12 | ✅ Terminé | Gestion multi-pharmacies |
| 👤 Patient | ~8 | ✅ Terminé | Fichier patient, historique |
| 🏛️ Syndicat | ~15 | ✅ Terminé | Adhésions, cotisations |
| 👨‍💼 Employé | ~12 | ✅ Terminé | RH, contrats, présences |
| 🌙 Garde | ~15 | ✅ Terminé | Planning, permanences |
| 📦 Produits & Stock | 28 | ✅ Terminé | Catalogue, mouvements, alertes |
| 💰 Ventes | 17 | ✅ Terminé | POS, ticket, 10 modes paiement |
| 🚚 Commandes | 20 | ✅ Terminé | Fournisseurs, workflow complet |
| 🏥 Mutuelles | 23 | ✅ Terminé | Tiers payant, remboursements |
| 📊 Dashboard | 7 | ✅ Terminé | KPIs, statistiques, alertes |
| 🔔 Notifications | 14 | ✅ Terminé | Email, SMS, Push, In-app |
| 💳 Paiements | ~8 | ✅ Terminé | Orange Money, Wave, Free Money |
| 📦 Abonnements | ~8 | ✅ Terminé | Plans, souscriptions patients |

**Total : ~210+ fichiers Java**

---

## ✨ Fonctionnalités

### 💊 Gestion des Produits & Stock
- Catalogue produits avec DCI, forme galénique, dosage
- Gestion multi-pharmacie avec stock par établissement
- Alertes automatiques : ruptures, stock faible, péremptions
- Mouvements de stock tracés (entrées, sorties, ajustements)
- Gestion des lots et dates de péremption

### 💰 Point de Vente (POS)
- Vente rapide avec recherche produit
- 10 modes de paiement :
  - Espèces, Carte bancaire, Chèque, Virement
  - **Mobile Money** : Orange Money, Wave, Free Money, E-Money
  - Crédit client, Mixte
- Gestion des remises (montant ou pourcentage)
- Ticket de caisse format thermique 80mm
- Décrémentation automatique du stock

### 🏥 Mutuelles & Tiers Payant
- Gestion des mutuelles : IPM, CMU, Assurances (AMSA, Allianz, SUNU...)
- Conventions pharmacie-mutuelle avec conditions négociées
- Adhérents avec suivi droits et consommation
- Workflow demande de remboursement complet :
  ```
  BROUILLON → SOUMISE → ACCEPTEE → PAYEE
  ```
- Suivi des créances en temps réel
- Calcul automatique de la prise en charge

### 🚚 Commandes Fournisseurs
- Catalogue fournisseurs avec conditions commerciales
- Workflow commande complet :
  ```
  BROUILLON → EN_ATTENTE → CONFIRMEE → EXPEDIEE → LIVREE
  ```
- Réception partielle avec gestion lots/péremptions
- Mise à jour automatique du stock à la réception
- Commandes urgentes

### 🌙 Gestion des Gardes
- Planning des gardes (jour, nuit, jour férié)
- Affectation des pharmacies et employés
- Rappels automatiques
- Historique et statistiques

### 👨‍💼 Gestion RH
- Fichier employé complet
- Gestion des présences et absences
- Suivi des contrats
- Affectation aux pharmacies

### 📊 Tableaux de Bord
- **KPIs Ventes** : CA jour/semaine/mois, panier moyen, évolution
- **KPIs Stock** : valeur stock, ruptures, péremptions
- **KPIs Mutuelles** : créances, taux recouvrement
- **Graphiques** : évolution CA, top produits, répartition paiements
- **Alertes** : ruptures, péremptions, gardes

### 🔔 Notifications
- **In-app** : temps réel
- **Email** : alertes, résumés quotidiens
- **SMS** : Orange API Sénégal, Twilio, Infobip
- **Push** : mobile
- Configuration par utilisateur (types, canaux, horaires)

### 💳 Paiements Mobile Money
- **Orange Money** : Intégration API Sénégal
- **Wave** : Paiement instantané
- **Free Money** : Support complet
- **E-Money** (Expresso)
- Suivi des transactions
- Webhooks de confirmation
- Remboursements

### 📦 Abonnements Patients
- Plans d'abonnement (Gratuit, Basic, Standard, Premium, Famille)
- Durée configurable (mensuel, trimestriel, annuel)
- Avantages : remises médicaments, livraison gratuite, consultations
- Renouvellement automatique
- Historique des paiements

---

## 🏗️ Architecture

### Stack Technique

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Flutter)                    │
│              Mobile (iOS/Android) + Web                  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   API REST (Spring Boot)                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │ Controllers │ │  Services   │ │  Repositories   │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │    DTOs     │ │   Mappers   │ │    Entities     │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Base de données                       │
│                   MySQL / PostgreSQL                     │
└─────────────────────────────────────────────────────────┘
```

### Technologies

| Couche | Technologie |
|--------|-------------|
| **Backend** | Java 17+, Spring Boot 3.2+ |
| **Sécurité** | Spring Security, JWT |
| **Base de données** | MySQL 8 / PostgreSQL 15 |
| **ORM** | Spring Data JPA, Hibernate |
| **Documentation API** | OpenAPI 3, Swagger UI |
| **Tests** | JUnit 5, Mockito |
| **Build** | Maven |
| **Email** | Spring Mail, JavaMail |
| **SMS** | Orange API, Twilio, Infobip |

### Structure des Packages

```
sn.sunufarmasi/
├── auth/                    # Authentification & autorisation
│   ├── entity/
│   ├── service/
│   └── controller/
├── localisation/            # Régions, Départements, Communes
├── pharmacie/               # Gestion pharmacies
├── patient/                 # Fichier patient
├── syndicat/                # Syndicat pharmaciens
├── employe/                 # Gestion RH
├── garde/                   # Planning gardes
├── produit/                 # Produits & Stock
│   ├── entity/
│   │   ├── Produit.java
│   │   ├── ProduitPharmacie.java
│   │   └── MouvementStock.java
│   ├── enums/
│   ├── repository/
│   ├── service/
│   └── controller/
├── vente/                   # Point de vente
│   ├── entity/
│   │   ├── Vente.java
│   │   └── LigneVente.java
│   ├── enums/
│   └── ...
├── commande/                # Commandes fournisseurs
│   ├── entity/
│   │   ├── Fournisseur.java
│   │   ├── Commande.java
│   │   └── LigneCommande.java
│   ├── enums/
│   └── ...
├── mutuelle/                # Mutuelles & Tiers payant
│   ├── entity/
│   │   ├── Mutuelle.java
│   │   ├── ContratMutuelle.java
│   │   ├── Adherent.java
│   │   └── DemandeRemboursement.java
│   ├── enums/
│   └── ...
├── dashboard/               # Tableaux de bord
├── notification/            # Notifications multi-canal
├── payment/                 # Paiements Mobile Money
│   └── entity/
│       └── Payment.java
├── subscription/            # Abonnements patients
│   └── entity/
│       ├── Subscription.java
│       └── SubscriptionPlan.java
└── shared/                  # Classes partagées
    ├── dto/
    ├── exception/
    └── config/
```

---

## 📦 Modules Détaillés

### 1. Module Produits & Stock (28 fichiers)

**Entités :**
- `Produit` : Catalogue national des médicaments
- `ProduitPharmacie` : Stock par pharmacie avec prix, seuils
- `MouvementStock` : Traçabilité complète des mouvements

**Enums :**
- `CategorieProduit` : MEDICAMENT, PARAPHARMACIE, COSMETIQUE, DISPOSITIF_MEDICAL...
- `FormeGalenique` : COMPRIME, GELULE, SIROP, INJECTABLE, POMMADE...
- `TypeMouvement` : ENTREE, SORTIE, AJUSTEMENT, VENTE, ACHAT, PEREMPTION...
- `StatutProduit` : ACTIF, INACTIF, RUPTURE, COMMANDE

**Fonctionnalités :**
- Recherche avancée (nom, DCI, code barre)
- Alertes automatiques (rupture, stock faible, péremption)
- Import/export catalogue
- Statistiques rotation

### 2. Module Ventes (17 fichiers)

**Entités :**
- `Vente` : En-tête vente avec totaux et paiement
- `LigneVente` : Détail produits vendus

**Enums :**
- `StatutVente` : EN_COURS, VALIDEE, ANNULEE, REMBOURSEE
- `ModePaiement` : ESPECES, CARTE_BANCAIRE, ORANGE_MONEY, WAVE, FREE_MONEY...
- `TypeVente` : COMPTOIR, ORDONNANCE, MUTUELLE, LIVRAISON

**Fonctionnalités :**
- Décrémentation automatique stock
- Calcul TVA, remises
- Ticket thermique 80mm
- Statistiques vendeur

### 3. Module Commandes (20 fichiers)

**Entités :**
- `Fournisseur` : Grossistes, laboratoires avec conditions
- `Commande` : En-tête avec workflow
- `LigneCommande` : Détail avec réception partielle

**Enums :**
- `TypeFournisseur` : GROSSISTE, LABORATOIRE, IMPORTATEUR, COOPERATIVE
- `StatutCommande` : BROUILLON → EN_ATTENTE → CONFIRMEE → EXPEDIEE → LIVREE

**Fonctionnalités :**
- Workflow complet avec 9 statuts
- Réception partielle
- Création automatique mouvements stock
- Traçabilité lots et péremptions

### 4. Module Mutuelles (23 fichiers)

**Entités :**
- `Mutuelle` : Organismes payeurs avec conditions
- `ContratMutuelle` : Convention pharmacie-mutuelle
- `Adherent` : Bénéficiaires avec droits
- `DemandeRemboursement` : Factures à rembourser

**Enums :**
- `TypeMutuelle` : MUTUELLE_SANTE, IPM, ASSURANCE, CMU, ENTREPRISE...
- `TypeCouverture` : TOTALE, PARTIELLE_80, PARTIELLE_70...
- `StatutDemande` : BROUILLON → SOUMISE → ACCEPTEE → PAYEE

**Fonctionnalités :**
- Calcul automatique prise en charge
- Vérification droits ouverts
- Suivi créances
- Tiers payant

### 5. Module Notifications (14 fichiers)

**Entités :**
- `Notification` : Notification avec statut et canal
- `ConfigNotification` : Préférences utilisateur

**Enums :**
- `TypeNotification` : RUPTURE_STOCK, STOCK_FAIBLE, PEREMPTION, GARDE...
- `CanalNotification` : APP, EMAIL, SMS, WHATSAPP, PUSH
- `StatutNotification` : EN_ATTENTE, ENVOYEE, LUE, ECHOUEE

**Services :**
- `NotificationService` : Orchestration
- `EmailService` : Envoi emails
- `SmsService` : Orange API, Twilio, Infobip

---

## 🚀 Installation

### Prérequis

- Java 17+
- Maven 3.8+
- MySQL 8+ ou PostgreSQL 15+

### Configuration

1. **Cloner le projet**
```bash
git clone https://github.com/votre-repo/sunufarmasi.git
cd sunufarmasi
```

2. **Configurer la base de données**

Créer le fichier `application.yml` :

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sunufarmasi?useSSL=false&serverTimezone=UTC
    username: root
    password: votre_mot_de_passe
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    
  mail:
    host: smtp.gmail.com
    port: 587
    username: votre_email
    password: votre_password
    
# Configuration SMS
sms:
  enabled: true
  provider: ORANGE
  api:
    url: https://api.orange.com/smsmessaging/v1/outbound
    key: votre_api_key
  sender: SunuFarmasi

# JWT
jwt:
  secret: votre_secret_key_tres_long_et_securise
  expiration: 86400000  # 24h
```

3. **Lancer l'application**
```bash
mvn spring-boot:run
```

4. **Accéder à l'API**
- API : http://localhost:8080/api/v1
- Swagger : http://localhost:8080/swagger-ui.html

---

## 📡 API Endpoints

### Authentification
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/auth/login` | Connexion |
| POST | `/api/v1/auth/register` | Inscription |
| POST | `/api/v1/auth/refresh` | Rafraîchir token |

### Produits & Stock
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/produits` | Liste produits |
| GET | `/api/v1/pharmacies/{id}/stock` | Stock pharmacie |
| POST | `/api/v1/pharmacies/{id}/stock/mouvements` | Créer mouvement |
| GET | `/api/v1/pharmacies/{id}/stock/alertes` | Alertes stock |

### Ventes
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/pharmacies/{id}/ventes` | Créer vente |
| GET | `/api/v1/pharmacies/{id}/ventes` | Liste ventes |
| GET | `/api/v1/ventes/{id}/ticket` | Ticket de caisse |

### Commandes
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/pharmacies/{id}/commandes` | Créer commande |
| POST | `/api/v1/commandes/{id}/envoyer` | Envoyer |
| POST | `/api/v1/commandes/{id}/receptionner` | Réceptionner |

### Mutuelles
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/mutuelles` | Liste mutuelles |
| GET | `/api/v1/adherents/numero/{num}` | Rechercher adhérent |
| POST | `/api/v1/pharmacies/{id}/demandes-remboursement` | Créer demande |

### Dashboard
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/pharmacies/{id}/dashboard` | Dashboard complet |
| GET | `/api/v1/pharmacies/{id}/dashboard/jour` | Dashboard jour |
| GET | `/api/v1/pharmacies/{id}/statistiques/ventes` | Stats ventes |

### Notifications
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/notifications` | Mes notifications |
| GET | `/api/v1/notifications/count` | Badge non lues |
| PUT | `/api/v1/notifications/{id}/lue` | Marquer comme lue |

---

## 👥 Rôles & Permissions

| Rôle | Permissions |
|------|-------------|
| `SUPER_ADMIN` | Accès total, gestion multi-pharmacies |
| `ADMIN` | Gestion pharmacie, utilisateurs, configuration |
| `PHARMACIEN` | Toutes opérations métier |
| `VENDEUR` | Ventes, consultation stock |
| `COMPTABLE` | Dashboard, statistiques, rapports |

---

## 🔐 Sécurité

- **Authentification** : JWT avec refresh token
- **Autorisation** : RBAC (Role-Based Access Control)
- **Mots de passe** : BCrypt avec salt
- **API** : Rate limiting, CORS configuré
- **Données** : Chiffrement sensibles (RGPD)

---

## 📱 Applications Clients

| Application | Technologie | Statut |
|-------------|-------------|--------|
| Mobile iOS/Android | Flutter | 🟡 En développement |
| Web Admin | React/Vue | 🟡 En développement |
| POS Desktop | Electron | ⏳ Planifié |

---

## 🛣️ Roadmap

### Version 1.0 (Actuelle)
- [x] Gestion produits & stock
- [x] Point de vente complet
- [x] Commandes fournisseurs
- [x] Mutuelles & tiers payant
- [x] Dashboard & statistiques
- [x] Notifications multi-canal

### Version 1.1 (Prévue)
- [ ] Rapports PDF (ventes, stock, comptabilité)
- [ ] Inventaire avec scanner
- [ ] Import catalogue LNCM
- [ ] Export comptable (SYSCOHADA)

### Version 1.2 (Future)
- [ ] E-commerce B2C
- [ ] Livraison à domicile
- [ ] Ordonnance électronique
- [ ] Intégration CNAM

---

## 📄 Licence

Propriétaire - Tous droits réservés © 2024

---

## 👨‍💻 Auteur

**WeCan** - Full Stack Developer & DevOps  
📍 Dakar, Sénégal  
🎓 Master Cybersécurité - UCAD

---

<div align="center">

**🇸🇳 Développé avec ❤️ pour les pharmacies sénégalaises**

</div>
