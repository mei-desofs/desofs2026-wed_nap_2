# Authentication — ASVS Security Requirements

## V6.1 — Authentication Documentation

### ASVS Requirement V6.1.1 — Planned

**ASVS Requirement Description**: Verify that application documentation defines how controls such as rate limiting, anti-automation, and adaptive response are used to defend against attacks.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because ArcadeHaven exposes a public login endpoint (`POST /api/auth/login`) that is susceptible to brute-force and credential stuffing attacks. Documenting and enforcing rate limiting and anti-automation controls is essential to protect user accounts. Spring Security provides built-in support for these controls, which will be configured and documented as part of the security architecture.

**Security Requirement**: Authentication Controls Documentation — The application must document how rate limiting, anti-automation, and adaptive response mechanisms are applied to authentication endpoints, particularly the login and registration flows.

---

### ASVS Requirement V6.1.2 — Planned

**ASVS Requirement Description**: Verify that a list of context-specific words is documented in order to prevent their use in passwords. The list could include permutations of the application name and username.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because users may use easily guessable passwords related to the application name or their username, significantly reducing password strength. For ArcadeHaven, common context-specific words such as "ArcadeHaven", "pixel", "vault", "admin", "games", and username variations will be defined and enforced during password validation.

**Security Requirement**: Context-Specific Password Blocklist — The application must maintain and enforce a list of context-specific words (e.g. "ArcadeHaven", "pixel", "vault", "admin") that cannot be used as passwords or as part of passwords.

---

### ASVS Requirement V6.1.3 — Planned

**ASVS Requirement Description**: Verify that, if the application includes multiple authentication pathways, these are all documented together with the security controls and authentication strength.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant to ensure that all authentication flows are documented and protected consistently. ArcadeHaven uses a single authentication pathway via JWT tokens issued at `POST /api/auth/login`. This single pathway simplifies compliance, but it must be fully documented alongside its security controls (token expiration, signing algorithm, refresh strategy).

**Security Requirement**: Authentication Pathway Documentation — All authentication pathways must be documented together with their associated security controls. ArcadeHaven's JWT-based authentication flow must include documentation of token issuance, validation, expiration, and revocation mechanisms.

---

## V6.2 — Password Security

### ASVS Requirement V6.2.1 — Planned

**ASVS Requirement Description**: Verify that user set passwords are at least 8 characters in length although a minimum of 15 characters is strongly recommended.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because short passwords are significantly more vulnerable to brute-force attacks. ArcadeHaven will enforce a minimum password length of 12 characters, exceeding the baseline of 8 and aligning closer to the recommended 15, striking a balance between usability and security. This will be validated server-side via a `@Size(min=12)` constraint on the registration DTO.

**Security Requirement**: Minimum Password Length — The application must enforce a minimum password length of 12 characters for all user-set passwords, validated server-side on registration and password change.

---

### ASVS Requirement V6.2.2 — Planned

**ASVS Requirement Description**: Verify that users can change their password.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because it allows users to respond quickly if they believe their credentials have been compromised. ArcadeHaven will provide a dedicated endpoint (`PUT /api/users/me/password`) accessible to all authenticated users regardless of role (ADMIN, PUBLISHER, BUYER), ensuring this capability is universally available.

**Security Requirement**: Password Change Capability — The application must provide all authenticated users with the ability to change their own password through a dedicated and secured endpoint.

---

### ASVS Requirement V6.2.3 — Planned

**ASVS Requirement Description**: Verify that password change functionality requires the user's current and new password.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because requiring the current password during a password change prevents an attacker from changing credentials if they gain temporary access to an active session. ArcadeHaven's password change endpoint will require both `currentPassword` and `newPassword` fields in the request body, verified server-side before any update is persisted.

**Security Requirement**: Current Password Verification on Change — The password change endpoint must require the user's current password and new password. The current password must be verified before the change is applied.

---

### ASVS Requirement V6.2.4 — Planned

