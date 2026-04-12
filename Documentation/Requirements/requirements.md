# ArcadeHaven Requirements


## 1. Introduction

### 1.1 Document Identification

**ArcadeHaven Requirements Document**

### 1.2 Glossary

**RF** - Functional Requirement

**RNF** - Non Functional Requirement

### 1.3 Requirement Priority

| **Level**     | **Description**                                                                                          |
| ------------- | -------------------------------------------------------------------------------------------------------- |
| **Low**       | Requirement with minimal impact. Can be implemented later without affecting core system functionalities. |
| **Medium**    | Important requirement, but not critical for the initial release.                                       |
| **High**      | Significant requirement that strongly improves system functionality or user experience.                |
| **Essential** | Critical requirement for system operation. Must be implemented for the system to function properly.    |

This table defines the priority assigned to each requirement. Higher priority requirements should be implemented first taking into account their greater impact on the system.

### 1.4 Requirement State

| **State**       | **Description**                                                       |
| --------------- | --------------------------------------------------------------------- |
| **Proposed**    | Requirement has been identified but not yet analyzed.                 |
| **In Analysis** | Requirement is being reviewed and validated.                          |
| **Approved**    | Requirement has been validated and is ready for implementation.       |
| **Implemented** | Requirement has been developed and integrated into the system.        |
| **Tested**      | Requirement has been verified through testing.                        |
| **Finalized**   | Requirement is complete and stable, with no further changes expected. |

This table represents the lifecycle of each requirement from initial proposal to final completion.

## 2. Functional Requirements

### 2.1 User Management Requirements

| **Field**        | **Value**                                                                         |
| ---------------- | --------------------------------------------------------------------------------- |
| **ID**           | RF-01                                                                             |
| **Name**         | User Registration                                                                 |
| **Description**  | The system must allow new users to register using username, email, and password.  |
| **Actors**       | Guest                                                                             |
| **Restrictions** | Email and username must be unique                                                 |
| **Verification** | Validate required fields and its uniqueness                                       |
| **Priority**     | Essential                                                                         |
| **State**        | Approved                                                                          |

---

| **Field**        | **Value**                                             |
| ---------------- | ----------------------------------------------------- |
| **ID**           | RF-02                                                 |
| **Name**         | User Authentication                                   |
| **Description**  | The system must authenticate users via JWT tokens.    |
| **Actors**       | User                                                  |
| **Restrictions** | User must be registed in the system                   |
| **Verification** | Validate user credentials                             |
| **Priority**     | Essential                                             |
| **State**        | Approved                                              |

---

| **Field**        | **Value**                                                                     |
| ---------------- | ----------------------------------------------------------------------------- |
| **ID**           | RF-03                                                                         | 
| **Name**         | User Roles                                                                    |
| **Description**  | The system must support the user roles of administrator, publisher and buyer. |
| **Actors**       | System                                                                        |
| **Restrictions** | Roles must be predefined                                                      |
| **Verification** | None                                                                          |
| **Priority**     | Essential                                                                     |
| **State**        | Approved                                                                      |

---

| **Field**        | **Value**                                                            |
| ---------------- | -------------------------------------------------------------------- |
| **ID**           | RF-04                                                                |
| **Name**         | Manage User Activation                                               |
| **Description**  | Administrator must be able to activate and deactivate user accounts. |
| **Actors**       | Administrator                                                        |
| **Restrictions** | Administrator must be authenticated                                  |
| **Verification** | Validate new user account status                                     |
| **Priority**     | High                                                                 |
| **State**        | Approved                                                             |

---

| **Field**        | **Value**                                                |
| ---------------- | -------------------------------------------------------- |
| **ID**           | RF-05                                                    |
| **Name**         | Change User Role                                         |
| **Description**  | Administrator must be able to change the role of a user. |
| **Actors**       | Administrator                                            |
| **Restrictions** | Administrator must be authenticated                      |
| **Verification** | Validate new user role                                   |
| **Priority**     | High                                                     |
| **State**        | Approved                                                 |

