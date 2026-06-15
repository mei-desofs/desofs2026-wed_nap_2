#!/bin/sh
# =============================================================================
# ArcadeHaven — self-signed TLS certificate generator (dev / CI only)
#
# ASVS V12.2.2: production deployments MUST replace this certificate with one
# issued by a publicly trusted CA (e.g. Let's Encrypt, ISEP institutional CA).
#
# Usage (from the Api/ directory):
#   chmod +x nginx/generate-self-signed-cert.sh
#   ./nginx/generate-self-signed-cert.sh
#
# The generated files (server.crt, server.key) are excluded from git via
# .gitignore so secrets are never committed to the repository.
# =============================================================================

CERT_DIR="$(cd "$(dirname "$0")" && pwd)/certs"
mkdir -p "$CERT_DIR"

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$CERT_DIR/server.key" \
  -out    "$CERT_DIR/server.crt" \
  -subj   "/C=PT/ST=Porto/L=Porto/O=ArcadeHaven/OU=Dev/CN=arcadehaven.local"

chmod 600 "$CERT_DIR/server.key"
chmod 644 "$CERT_DIR/server.crt"

echo ""
echo "Self-signed certificate generated:"
echo "  $CERT_DIR/server.crt"
echo "  $CERT_DIR/server.key"
echo ""
echo "IMPORTANT — For production, replace these files with a certificate"
echo "issued by Let's Encrypt or an institutional CA (ASVS V12.2.2)."