**ASVS Requirement Description**: Verify that passwords submitted during account registration or password change are checked against an available set of, at least, the top 3000 most common passwords.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because commonly used passwords are the primary target in dictionary and credential stuffing attacks. ArcadeHaven will integrate a local blocklist of at least the top 3000 most common passwords, validated during both registration and password change flows. Integration with the HaveIBeenPwned API (k-anonymity model) is also planned to extend this check to known breached passwords without exposing the plain-text password.

**Security Requirement**: Common Password Blocklist — The application must check all passwords submitted during registration or password change against a blocklist of at least the top 3000 most common passwords. Passwords found on the blocklist must be rejected with an appropriate error message.

---

### ASVS Requirement V6.2.5 — Planned

**ASVS Requirement Description**: Verify that passwords of any composition can be used, without rules limiting the type of characters permitted. There must be no requirement for upper or lower case or numbers or special characters.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because composition rules do not significantly improve security and often lead to predictable patterns (e.g. "Password1!"). ArcadeHaven will not enforce any character composition rules, only a minimum length requirement, in alignment with NIST SP 800-63B guidelines.

**Security Requirement**: No Password Composition Rules — The application must not impose character composition rules on passwords. No requirement for uppercase, lowercase, numbers, or special characters shall be enforced. Only minimum length will be validated.

---

### ASVS Requirement V6.2.6 — N/A

**ASVS Requirement Description**: Verify that password input fields use type=password to mask the entry. Applications may allow the user to temporarily view the entire masked password.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement applies exclusively to front-end implementations. ArcadeHaven is a back-end only REST API with no user interface, therefore this requirement is not applicable to the current scope of the project.

**Security Requirement**: NOT APPLICABLE — ArcadeHaven is a REST API with no front-end. Password masking is a client-side concern and falls outside the project scope.

---

### ASVS Requirement V6.2.7 — N/A

**ASVS Requirement Description**: Verify that "paste" functionality, browser password helpers, and external password managers are permitted.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement applies to front-end implementations where input fields can block paste events. Since ArcadeHaven is a back-end only REST API, there are no input fields to restrict, making this requirement not applicable to the current scope.

**Security Requirement**: NOT APPLICABLE — ArcadeHaven is a REST API with no front-end. Paste functionality and password manager compatibility are client-side concerns outside the project scope.

---

### ASVS Requirement V6.2.8 — Planned

**ASVS Requirement Description**: Verify that the application verifies the user's password exactly as received from the user, without any modifications such as truncation or transformation.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because password truncation or silent transformation can lead to two different passwords being treated as equivalent, weakening security. ArcadeHaven will use Spring Security's `BCryptPasswordEncoder`, which does not truncate passwords, and this behaviour will be explicitly validated in security unit tests to ensure no silent transformation occurs at any layer.

**Security Requirement**: No Password Truncation or Transformation — The application must verify passwords exactly as received. No truncation, trimming, or transformation of any kind shall be applied to passwords before hashing or comparison.

---

### ASVS Requirement V6.2.9 — Planned

**ASVS Requirement Description**: Verify that passwords of at least 64 characters are permitted.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because limiting maximum password length unnecessarily restricts users who wish to use passphrases or long randomly generated passwords. ArcadeHaven will permit passwords of up to 128 characters, with server-side validation confirming that passwords of 64+ characters are accepted without truncation at any layer including the database column definition.

**Security Requirement**: Maximum Password Length Support — The application must permit passwords of at least 64 characters in length. The database schema and all validation layers must support this without truncation.

---

### ASVS Requirement V6.2.10 — Planned

**ASVS Requirement Description**: Verify that a user's password stays valid until it is discovered to be compromised or the user rotates it. The application must not require periodic credential rotation.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because forced periodic password rotation leads to predictable incremental passwords (e.g. "Password1", "Password2") and does not improve security. ArcadeHaven will not implement forced password expiration, in alignment with NIST SP 800-63B, which explicitly recommends against periodic password rotation unless a breach is detected.

**Security Requirement**: No Forced Password Rotation — The application must not enforce periodic password expiration. Passwords remain valid until the user changes them voluntarily or a breach is detected.

---

### ASVS Requirement V6.2.11 — Planned

