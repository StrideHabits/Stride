# StrideHabits – Rubric Allocation README

Purpose: This document maps each rubric requirement to concrete evidence in the repositories (code, configuration, documentation, and videos). The lecturer can follow this file top-to-bottom and verify each item against the POE rubric.

---

## 1. Core Functional Features

### 1.1 Application runs on a physical mobile device

* **Rubric reference:** Functional – “App runs on mobile device / emulator.”
* **Implementation summary:**
  * StrideHabits is built as a native Android app using Kotlin, Jetpack Compose, MVVM, and Hilt.
  * The APK builds via Gradle and installs on a physical Android phone.
  * The submission demonstration video shows the app running on a real device, including authentication, habit management, and notifications.

---

### 1.2 Biometrics (secure access)

* **Rubric reference:** Functional – “Make use of Biometric Authentication.”
* **Implementation summary:**
  * Biometric unlock (fingerprint / face where supported) implemented using AndroidX Biometric APIs.
  * After initial sign-in, the user can enable biometrics in Settings.
  * A locked screen gates access to all habit data; users must authenticate via biometrics or a secure fallback (PIN/password) before viewing or editing habits.

---

### 1.3 Offline mode (local persistence with sync)

* **Rubric reference:** Functional – “Offline mode with Sync.”
* **Implementation summary:**
  * Room database stores habits and check-ins locally on the device.
  * Users can create, edit, and complete habits while fully offline.
  * WorkManager background workers synchronise local changes with the SummitAPI backend when connectivity is restored.
  * Conflict handling follows a “last edit wins” approach, with the local write queued and posted to the API.

---

### 1.4 Real-time / push notifications

* **Rubric reference:** Functional – “Implementation of Real-time Notification.”
* **Implementation summary:**
  * Local reminder notifications are scheduled per habit using WorkManager.
  * Users can configure reminder times and frequencies in a dedicated Reminders area.
  * On Android 13+, the app requests the POST_NOTIFICATIONS runtime permission and guides the user if notifications are blocked.
  * Firebase Cloud Messaging (FCM) is integrated to support server-initiated push notifications where required.

---

### 1.5 Multi-language support

* **Rubric reference:** Functional – “Multi-language support (at least two South African languages).”
* **Implementation summary:**
  * All user-facing text is defined in `strings.xml`.
  * Additional locale-specific resource files are provided for at least one South African language alongside English.
  * A language option in Settings allows the user to switch the display language without reinstalling the app.
  * The demonstration video shows key screens rendered in more than one language.

---

### 1.6 Additional Feature 1 – Themes and appearance customisation

* **Rubric reference:** Functional – “Your features that were specified in Part 1.”
* **Feature name:** Application themes and appearance.
* **Implementation summary:**
  * Material 3 theming applied across all core screens.
  * Users can choose between theme options (for example, system-follow, dark, or light) in Settings.
  * The theme setting is stored persistently so that the chosen appearance is restored on next launch.
  * Colours, typography, and elevation levels are consistent across the app for a professional finish.

---

### 1.7 Additional Feature 2 – Calendar view and streak visualisation

* **Rubric reference:** Functional – “Your features that were specified in Part 1.”
* **Feature name:** Calendar and streak tracking.
* **Implementation summary:**
  * A dedicated calendar view shows on which days a habit was completed.
  * The view highlights current streak length and supports browsing across months and years.
  * Streak logic is based on consecutive days of completion, using the locally cached check-ins.
  * The calendar integrates with the same Room data used by the home dashboard, ensuring consistency between views.

---

### 1.8 Part 1 Feature Set – Implemented in Part 3