---

| **Field**        | **Value**                                           |
| ---------------- | --------------------------------------------------- |
| **ID**           | RF-06                                               |
| **Name**         | Profile Management                                  |
| **Description**  | Users must be able to view and edit their profiles. |
| **Actors**       | User                                                |
| **Restrictions** | User must be authenticated                          |
| **Verification** | Validate update to profile fields                   |
| **Priority**     | High                                                |
| **State**        | Approved                                            |


### 2.2 Game Management Requirements

| **Field**        | **Value**                                                                                     |
| ---------------- | --------------------------------------------------------------------------------------------- |
| **ID**           | RF-07                                                                                         |
| **Name**         | Submit Game                                                                                   |
| **Description**  | Publishers must be able to submit new games with title, description, price, and category.     |
| **Actors**       | Publisher                                                                                     |
| **Restrictions** | Publisher must be authenticated                                                               |
| **Verification** | Validate game fields                                                                          |
| **Priority**     | Essential                                                                                     |
| **State**        | Approved                                                                                      |

---

| **Field**        | **Value**                                               |
| ---------------- | ------------------------------------------------------- |
| **ID**           | RF-08                                                   |
| **Name**         | Update Games                                            |
| **Description**  | Publishers must be able to view and update their games. |
| **Actors**       | Publisher                                               |
| **Restrictions** | Publisher must be authenticated                         |
| **Verification** | Validate game updated fields                            |
| **Priority**     | High                                                    |
| **State**        | Approved                                                |

---

| **Field**        | **Value**                                                        |
| ---------------- | ---------------------------------------------------------------- |
| **ID**           | RF-09                                                            |
| **Name**         | Approve or Reject Game                                           |
| **Description**  | Administrator must be able to approve or reject submitted games. |
| **Actors**       | Administrator                                                    |
| **Restrictions** | Administrator must be authenticated                              |
| **Verification** | Validate game approval or rejection                              |
| **Priority**     | Essential                                                        |
| **State**        | Approved                                                         |


---

| **Field**        | **Value**                                        |
| ---------------- | ------------------------------------------------ |
| **ID**           | RF-10                                            |
| **Name**         | List Available Games                             |
| **Description**  | The system must list active games for all users. |
| **Actors**       | User                                             |
| **Restrictions** | Only active games must be displayed              |
| **Verification** | Validate game listing filtering                  |
| **Priority**     | Essential                                        |
| **State**        | Approved                                         |

---

| **Field**        | **Value**                                                                             |
| ---------------- | ------------------------------------------------------------------------------------- |
| **ID**           | RF-11                                                                                 |
| **Name**         | Search and Filter Games                                                               |
| **Description**  | The system must allow users to search and filter games by title, category, and price. |
| **Actors**       | User                                                                                  |
| **Restrictions** | None                                                                                  |
| **Verification** | Validate search and filtering results                                                 |
| **Priority**     | High                                                                                  |
| **State**        | Approved                                                                              |

---

| **Field**        | **Value**                                                        |
| ---------------- | ---------------------------------------------------------------- |
| **ID**           | RF-12                                                            |
| **Name**         | RAWG API Integration                                             |
| **Description**  | The system must integrate with the RAWG API to enrich game data. |
| **Actors**       | System                                                           |
| **Restrictions** | RAWG API must be available                                       |
| **Verification** | Validate connection to API and received data                     |
| **Priority**     | Medium                                                           |
| **State**        | Approved                                                         |

---

| **Field**        | **Value**                                                         |
| ---------------- | ----------------------------------------------------------------- |
| **ID**           | RF-13                                                             |
| **Name**         | View Game Sales Metrics                                           |
| **Description**  | Publishers must be able to consult sales metrics for their games. |
| **Actors**       | Publisher                                                         |
| **Restrictions** | Publisher must be authenticated                                   |
| **Verification** | None                                                              |
| **Priority**     | Medium                                                            |
| **State**        | Approved                                                          |