**ASVS Requirement Description**: Verify that the documented list of context specific words is used to prevent easy to guess passwords being created.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is directly linked to V6.1.2. The context-specific blocklist defined in V6.1.2 (e.g. "ArcadeHaven", "admin", "games") must be actively enforced during password validation, not just documented. ArcadeHaven will implement a custom password validator that checks against this list on registration and password change.

**Security Requirement**: Enforce Context-Specific Password Blocklist — The application must actively validate passwords against the context-specific blocklist defined in V6.1.2 during registration and password change, rejecting any password that contains a blocked term.

---

### ASVS Requirement V6.2.12 — Planned

**ASVS Requirement Description**: Verify that passwords submitted during account registration or password changes are checked against a set of breached passwords.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement extends V6.2.4 by checking passwords against known breached credential databases, not just common password lists. ArcadeHaven plans to integrate with the HaveIBeenPwned Passwords API using the k-anonymity model, where only the first 5 characters of the SHA-1 hash of the password are sent to the API, ensuring the plain-text password is never transmitted externally.

**Security Requirement**: Breached Password Check — The application must check passwords submitted during registration and password changes against a breached password database. The HaveIBeenPwned API (k-anonymity model) will be used to perform this check without exposing the plain-text password.

---

## V6.3 — General Authentication Security

### ASVS Requirement V6.3.1 — Planned

**ASVS Requirement Description**: Verify that controls to prevent attacks such as credential stuffing and password brute force are implemented according to the application's requirements.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because PixelVault's public authentication endpoint (`POST /api/auth/login`) is a primary target for automated brute-force and credential stuffing attacks. Without rate limiting and lockout mechanisms, an attacker could systematically attempt millions of credential combinations. Spring Security will be configured to enforce rate limiting per IP address, and account lockout after repeated failed attempts will be implemented at the application level.

**Security Requirement**: Brute-Force and Credential Stuffing Protection — The application must implement rate limiting on authentication endpoints and enforce temporary account lockout after a configurable number of consecutive failed login attempts.

---

### ASVS Requirement V6.3.2 — Planned

**ASVS Requirement Description**: Verify that default user accounts (e.g., "root", "admin", or "sa") are not present in the application or are disabled.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because default accounts with predictable credentials are a common attack vector. PixelVault will not ship with any default user accounts. The initial ADMIN account will be seeded via a secure, environment-variable-driven database migration script (Flyway), with a randomly generated password that must be changed on first login.

**Security Requirement**: No Default User Accounts — The application must not include any default or well-known user accounts. Any seed accounts required for initial setup must use randomly generated credentials injected via environment variables and must be documented in the operations guide.

---

### ASVS Requirement V6.3.3 — Planned

**ASVS Requirement Description**: Verify that either a multi-factor authentication mechanism or a combination of single-factor authentication mechanisms must be used.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because single-factor authentication based solely on a password is insufficient for protecting sensitive user data such as purchase history, activation keys, and personal information. PixelVault will evaluate the feasibility of implementing TOTP-based MFA (e.g. via Google Authenticator) as an optional but strongly recommended second factor for all users, and mandatory for ADMIN accounts.

**Security Requirement**: Multi-Factor Authentication Support — The application must support multi-factor authentication. MFA must be mandatory for ADMIN role accounts and optional for PUBLISHER and BUYER roles. TOTP (RFC 6238) is the target mechanism.

---

### ASVS Requirement V6.3.4 — Planned

**ASVS Requirement Description**: Verify that, if the application includes multiple authentication pathways, there are no undocumented pathways and that security controls and authentication strength are consistent across all pathways.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant to ensure all authentication flows are consistently secured. PixelVault uses a single authentication pathway via JWT. However, future extensions such as OAuth2 social login may be considered. Any additional pathway will be fully documented alongside its security controls before implementation.

**Security Requirement**: Consistent Authentication Pathway Security — All authentication pathways must be documented and subject to the same security controls. No undocumented or backdoor authentication pathways are permitted.

---

### ASVS Requirement V6.3.5 — Planned

