# 🏃‍♂️ Stride — Android Habit Tracker

Stride is a Kotlin-based Android habit tracking app built with a clean **MVVM architecture**, **Hilt dependency injection**, and **Jetpack Compose** for a modern, responsive UI.
It integrates with the **SummitAPI** backend for authentication, habit storage, and habit analytics.

> 🧭 Full documentation and development notes are available on the Wiki:
> 🔗 [https://github.com/StrideHabits/Stride/wiki](https://github.com/StrideHabits/Stride/wiki)

---

## 🔍 Overview

| Feature              | Description                                                    |
| -------------------- | -------------------------------------------------------------- |
| **Authentication**   | Secure login via email/password and Google SSO.                |
| **Habits & Streaks** | Create habits, track completion, and maintain streaks.         |
| **Calendar View**    | Visual overview of habit completion over time.                 |
| **Reminders**        | Local notifications and reminder scheduling per habit.         |
| **Themes**           | Light/dark and accent theme configuration in Settings.         |
| **Settings**         | Notification, theme, language, and sync preferences.           |
| **Backend**          | Cloud-hosted REST API built in ASP.NET Core 8 (**SummitAPI**). |
| **Monitoring**       | Firebase Crashlytics & Performance for production diagnostics. |

---

## 🎯 Purpose of the App

Stride helps users build and maintain positive daily habits by making tracking, streaks, and reminders simple and visual. The app focuses on:

* Turning vague goals into concrete, trackable habits.
* Giving users fast feedback via streaks, calendars, and daily checklists.
* Reducing friction with biometric unlock, offline support, and local notifications.
* Keeping data private and secure through authenticated access and device‑local image storage.

The app is designed to be “production ready”: stable on real devices, resilient to network failures, and suitable for publishing to the Google Play Store as a fully functioning habit tracker.

---

## 🧱 Design Considerations

Key design decisions made during development:

* **Architecture & Maintainability**: MVVM + Repository with clear separation of concerns, so UI, data, and domain logic can evolve independently.
* **Offline‑first behaviour**: Habits and check‑ins are cached locally, with background sync workers to push changes to SummitAPI when connectivity is available.
* **User experience**: Jetpack Compose + Material 3 for a modern, responsive UI; calendar and streak views to make progress highly visible; biometrics to reduce login friction.
* **Resilience & error handling**: Structured `ApiResult` types, explicit error messaging, and non‑blocking image handling so core flows continue even when network calls fail.
* **Security & privacy**: Authentication via SummitAPI, HTTPS for all remote traffic, and device‑local storage for habit images to avoid third‑party blob dependencies.
* **Configurability**: Themes, language selection, and notification preferences exposed via a dedicated Settings screen for better user control.

These considerations ensure the app is robust enough for real users while still being easy to understand and extend for future coursework or features.

---

## 🔁 GitHub & GitHub Actions

Version control and automation are central to how Stride was developed and delivered:

* **GitHub repository**: All source code, issues, and documentation (including the Wiki and release notes) are hosted in a single public repository.
* **Branching & commits**: Features and fixes are developed on topic branches and merged into `main` with descriptive commit messages, providing a clear history of changes.
* **Tags & releases**: Semantic tags are used to mark key milestones; the final submission is tagged as the **Final POE** release for easy verification.
* **GitHub Actions CI**: A workflow in `.github/workflows/` automatically restores dependencies, builds the Android project, and runs tests on every push and pull request to `main`.

  * This ensures the app compiles on a clean environment, not just on a single developer machine.
  * Failing builds or tests surface early in pull requests, enforcing a basic quality gate.

Together, GitHub and GitHub Actions provide traceability (who changed what and when), automated verification of the codebase, and evidence of professional development practices for the POE.

---

## 🧩 Project Structure

```text
com.mpieterse.stride/
├── core/       → Domain models, DI modules, services, utilities
├── data/       → DTOs, repositories, network + persistence integration
└── ui/         → Jetpack Compose screens (startup, shared, central)
```

The app follows a modular **MVVM + Repository** pattern:

* ViewModels expose state via `StateFlow`.
* UI screens use `collectAsStateWithLifecycle`.
* Repositories coordinate local + remote data, wrapping calls to **SummitAPI**.
* Hilt is used for dependency graph wiring and scoped injections.

---

## ⚙️ Tech Stack

| Layer            | Technology                                    |
| ---------------- | --------------------------------------------- |
| **Language**     | Kotlin                                        |
| **UI**           | Jetpack Compose + Material 3                  |
| **Architecture** | MVVM + Repository                             |
| **DI**           | Hilt (Dagger)                                 |
| **Networking**   | Retrofit + OkHttp                             |
| **Async**        | Kotlin Coroutines + Flows                     |
| **Monitoring**   | Firebase Crashlytics & Performance Monitoring |
| **Backend**      | SummitAPI (C# ASP.NET Core 8 + SQLite)        |

---

## 🚀 Getting Started

### 1. Prerequisites

* Android Studio Jellyfish (or newer) with:

  * Kotlin support
  * Android Gradle Plugin 8+
* JDK 17+
* A running instance of **SummitAPI** (local or hosted)

### 2. Clone the Repository

```bash
git clone https://github.com/StrideHabits/Stride.git
cd Stride
```

### 3. Open in Android Studio

1. Open Android Studio.
2. Select **Open an existing project**.
3. Choose the `Stride` directory.
4. Let Gradle sync complete.

### 4. Configure API Endpoint

Configure the **SummitAPI** base URL in the appropriate configuration file (see the Wiki for the current location and naming):

* Set the base URL for:

  * Authentication endpoints
  * Habits, check-ins, and reminders
* Optionally configure a separate **dev** and **prod** endpoint.

> For detailed setup steps and API routes, see the Wiki:
> [https://github.com/StrideHabits/Stride/wiki](https://github.com/StrideHabits/Stride/wiki)

### 5. Build & Run

1. Select a device (physical or emulator, Android 8.0+ recommended).
2. Click **Run ▶** in Android Studio.

---

## 📐 Architecture Highlights

* **MVVM** ViewModels drive UI state and handle user events.
* **Hilt** provides constructor injection for ViewModels, repositories, and services.
* **Repository layer** mediates between UI and SummitAPI.
* **Type-safe networking** via Retrofit interfaces and DTOs.
* **Error handling** and loading states exposed through UI state models.

---

## 📸 Screenshots

**Firebase Performance dashboard**

![Firebase Performance](https://github.com/user-attachments/assets/c0512861-15d6-493a-b77e-3a76f3a9c310)

---

### Core Flows

**App running on device** 

<img width="484" height="1022" alt="image" src="https://github.com/user-attachments/assets/5adbba63-9e8b-4b26-aae4-22e4305a2ff7" />


**Create habit** 

<img width="469" height="1013" alt="image" src="https://github.com/user-attachments/assets/a7ede389-22c1-4c59-af80-37cb57d3c2db" />


**Calendar view** 

<img width="478" height="1021" alt="image" src="https://github.com/user-attachments/assets/ce44aada-b547-4926-9411-58b3c17ca872" />


**Edit habit** 

<img width="483" height="1017" alt="Edit habit" src="https://github.com/user-attachments/assets/9b0987f8-36fd-4205-8cad-7bd9a04ddbfa" />

---

### Reminders & Settings

**Habit reminders list** 

<img width="477" height="1017" alt="image" src="https://github.com/user-attachments/assets/7c1c7aba-a56f-46f4-b474-b9a1d4ecfc0c" />


**Add reminder** 


<img width="491" height="1016" alt="image" src="https://github.com/user-attachments/assets/82fd46ca-f6bc-461f-a21a-b8508354562c" />

**Settings screen** 

<img width="489" height="1014" alt="image" src="https://github.com/user-attachments/assets/f0293a73-77e3-4358-b187-acd4b9c4bf1a" />

---

### Debug / Developer Tools

**Debug screen** 

<img width="465" height="1014" alt="Debug tools" src="https://github.com/user-attachments/assets/8723bb5d-1f70-4453-9cf5-a4f2ace41182" />

---

## 📝 Release Notes – Part 2 → Part 3

This section summarises the work completed between **7 October and 19 November 2025** for Part 3 of the POE. It explains exactly which features were implemented or changed relative to the Part 2 prototype.

### New Features

* **Offline sync with SummitAPI**: Introduced a Room database that caches habits and check‑ins locally, with background workers that sync changes to SummitAPI when a network connection is available.
* **Calendar view**: Added a dedicated calendar screen that shows which days a habit was completed, supporting streak calculations and monthly overviews.
* **Notification system**:

  * Local notifications per habit using WorkManager and Alarm/Worker scheduling.
  * Reusable templates and a 24‑hour time picker to avoid AM/PM confusion.
  * Runtime notification permission handling for Android 13+ and in‑app guidance if permissions are denied.
* **Offline image support**:

  * Users can attach images to habits.
  * Images are stored locally on the device inside app‑private storage.
  * The previous remote image upload to SummitAPI was removed to avoid blob storage issues and to improve privacy.
* **Theme support**:

  * Upgraded to Material 3 and introduced a theming system.
  * Foundation in place for light/dark themes and accent colour variations exposed through the Settings screen.
* **Biometrics & security improvements**:

  * Hardened biometric authentication with clearer fallbacks to PIN/password when biometrics are unavailable or fail.

### UI / UX Improvements

* Applied Material 3 styling consistently across core flows (habit lists, detail views, dialogs, and settings).
* Added smoother navigation transitions between the home dashboard, habit viewer, calendar, and reminders.
* Reorganised **Settings** to group theme, notification, language, and privacy options more logically.
* Removed or renamed ambiguous toggles and cleaned up overflow menus so only fully‑supported features are exposed.

### Data & Privacy Changes

* Switched habit image storage from remote uploads to **local‑only** storage for better privacy and to remove the dependency on external blob storage.
* Limited SummitAPI usage to text‑based habit data (names, schedules, frequency, completion flags, etc.).
* Updated in‑app wording and supporting websites to emphasise that Stride Habits is a **student project**, not a medical or commercial app.

### Testing & Quality

* Added unit tests for:

  * Habit scheduling and due/overdue calculations.
  * Streak calculations over calendar days.
  * Mapping between Room entities and SummitAPI DTOs.
* Introduced repository tests using in‑memory Room to validate read/write behaviour and schema migrations.
* Added simple ViewModel tests to confirm that UI state (loading, error, and success) responds correctly to repository results.
* Integrated these tests into the GitHub Actions CI workflow so they run automatically on each push to `main`.

### Supporting Websites

* **Privacy Policy site** (GitHub Pages): Documents how SummitAPI and Firebase are used, explains local‑only image storage, and clarifies that the backend is temporary and will be decommissioned after the project.
* **Help & FAQ site** (GitHub Pages): Explains how to add/edit/delete habits, how the calendar and streaks work, how reminders behave (including edge cases on some Android OEMs), and common troubleshooting steps.

These changes represent the Part 3 evolution from a working prototype (Part 2) to a more robust, offline‑capable, privacy‑aware, and Play‑Store‑ready habit tracking app.

---

## 📚 Documentation

In-depth documentation (architecture, data flow, API integration, and implementation notes) is maintained on the project Wiki:

* Project overview
* SummitAPI integration
* Screens & navigation
* Debug tools and internal utilities

🔗 [https://github.com/StrideHabits/Stride/wiki](https://github.com/StrideHabits/Stride/wiki)

---

## 👥 Contributors

Development for **Stride (Android client)** was completed as part of **Varsity College BCAD** coursework.

| Name                 | Student Number |
| -------------------- | -------------- |
| **Musa Ntuli**       | ST1029336      |
| **Dean Gibson**      | ST10326084     |
| **Fortune Mabona**   | ST10187287     |
| **Matthew Pieterse** | ST10257002     |

---

## 📄 License / Usage

This project was developed for **Varsity College BCAD** coursework.

* Intended for educational and assessment purposes.
* Redistribution, commercial use, or rebranding without permission is **not** allowed.
* SummitAPI and related backend services may be deployed only for demo, testing, or grading environments.