### 2.3 Orders Management Requirements

| **Field**        | **Value**                                                            |
| ---------------- | -------------------------------------------------------------------- |
| **ID**           | RF-14                                                                |
| **Name**         | Create Order                                                         |
| **Description**  | Buyers must be able to create an order containing one or more games. |
| **Actors**       | Buyer                                                                |
| **Restrictions** | User must be authenticated                                           |
| **Verification** | Validate games added to order                                        |
| **Priority**     | Essential                                                            |
| **State**        | Approved                                                             |

---

| **Field**        | **Value**                                                                        |
| ---------------- | -------------------------------------------------------------------------------- |
| **ID**           | RF-15                                                                            |
| **Name**         | Prevent Duplicate Purchase                                                       |
| **Description**  | The system must prevent a buyer from purchasing a game already in their library. |
| **Actors**       | System                                                                           |
| **Restrictions** | Library check required                                                           |
| **Verification** | Validate duplicate purchase prevention                                           |
| **Priority**     | Essential                                                                        |
| **State**        | Approved                                                                         |

---

| **Field**        | **Value**                                                                     |
| ---------------- | ----------------------------------------------------------------------------- |
| **ID**           | RF-16                                                                         |
| **Name**         | Generate Invoice                                                              |
| **Description**  | The system must automatically generate an invoice file upon order completion. |
| **Actors**       | System                                                                        |
| **Restrictions** | Order must be completed                                                       |
| **Verification** | Validate invoice file generation                                              |
| **Priority**     | High                                                                          |
| **State**        | Approved                                                                      |

---

| **Field**        | **Value**                                                                     |
| ---------------- |-------------------------------------------------------------------------------|
| **ID**           | RF-17                                                                         |
| **Name**         | Activate Game Key                                                             |
| **Description**  | The system must activate game key for a purchased game upon order completion. |
| **Actors**       | System                                                                        |
| **Restrictions** | Order must be completed and invoice generated                                 |
| **Verification** | Validate activation key                                                       |
| **Priority**     | Essential                                                                     |
| **State**        | Approved                                                                      |

---

| **Field**        | **Value**                                        |
| ---------------- | ------------------------------------------------ |
| **ID**           | RF-18                                            |
| **Name**         | View Order History                               |
| **Description**  | Buyers must be able to view their order history. |
| **Actors**       | Buyer                                            |
| **Restrictions** | User must be authenticated                       |
| **Verification** | Validate order history retrieval                 |
| **Priority**     | High                                             |
| **State**        | Approved                                         |

---

| **Field**        | **Value**                                                 |
| ---------------- | --------------------------------------------------------- |
| **ID**           | RF-19                                                     |
| **Name**         | Download Invoice                                          |
| **Description**  | Buyers must be able to download the invoice for an order. |
| **Actors**       | Buyer                                                     |
| **Restrictions** | User must be authenticated                                |
| **Verification** | Validate invoice download                                 |
| **Priority**     | High                                                      |
| **State**        | Approved                                                  |

---

| **Field**        | **Value**                                                         |
| ---------------- | ----------------------------------------------------------------- |
| **ID**           | RF-20                                                             |
| **Name**         | Cancel Pending Order                                              |
| **Description**  | Buyers must be able to cancel an order that is in pending status. |
| **Actors**       | Buyer                                                             |
| **Restrictions** | Order must be associated to user and in pending state             |
| **Verification** | Validate order cancellation                                       |
| **Priority**     | Medium                                                            |
| **State**        | Approved                                                          |


### 2.4 Library Management Requirements

