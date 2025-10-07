# 🏃‍♂️ Stride — Android Habit Tracker

**Stride** is a Kotlin-based Android habit tracking application built with a clean **MVVM architecture**, **Hilt dependency injection**, and **Jetpack Compose** for a modern, responsive UI. It integrates with the **SummitAPI** backend for authentication, habit tracking, and analytics. Firebase is used for **performance monitoring**, **logging**, **Crashlytics**, and **SSO (Google Sign-In)** — not as the primary data store.

---

## 🚀 Features

* **User Authentication** — Secure email/password and Google SSO sign-in.
* **Habit Management** — Add, edit, and delete daily habits seamlessly.
* **Check-In Tracking** — Record and view your progress per habit.
* **Performance Analytics** — Integrated with Firebase Performance and Crashlytics.
* **Modern UI/UX** — Built with Jetpack Compose and Material3 for smooth animations and consistent design.
* **Cloud-Synced** — All data is stored on **SummitAPI**; no local Room database is currently used.
* **Lightweight Navigation** — Single-activity structure with Compose `NavHost` and modular roots.
* **Composable Architecture** — Each UI module (startup, shared, central) is structured for reusability.

---

## 🧩 Project Structure

```
app/
├── manifests/
├── kotlin+java/
│   └── com.mpieterse.stride/
│       ├── core/
│       │   ├── dependencies/          # Dependency injection modules (Hilt)
│       │   ├── models/                # Core app models (User, Habit, etc.)
│       │   ├── net/                   # Network utilities, API configs
│       │   ├── services/              # Core Android & Firebase services
│       │   ├── utils/                 # Common tools (Clogger, formatters, etc.)
│       │   └── LocalApplication.kt    # Custom Application class
│       │
│       ├── data/
│       │   ├── dto/                   # Data transfer objects (auth, habits, check-ins, etc.)
│       │   ├── local/                 # Local stores (notifications, tokens)
│       │   ├── remote/                # Retrofit network module + Summit API service
│       │   └── repo/                  # Repositories for each feature (auth, habits, etc.)
│       │
│       └── ui/
│           ├── layout/
│           │   ├── central/           # Main app views, dashboards, viewmodels
│           │   ├── shared/            # Common UI components used across modules
│           │   ├── startup/           # Authentication & startup flow (AuthNavGraph, screens)
│           │   └── theme/             # Material3 theme setup
│           
│           └── README.md (per section)
```

---

## 📱 How to Use

1. **Clone the repository:**

   ```bash
   git clone https://github.com/username/stride.git
   ```

2. **Open in Android Studio:**

   * Open the `stride` folder.
   * Let Gradle sync dependencies automatically.

3. **Run the app:**

   * Configure an emulator or connect a device.
   * Click ▶️ *Run* to start the project.

4. **Sign Up or Sign In:**

   * Create a new account or use Google SSO.
   * Data will sync to SummitAPI (cloud-hosted backend).

---

## ⚙️ Tech Stack

| Layer                    | Technology                                 |
| ------------------------ | ------------------------------------------ |
| **Language**             | Kotlin                                     |
| **UI**                   | Jetpack Compose + Material3                |
| **Architecture**         | MVVM                                       |
| **DI**                   | Hilt                                       |
| **Networking**           | Retrofit 2 + OkHttp                        |
| **Logging & Monitoring** | Firebase Crashlytics, Performance, Clogger |
| **Backend API**          | SummitAPI (ASP.NET Core + SQLite)          |
| **Authentication**       | JWT + Google SSO                           |

---

## 🧠 Development Insights

This project demonstrates a modern mobile app connected to a C# ASP.NET Core REST API. The structure follows clean architecture principles, emphasizing separation of concerns, modular repositories, and dependency injection for maintainability and testing.

