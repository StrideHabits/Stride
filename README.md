# 🏃‍♂️ Stride — Android Habit Tracker

**Stride** is a Kotlin-based Android habit tracking app built with a clean **MVVM architecture**, **Hilt dependency injection**, and **Jetpack Compose** for a modern, responsive UI.
It integrates with the **SummitAPI** backend for authentication, habit tracking, and analytics.

> 🧭 **Full documentation and development notes are available on the Wiki:**
> 🔗 [https://github.com/StrideHabits/Stride/wiki](https://github.com/StrideHabits/Stride/wiki)

---

## 🚀 Quick Overview

| Feature              | Description                                                |
| -------------------- | ---------------------------------------------------------- |
| **Authentication**   | Secure login via email/password and Google SSO.            |
| **Habits & Streaks** | Add, edit, and track daily progress.                       |
| **Reminders**        | Daily notifications for check-ins.                         |
| **Settings**         | Theme, notification, and sync preferences.                 |
| **Backend**          | Cloud-hosted REST API built in ASP.NET Core 8 (SummitAPI). |
| **Analytics**        | Firebase Crashlytics and Performance monitoring.           |

---

## 🧩 Project Structure

```
com.mpieterse.stride/
├── core/       → Models, DI, services, and utilities
├── data/       → DTOs, repositories, and network modules
└── ui/         → Jetpack Compose layouts (startup, shared, central)
```

The app follows a modular **MVVM + Repository** pattern for clarity and scalability.

---

## ⚙️ Tech Stack

| Layer            | Technology                           |
| ---------------- | ------------------------------------ |
| **Language**     | Kotlin                               |
| **UI**           | Jetpack Compose + Material3          |
| **Architecture** | MVVM                                 |
| **DI**           | Hilt                                 |
| **Networking**   | Retrofit + OkHttp                    |
| **Monitoring**   | Firebase Crashlytics + Performance   |
| **Backend**      | SummitAPI (C# ASP.NET Core + SQLite) |

---

## 🧠 Development Highlights

* **Clean architecture** and **Hilt DI** for separation of concerns.
* **Reactive UI** powered by `StateFlow` and `collectAsStateWithLifecycle`.
* **SummitAPI** backend for secure, cloud-based habit storage.
* Firebase used for **monitoring**, not data storage.

> 💡 For setup, API details, and feature breakdown — visit the [Wiki](https://github.com/StrideHabits/Stride/wiki).

---

## 📸 Screenshots

**Firebase Performance:**
![Firebase Perf](https://github.com/user-attachments/assets/c0512861-15d6-493a-b77e-3a76f3a9c310)

**App Running on Device:** <img width="390" height="878" alt="image" src="https://github.com/user-attachments/assets/ea3d3b6c-1b48-420d-ad4f-3e79dbb514b2" />

---

## 📄 License

Developed for **Varsity College BCAD** coursework.
For educational use only — redistribution or commercial use is not permitted.