| **Field**        | **Value**                                                                                       |
| ---------------- | ----------------------------------------------------------------------------------------------- |
| **ID**           | RF-21                                                                                           |
| **Name**         | Add Games to Library                                                                            |
| **Description**  | The system must automatically add purchased games to the buyer's library upon order completion. |
| **Actors**       | System                                                                                          |
| **Restrictions** | Order must be completed                                                                         |
| **Verification** | None                                                                                            |
| **Priority**     | Essential                                                                                       |
| **State**        | Approved                                                                                        |

---

| **Field**        | **Value**                                               |
| ---------------- | ------------------------------------------------------- |
| **ID**           | RF-22                                                   |
| **Name**         | View Library                                            |
| **Description**  | Buyers must be able to view all games in their library. |
| **Actors**       | Buyer                                                   |
| **Restrictions** | User must be authenticated                              |
| **Verification** | Validate library data retrieval                         |
| **Priority**     | Essential                                               |
| **State**        | Approved                                                |

---

| **Field**        | **Value**                                                                         |
| ---------------- |-----------------------------------------------------------------------------------|
| **ID**           | RF-23                                                                             |
| **Name**         | Prevent Duplicate Library Entries                                                 |
| **Description**  | The system must prevent the same game from being added twice to the same library. |
| **Actors**       | System                                                                            |
| **Restrictions** | Library uniqueness required                                                       |
| **Verification** | Validate that a new game is not in library yet                                    |
| **Priority**     | Essential                                                                         |
| **State**        | Approved                                                                          |

---

| **Field**        | **Value**                                                                   |
| ---------------- |-----------------------------------------------------------------------------|
| **ID**           | RF-24                                                                       |
| **Name**         | Manage Library Entries                                                      |
| **Description**  | Administrator must be able to suspend or revoke library entries for a user. |
| **Actors**       | Administrator                                                               |
| **Restrictions** | Administrator must be authenticated                                         |
| **Verification** | Validate suspended/revoked entry                                            |
| **Priority**     | High                                                                        |
| **State**        | Approved                                                                    |


### 2.5 File Operations

| **Field**        | **Value**                                                                          |
| ---------------- |------------------------------------------------------------------------------------|
| **ID**           | RF-25                                                                              |
| **Name**         | Automatic Directory Structure                                                      |
| **Description**  | The system must automatically create the necessary directory structure on startup. |
| **Actors**       | System                                                                             |
| **Restrictions** | None                                                                               |
| **Verification** | Validate directory creation                                                        |
| **Priority**     | High                                                                               |
| **State**        | Approved                                                                           |

---

| **Field**        | **Value**                                                    |
| ---------------- |--------------------------------------------------------------|
| **ID**           | RF-26                                                        |
| **Name**         | Store Generated Invoices                                     |
| **Description**  | The system must store generated invoice files on the server. |
| **Actors**       | System                                                       |
| **Restrictions** | Server storage must be available                             |
| **Verification** | Validate file to store                                       |
| **Priority**     | High                                                         |
| **State**        | Approved                                                     |

---

| **Field**        | **Value**                                                               |
| ---------------- |-------------------------------------------------------------------------|
| **ID**           | RF-27                                                                   |
| **Name**         | Store Activation Keys                                                   |
| **Description**  | The system must store generated activation keys in files on the server. |
| **Actors**       | System                                                                  |
| **Restrictions** | Server storage must be available                                        |
| **Verification** | Validate activation key storage                                         |
| **Priority**     | High                                                                    |
| **State**        | Approved                                                                |

---

| **Field**        | **Value**                                                                                 |
| ---------------- |-------------------------------------------------------------------------------------------|
| **ID**           | RF-28                                                                                     |
| **Name**         | Upload Game Images                                                                        |
| **Description**  | Publishers must be able to upload game images to server.                                  |
| **Actors**       | Publisher                                                                                 |
| **Restrictions** | Publisher must be authenticated; file type and size must abide by the defined constraints |
| **Verification** | Validate file upload                                                                      |
| **Priority**     | High                                                                                      |
| **State**        | Approved                                                                                  |

---