> Reference: *App Dev Insights (2024). Repository Design Pattern in Kotlin.* [Medium](https://medium.com/@appdevinsights/repository-design-pattern-in-kotlin-1d1aeff1ad40)

> Reference: *Android Developers (2025). Dependency Injection in Android.* [Android Docs](https://developer.android.com/training/dependency-injection)

---

## 🌐 SummitAPI Overview

**SummitAPI** is the RESTful backend supporting Stride. Developed with **C# ASP.NET Core 8**, it provides endpoints for authentication, habit management, and check-in tracking. It uses **SQLite** as a lightweight relational database and issues **JWT tokens** for secure access.

**Example Endpoints:**

* `POST /api/users/register` → Register a new user
* `POST /api/users/login` → Authenticate and receive JWT
* `GET /api/habits` → Fetch all user habits
* `POST /api/checkins` → Log a daily habit check-in

> Reference: *GeeksforGeeks (2017). Introduction to Retrofit in Android.* [GeeksforGeeks](https://www.geeksforgeeks.org/android/introduction-retofit-2-android-set-1/)

---

## 📚 References and Citations

This project has been extensively referenced to ensure proper academic and professional standards. All major functions and methods that make the app work have been documented with inline comments and citations.

### 📖 Reference Documentation

**Complete reference list:** [`references.md`](./references.md)

The `references.md` file contains comprehensive documentation of all sources used throughout the codebase, including:

- **Android Development References** - ViewModel, DataStore, Biometric, Compose, Credential Manager
- **Firebase References** - Authentication and API documentation  
- **Network & API References** - Retrofit and networking documentation
- **Architecture Pattern References** - Repository pattern documentation
- **Kotlin References** - Coroutines, Data Classes, Enums, Sealed Classes
- **Dependency Injection References** - Hilt documentation
- **Material Design References** - Material Design 3 and components
- **Navigation References** - Navigation component documentation
- **State Management References** - StateFlow and SharedFlow documentation

### 🏷️ Inline Citations

Throughout the codebase, major functions and methods include inline citations following this format:
```kotlin
fun methodName() { //This method [description] using [technology] ([Source], [Year]).
```

**Referenced Components Include:**
- Authentication services and user management
- Repository pattern implementations
- API service interfaces and networking
- ViewModel lifecycle management
- UI components and Jetpack Compose
- Data transfer objects and models
- Configuration and settings management
- Local storage and caching

---

# Part 2 — App Prototype Development Summary

This document outlines what has been completed for each criterion in **Part 2: App Prototype Development** of the Stride / Summit project.

---

## ✅ Application Functionality

### App runs on a mobile device

The app runs smoothly on both emulator and physical Android devices. All major features operate without crashes or performance issues.

---

## 🔐 Authentication & Core Features

### Feature 1 — Credential Login

Implements secure email and password login using Firebase Authentication and the Summit API. Handles validation correctly and integrates with the backend for secure access.

### Feature 2 — Daily Logging & Streaks

Allows users to log their daily progress and track streaks. Data is persisted and synced with the API, forming a core part of the app’s habit-tracking functionality.

### Feature 3 — Reminders

Implements daily reminder notifications for user check-ins. Notifications are scheduled locally and trigger reliably, helping users stay consistent with their habits.

---

## ⚙️ App Features & Integration

### SSO Sign-in

Google SSO login works with Firebase integration. Users can sign in using their Google account seamlessly as an alternative to email-based login.

### Settings Menu

The settings screen includes functional dropdowns for Theme, Sync Frequency, and Notifications. Data is stored and persisted locally through ConfigurationService. Debug Tools and Logout options both work as intended. Theme switching logic is implemented, though the UI does not yet refresh in real time.

### REST API Creation

SummitAPI was built using C# ASP.NET Core and includes working endpoints for authentication, user data, habits, and settings. The API is hosted on Render and fully tested using Swagger and Postman.

### API Integration in App

The Stride Android app integrates with the SummitAPI to manage users, authentication, and data. Endpoints are connected and functional.

---

## 🎨 User Interface

Developed with Jetpack Compose and Material 3. The interface maintains a consistent color scheme, clear typography, and modern layout structure. It adapts across screen sizes for a smooth user experience.

---

## 💾 GitHub, Testing & Documentation

The project is maintained on GitHub with structured commits and a complete README file. Basic testing has been added to verify functionality. Documentation includes setup instructions and API details.

---

## 🎥 Demonstration Video

A short demonstration video will showcase the main app features, including login, daily tracking, reminders, settings, debug tools, and logout.

---

## 📄 License

This project is for **educational purposes** under the Varsity College BCAD program. Redistribution or commercial use is not permitted without written consent.