**ASVS Requirement Description**: Verify that users are notified of suspicious authentication attempts (successful or unsuccessful). This may include authentication attempts from new locations or devices.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because notifying users of suspicious activity allows them to respond quickly to potential account compromise. PixelVault will implement an audit log for authentication events, and email notifications will be sent to users upon login from a new IP address or after multiple failed attempts. This is a Level 3 requirement and will be considered for Phase 2 Sprint 2.

**Security Requirement**: Suspicious Activity Notification — The application must log all authentication events and notify users via email when suspicious activity is detected, such as logins from new IP addresses or repeated failed attempts.

---

### ASVS Requirement V6.3.6 — N/A

**ASVS Requirement Description**: Verify that email is not used as either a single-factor or multi-factor authentication mechanism.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not use email as an authentication factor. Authentication is performed exclusively via username/password credentials with JWT tokens. Email is only used for account registration confirmation and security notifications, never as an authentication mechanism.

**Security Requirement**: NOT APPLICABLE — PixelVault does not use email as an authentication factor. This requirement is satisfied by design.

---

### ASVS Requirement V6.3.7 — Planned

**ASVS Requirement Description**: Verify that users are notified after updates to authentication details, such as credential resets or modification of the username or email.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because notifying users of changes to their authentication details helps them detect unauthorised account modifications. PixelVault will send email notifications whenever a user's password, email address, or username is changed, regardless of whether the change was initiated by the user or an administrator.

**Security Requirement**: Authentication Detail Change Notification — The application must send an email notification to the user's registered address whenever authentication details (password, email, username) are modified, including changes initiated by administrators.

---

### ASVS Requirement V6.3.8 — Planned

**ASVS Requirement Description**: Verify that valid users cannot be deduced from failed authentication challenges, such as by basing on error messages, HTTP response codes, or differing response times.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because differing error messages or response times for valid versus invalid usernames allow attackers to enumerate valid accounts. PixelVault will return a uniform error message ("Invalid credentials") for all failed authentication attempts, with consistent response times achieved through constant-time comparison of credentials.

**Security Requirement**: User Enumeration Prevention — The application must return identical error messages and HTTP status codes (401) for all failed authentication attempts, regardless of whether the username exists. Response times must be consistent to prevent timing-based enumeration.

---

## V6.4 — Authentication Factor Lifecycle and Recovery

### ASVS Requirement V6.4.1 — Planned

**ASVS Requirement Description**: Verify that system generated initial passwords or activation codes are securely randomly generated, follow the existing password policy, and expire after a short period of time.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because weak or long-lived system-generated passwords represent a significant attack surface. PixelVault will generate initial passwords and activation codes using `SecureRandom`, ensuring cryptographic randomness. All system-generated credentials will expire after 24 hours and must comply with the password policy defined in V6.2.1.

**Security Requirement**: Secure System-Generated Credentials — System-generated passwords and activation codes must be generated using a cryptographically secure random number generator (`SecureRandom`), comply with the password policy, and expire within 24 hours of generation.

---

### ASVS Requirement V6.4.2 — Planned

**ASVS Requirement Description**: Verify that password hints or knowledge-based authentication (so-called "secret questions") are not present.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because password hints and secret questions are easily guessable or discoverable through social engineering and public information. PixelVault will not implement any password hint or knowledge-based authentication mechanism. Password recovery will be handled exclusively via a secure, time-limited reset token sent to the user's registered email address.

**Security Requirement**: No Password Hints or Secret Questions — The application must not implement password hints or knowledge-based authentication (secret questions). Password recovery must use secure, time-limited tokens delivered to the user's registered email address.

---

### ASVS Requirement V6.4.3 — Planned

**ASVS Requirement Description**: Verify that a secure process for resetting a forgotten password is implemented, that does not bypass any enabled multi-factor authentication.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because insecure password reset flows are a common vulnerability that can be exploited to bypass authentication entirely. PixelVault will implement a password reset flow using a cryptographically secure, single-use token with a 1-hour expiry, delivered to the user's registered email. The reset flow will not bypass MFA where it is enabled.

**Security Requirement**: Secure Password Reset Flow — The password reset process must use a cryptographically secure, single-use token with a maximum validity of 1 hour. The process must not bypass MFA and must invalidate the token immediately after use.

---

### ASVS Requirement V6.4.4 — Planned