| **Field**        | **Value**                                                     |
| ---------------- |---------------------------------------------------------------|
| **ID**           | RF-29                                                         |
| **Name**         | Validate Uploaded Files                                       |
| **Description**  | The system must validate the type and size of uploaded files. |
| **Actors**       | System                                                        |
| **Restrictions** | File type and size rules must be enforced                     |
| **Verification** | Validate file checks                                          |
| **Priority**     | High                                                          |
| **State**        | Approved                                                      |


## 3. Non-Functional Requirements

### 3.1 Security Requirements

| **Field**        | **Value**                                      |
| ---------------- | ---------------------------------------------- |
| **ID**           | RNF-01                                         |
| **Name**         | Password Storage                               |
| **Description**  | Passwords must be stored using BCrypt hashing. |
| **Actors**       | System                                         |
| **Restrictions** | None                                           |
| **Verification** | Validate password hashing                      |
| **Priority**     | Essential                                      |
| **State**        | Approved                                       |

---

| **Field**        | **Value**                                                                |
| ---------------- | ------------------------------------------------------------------------ |
| **ID**           | RNF-02                                                                   |
| **Name**         | JWT Authentication                                                       |
| **Description**  | Authentication must be performed using JWT with configurable expiration. |
| **Actors**       | System                                                                   |
| **Restrictions** | None                                                                     |
| **Verification** | Validate token generation                                                |
| **Priority**     | Essential                                                                |
| **State**        | Approved                                                                 |

---

| **Field**        | **Value**                                                                  |
| ---------------- | -------------------------------------------------------------------------- |
| **ID**           | RNF-03                                                                     |
| **Name**         | Endpoint Authentication                                                    |
| **Description**  | All endpoints (except registration and login) must require authentication. |
| **Actors**       | System                                                                     |
| **Restrictions** | None                                                                       |
| **Verification** | Validate authentication enforcement                                        |
| **Priority**     | High                                                                       |
| **State**        | Approved                                                                   |

---

| **Field**        | **Value**                                       |
| ---------------- | ----------------------------------------------- |
| **ID**           | RNF-04                                          |
| **Name**         | Role-Based Access Control                       |
| **Description**  | Access to resources must be restricted by role. |
| **Actors**       | System                                          |
| **Restrictions** | Roles must be predefined                        |
| **Verification** | Validate role-based access to resources         |
| **Priority**     | High                                            |
| **State**        | Approved                                        |

---

| **Field**        | **Value**                                      |
| ---------------- | ---------------------------------------------- |
| **ID**           | RNF-05                                         |
| **Name**         | HTTPS Communication                            |
| **Description**  | All communications must use HTTPS exclusively. |
| **Actors**       | System                                         |
| **Restrictions** | None                                           |
| **Verification** | Validate HTTPS enforcement                     |
| **Priority**     | Essential                                      |
| **State**        | Approved                                       |

---

| **Field**        | **Value**                                                      |
| ---------------- | -------------------------------------------------------------- |
| **ID**           | RNF-06                                                         |
| **Name**         | Input Validation                                               |
| **Description**  | User inputs must be validated and sanitized before processing. |
| **Actors**       | System                                                         |
| **Restrictions** | None                                                           |
| **Verification** | Validate input validation and sanitization                     |
| **Priority**     | High                                                           |
| **State**        | Approved                                                       |

---

| **Field**        | **Value**                                                              |
| ---------------- | ---------------------------------------------------------------------- |
| **ID**           | RNF-07                                                                 |
| **Name**         | Critical Event Logging                                                 |
| **Description**  | The system must log critical events (login, role changes, game order). |
| **Actors**       | System                                                                 |
| **Restrictions** | Logging storage must be available                                      |
| **Verification** | Validate event logging                                                 |
| **Priority**     | High                                                                   |
| **State**        | Approved                                                               |

---