* **Rubric reference:** Functional – “Your features that were specified in Part 1.”
* **Implementation summary:**
  The following features were declared in Part 1 and are fully implemented in the Part 3 submission:

  1. **Credential-based login (in addition to SSO)**  
     * Users can sign in with standard email-and-password credentials backed by SummitAPI.  
     * Google single sign-on is also available, giving the user a choice of login methods.

  2. **Create and edit habits**  
     * Users can add new habits with a name, tag, frequency, and optional image.  
     * Existing habits can be edited (name, schedule, tag, image) from the habit detail screen.

  3. **Schedule habits**  
     * Each habit has a frequency and reminder schedule.  
     * Reminders are persisted locally and scheduled via WorkManager so that they continue after app restarts.  

  4. **Attach images to habits**  
     * Users can attach an image to a habit from local storage or camera.  
     * Images are stored locally on the device in app-private storage (offline image support).

  5. **Calendar to view streaks**  
     * A calendar page presents each habit’s completion history and shows the current streak length.  
     * Users can move between months and years to review historical completion patterns.

  6. **Change themes from light to dark**  
     * Theme selection is exposed in Settings.  
     * Users can switch between light and dark modes (or follow system), with the choice remembered across launches.

  7. **Create habits locally and check in locally**  
     * Habit creation and daily check-ins work entirely offline using the Room database.  
     * When connectivity is available, background sync workers push these local changes to SummitAPI.

---

## 2. User Interface and User Experience

### 2.1 Overall UI design

* **Rubric reference:** UI / UX – “Layout, navigation, and visual consistency.”
* **Implementation summary:**
  * The UI is built entirely with Jetpack Compose and Material 3 components.
  * Screens include: authentication, home dashboard, habit creation/editing, calendar, reminders, and settings.
  * Navigation is handled through a structured navigation graph; users can move between core flows without getting “stuck”.
  * Typography, spacing, and colour usage are consistent to present a coherent visual identity for StrideHabits.

---

### 2.2 Usability details

* **Rubric reference:** UI / UX – “Usability, feedback, and error handling.”
* **Implementation summary:**
  * Forms validate required fields (for example, habit name and frequency) and show clear error messages.
  * Snackbars and dialogs provide feedback for key actions (creation, deletion, failed network operations).
  * Empty states explain what the user needs to do (e.g., “No habits yet” with a prompt to create one).
  * Error messages distinguish between network issues, authentication problems, and server failures, and where possible the app continues to function offline.

---

## 3. Code Quality, Version Control, and Testing

### 3.1 GitHub repository and branching

* **Rubric reference:** Process – “Use of version control with GitHub.”
* **Implementation summary:**
  * Android client repository: `https://github.com/StrideHabits/Stride`
  * Backend API repository: `https://github.com/StrideHabits/SummitAPI`
  * The `main` branch holds the stable codebase; features for Part 3 (offline sync, notifications, calendar, themes, multi-language) were developed on separate branches and merged into `main`.
  * Commit messages document incremental work such as bug fixes, new screens, API integrations, and refactors.
  * GitHub Wikis are used to document architecture, endpoints, and development notes.

---

### 3.2 Automated tests

* **Rubric reference:** Process – “Conduct automated testing on the main functionality of your app.”
* **Implementation summary:**
  * Unit tests cover:
    * Habit scheduling and due/overdue calculations.
    * Streak computation from stored check-ins.
    * Mapping between Room entities and SummitAPI DTOs.
  * Repository tests use in-memory Room to verify read/write behaviour and schema migrations.
  * Basic ViewModel tests validate that loading and error states are exposed correctly to the UI.
  * Tests can be executed via Gradle or Android Studio and are included in the CI workflow.

---

### 3.3 GitHub Actions and build automation

* **Rubric reference:** Process – “Use of GitHub Actions to run tests and build your code.”
* **Implementation summary:**
  * A workflow under `.github/workflows/` sets up the Android build environment on GitHub’s runners.
  * The workflow:
    * Checks out the repository.
    * Restores dependencies.
    * Builds the Android project.
    * Runs unit tests.
  * This provides automated verification that the project compiles and that tests pass on a clean machine, not only on the developer’s local environment.

