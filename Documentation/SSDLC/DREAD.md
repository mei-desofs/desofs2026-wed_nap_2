# DREAD Analysis for ArcadeHaven

| **Threat** | **Damage** | **Reproducibility** | **Exploitability** | **Affected Users** | **Discoverability** | **Total** | **Justification** |
|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| **Spoofing** | 8 | 7 | 8 | 9 | 7 | 7.8 | Identity attacks (JWT issues, API impersonation, credentials attack) can lead to account takeover. Higly impactful and scalable but not all vectors are easy to detect or reproduce |
| **Tampering** | 9 | 8 | 8 | 9 | 8 | 8.4 | Critical due to SQL injection, file manipulation and manipulation of financial and game-related data. Higly reproducible and directly impact system integrity, financial and game data |
| **Repudiation** | 6 | 7 | 6 | 7 | 6 | 6.4 | Medium severity since it affects auditability rather than direct data integrity. Exploitation depends on missing logs and weak traceability, making attribution of action difficult but not system breaking |
| **Information Disclosure** | 8 | 9 | 8 | 9 | 9 | 8.6 | Extremely critical due to exposure of API keys, tokens, credentials and internal data. Many leaks are easily discoverable and highly reproducible |
| **Denial of Service** | 7 | 8 | 7 | 8 | 8 | 7.6 | System availability can be disrupted via unbounded queries, rate limit abuse or resource exhaustion. Attacks are easy to reproduce and affects all users but not compromise data integrity |
| **Elevation of Privilege** | 9 | 7 | 7 | 9 | 7 | 7.8 | Missing authorization checks allow privilege escalation. High impact due to full system control risk, though exploitation requires identifying specific authorization gaps |

1. **Spoofing:** Pretending to be someone or something
    - Number 72: User identity impersonation at system boundary
    - Number 9: JWT not validated on every route
    - Number 74: Rogue Auth API impersonation 
    - Number 76: Spoofed RAWG endpoint (DNS/MitM)
    - Number 15: Credential brute-force/credential stuffing
    - Number 21: Publisher identity spoofing
    - Number 33: Accessing another user's library
    - Number 47: User identity impersonation
    - Number 49: Rogue/spoofed RAWG API endpoint
    - Number 231: Publisher ID not bound from JWT on game create
    - Number 237: RAWG API key substitution in handler config.

2. **Tampering:** Malicious modification of data, code or configuration
    - Number 8: Request body manipulation/mass assignment
    - Number 78: Unauthorised direct DB writes
    - Number 82: Arbitrary file write/path traversal
    - Number 86: MitM-request body tampering
    - Number 89: JWT payload tampering in transit
    - Number 63: SQL injection
    - Number 93: SQL injection via Flow payload
    - Number 96: Path traversal in file read/write path
    - Number 99: Malicious content in RAWG response persisted to DB (Stored XSS)
    - Number 16: JWT claims manipulation 
    - Number 22: Malicious file upload
    - Number 34: Injecting games into another user's library
    - Number 43: Path traversal / arbitrary file write
    - Number 60: MitM — request/response body modification
    - Number 66: Malicious content in RAWG response stored to DB
    - Number 69: Invoice PDF content manipulation in transit
    - Number 170: SQL injection via game search / filter parameters
    - Number 173: Game status field written directly to ACTIVE without approval
    - Number 176: Library entry written without a corresponding completed order
    - Number 185: Price written to order from client-supplied value
    - Number 232: Game price accepts negative or zero values.

3. **Repudiation:** User denies performing an action
    - Number 73: Denial of system-level actions
    - Number 10: Insufficient request logging
    - Number 75: Auth events not attributed to ArcadeHaven context
    - Number 77: No record of what external data was consumed
    - Number 79: No DB-level change attribution
    - Number 83: No file access/modification log
    - Number 48: Denial of performed actions
    - Number 17: No authentication event audit trail
    - Number 23: Unsigned game status changes
    - Number 35: Untracked profile changes
    - Number 50: No accountability for bad external data
    - Number 202: No version history of metadata updates
    - Number 233: No before/after values logged on game update.

4. **Information Disclosure:** Unauthorized access to sensitive data
    - Number 12: Verbose error messages expose internals
    - Number 80: Unencrypted sensitive data at rest
    - Number 84: Files served without auth check
    - Number 87: Sensitive data in cleartext responses
    - Number 91: Token interception/exposure
    - Number 64: Credentials in DB connection string exposed
    - Number 94: DB credentials exposed in config/logs
    - Number 97: Unauthenticated file access
    - Number 100: RAWG API key exposed
    - Number 18: Token leakage via insecure storage or logging
    - Number 61: Sensitive data in HTTP responses over unencrypted channel
    - Number 67: RAWG API key exposed in logs or source
    - Number 70: Game image/invoice served without access control
    - Number 171: Read flow returns PENDING / DRAFT games to public callers
    - Number 174: Read response includes internal only fields (cost price)
    - Number 177: Activation keys returned in plaintext in datastore response
    - Number 229: Game Details Response cached insecurely.

5. **Denial of Service:** Disrupting or degrading service availability
    - Number 13: No rate limiting on public endpoints
    - Number 81: DB overload from unbounded queries
    - Number 85: Disk exhaustion via upload abuse
    - Number 88: HTTP Flood/slow-loris
    - Number 92: JWKS endpoint unavailability
    - Number 65: Unbounded query results exhausting memory (OOM no Spring Boot)
    - Number 98: Concurrent large file reads exhausting I/O
    - Number 101: RAWG rate limit exhaustion
    - Number 62: HTTP flood / slow-loris attack
    - Number 203: Metadata refresh loop causing repeated RAWG calls
    - Number 254: Many large concurrent image downloads exhausting I/O bandwidth.

6. **Elevation of Privilege:** Attacker gains higher-level access than intended
    - Number 14: Missing role checks on admin routes
    - Number 26: Publisher self approving games 
    - Number 204: Publisher overwriting admin-curated metadata fields.