| **Field**        | **Value**                                                            |
| ---------------- | -------------------------------------------------------------------- |
| **ID**           | RNF-08                                                               |
| **Name**         | Activation Key Generation                                            |
| **Description**  | Activation keys must be generated securely using UUID or equivalent. |
| **Actors**       | System                                                               |
| **Restrictions** | None                                                                 |
| **Verification** | Validate activation key generation                                   |
| **Priority**     | High                                                                 |
| **State**        | Approved                                                             |

---

| **Field**        | **Value**                                                                                   |
| ---------------- | ------------------------------------------------------------------------------------------- |
| **ID**           | RNF-09                                                                                      |
| **Name**         | Brute Force Protection                                                                      |
| **Description**  | The system must protect against brute force attacks using rate limiting on login endpoints. |
| **Actors**       | System                                                                                      |
| **Restrictions** | None                                                                                        |
| **Verification** | Validate rate limiting                                                                      |
| **Priority**     | High                                                                                        |
| **State**        | Approved                                                                                    |

---

| **Field**        | **Value**                                                                             |
| ---------------- | ------------------------------------------------------------------------------------- |
| **ID**           | RNF-10                                                                                |
| **Name**         | File MIME Verification                                                                |
| **Description**  | Uploaded files must be verified for their actual MIME type, not just their extension. |
| **Actors**       | System                                                                                |
| **Restrictions** | None                                                                                  |
| **Verification** | Validate file type verification                                                       |
| **Priority**     | High                                                                                  |
| **State**        | Approved                                                                              |


### 3.2 Performance Requirements

| **Field**        | **Value**                                                            |
| ---------------- | -------------------------------------------------------------------- |
| **ID**           | RNF-11                                                               |
| **Name**         | Game Listing Response Time                                           |
| **Description**  | The system must respond to game listing requests in less than 500ms. |
| **Actors**       | System                                                               |
| **Restrictions** | None                                                                 |
| **Verification** | Validate response time                                               |
| **Priority**     | High                                                                 |
| **State**        | Approved                                                             |

---

| **Field**        | **Value**                                              |
| ---------------- | ------------------------------------------------------ |
| **ID**           | RNF-12                                                 |
| **Name**         | Concurrent Users Support                               |
| **Description**  | The system must support at least 100 concurrent users. |
| **Actors**       | System                                                 |
| **Restrictions** | None                                                   |
| **Verification** | Validate system under concurrent load                  |
| **Priority**     | High                                                   |
| **State**        | Approved                                               |

---

| **Field**        | **Value**                                         |
| ---------------- | ------------------------------------------------- |
| **ID**           | RNF-13                                            |
| **Name**         | File Upload Size                                  |
| **Description**  | The system must support image uploads up to 25MB. |
| **Actors**       | System                                            |
| **Restrictions** | File size limit must be enforced                  |
| **Verification** | Validate file upload size limits                  |
| **Priority**     | High                                              |
| **State**        | Approved                                          |


### 3.3 Availability and Reliability Requirements

| **Field**        | **Value**                                                                      |
| ---------------- | ------------------------------------------------------------------------------ |
| **ID**           | RNF-14                                                                         |
| **Name**         | System Availability                                                            |
| **Description**  | The system must have a minimum availability of 99% in production environments. |
| **Actors**       | System                                                                         |
| **Restrictions** | None                                                                           |
| **Verification** | Validate system availability                                                   |
| **Priority**     | Essential                                                                      |
| **State**        | Approved                                                                       |

---

| **Field**        | **Value**                                       |
| ---------------- | ----------------------------------------------- |
| **ID**           | RNF-15                                          |
| **Name**         | Database Backup                                 |
| **Description**  | The database must have automatic daily backups. |
| **Actors**       | System                                          |
| **Restrictions** | Backup storage must be available                |
| **Verification** | Validate backup execution and recovery          |
| **Priority**     | High                                            |
| **State**        | Approved                                        |

---

