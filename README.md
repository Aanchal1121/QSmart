# QSmart — Inclusive & Intelligent Virtual Queue Management System

> **Official Problem Statement:**
> Smart Virtual Queue Management System: Build a responsive dashboard that enables users to join virtual queues, receive digital token numbers, track real-time waiting times, and receive notifications when their turn approaches. The platform reduces physical waiting, improves queue management, and provides a smooth, convenient experience for both users and service providers.

---

## 🌟 Our Core Innovation: 3 Channels • ONE Centralized Engine

In public hospitals and community healthcare centers across India and developing regions, a purely smartphone-dependent queue system inevitably excludes elderly citizens, rural patients, and low-income individuals who rely on basic keypad (feature) phones.

**QSmart solves this through an inclusive 3-channel architecture where all three channels feed into ONE single centralized FIFO queue engine:**

```
                  ┌────────────────────────────────────────┐
                  │          PATIENT TOUCHPOINTS           │
                  └────────────────────────────────────────┘
                    │                   │                 │
             [Channel A]          [Channel B]       [Channel C]
           Smartphone Web          Keypad SMS         Walk-in
             (Browser)          (No Internet)     (Receptionist)
                    │                   │                 │
                    ▼                   ▼                 ▼
          ┌────────────────────────────────────────────────────────┐
          │      Spring Boot 3 Centralized Queue Engine (FIFO)     │
          │      • Strict Sequence Ordering   • Duplicate Check    │
          │      • Priority Tagging (Seniors) • Audit Logging      │
          └────────────────────────────────────────────────────────┘
                    │                   │                 │
                    ▼                   ▼                 ▼
          ┌────────────────────────────────────────────────────────┐
          │                   MYSQL 8.0 DATABASE                   │
          │    (Tokens, Departments, Availabilities, Audit Logs)    │
          └────────────────────────────────────────────────────────┘
                    │                   │                 │
                    ▼                   ▼                 ▼
             WebSocket Live       2-Way SMS Reply     Print Slip
            (Patient/TV Room)      (Keypad Phone)       with QR
```

### 1. Channel A: Smartphone Users (Web UI)
- Book virtual tokens from any browser.
- Real-time queue position tracking (e.g., *"You are #2 in line"*).
- Dynamic statistical Estimated Waiting Time (ETA) calculation.
- Digital token pass with verification QR code.
- Real-time WebSocket notifications when turn approaches or delays occur.
- One-click token cancellation.

### 2. Channel B: Keypad Phone Users (SMS Gateway)
- **Zero internet and zero smartphone required.**
- Standard SMS commands:
  - `BOOK HOSPITAL01 OPD` — Books a token in General Medicine.
  - `STATUS OPD-001` — Returns live waiting position and ETA.
  - `CANCEL OPD-001` — Cancels active booking.
  - `HELP` — Lists syntax and instructions.
- Automated inbound webhook and outbound notification dispatcher.
- Includes an interactive **Mobile Keypad SMS Simulator** for local browser testing.

### 3. Channel C: Walk-in / Assisted Users (Staff Desk)
- Staff registers walk-in patients from the reception desk.
- Generates a printable or digital token slip with secure QR code.
- Enters the **exact same FIFO sequence** as smartphone and SMS bookings.

---

## ⚖️ Centralized Queue Fairness Policy

- **No Digital Queue-Jumping:** Online bookers do not receive unfair priority over physically waiting walk-in patients. All three channels follow the same sequence.
- **Transparent Priority Categories:** Senior Citizens (65+ years) and Persons with Disabilities (PwD) are supported through transparent, staff-approved rules.
- **Mandatory Skip Reasons:** If a patient is absent after being called, staff must record a mandatory reason (e.g., *"Absent after 3 calls"*), which is sent via SMS to the patient and logged to the permanent audit log.
- **No Automatic Mass Cancellations:** Doctor delays recalculate waiting times and alert affected patients without cancelling their spots.

---

## ⏱️ Mathematical Waiting Time Calculation

Waiting time is calculated using a dynamic statistical formula:

$$T_{\text{wait}} = \max\left(0, \text{round}\left(\frac{N_{\text{ahead}} \times D_{\text{avg}}}{\max(1, C_{\text{active}})}\right) + \Delta_{\text{delay}}\right)$$

