# 🛡️ Reactive Chat Core with Zero-Trust Security Implementation

**A messaging engine focused on security and concurrency, built with Java 21 and Spring WebFlux.**

[![Security](https://img.shields.io/badge/Security-Zero%20Trust-green)](https://github.com/Steph7478/)
[![Reactive](https://img.shields.io/badge/Engine-WebFlux-blue)](https://projectreactor.io/)
[![Java](https://img.shields.io/badge/Language-Java%2021-orange)](https://www.oracle.com/java/)

---

### ⚠️ Learning & Portfolio Project
This repository is a **dedicated learning and practice project**. It was developed to master advanced concepts in **Reactive Programming**, **Asymmetric Cryptography**, and **Zero-Trust Security Architectures**. It serves as a technical showcase of backend engineering patterns and the Java 21 ecosystem.

---

## 🎯 Strategic Overview

Real-time messaging core designed to explore **Zero-Trust** security patterns. Using **Java 21** and **Project Reactor**, the system implements a non-blocking lifecycle where no connection or hardware fingerprint is trusted without continuous, reactive verification.

The implementation focuses on robust session management, preventing "Ghost Sockets," and ensuring session integrity through cryptographic binding.

---

## 🚀 Security Architecture & Technical Checklist

### 1. 🔐 Authentication & Token Management (Auth Hardening)
* **Asymmetric JWT (RS256):** Signed with RSA key pairs (Private signs, Public validates) via `KeyProvider.java`, ensuring only the backend can issue valid tokens.
* **BFF (Backend-for-Frontend):** An intermediary layer where the frontend does not store tokens accessible by JS, preventing XSS-based theft.
* **Military-Grade Cookies:** Strict implementation of `HttpOnly`, `Secure`, `SameSite=Strict`, and the `__Host-` prefix.
* **Refresh Token Rotation:** Single-use tokens with reuse detection that invalidates the entire token family upon fraud attempt.
* **Key Rotation:** Periodic rotation of RSA keys using the `kid` (Key ID) header via `KeyRotationScheduler.java`.
* **Silent JWT Refresh:** Background token updates via BFF to maintain the WebSocket connection without user interruption.

### 2. 👤 Session Management & Anti-Hijacking
* **Cryptographic Fingerprinting:** SHA-256 hash of `userId + User-Agent + IP` to bind the session to a specific device.
* **Strict Session Binding:** Fingerprint comparison on every request and during the initial WebSocket handshake.
* **Multi-Session Registry:** Granular control of multiple sessions per user, allowing remote termination of specific instances.
* **Inconsistency Alerts & Cleanup:** Automatic anomaly logging and immediate termination if connection metadata (IP/UA) changes mid-session.

### 3. 🌐 WebSocket Security (Real-Time)
* **Handshake Validation:** Rigorous JWT and Fingerprint verification before the protocol upgrade.
* **WSS (WebSocket Secure):** Total block of unencrypted connections.
* **Context-Based Messaging:** Targeted delivery structure using `userId + fingerprint` identifiers.
* **Idle Timeout & Heartbeat:** Active monitoring via Ping/Pong frames to prevent "Ghost Sockets" and free up resources.

### 4. 🧪 Data Integrity & Payload Defense
* **Immutable DTOs:** Leveraging **Java 21 Records** for messages (`ChatMessage`), ensuring thread-safe data integrity.
* **Safe JSON Parsers:** ObjectMapper configuration to defend against recursion and polymorphism attacks.
* **Global Sanitization:** Filters against SQL Injection, NoSQL Injection, and XSS in all text fields via `JsonSanitizer.java`.
* **Schema Validation:** Mandatory Bean Validation (JSR-303) on every reactive payload received.

### 5. 🥊 Active Defense & Resilience
* **Reactive Rate Limiting:** Connection and message throttling per IP and User using Redis, protecting against flooding.
* **HSTS (HTTP Strict Transport Security):** Enforcing secure traffic across the entire domain.

### 📜 Auditing & Monitoring (Logback)
* **Security Audit Trail:** Dedicated `security-audit.log` recording logins, logouts, signature failures, and key swaps.
* **PII Masking:** Ensuring sensitive data and tokens are never exposed in logs.
* **Session Tracing:** Unique Correlation ID linking HTTP requests to WebSocket events for end-to-end observability.

---

## ⚡ Reactive Architecture & Featured Operators

The `ChatHandler` coordinates four independent reactive pipelines. This architecture allows the system to handle high concurrency with low memory overhead.



**The Handshake & Stream Lifecycle:**
1. **`initial`**: Validates security context, registers the session, and broadcasts system notifications.
2. **`send`**: Manages the outbound heartbeat stream, tied to the session's `closeStatus()`.
3. **`receive`**: Filters and processes incoming messages, including rate limiting and routing.
4. **`idle`**: Monitors activity to ensure resources are freed when no longer in use.

**Examples of Reactive Operators Used:**
To coordinate these flows, the project utilizes several Project Reactor operators, such as:
* **`Mono.when(send, receive, idle)`**: To orchestrate the parallel execution of the session's main streams.
* **`takeUntilOther`**: To gracefully terminate the heartbeat stream upon session closure.
* **`flatMap`**: For sequential processing of rate limiting and message routing.
* **`then(cleanup)`**: To guarantee the execution of unregistration and audit logging after the stream ends.

---

## 🚧 Project Status

### Backend (Java 21 Core)
The core messaging and security logic is fully implemented, following a reactive and non-blocking approach.

### Frontend (In Progress) 🏗️
The frontend is being developed in **Angular**, with a focus on:
* **E2EE (End-to-End Encryption):** Moving toward a model where the backend acts as a zero-knowledge relayer.

---

## ⚙️ Installation and Running

### Prerequisites
* **Java 21**
* **Maven 3.x**
* **Redis** (Required for session management and rate limiting)

### Steps
1. **Clone the repository:**