**ASVS Requirement Description**: Verify that if a multi-factor authentication factor is lost, evidence of identity proofing is performed at the same level as during enrollment.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because a lost MFA device could be exploited to gain unauthorised access if the account recovery process is weaker than the original authentication. PixelVault will require identity re-verification at the same level as enrollment before recovering access to an account where MFA has been lost.

**Security Requirement**: MFA Recovery Identity Proofing — Account recovery following loss of an MFA factor must require identity verification at the same assurance level as the original enrollment process.

---

### ASVS Requirement V6.4.5 — Planned

**ASVS Requirement Description**: Verify that renewal instructions for authentication mechanisms which expire are sent with enough time to be carried out before the old mechanism expires.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant to ensure users are not unexpectedly locked out of their accounts due to expiring authentication mechanisms. PixelVault will send advance notifications before any authentication-related credential (e.g. activation codes, password reset tokens) expires, giving users sufficient time to act.

**Security Requirement**: Advance Expiry Notification — The application must notify users sufficiently in advance before any authentication credential or mechanism expires, to prevent unintended account lockout.

---

### ASVS Requirement V6.4.6 — Planned

**ASVS Requirement Description**: Verify that administrative users can initiate the password reset process for the user, but that this does not allow them to change or choose the new password.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because allowing administrators to directly set a user's password creates a privilege abuse risk. PixelVault will implement an admin-initiated password reset that triggers a secure reset email to the user, ensuring only the user can define the new password. Administrators will never have access to or be able to set a user's plain-text password.

**Security Requirement**: Admin-Initiated Password Reset — Administrators must be able to trigger a password reset for a user, which sends a secure reset link to the user's email. Administrators must not be able to directly set or view a user's password.

---

## V6.5 — General Multi-Factor Authentication Requirements

### ASVS Requirement V6.5.1 — Planned

**ASVS Requirement Description**: Verify that lookup secrets, out-of-band authentication requests or codes, and time-based one-time passwords (TOTPs) are only successfully usable once.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because reusable one-time codes defeat the purpose of multi-factor authentication. PixelVault will implement single-use enforcement for all OTPs and lookup secrets, invalidating them immediately upon successful use. TOTP codes will be validated against a time window and marked as used to prevent replay attacks.

**Security Requirement**: Single-Use OTP Enforcement — All one-time passwords, lookup secrets, and out-of-band codes must be invalidated immediately after a single successful use. Replay of used codes must be rejected.

---

### ASVS Requirement V6.5.2 — Planned

**ASVS Requirement Description**: Verify that, when being stored in the application's backend, lookup secrets with less than 112 bits of entropy are hashed with an approved password hashing algorithm.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because storing lookup secrets in plain text or with weak hashing exposes them to theft if the database is compromised. PixelVault will hash all stored lookup secrets using BCrypt, the same algorithm used for passwords, ensuring that even short secrets are protected at rest.

**Security Requirement**: Secure Storage of Lookup Secrets — All lookup secrets stored in the database must be hashed using BCrypt or an equivalent approved password hashing algorithm. Plain-text storage of any secret is strictly prohibited.

---

### ASVS Requirement V6.5.3 — Planned

**ASVS Requirement Description**: Verify that lookup secrets, out-of-band authentication codes, and time-based one-time password seeds are generated using a Cryptographically Secure Pseudorandom Number Generator (CSPRNG).

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because predictable OTPs or seeds can be exploited by attackers to bypass MFA. PixelVault will use Java's `SecureRandom` class for all cryptographic random number generation, ensuring that OTPs and seeds are unpredictable.

**Security Requirement**: CSPRNG for OTP Generation — All lookup secrets, out-of-band authentication codes, and TOTP seeds must be generated using `SecureRandom` or an equivalent CSPRNG. Use of `Math.random()` or similar non-cryptographic generators is strictly prohibited.

---

### ASVS Requirement V6.5.4 — Planned

**ASVS Requirement Description**: Verify that lookup secrets and out-of-band authentication codes have a minimum of 20 bits of entropy (typically 4 random alphanumeric characters).

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because low-entropy codes are vulnerable to brute-force attacks. PixelVault will generate OTPs and lookup codes with a minimum of 6 random alphanumeric characters, providing at least 31 bits of entropy, well above the 20-bit minimum.