| **Field**        | **Value**                                                                        |
| ---------------- | -------------------------------------------------------------------------------- |
| **ID**           | RNF-16                                                                           |
| **Name**         | API Resilience                                                                   |
| **Description**  | The system must be resilient to RAWG API failures, using fallback to local data. |
| **Actors**       | System                                                                           |
| **Restrictions** | Local data must be up-to-date                                                    |
| **Verification** | Validate fallback mechanism                                                      |
| **Priority**     | High                                                                             |
| **State**        | Approved                                                                         |


### 3.4 Maintainability Requirements

| **Field**        | **Value**                                                                         |
| ---------------- | --------------------------------------------------------------------------------- |
| **ID**           | RNF-17                                                                            |
| **Name**         | DDD Code Structure                                                                |
| **Description**  | Code must follow Domain-Driven Design principles with clear separation of layers. |
| **Actors**       | Developer Team                                                                    |
| **Restrictions** | None                                                                              |
| **Verification** | Validate code structure and architecture                                          |
| **Priority**     | High                                                                              |
| **State**        | Approved                                                                          |

---

| **Field**        | **Value**                                         |
| ---------------- | ------------------------------------------------- |
| **ID**           | RNF-18                                            |
| **Name**         | Database Migration Management                     |
| **Description**  | Database migrations must be managed using Flyway. |
| **Actors**       | Developer Team                                    |
| **Restrictions** | None                                              |
| **Verification** | Validate migration execution                      |
| **Priority**     | Medium                                            |
| **State**        | Approved                                          |

---

| **Field**        | **Value**                                                  |
| ---------------- | ---------------------------------------------------------- |
| **ID**           | RNF-19                                                     |
| **Name**         | Unit Test Coverage                                         |
| **Description**  | The project must have a minimum of 80% unit test coverage. |
| **Actors**       | Developer Team                                             |
| **Restrictions** | None                                                       |
| **Verification** | Validate test coverage                                     |
| **Priority**     | High                                                       |
| **State**        | Approved                                                   |

---

| **Field**        | **Value**                                                     |
| ---------------- | ------------------------------------------------------------- |
| **ID**           | RNF-20                                                        |
| **Name**         | CI/CD Static Analysis                                         |
| **Description**  | The CI/CD pipeline must execute static analysis on each push. |
| **Actors**       | System                                                        |
| **Restrictions** | None                                                          |
| **Verification** | Validate CI/CD pipeline execution                             |
| **Priority**     | High                                                          |
| **State**        | Approved                                                      |


### 3.5 Portability Requirements

| **Field**        | **Value**                                      |
| ---------------- | ---------------------------------------------- |
| **ID**           | RNF-21                                         |
| **Name**         | Containerization                               |
| **Description**  | The system must be containerized using Docker. |
| **Actors**       | System                                         |
| **Restrictions** | None                                           |
| **Verification** | Validate container build and deployment        |
| **Priority**     | High                                           |
| **State**        | Approved                                       |

---

| **Field**        | **Value**                                                  |
| ---------------- | ---------------------------------------------------------- |
| **ID**           | RNF-22                                                     |
| **Name**         | Environment Replication                                    |
| **Description**  | The environment must be reproducible using Docker Compose. |
| **Actors**       | Developers                                                 |
| **Restrictions** | None                                                       |
| **Verification** | Validate environment setup                                 |
| **Priority**     | High                                                       |
| **State**        | Approved                                                   |

---

| **Field**        | **Value**                                                                                 |
| ---------------- | ----------------------------------------------------------------------------------------- |
| **ID**           | RNF-23                                                                                    |
| **Name**         | Sensitive Configuration Management                                                        |
| **Description**  | Sensitive configurations must be injected via environment variables and never hard-coded. |
| **Actors**       | System                                                                                    |
| **Restrictions** | Environment variable injection required                                                   |
| **Verification** | Validate configuration management                                                         |
| **Priority**     | High                                                                                      |
| **State**        | Approved                                                                                  |
