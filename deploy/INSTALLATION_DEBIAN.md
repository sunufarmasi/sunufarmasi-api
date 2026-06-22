# Installation SunuFarmasi API sur Debian

## Prérequis serveur Debian 11/12

### 1. Mettre à jour le système
```bash
sudo apt update && sudo apt upgrade -y
```

### 2. Installer Java 21
```bash
sudo apt install -y wget gnupg
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-21-jdk
java -version   # Vérifier
```

### 3. Installer PostgreSQL 16
```bash
sudo apt install -y postgresql postgresql-contrib
sudo systemctl enable postgresql
sudo systemctl start postgresql
```

Créer la base de données et l'utilisateur :
```bash
sudo -u postgres psql << 'EOF'
CREATE USER sunufarmasi WITH PASSWORD 'VOTRE_MOT_DE_PASSE_DB';
CREATE DATABASE sunufarmasi OWNER sunufarmasi;
GRANT ALL PRIVILEGES ON DATABASE sunufarmasi TO sunufarmasi;
\q
EOF
```

### 4. Installer Redis
```bash
sudo apt install -y redis-server
# Configurer un mot de passe Redis
sudo nano /etc/redis/redis.conf
# Trouver la ligne "# requirepass foobared" et remplacer par :
# requirepass VOTRE_MOT_DE_PASSE_REDIS
sudo systemctl enable redis-server
sudo systemctl restart redis-server
```

### 5. Installer Nginx
```bash
sudo apt install -y nginx certbot python3-certbot-nginx
sudo systemctl enable nginx
```

---

## Préparer le répertoire applicatif

```bash
# Créer l'utilisateur système (sans login shell)
sudo useradd -r -m -d /opt/sunufarmasi -s /bin/false sunufarmasi

# Créer les répertoires
sudo mkdir -p /opt/sunufarmasi
sudo mkdir -p /var/log/sunufarmasi
sudo chown -R sunufarmasi:sunufarmasi /opt/sunufarmasi /var/log/sunufarmasi
```

---

## Configurer les variables d'environnement

```bash
# Copier le fichier .env.example et le remplir
sudo cp .env.example /opt/sunufarmasi/.env
sudo nano /opt/sunufarmasi/.env
# Remplir toutes les valeurs (DB_PASSWORD, JWT_SECRET, etc.)

# Sécuriser les permissions (lecture uniquement par sunufarmasi)
sudo chown sunufarmasi:sunufarmasi /opt/sunufarmasi/.env
sudo chmod 600 /opt/sunufarmasi/.env

# Générer le JWT_SECRET :
openssl rand -base64 64
```

---

## Déployer le JAR

```bash
# Sur votre machine locale, build :
./mvnw clean package -DskipTests

# Copier vers le serveur
scp target/sunufarmasi-api-*.jar user@VOTRE_IP:/opt/sunufarmasi/sunufarmasi-api.jar

# Sur le serveur
sudo chown sunufarmasi:sunufarmasi /opt/sunufarmasi/sunufarmasi-api.jar
sudo chmod 750 /opt/sunufarmasi/sunufarmasi-api.jar
```

---

## Installer le service systemd

```bash
# Copier le fichier service
sudo cp sunufarmasi-api.service /etc/systemd/system/

# Activer et démarrer
sudo systemctl daemon-reload
sudo systemctl enable sunufarmasi-api
sudo systemctl start sunufarmasi-api

# Vérifier le statut
sudo systemctl status sunufarmasi-api

# Voir les logs
sudo journalctl -u sunufarmasi-api -f
```

---

## Configurer Nginx

```bash
# Copier la config Nginx
sudo cp nginx/sunufarmasi.conf /etc/nginx/sites-available/sunufarmasi
sudo ln -s /etc/nginx/sites-available/sunufarmasi /etc/nginx/sites-enabled/

# Obtenir un certificat SSL (remplacer api.sunufarmasi.sn par votre domaine ou IP)
sudo certbot --nginx -d api.sunufarmasi.sn

# Tester la config Nginx
sudo nginx -t

# Recharger Nginx
sudo systemctl reload nginx
```

> **Note**: Si vous n'avez pas de domaine, vous pouvez utiliser l'IP directement en HTTP pour commencer,
> ou utiliser un certificat auto-signé. Modifier le fichier nginx/sunufarmasi.conf en conséquence.

---

## Initialisation des données (1ère fois seulement)

```bash
# 1. Modifier .env temporairement pour activer l'init
sudo nano /opt/sunufarmasi/.env
# Changer : APP_INIT_ENABLED=true

# 2. Redémarrer le service
sudo systemctl restart sunufarmasi-api

# 3. Appeler l'endpoint d'init (depuis votre machine locale)
curl -X POST https://api.sunufarmasi.sn/api/v1/init/seed-all

# 4. Après init réussie, REMETTRE à false IMMÉDIATEMENT
sudo nano /opt/sunufarmasi/.env
# Changer : APP_INIT_ENABLED=false

# 5. Redémarrer
sudo systemctl restart sunufarmasi-api
```

### Endpoints d'initialisation disponibles :
- `POST /api/v1/init/seed-all` → Tout initialiser (régions, pharmacies, users, etc.)
- `POST /api/v1/init/seed-localisation` → Seulement les régions/départements/communes du Sénégal
- `POST /api/v1/init/seed-users` → Créer les users admin/pharmacien de base
- `POST /api/v1/init/seed-pharmacies` → Créer les pharmacies de demo

---

## Vérification finale

```bash
# Santé de l'API
curl http://localhost:8080/actuator/health

# Via Nginx (HTTPS)
curl https://api.sunufarmasi.sn/actuator/health

# Régions du Sénégal (test endpoint public)
curl https://api.sunufarmasi.sn/api/v1/public/regions
```

---

## Commandes utiles

```bash
# Voir les logs en temps réel
sudo journalctl -u sunufarmasi-api -f

# Redémarrer l'API
sudo systemctl restart sunufarmasi-api

# Voir les dernières erreurs
sudo journalctl -u sunufarmasi-api -n 50 --no-pager

# Vérifier la connexion PostgreSQL
sudo -u sunufarmasi psql -h localhost -U sunufarmasi -d sunufarmasi -c "\dt"
```