**Security Requirement**: Minimum OTP Entropy — All lookup secrets and out-of-band authentication codes must have a minimum of 20 bits of entropy. PixelVault will use 6-character alphanumeric codes as the baseline, providing at least 31 bits of entropy.

---

### ASVS Requirement V6.5.5 — Planned

**ASVS Requirement Description**: Verify that out-of-band authentication requests, codes, or tokens, as well as time-based one-time passwords (TOTPs) have a defined lifetime.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because long-lived OTPs increase the window of opportunity for an attacker to exploit a stolen code. PixelVault will enforce a 5-minute expiry on all OTPs and out-of-band codes, and TOTP validation will use a time window of ±1 step (30 seconds per step) to account for clock drift.

**Security Requirement**: OTP Lifetime Enforcement — Out-of-band authentication codes must expire after a maximum of 5 minutes. TOTP validation must use a time window of ±1 step (30 seconds). Expired codes must be rejected.

---

### ASVS Requirement V6.5.6 — Planned

**ASVS Requirement Description**: Verify that any authentication factor (including physical devices) can be revoked in case of theft or other loss.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because a stolen authentication factor (e.g. a TOTP device) must be revocable to prevent unauthorised access. PixelVault will provide users with the ability to revoke and re-enroll their MFA device through the account settings, with re-verification required before revocation.

**Security Requirement**: MFA Factor Revocation — The application must allow users to revoke any registered authentication factor. Revocation must require re-verification of identity and must take effect immediately.

---

### ASVS Requirement V6.5.7 — N/A

**ASVS Requirement Description**: Verify that biometric authentication mechanisms are only used as secondary factors together with either something you have or something you know.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not implement biometric authentication. Authentication is based on password (something you know) and optionally TOTP (something you have). Biometric mechanisms are outside the scope of this project.

**Security Requirement**: NOT APPLICABLE — PixelVault does not implement biometric authentication mechanisms. This requirement is not applicable to the current project scope.

---

### ASVS Requirement V6.5.8 — Planned

**ASVS Requirement Description**: Verify that time-based one-time passwords (TOTPs) are checked based on a time source from a trusted service and not from an untrusted or client-provided source.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because using a client-provided time source for TOTP validation would allow attackers to manipulate the time window and reuse expired codes. PixelVault's TOTP validation will rely exclusively on the server's system clock, synchronised via NTP, and will never accept a time value provided by the client.

**Security Requirement**: Trusted Time Source for TOTP — TOTP validation must use the server's system clock as the time source, synchronised via NTP. Client-provided time values must never be used in TOTP validation.

---

## V6.6 — Out-of-Band Authentication Mechanisms

### ASVS Requirement V6.6.1 — N/A

**ASVS Requirement Description**: Verify that authentication mechanisms using the Public Switched Telephone Network (PSTN) to deliver One-time Passwords (OTPs) via phone or SMS are offered only when alternate stronger methods are also offered.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not implement SMS or phone-based OTP delivery. MFA will be implemented via TOTP (authenticator apps), which is a stronger mechanism than PSTN-based delivery. This requirement is not applicable to the current project scope.

**Security Requirement**: NOT APPLICABLE — PixelVault does not use PSTN (SMS/phone) for OTP delivery. MFA is implemented via TOTP only.

---

### ASVS Requirement V6.6.2 — Planned

**ASVS Requirement Description**: Verify that out-of-band authentication requests, codes, or tokens are bound to the original authentication request for which they were generated.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because an unbound OTP could be intercepted and used to authenticate a different session. PixelVault will bind all out-of-band codes to a specific session identifier and authentication request, ensuring that a code generated for one session cannot be reused in another.

**Security Requirement**: OTP Session Binding — All out-of-band authentication codes must be cryptographically bound to the specific authentication request and session for which they were generated. Cross-session reuse must be rejected.

---

### ASVS Requirement V6.6.3 — Planned

