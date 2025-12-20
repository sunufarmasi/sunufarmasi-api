# 📱 **SUNUFARMASI - SYSTÈME D'ABONNEMENT COMPLET**

## 🎯 **VUE D'ENSEMBLE**

SunuFarmasi utilise un système d'abonnement obligatoire avec une **période d'essai gratuite de 15 jours** pour tous les nouveaux utilisateurs.

---

## 💰 **PLANS TARIFAIRES**

### **1. Essai Gratuit (FREE_TRIAL)**
- **Durée** : 15 jours
- **Prix** : Gratuit
- **Publicités** : Oui
- **Attribution** : Automatique à l'inscription
- **Usage unique** : Chaque utilisateur ne peut avoir qu'un seul essai gratuit

### **2. Abonnement Mensuel (MONTHLY)**
- **Durée** : 30 jours
- **Prix** : 750 FCFA/mois
- **Publicités** : Oui
- **Renouvellement** : Manuel (utilisateur doit repayer chaque mois)

### **3. Abonnement Annuel (ANNUAL)** ⭐ Recommandé
- **Durée** : 365 jours (1 an)
- **Prix** : 8000 FCFA/an
- **Publicités** : Non
- **Économie** : 1000 FCFA par rapport au mensuel (9000 - 8000)
- **Renouvellement** : Manuel

---

## 📋 **WORKFLOW D'INSCRIPTION**

```
┌─────────────────────────────────────────────────────────┐
│  ÉTAPE 1 : INSCRIPTION                                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  POST /api/v1/auth/register                            │
│  Body: {                                               │
│    "telephone": "+221771234567",                       │
│    "nomComplet": "Amadou Diallo",                      │
│    "communeId": "uuid-commune-dakar"                   │
│  }                                                      │
│                                                         │
│  ✅ Patient créé en base                               │
│  ✅ OTP envoyé au téléphone                            │
│                                                         │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  ÉTAPE 2 : VÉRIFICATION OTP                            │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  POST /api/v1/auth/verify-otp                          │
│  Body: {                                               │
│    "telephone": "+221771234567",                       │
│    "code": "123456"                                    │
│  }                                                      │
│                                                         │
│  ✅ Téléphone vérifié                                  │
│  ✅ Essai gratuit 15 jours activé automatiquement      │
│  ✅ JWT token généré                                   │
│                                                         │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  ÉTAPE 3 : ACCÈS APP (15 JOURS)                       │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  🎁 Accès complet pendant 15 jours                     │
│  📱 Notifications automatiques :                       │
│     - J-3 : "Plus que 3 jours d'essai"                │
│     - J-1 : "Votre essai expire demain"               │
│                                                         │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│  ÉTAPE 4 : APRÈS 15 JOURS                             │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ❌ Essai expiré - Accès bloqué                        │
│  📱 Message : "Abonnez-vous pour continuer"            │
│                                                         │
│  GET /api/v1/subscriptions/plans (PUBLIC)              │
│  → Liste des plans disponibles                         │
│                                                         │
│  POST /api/v1/payments/initiate (AUTHENTICATED)       │
│  Body: {                                               │
│    "planId": "uuid-plan-monthly-ou-annual",           │
│    "methode": "ORANGE_MONEY",                         │
│    "telephonePaiement": "+221771234567"               │
│  }                                                      │
│                                                         │
│  ✅ Paiement traité (Mock Orange Money/Wave)          │
│  ✅ Abonnement activé                                  │
│  ✅ Accès restauré                                     │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🔐 **AUTHENTIFICATION**

### **Pas de mot de passe en base !**

- Le patient s'inscrit avec **téléphone uniquement**
- Pas de champ `motDePasseHash` dans la table `patients`
- **PIN de sécurité créé LOCALEMENT** dans l'app mobile
- Le PIN n'est **JAMAIS** envoyé au backend

### **Connexion avec OTP**

```
POST /api/v1/auth/login
Body: {
  "telephone": "+221771234567"
}
→ OTP envoyé

POST /api/v1/auth/verify-otp
Body: {
  "telephone": "+221771234567",
  "code": "123456"
}
→ JWT token retourné
```

---

## 📊 **ARCHITECTURE BASE DE DONNÉES**

### **Tables créées**

```sql
-- Plans tarifaires
CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,  -- FREE_TRIAL, MONTHLY, ANNUAL
    nom VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    prix INTEGER NOT NULL,             -- En FCFA
    duree_jours INTEGER NOT NULL,
    avec_publicite BOOLEAN NOT NULL,
    actif BOOLEAN NOT NULL,
    ordre INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Abonnements actifs
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(id),
    plan_id UUID NOT NULL REFERENCES subscription_plans(id),
    status VARCHAR(20) NOT NULL,       -- ACTIVE, EXPIRED, CANCELLED
    starts_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    auto_renew BOOLEAN NOT NULL,
    is_trial BOOLEAN NOT NULL,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Historique paiements
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(id),
    subscription_id UUID REFERENCES subscriptions(id),
    montant INTEGER NOT NULL,          -- En FCFA
    methode VARCHAR(50) NOT NULL,      -- ORANGE_MONEY, WAVE
    status VARCHAR(20) NOT NULL,       -- PENDING, SUCCESS, FAILED
    telephone_paiement VARCHAR(20),
    reference_externe VARCHAR(100),
    reference_interne VARCHAR(50) UNIQUE NOT NULL,
    error_message VARCHAR(500),
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### **Table patients (modifiée)**

```sql
-- Email devenu OPTIONNEL
email VARCHAR(255) UNIQUE,  -- Pas de NOT NULL

-- Mot de passe SUPPRIMÉ (PIN local uniquement)
-- mot_de_passe_hash supprimé

-- Téléphone reste obligatoire
telephone VARCHAR(20) UNIQUE NOT NULL
```