Where:
- $N_{\text{ahead}}$ = Number of active waiting tokens ahead in this department today.
- $D_{\text{avg}}$ = Average service duration configured for the department (minutes).
- $C_{\text{active}}$ = Count of currently available and active service counters / doctors.
- $\Delta_{\text{delay}}$ = Current reported delay (minutes) due to emergencies or delays.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 3.2.5, Spring Security 6, Spring Data JPA, Hibernate |
| **Real-time** | Spring WebSocket, STOMP over SockJS, Fallback HTTP Polling |
| **Database** | MySQL 8.0 (Relational schema with InnoDB foreign keys & indexes) |
| **Frontend** | HTML5, CSS3, JavaScript (ES6+), Bootstrap 5, Bootstrap Icons |
| **Testing** | JUnit 5, Mockito, Spring Boot Test |
| **Build Tool** | Apache Maven 3.9.6 |

---

## 👥 Demo Accounts (1-Click Login Available)

All passwords are pre-seeded with BCrypt hashing:

| Role | Username / Email | Password | Responsibilities |
|---|---|---|---|
| **PATIENT** | `patient.amit@qsmart.com` | `Patient@123` | Book tokens, monitor live ETA, view QR slip, cancel booking |
| **STAFF** | `staff.rahul@qsmart.com` | `Staff@123` | Register walk-in, Call Next, Mark Served, Skip with reason, Pause queue |
| **DOCTOR** | `dr.sharma@qsmart.com` | `Doctor@123` | Availability status, consultation queue, report emergency delays |
| **ADMIN** | `admin@qsmart.com` | `Admin@123` | Analytics dashboard, multi-channel metrics, immutable audit logs |

---

## 🖥️ Screen Directory

| URL Path | Screen Name | Description |
|---|---|---|
| `http://localhost:8080/index.html` | **Landing Page** | Overview, 3-channel showcase, bilingual switcher (EN/हिंदी) |
| `http://localhost:8080/login.html` | **Login Page** | 1-click quick demo switcher for all 4 roles |
| `http://localhost:8080/register.html` | **Registration** | Self-registration for smartphone patients |
| `http://localhost:8080/patient-dashboard.html` | **Patient Portal** | Active token hero card, ETA, progress bar, QR pass, history |
| `http://localhost:8080/staff-dashboard.html` | **Staff Desk** | Walk-in registration, Call Next, Mark Served, Skip modal, delay |
| `http://localhost:8080/doctor-dashboard.html` | **Doctor Clinic** | Availability status, consultation call, emergency delay reporting |
| `http://localhost:8080/admin-dashboard.html` | **Admin Console** | System analytics, channel breakdown, audit log table |
| `http://localhost:8080/public-display.html` | **Waiting Room TV** | Large TV monitor view, audio chimes, speech announcements |
| `http://localhost:8080/sms-simulator.html` | **SMS Simulator** | Keypad mobile simulation with live 2-way SMS backend execution |

---

## 🇮🇳 Indian Telecom (DLT) Two-Way SMS Integration Guide

In production, QSmart connects to Indian Telecom SMS gateways via TRAI-mandated DLT compliance:

1. **DLT Entity Registration:** Register the hospital or health authority on telecom DLT portals (Jio DLT, VilPower, Airtel DLT, BSNL DLT).
2. **Sender ID (Header):** Obtain an approved 6-character alphabetic header (e.g., `QSMRT` or `QSMART`).
3. **Template Approval:** Register transactional and service implicit templates:
   - *Token Booked:* `QSmart: Token {#var#} confirmed for {#var#}. People ahead: {#var#}. Est wait: ~{#var#} mins.`
   - *Turn Called:* `ALERT: Token {#var#} is now called at {#var#} for {#var#}. Please proceed immediately.`
   - *Token Skipped:* `QSmart Notice: Token {#var#} was skipped: {#var#}. Please visit reception.`
4. **2-Way SMS Short/Long Code:** Map an approved 10-digit virtual number or 5-digit short code (e.g. `56767`) to QSmart's webhook:
   - `POST https://<your-domain>/api/sms/webhook`
5. **Gateway Provider:** Supported via `SmsProvider` interface (Gupshup, Textlocal, Exotel, Kaleyra, Twilio).

---

## 🚀 How to Run Locally

### Prerequisites
- Java 21+
- MySQL 8.0+ running on `localhost:3306`

### Running the Application
```bash
# 1. Ensure MySQL database is created (or application will auto-create):
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS qsmart_db;"

# 2. Build and run tests with Maven:
mvn test

# 3. Package and run:
mvn package -DskipTests
java -jar target/qsmart-queue-system-1.0.0.jar
```

The application will start on **`http://localhost:8080`**.