**ASVS Requirement Description**: Verify that a code based out-of-band authentication mechanism is protected against brute force attacks by using rate limiting. Consider also using a code expiry.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is relevant because short OTP codes (e.g. 6 digits) are vulnerable to brute-force if no rate limiting is applied. PixelVault will enforce rate limiting on OTP verification endpoints, locking the verification attempt after 5 consecutive failures and expiring codes after 5 minutes, as defined in V6.5.5.

**Security Requirement**: OTP Brute-Force Protection — OTP verification endpoints must enforce rate limiting, rejecting further attempts after 5 consecutive failures. Codes must expire after a maximum of 5 minutes as defined in V6.5.5.

---

### ASVS Requirement V6.6.4 — N/A

**ASVS Requirement Description**: Verify that, where push notifications are used for multi-factor authentication, rate limiting is used to prevent push bombing attacks.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not implement push notification-based MFA. Authentication uses TOTP via authenticator apps only. Push bombing protection is therefore not applicable to the current project scope.

**Security Requirement**: NOT APPLICABLE — PixelVault does not use push notifications for MFA. This requirement does not apply to the current project scope.

---

## V6.7 — Cryptographic Authentication Mechanism

### ASVS Requirement V6.7.1 — N/A

**ASVS Requirement Description**: Verify that the certificates used to verify cryptographic authentication assertions are stored in a way that protects them from modification.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not implement certificate-based cryptographic authentication. Authentication is performed via username/password with JWT tokens. Certificate-based authentication is outside the current project scope.

**Security Requirement**: NOT APPLICABLE — PixelVault does not use certificate-based cryptographic authentication. This requirement does not apply to the current project scope.

---

### ASVS Requirement V6.7.2 — N/A

**ASVS Requirement Description**: Verify that the challenge nonce is at least 64 bits in length, and statistically unique or unique over the lifetime of the cryptographic device.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not implement challenge-response cryptographic authentication or cryptographic devices. This requirement is not applicable to the current project scope.

**Security Requirement**: NOT APPLICABLE — PixelVault does not implement challenge-response cryptographic authentication. This requirement does not apply to the current project scope.

---

## V6.8 — Authentication with an Identity Provider

### ASVS Requirement V6.8.1 — N/A

**ASVS Requirement Description**: Verify that, if the application supports multiple identity providers (IdPs), the user's identity cannot be spoofed via another supported identity provider.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not integrate with external identity providers in Phase 1. Authentication is handled internally via username/password and JWT. OAuth2/OIDC social login may be considered in future phases, at which point this requirement will become applicable.

**Security Requirement**: NOT APPLICABLE — PixelVault does not support external identity providers in the current phase. If OAuth2/OIDC is introduced in future phases, this requirement must be revisited.

---

### ASVS Requirement V6.8.2 — Planned

**ASVS Requirement Description**: Verify that the presence and integrity of digital signatures on authentication assertions (for example on JWTs or SAML assertions) are always validated.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: This requirement is directly relevant to PixelVault's JWT-based authentication. JWTs must be signed using a strong algorithm (RS256 or HS256 with a sufficiently long secret) and the signature must be validated on every request. The application must reject unsigned tokens, tokens with the `alg: none` header, and tokens with invalid signatures.

**Security Requirement**: JWT Signature Validation — All JWT tokens must be signed using RS256 or HS256. The signature must be validated on every request. Unsigned tokens, tokens with `alg: none`, and tokens with invalid signatures must be rejected with a 401 response.

---

### ASVS Requirement V6.8.3 — Planned

**ASVS Requirement Description**: Verify that SAML assertions are uniquely processed and used only once within the validity period to prevent replay attacks.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not use SAML assertions in Phase 1. However, JWT tokens are subject to a similar replay risk and will be addressed through short token expiry (15 minutes for access tokens) and token revocation via a denylist for logout events.

**Security Requirement**: JWT Replay Prevention — JWT access tokens must have a maximum validity of 15 minutes. A token denylist mechanism must be implemented to support immediate invalidation upon logout or security events.

---

### ASVS Requirement V6.8.4 — N/A

**ASVS Requirement Description**: Verify that, if an application uses a separate Identity Provider (IdP) and expects specific authentication strength, methods, or recentness for specific actions, the application verifies this.

