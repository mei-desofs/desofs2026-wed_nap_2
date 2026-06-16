[← Back to requirements index](../Requirements/requirements.md)

# Input Validation Requirements

## 1. Introduction

### 1.1 Document Identification

**ArcadeHaven Input Validation Requirements Document**

### 1.2 Purpose

This document defines the rules and constraints for validating all input data processed by the ArcadeHaven system. It specifies expected formats, structures, and constraints for user-provided data.


## 2. Input Validation Rules

### 2.1 User Validations

| Data Item    | Constraint Rules                                                                                      |
| ------------ | ----------------------------------------------------------------------------------------------------- |
| username     | Required. Username must not be null or blank and have a maximum length of 50 characters.              |
| email        | Required. Email must be unique, have a maximum length of 50 characters and follow valid email format. |
| role         | Required. Role must be a valid value in `Role` enum.                                                  |
| active       | Admin Restricted. Active must be either True or False (Boolean).                                      |

---

### 2.2 Game Validations

| Data Item   | Constraint Rules                                                                                                   |
| ----------- | ------------------------------------------------------------------------------------------------------------------ |
| title       | Required. Title must not be null or blank and have a maximum length of 100 characters.                             |
| description | Optional. Description must have a maximum length 1000 character.                                                   |
| price       | Required. Price must a positive number value between 0.01 and 9999.99 with an enforced maximum of 2 decimal places |
| status      | Optional. Status must be a valid value in `GameStatus` enum. Default value must be `PENDING`.                      | 
| rawgApiId   | Optional. RAWG API id must have a maximum length of 50 characters.                                                 |
| category    | Optional; Category must have a maximum length of 100 characters.                                                   |
| publisher   | Required. Publisher must reference a valid Publisher entity in `User` table.                                       |
| files       | Optional. Files must reference valid File entities in `GameFile` table.                                            |

---

### 2.3 Game File Validations

| Data Item  | Constraint Rules                                                                 |
| ---------- | -------------------------------------------------------------------------------- |
| filename   | Required. Must not be null or blank and have a maximum length of 255 characters. |
| path       | Required. Must not be null or blank and have a maximum length of 500 characters. |
| fileType   | Required. Must be a valid value in `FileType` enum.                              |

---

### 2.4 Library Validations

| Data Item | Constraint Rules                                                                                              |
| --------- | ------------------------------------------------------------------------------------------------------------- |
| user      | Required. Must reference a valid `User` entity and each user may have at most one Library (1:1 relationship). |
| entries   | Optional. Must contain a list of valid `LibraryEntry`.                                                        |

---

### 2.5 Library Entry Validations

| Data Item     | Constraint Rules                                                                                         |
| ------------- | -------------------------------------------------------------------------------------------------------- |
| game          | Required. Must reference a valid `Game` entity.                                                          |
| activationKey | Required. Must not be null or blank, be unique and have a valid 128-bit CSPRNG format. |
| status        | System Restricted. Must be a valid value in `EntryStatus` enum. Default value must be `ACTIVE`.                             |

---

### 2.6 Order Validations

| Data Item   | Constraint Rules                                                                                                                                           |
| ----------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| buyer       | Required. Must reference a valid `User` entity.                                                                                                            |
| items       | Required. List of items, where each item must reference a valid `OrderItem`.                                                                               |
| status      | System Restricted. Must be a valid value in `OrderStatus` enum. Default value must be `PENDING`.                                                           |
| totalPrice  | Optional. Must be a positive value between 0.00 and 1,000,000.00 with an enforced maximum of 2 decimal places. Must not exceed system maximum (`MAX_TOTAL = 1,000,000.00`). |
| invoicePath | System Generated. Must be a valid file with a maximum length of 500 characters.                                                                            |

### 2.7 Order Item Validations

| Data Item     | Constraint Rules                                                                                                            |
| ------------- | --------------------------------------------------------------------------------------------------------------------------- |
| order         | Required. Must reference a valid `Order` entity.                                                                            |
| game          | Required. Must reference an existing `Game` entity.                                                                         |
| price         | Required. Must be a positive decimal value between 0.01 and 9999.99 with an enforced maximum of 2 decimal places.           |
| activationKey | System Generated. Must follow a valid 128-bit CSPRNG format.                                                                |


## 3. Combined Data Validation Rules

| Data Items | Constraint Rules |
| ---------- | ---------------- |
| Cross-table References | All foreign key fields must reference an existing and valid entity in the target table. |
| Game and OrderItem price | `OrderItem` price must be a copy of `Game` price at time of order conclusion. Changes in `Game` price must not affect existing `OrderItem.price` values. |
| Invoices and OrderItem price | When generating Invoices, total price must be match the total sum of all `OrderItem` price values. |
| LibraryEntry and OrderItem activation key | When activating a library key after finishing an error, `LibraryEntry` key must match the `OrderItem` key. |


## 4. Per-Entity Logic Limits

| Constraint Entities                     | Constraint Rules                                                                                               |
| --------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| User and Library (1:1)                  | Each `User` cannot have more than one `Library`. A `Library` must uniquely reference a single `User`.          |
| Library and unique game in LibraryEntry | A `Library` must not contain more than one `LibraryEntry` referencing the same `Game`. Duplicate `Game.id`     |
| Order and User (1:1)                    | Each `Order` cannot be associate to more than one `User`. An `Order` must uniquelly reference a single `User`. |
| Order and unique game in OrderItem      | An `Order` must not contain multiple `OrderItem` entries referencing the same `Game`.                          |


## 5. Global Logic Limits

| Constraint             | Constraint Rules                                                                                                               |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| Order total limit      | The calculated `Order.totalPrice` cannot exceed a total **1,000,000.00**. Any order exceeding this limit must be rejected.     |
| Price consistency      | All price related calculations must use `BigDecimal` with an enforced maximum of 2 decimal places.                             |
| Game purchase validity | Only `ACTIVE` games must be visible in filters and libraries. `LibraryEntry` and `OrderItem` cannot allow non `ACTIVE` games.  |

