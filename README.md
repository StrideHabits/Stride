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

## 📄 License

This project is for **educational purposes** under the Varsity College BCAD program. Redistribution or commercial use is not permitted without written consent.

---