**Related Functional Requirement**: User Registration and Authentication

**Justification**: PixelVault does not use a separate Identity Provider in Phase 1. Authentication is handled entirely within the application. This requirement will be revisited if external IdP integration is introduced in future phases.

**Security Requirement**: NOT APPLICABLE — PixelVault does not use a separate Identity Provider in the current phase. This requirement does not apply to the current project scope.

---

## Summary

| Section | Total  | ✅ Compliant | ⚠️ Planned | 🔵 N/A | ❌ Non-Compliant |
|---|--------|-------------|------------|--------|-----------------|
| V6.1 Authentication Documentation | 3      | 0           | 3          | 0      | 0               |
| V6.2 Password Security | 12     | 0           | 10         | 2      | 0               |
| V6.3 General Authentication Security | 8      | 0           | 6          | 1      | 0               |
| V6.4 Authentication Factor Lifecycle and Recovery | 6      | 0           | 6          | 0      | 0               |
| V6.5 General Multi-Factor Authentication | 8      | 0           | 6          | 2      | 0               |
| V6.6 Out-of-Band Authentication | 4      | 0           | 2          | 2      | 0               |
| V6.7 Cryptographic Authentication | 2      | 0           | 0          | 2      | 0               |
| V6.8 Authentication with Identity Provider | 4      | 0           | 2          | 2      | 0               |
| **Total** | **47** | **0**       | **35**     | **11** | **0**           |

> All requirements are currently **Planned** as the project is in Phase 1 (Analysis & Design).
> Statuses will be updated as implementation progresses in Phase 2.

---

## Traceability Matrix

| Req ID | Security Requirement | STRIDE Threat | Abuse Case |
|---|---|---|---|
| V6.1.1 | Rate limiting on authentication endpoints | Denial of Service, Spoofing | AC-01: Brute force attack on login endpoint |
| V6.1.2 | Context-specific password blocklist | Spoofing | AC-02: Credential stuffing using application-related passwords |
| V6.2.1 | Minimum password length of 12 characters | Spoofing | AC-02: Credential stuffing |
| V6.2.3 | Current password required on change | Spoofing, Elevation of Privilege | AC-03: Session hijacking leading to password change |
| V6.2.4 | Common password blocklist (top 3000) | Spoofing | AC-02: Dictionary attack on user accounts |
| V6.2.8 | No password truncation or transformation | Tampering | AC-04: Two users sharing the same effective password due to truncation |
| V6.2.12 | Breached password check via HaveIBeenPwned | Spoofing | AC-02: Use of previously breached credentials |
| V6.3.1 | Rate limiting and account lockout on login | Denial of Service, Spoofing | AC-01: Brute-force and credential stuffing on login |
| V6.3.2 | No default user accounts | Elevation of Privilege | AC-05: Login with default admin credentials |
| V6.3.3 | MFA mandatory for ADMIN | Spoofing, Elevation of Privilege | AC-06: Account takeover via password compromise |
| V6.3.8 | User enumeration prevention | Information Disclosure | AC-07: Username enumeration via login error messages |
| V6.4.1 | Secure random generation of initial credentials | Spoofing | AC-08: Prediction of system-generated activation codes |
| V6.4.2 | No password hints or secret questions | Spoofing | AC-09: Social engineering via secret question answers |
| V6.4.3 | Secure password reset with single-use token | Spoofing, Elevation of Privilege | AC-10: Account takeover via password reset flow |
| V6.5.1 | Single-use OTP enforcement | Spoofing | AC-11: OTP replay attack |
| V6.5.3 | CSPRNG for OTP generation | Spoofing | AC-12: Prediction of OTP via weak random generator |
| V6.6.3 | Rate limiting on OTP verification | Denial of Service, Spoofing | AC-13: Brute-force of OTP verification endpoint |
| V6.8.2 | JWT signature validation on every request | Spoofing, Tampering | AC-14: Forged JWT token with modified claims |
| V6.8.3 | JWT replay prevention via short expiry and denylist | Spoofing | AC-15: Replay attack using a captured JWT token |
