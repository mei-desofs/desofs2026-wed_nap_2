#!/bin/sh
# RNF-27 — Daily PostgreSQL backup for ArcadeHaven.
# Backs up both the remote application database and the local Keycloak database.
# Retains backups for RETENTION_DAYS days (default: 7).
#
# Environment variables expected (injected via docker-compose):
#   REMOTE_HOST         — PostgreSQL host for the application DB
#   REMOTE_PORT         — PostgreSQL port for the application DB
#   REMOTE_USER         — PostgreSQL username for the application DB
#   REMOTE_DB_PASSWORD  — PostgreSQL password for the application DB
#   REMOTE_DB           — Database name for the application DB (default: postgres)
#   KC_DB_PASSWORD      — PostgreSQL password for the Keycloak DB

set -e

DATE=$(date +%Y-%m-%d_%H-%M-%S)
BACKUP_DIR="/backups"
RETENTION_DAYS="${RETENTION_DAYS:-7}"
REMOTE_DB="${REMOTE_DB:-postgres}"

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"; }

log "========== ArcadeHaven backup started =========="

# ── 1. Remote application database ────────────────────────────────────────────
log "Backing up application DB: ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PORT}/${REMOTE_DB}"
PGPASSWORD="$REMOTE_DB_PASSWORD" pg_dump \
    -h "$REMOTE_HOST" \
    -p "$REMOTE_PORT" \
    -U "$REMOTE_USER" \
    -d "$REMOTE_DB" \
    --format=custom \
    --no-password \
    -f "${BACKUP_DIR}/arcadehaven_${DATE}.dump"
log "Application DB backup: arcadehaven_${DATE}.dump ($(du -sh "${BACKUP_DIR}/arcadehaven_${DATE}.dump" | cut -f1))"

# ── 2. Local Keycloak database ────────────────────────────────────────────────
log "Backing up Keycloak DB: keycloak@keycloak-db:5432/keycloak"
PGPASSWORD="$KC_DB_PASSWORD" pg_dump \
    -h keycloak-db \
    -p 5432 \
    -U keycloak \
    -d keycloak \
    --format=custom \
    --no-password \
    -f "${BACKUP_DIR}/keycloak_${DATE}.dump"
log "Keycloak DB backup: keycloak_${DATE}.dump ($(du -sh "${BACKUP_DIR}/keycloak_${DATE}.dump" | cut -f1))"

# ── 3. Retain only the last RETENTION_DAYS days ────────────────────────────────
log "Removing backups older than ${RETENTION_DAYS} days..."
find "$BACKUP_DIR" -name "*.dump" -mtime +"$RETENTION_DAYS" -delete
log "Cleanup complete."

log "========== Backup finished successfully =========="
