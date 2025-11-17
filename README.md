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

**App running on device** <img width="390" height="878" alt="Stride app running" src="https://github.com/user-attachments/assets/ea3d3b6c-1b48-420d-ad4f-3e79dbb514b2" />

**Create habit** <img width="481" height="1004" alt="Create habit" src="https://github.com/user-attachments/assets/864b6ca6-44a6-4eed-a5d0-ca52af41c64a" />

**Calendar view** <img width="486" height="1015" alt="Calendar view" src="https://github.com/user-attachments/assets/2886f46d-c4a3-48ef-8e92-9d53e58d1470" />

**Edit habit** <img width="483" height="1017" alt="Edit habit" src="https://github.com/user-attachments/assets/9b0987f8-36fd-4205-8cad-7bd9a04ddbfa" />

---

### Reminders & Settings

**Habit reminders list** <img width="462" height="1014" alt="Habit reminders" src="https://github.com/user-attachments/assets/eaab4530-674d-483b-a5c4-1eaf59cda14b" />

**Add reminder** <img width="473" height="1013" alt="Add reminder" src="https://github.com/user-attachments/assets/763ac4df-3df5-4d29-b955-f550868243f9" />

**Settings screen** <img width="464" height="1009" alt="Settings screen" src="https://github.com/user-attachments/assets/26c87a7d-7302-4312-aa2a-13a95a87d388" />

---

### Debug / Developer Tools

**Debug screen** <img width="465" height="1014" alt="Debug tools" src="https://github.com/user-attachments/assets/8723bb5d-1f70-4453-9cf5-a4f2ace41182" />

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
