# StrideHabits – Rubric Allocation README (DRAFT V1 - need to fill in real info) 

Purpose: This document maps each rubric requirement to concrete evidence in the repository (screenshots, videos, code, and configuration). The lecturer can follow this file top‑to‑bottom and verify each item.

> Replace all `TODO` markers and file names with your final evidence paths before submission.

---

## 1. Core Functional Features

### 1.1 Application runs on a physical mobile device

* **Rubric reference:** Functional – “App runs on mobile device / emulator.”
* **Implementation summary:**

  * StrideHabits is built as a native Android app using Kotlin, Jetpack Compose, MVVM, and Hilt.
  * The APK builds and installs on a physical Android device (tested on TODO: device name + Android version).
* **Evidence:**

  * Screenshot of the app running on a physical device home screen and main dashboard.
  * File: `docs/evidence/01-device-running.png`

### 1.2 Biometrics (secure access)

* **Rubric reference:** Functional – “Security / secure authentication.”
* **Implementation summary:**

  * Biometric unlock (fingerprint / face where supported) using AndroidX Biometric APIs.
  * Locked screen prevents access to habits until the user authenticates.
* **Evidence:**

  * Screenshot of the lock screen and biometric prompt.
  * File: `docs/evidence/02-biometrics-lock-screen.png`

### 1.3 Offline mode (local persistence)

* **Rubric reference:** Functional – “Offline support / local storage.”
* **Implementation summary:**

  * Room database stores habits, completions, streaks, and images locally.
  * Users can add, update, and complete habits while offline.
  * Background WorkManager sync pushes local changes to the remote API when connectivity returns.
* **Evidence:**

  * Screenshot showing airplane‑mode enabled and successful habit completion.
  * Optional: logcat snippet demonstrating queued WorkManager tasks.
  * File: `docs/evidence/03-offline-mode.png`

### 1.4 Real‑time / push notifications

* **Rubric reference:** Functional – “Notifications / reminders / background work.”
* **Implementation summary:**

  * Reminders scheduled locally using WorkManager / AlarmManager.
  * Optional remote push notifications via FCM for habit reminders or sync events.
* **Evidence:**

  * Screenshot of a notification firing on the device (showing habit name, time, and actions if applicable).
  * File: `docs/evidence/04-reminder-notification.png`

### 1.5 Multi‑language support

* **Rubric reference:** Functional – “Internationalisation / localisation.”
* **Implementation summary:**

  * App strings externalised into `strings.xml` and translated into TODO: languages (e.g., English, Afrikaans, isiZulu).
  * In‑app language selector in Settings updates the locale without reinstalling the app.
* **Evidence:**

  * Screenshot: same screen in two different languages.
  * Files: `docs/evidence/05-language-en.png`, `docs/evidence/06-language-alt.png`

### 1.6 Additional Feature 1 – TODO (rename)

* **Rubric reference:** Functional – “Additional feature 1.”
* **Feature name:** TODO (e.g., “Themes and appearance customisation”).
* **Implementation summary:**

  * TODO: 3–4 bullets explaining how this feature works.
* **Evidence:**

  * Screenshot(s) demonstrating the feature.
  * File: `docs/evidence/07-feature1.png`

### 1.7 Additional Feature 2 – TODO (rename)

* **Rubric reference:** Functional – “Additional feature 2.”
* **Feature name:** TODO (e.g., “Calendar view and streak heatmap”).
* **Implementation summary:**

  * TODO: 3–4 bullets explaining how this feature works.
* **Evidence:**

  * Screenshot(s) demonstrating the feature.
  * File: `docs/evidence/08-feature2.png`

---

## 2. User Interface and User Experience

### 2.1 Overall UI design

* **Rubric reference:** UI / UX – “Layout, navigation, and visual consistency.”
* **Implementation summary:**

  * Modern Compose UI with consistent spacing, typography, and colours.
  * Bottom navigation / drawer / top app bar patterns for discoverability.
  * Dark and light theme support.
* **Evidence:**

  * Collage or multiple screenshots showing:

    * Authentication flow
    * Dashboard / home screen
    * Habit detail and edit screens
    * Settings screen
  * File: `docs/evidence/10-ui-overview.png` (or multiple files: `10a`, `10b`, etc.)

### 2.2 Usability details

* **Rubric reference:** UI / UX – “Usability, feedback, and error handling.”
* **Implementation summary:**

  * Clear validation messages on forms.
  * Snackbars / dialogs for confirmations and errors.
  * Empty‑state screens with guidance text and CTA buttons.
* **Evidence:**

  * Screenshot(s) of validation errors and empty‑state UI.
  * Files: `docs/evidence/11-validation.png`, `docs/evidence/12-empty-state.png`

---

## 3. Code Quality, Version Control, and Testing

### 3.1 GitHub repository and branching

* **Rubric reference:** Process – “Use of version control.”
* **Implementation summary:**

  * Project hosted on GitHub at: `https://github.com/TODO/StrideHabits`.
  * Branch strategy (e.g., `main` + feature branches).
  * Conventional commit messages / semantic version tags if used.
* **Evidence:**

  * Screenshot of GitHub repo: commit history and branches.
  * File: `docs/evidence/20-github-history.png`

### 3.2 Automated tests

