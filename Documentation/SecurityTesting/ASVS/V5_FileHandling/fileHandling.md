# File Handling — ASVS Security Requirements (V5.1 – V5.4)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** File handling is a core feature of ArcadeHaven. Publishers upload game images and screenshots, the system generates invoices (PDF) and activation keys, and buyers download invoices. All file-related requirements are highly relevant.

---

## V5.1 — File Handling Documentation

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V5.1.1 | 2 | ⚠️ Planned | Documentation will define permitted file types per upload feature: game images (JPEG, PNG, WebP — max 5MB), game cover art (JPEG, PNG — max 2MB). System-generated files (PDF invoices, TXT activation keys) have no upload surface. The documentation will also specify behaviour when a malicious file is detected (reject with 400, log the event, notify the admin). |

---

## V5.2 — File Upload and Content

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V5.2.1 | 1 | ⚠️ Planned | Maximum file size limits will be enforced at the Spring Boot level via `spring.servlet.multipart.max-file-size` and `spring.servlet.multipart.max-request-size`. Requests exceeding the limit will be rejected with a 413 response before processing begins. |
| V5.2.2 | 1 | ⚠️ Planned | All uploaded files will be validated by checking the file extension against an allowlist (JPEG, PNG, WebP) and verifying the magic bytes of the file content. Image files will be re-written server-side (via Java ImageIO) to strip any embedded malicious content and confirm the file is a valid image. |
| V5.2.3 | 2 | 🔵 N/A | ArcadeHaven does not accept compressed archive uploads (zip, gz, etc.) from users in Phase 1. Only individual image files are accepted. |
| V5.2.4 | 3 | ⚠️ Planned | A per-user file quota will be enforced: Publishers will be limited to a maximum number of images per game listing and a total storage quota per account. Enforcement will be implemented at the application service layer before file storage operations. |
| V5.2.5 | 3 | 🔵 N/A | No compressed file uploads are accepted. Symlink exploitation via zip files is not applicable in the current scope. |
| V5.2.6 | 3 | ⚠️ Planned | Uploaded images will be validated for maximum pixel dimensions before processing. Images exceeding the defined maximum (e.g. 4096x4096 pixels) will be rejected to prevent pixel flood (decompression bomb) attacks. |

---

## V5.3 — File Storage

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V5.3.1 | 1 | ⚠️ Planned | Uploaded files will be stored in a dedicated directory outside the application's web root and classpath. The storage directory will have no execute permissions. Files will be served via a dedicated download endpoint that reads and streams the file, never by exposing the file path directly via HTTP. |
| V5.3.2 | 1 | ⚠️ Planned | File paths will be constructed using internally generated UUIDs (e.g. `{uuid}_{sanitized_extension}`), never using the original user-submitted filename. If the original filename must be referenced (e.g. for display), it will be stored separately in the database after sanitization, and never used in file system operations. |
| V5.3.3 | 3 | 🔵 N/A | No server-side file decompression is performed on user-uploaded files. Zip slip attacks are not applicable in the current scope. |

---

## V5.4 — File Download

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V5.4.1 | 2 | ⚠️ Planned | Download endpoints will ignore any user-submitted filename parameters. The `Content-Disposition` header will always be set server-side using the internally stored filename (e.g. `Content-Disposition: attachment; filename="invoice_uuid.pdf"`), never reflecting user input. |
| V5.4.2 | 2 | ⚠️ Planned | Filenames included in `Content-Disposition` and other response headers will be encoded following RFC 6266 (UTF-8 percent-encoding) to prevent header injection attacks. Sanitization will be applied before any filename is inserted into a response header. |
| V5.4.3 | 2 | ⚠️ Planned | Game image files uploaded by Publishers will be scanned for known malicious content. Integration with a lightweight antivirus solution (e.g. ClamAV via a Java binding) is planned for Phase 2 Sprint 1. In Phase 1, image re-writing (V5.2.2) serves as the primary mitigation. |

---

## Summary

| Section | Total | ⚠️ Planned | 🔵 N/A |
|---|---|---|---|
| V5.1 File Handling Documentation | 1 | 1 | 0 |
| V5.2 File Upload and Content | 6 | 4 | 2 |
| V5.3 File Storage | 3 | 2 | 1 |
| V5.4 File Download | 3 | 3 | 0 |
| **Total** | **13** | **10** | **3** |

> File handling is a critical area for ArcadeHaven given that Publishers upload game images
> and the system generates and serves invoices and activation keys.
> Key implementation priorities: magic byte validation + image re-writing (V5.2.2),
> UUID-based file path generation (V5.3.2), no user-controlled filenames in headers (V5.4.1),
> and ClamAV integration for uploaded file scanning (V5.4.3).
