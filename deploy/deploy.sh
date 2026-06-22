#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════
# deploy.sh - Déployer SunuFarmasi API sur serveur Debian
# Usage : ./deploy.sh
# Prérequis : SSH configuré, Java 21 + PostgreSQL + Redis installés sur le serveur
# ═══════════════════════════════════════════════════════════════════════════════

set -e  # Arrêter en cas d'erreur

# ── CONFIGURATION ─────────────────────────────────────────────────────────────
# Remplacer avec l'IP ou domaine de votre serveur Debian
SERVER_HOST="VOTRE_IP_SERVEUR"
SERVER_USER="sunufarmasi"
SERVER_DIR="/opt/sunufarmasi"
JAR_NAME="sunufarmasi-api.jar"
SERVICE_NAME="sunufarmasi-api"

# ── COULEURS ──────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log() { echo -e "${GREEN}[DEPLOY]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ── 1. BUILD ──────────────────────────────────────────────────────────────────
log "Build du projet Maven (skip tests)..."
./mvnw clean package -DskipTests -q

JAR_PATH=$(find target -name "*.jar" ! -name "*sources*" | head -1)
if [ -z "$JAR_PATH" ]; then
    error "JAR introuvable dans target/"
fi
log "JAR généré : $JAR_PATH"

# ── 2. COPIER LE JAR ──────────────────────────────────────────────────────────
log "Copie du JAR vers le serveur $SERVER_HOST..."
scp "$JAR_PATH" "${SERVER_USER}@${SERVER_HOST}:${SERVER_DIR}/${JAR_NAME}.new"

# ── 3. REDÉMARRAGE SUR LE SERVEUR ─────────────────────────────────────────────
log "Redémarrage du service sur le serveur..."
ssh "${SERVER_USER}@${SERVER_HOST}" bash << EOF
    set -e
    # Remplacer l'ancien JAR
    mv ${SERVER_DIR}/${JAR_NAME}.new ${SERVER_DIR}/${JAR_NAME}
    chmod 750 ${SERVER_DIR}/${JAR_NAME}

    # Redémarrer le service
    sudo systemctl restart ${SERVICE_NAME}
    sleep 5

    # Vérifier le statut
    if sudo systemctl is-active --quiet ${SERVICE_NAME}; then
        echo "Service démarré avec succès"
    else
        echo "ERREUR: Le service n'a pas démarré"
        sudo journalctl -u ${SERVICE_NAME} -n 30 --no-pager
        exit 1
    fi
EOF

log "Déploiement terminé avec succès !"
log "Vérification santé : https://${SERVER_HOST}/actuator/health"