* **Rubric reference:** Process – “Testing (unit / instrumented tests).”
* **Implementation summary:**

  * `test/` folder: pure JVM unit tests (e.g., view models, mappers, utility classes).
  * `androidTest/` folder: instrumented tests (e.g., DAO tests, navigation, or basic UI flows).
  * Tests run via Gradle and/or Android Studio.
* **Evidence:**

  * Screenshot of green test run in Android Studio / Gradle.
  * Example test class names listed in this section.
  * File: `docs/evidence/21-tests-passing.png`

### 3.3 Quality checks (optional)

* **Rubric reference:** Process – “Code quality / static analysis” (if applicable).
* **Implementation summary:**

  * Optional tools: Detekt, Ktlint, or Android Lint.
  * Warnings resolved or documented.
* **Evidence:**

  * Screenshot of lint / Detekt report with minimal critical issues.
  * File: `docs/evidence/22-lint-report.png`

---

## 4. Play Store Readiness

### 4.1 Store listing content

* **Rubric reference:** Deployment – “App store readiness / documentation.”
* **Implementation summary:**

  * Draft Play Store listing prepared, including:

    * App name and short description
    * Full description
    * Feature graphics / icons
    * Screenshots
* **Evidence:**

  * Markdown subsection with final text used for the Store listing.

#### 4.1.1 Draft Play Store listing (text)

* **App name:** StrideHabits – Build Better Habits
* **Short description:**

  > TODO: 80‑character punchy description.
* **Full description:**

  > TODO: 3–5 short paragraphs describing features, offline mode, notifications, and privacy.

### 4.2 Product shots and assets

* **Evidence:**

  * Screen‑sized product shots matching Play Store guidelines.
  * Icon and feature graphic mock‑ups.
  * Files: `docs/evidence/30-playstore-screens-*.png`, `docs/evidence/31-app-icon.png`, `docs/evidence/32-feature-graphic.png`

### 4.3 Build configuration

* **Implementation summary:**

  * `versionCode` and `versionName` correctly set in `build.gradle`.
  * `applicationId` fixed and stable.
  * Release build type configured with minified / shrinked resources if applicable.
* **Evidence:**

  * Snippet or screenshot of `build.gradle` (module level) highlighting release config.
  * File: `docs/evidence/33-gradle-release-config.png`

---

## 5. AI Usage Declaration

* **Rubric reference:** Academic integrity – “Responsible use of AI tools.”
* **Summary:**

  * Brief explanation of where AI tools were used (e.g., idea generation, copyediting, code review) and where they were not used (e.g., no direct generation of full assignment deliverables without review).
  * Confirmation that all AI‑assisted content was critically reviewed, tested, and adapted by the student.
* **Evidence:**

  * Short narrative paragraph here (1–2 paragraphs max).
  * Optional reference to module / institutional AI policy.

Example structure:

> This project used AI tools for X, Y, Z. All generated content was reviewed, tested, and integrated by the team. No AI outputs were submitted without modification or critical evaluation. All architectural decisions, final code, and testing strategy were owned by the team.

---

## 6. Demonstration and Walkthrough Video

### 6.1 Demo video link

* **Rubric reference:** Demonstration – “Video walkthrough / presentation.”
* **Implementation summary:**

  * Recorded a full walkthrough of core features, settings, and offline behaviour.
* **Evidence:**

  * YouTube / OneDrive / Teams link:

    * URL: `https://TODO`
  * Timestamp outline:

    * `00:00 – 01:00` Introduction and login
    * `01:00 – 03:00` Creating and completing habits
    * `03:00 – 04:00` Notifications and calendar
    * `04:00 – 05:00` Settings (language, theme, privacy)
    * `05:00 – 06:00` Offline demo and sync

---

## 7. Quick Rubric Checklist

Use this mini‑table to quickly check that everything is covered before submission.

| Rubric item                        | Section / heading in this file            | Evidence file / link                        |
| ---------------------------------- | ----------------------------------------- | ------------------------------------------- |
| App runs on physical device        | 1.1 Application runs on a physical device | `01-device-running.png`                     |
| Biometrics / secure authentication | 1.2 Biometrics (secure access)            | `02-biometrics-lock-screen.png`             |
| Offline mode / local storage       | 1.3 Offline mode (local persistence)      | `03-offline-mode.png`                       |
| Notifications / reminders          | 1.4 Real‑time / push notifications        | `04-reminder-notification.png`              |
| Multi‑language support             | 1.5 Multi‑language support                | `05-language-en.png`, `06-language-alt.png` |
| Additional feature 1               | 1.6 Additional Feature 1                  | `07-feature1.png`                           |
| Additional feature 2               | 1.7 Additional Feature 2                  | `08-feature2.png`                           |
| UI / UX design                     | 2.1, 2.2 User Interface and UX            | `10-ui-overview.png`, etc.                  |
| Version control                    | 3.1 GitHub repository and branching       | `20-github-history.png`                     |
| Testing                            | 3.2 Automated tests                       | `21-tests-passing.png`                      |
| Store readiness                    | 4.1–4.3 Play Store Readiness              | `30-*`, `31-*`, `32-*`, `33-*`              |
| AI usage declaration               | 5. AI Usage Declaration                   | Text in section 5                           |
| Demo / presentation video          | 6. Demonstration and Walkthrough Video    | Demo video URL                              |

Update this table once all evidence files and links have been finalised.