---

## 4. Play Store Readiness

### 4.1 Store listing content

* **Rubric reference:** Deployment – “Prepare an app for publication in the Play Store.”
* **Implementation summary:**
  * A draft Play Store listing has been prepared containing:
    * App name and short description.
    * Full description explaining key features (habit tracking, calendar, reminders, offline support, privacy approach).
    * High-level privacy summary aligned with the privacy policy website.
  * This text can be pasted directly into the Play Console when submitting the app.

---

### 4.2 Product shots and assets

* **Rubric reference:** Deployment – “App icon and final image assets.”
* **Implementation summary:**
  * Launcher icon assets are generated and referenced in the Android manifest.
  * Screens for home, calendar, reminders, and settings are captured and formatted for store-style product shots.
  * These images are also reused in the report and documentation to illustrate the UI.

---

### 4.3 Build configuration

* **Rubric reference:** Deployment – “Build configuration suitable for release.”
* **Implementation summary:**
  * `applicationId`, `versionCode`, and `versionName` are configured in the app module’s `build.gradle` file.
  * Release build type is defined with appropriate minSdk/targetSdk values for modern Play Store requirements.
  * The project can produce a signed release APK/AAB suitable for Play Store upload.

---

## 5. AI Usage Declaration

* **Rubric reference:** Academic integrity – “Responsible use of AI tools.”

**Summary:**

This project used software-assisted tooling in a limited, responsible manner. Assistance was mainly used for:

- Brainstorming alternative wording for documentation and UI copy.
- Clarifying error messages and suggesting possible fixes.
- Reviewing code structure and highlighting potential refactors.

All code and documentation were written, adapted, and tested by the student team. Any AI-assisted suggestions were treated as recommendations only and were evaluated, modified, or rejected based on understanding of the module content and project requirements. The final implementation decisions and submitted work remain the responsibility of the team.

---

## 6. Demonstration and Walkthrough Video

### 6.1 Demo video link

* **Rubric reference:** Demonstration – “Video walkthrough / presentation.”
* **Implementation summary:**
  * A full demonstration video accompanies this report.
  * The video:
    * Shows the app running on a physical device.
    * Covers registration/login (including credential login), biometric unlock, habit creation and completion.
    * Demonstrates notifications, the calendar view, image attachments, and settings (language, theme, privacy).
    * Includes an offline scenario where actions are performed without connectivity and later synchronised.
  * A second technical walkthrough video explains GitHub usage, GitHub Actions, backend hosting, and Firebase configuration.

---

## 7. Quick Rubric Checklist

| Rubric item                               | Section / heading in this file                             |
| ----------------------------------------- | ---------------------------------------------------------- |
| App runs on physical device               | 1.1 Application runs on a physical mobile device           |
| Biometric authentication                  | 1.2 Biometrics (secure access)                             |
| Offline mode with sync                    | 1.3 Offline mode (local persistence with sync)             |
| Real-time notifications                   | 1.4 Real-time / push notifications                         |
| Multi-language support                    | 1.5 Multi-language support                                 |
| Themes and appearance                     | 1.6 Additional Feature 1 – Themes and appearance           |
| Calendar view and streaks                 | 1.7 Additional Feature 2 – Calendar view and streaks       |
| Part 1 feature set (all listed features)  | 1.8 Part 1 Feature Set – Implemented in Part 3             |
| UI / UX design and usability              | 2.1, 2.2 User Interface and User Experience                |
| Version control                           | 3.1 GitHub repository and branching                        |
| Automated testing                         | 3.2 Automated tests                                        |
| GitHub Actions / CI                       | 3.3 GitHub Actions and build automation                    |
| Play Store readiness                      | 4.1–4.3 Play Store Readiness                               |
| AI usage declaration                      | 5. AI Usage Declaration                                    |
| Demo / presentation video                 | 6. Demonstration and Walkthrough Video                     |
