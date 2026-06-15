# =============================================================================
# ArcadeHaven - self-signed TLS certificate generator (dev / CI only)
#
# ASVS V12.2.2: production deployments MUST replace this certificate with one
# issued by a publicly trusted CA (e.g. Lets Encrypt, ISEP institutional CA).
#
# Usage (from the Api/ directory):
#   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
#   .\nginx\generate-self-signed-cert.ps1
#
# The generated files (server.crt, server.key) are excluded from git via
# .gitignore so secrets are never committed to the repository.
# =============================================================================

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$CertDir   = Join-Path $ScriptDir 'certs'

# Create the certs directory if it does not exist
if (-not (Test-Path $CertDir)) {
    New-Item -ItemType Directory -Path $CertDir | Out-Null
}

# Check if openssl is available
if (-not (Get-Command openssl -ErrorAction SilentlyContinue)) {
    Write-Error 'openssl not found in PATH.'
    Write-Error 'Install OpenSSL for Windows, for example:'
    Write-Error '  - Git for Windows (includes OpenSSL): https://gitforwindows.org/'
    Write-Error '  - Win64 OpenSSL: https://slproweb.com/products/Win32OpenSSL.html'
    exit 1
}

$KeyFile  = Join-Path $CertDir 'server.key'
$CertFile = Join-Path $CertDir 'server.crt'

# Write a minimal openssl.cnf to a temp file.
# Needed on Windows when the OpenSSL binary cannot find its default config
# (e.g. portable installs where the original config path no longer exists).
$TempConf = Join-Path $env:TEMP 'arcadehaven-openssl.cnf'
@(
    '[req]',
    'distinguished_name = req_dn',
    'prompt = no',
    '[req_dn]'
) | Set-Content -Path $TempConf -Encoding ASCII
$env:OPENSSL_CONF = $TempConf

# Generate the self-signed certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 `
    -keyout "$KeyFile" `
    -out    "$CertFile" `
    -subj   '/C=PT/ST=Porto/L=Porto/O=ArcadeHaven/OU=Dev/CN=arcadehaven.local'

$exitCode = $LASTEXITCODE
Remove-Item $TempConf -ErrorAction SilentlyContinue

if ($exitCode -ne 0) {
    Write-Error 'Certificate generation failed.'
    exit 1
}

# Restrict server.key permissions to the current user only (equivalent to chmod 600)
# Use the fully-qualified identity (DOMAIN\user or COMPUTERNAME\user) so the ACL
# lookup succeeds on both domain-joined and local-only machines.
$currentUser = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
$Acl = Get-Acl "$KeyFile"
$Acl.SetAccessRuleProtection($true, $false)
$Rule = New-Object System.Security.AccessControl.FileSystemAccessRule(
    $currentUser, 'FullControl', 'Allow'
)
$Acl.SetAccessRule($Rule)
Set-Acl -Path "$KeyFile" -AclObject $Acl

Write-Host ''
Write-Host 'Self-signed certificate generated:'
Write-Host "  $CertFile"
Write-Host "  $KeyFile"
Write-Host ''
Write-Host 'IMPORTANT - For production, replace these files with a certificate'
Write-Host 'issued by Lets Encrypt or an institutional CA (ASVS V12.2.2).'