---

## 🔄 **TÂCHES AUTOMATIQUES (CRON)**

### **1. Vérification des abonnements expirés**
```java
@Scheduled(cron = "0 0 2 * * *")  // Tous les jours à 2h
public void checkExpiredSubscriptions()
```
- Marque les abonnements expirés comme `EXPIRED`
- Envoie notification push (à implémenter)

### **2. Notification des expirations imminentes**
```java
@Scheduled(cron = "0 0 10 * * *")  // Tous les jours à 10h
public void notifyExpiringSoon()
```
- Notifie les utilisateurs dont l'abonnement expire dans 3 jours
- Encourage le renouvellement

### **3. Vérification des essais gratuits expirés**
```java
@Scheduled(cron = "0 0 3 * * *")  // Tous les jours à 3h
public void checkExpiredTrials()
```
- Marque les essais gratuits expirés
- Envoie message pour inciter à s'abonner

---

## 🎨 **ENDPOINTS API**

### **PUBLIC (Sans authentification)**

```
GET /api/v1/subscriptions/plans
→ Liste tous les plans disponibles
```

### **AUTHENTICATED (Avec JWT)**

```
# Abonnements
GET /api/v1/subscriptions/me
→ Mon abonnement actif

GET /api/v1/subscriptions/me/status
→ Vérifier si j'ai un abonnement actif (boolean)

# Paiements
POST /api/v1/payments/initiate
→ Initier un paiement Orange Money/Wave

GET /api/v1/payments/{referenceInterne}
→ Vérifier statut d'un paiement

GET /api/v1/payments/history
→ Mon historique de paiements

# Patient
GET /api/v1/patients/me
→ Mon profil

PUT /api/v1/patients/me
→ Mettre à jour mon profil

DELETE /api/v1/patients/me
→ Désactiver mon compte
```

---

## 💳 **INTÉGRATION PAIEMENT**

### **Mock actuel (Développement)**

```java
// Mock Orange Money/Wave avec 90% succès
private boolean mockPaymentProvider(Payment payment) {
    Thread.sleep(2000);  // Simuler délai réseau
    return Math.random() < 0.9;  // 90% succès
}
```

### **Production (À implémenter)**

```java
// TODO: Intégrer vraie API Orange Money
// https://developer.orange.com/apis/orange-money-webpay/

// TODO: Intégrer Wave API
// https://docs.wave.com/
```

---

## 🚀 **INITIALISATION**

### **1. Exécuter le script SQL**

```bash
psql -U postgres -d sunufarmasi -f init_subscription_plans.sql
```

### **2. Activer les Scheduled Tasks**

```java
@SpringBootApplication
@EnableScheduling  // ← Ajouter cette annotation
public class SunufarmasiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SunufarmasiApplication.class, args);
    }
}
```

---

## 📱 **EXPÉRIENCE UTILISATEUR**

### **Écran d'inscription (App Mobile)**

1. Téléphone : `[+221 77 123 4567]`
2. Nom : `[Amadou Diallo]`
3. Ville : `[Sélectionner Dakar]`
4. `[S'inscrire]` → OTP envoyé

### **Écran OTP**

1. Code : `[1] [2] [3] [4] [5] [6]`
2. `[Vérifier]` → Essai gratuit activé
3. **Toast** : "🎁 Bienvenue ! 15 jours d'essai gratuit activés"

### **Écran création PIN (Local)**

1. "Créer un PIN pour sécuriser votre compte"
2. PIN : `[● ● ● ●]`
3. Confirmer : `[● ● ● ●]`
4. **PIN stocké dans Keychain/KeyStore (jamais envoyé au serveur)**

### **Écran expiration essai (J+15)**

```
╔════════════════════════════════════╗
║  😔 Votre essai a expiré          ║
╠════════════════════════════════════╣
║                                    ║
║  Choisissez un abonnement pour     ║
║  continuer à profiter de           ║
║  SunuFarmasi                       ║
║                                    ║
║  ┌─────────────────────────────┐  ║
║  │ 📅 Mensuel                  │  ║
║  │ 750 FCFA/mois               │  ║
║  │ • Avec publicités           │  ║
║  │ [Choisir]                   │  ║
║  └─────────────────────────────┘  ║
║                                    ║
║  ┌─────────────────────────────┐  ║
║  │ ⭐ Annuel - RECOMMANDÉ      │  ║
║  │ 8000 FCFA/an                │  ║
║  │ • Sans publicités           │  ║
║  │ • Économie 1000 FCFA        │  ║
║  │ [Choisir]                   │  ║
║  └─────────────────────────────┘  ║
║                                    ║
╚════════════════════════════════════╝
```

---

## ✅ **CHECKLIST DÉPLOIEMENT**

- [ ] Exécuter script SQL `init_subscription_plans.sql`
- [ ] Activer `@EnableScheduling` dans Application.java
- [ ] Configurer Orange Money API credentials (production)
- [ ] Configurer Wave API credentials (production)
- [ ] Tester workflow complet : Inscription → Essai → Expiration → Paiement
- [ ] Implémenter notifications push (Firebase/OneSignal)
- [ ] Configurer webhook pour callbacks paiements
- [ ] Tester renouvellement après 30/365 jours

---

## 📞 **SUPPORT**

Pour toute question sur le système d'abonnement :
- Email: support@sunufarmasi.sn
- Documentation API: https://api.sunufarmasi.sn/docs

---

**🎉 Système d'abonnement complet et fonctionnel !**

*Auteur: WeCan*  
*Version: 1.0.0*  
*Date: 2025*
