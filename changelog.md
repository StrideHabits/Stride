# Stride Habits – Changelog

This file tracks notable changes to Stride Habits.  
This section covers **Part 2 → Part 3** work completed between **7 October and 19 November 2025**.

---

## Part 3 – Offline Sync, Notifications & Privacy

### New Features
- Added **Firebase Cloud Messaging (FCM)** integration for push notifications.
- Implemented **local Room database** with offline data synchronization to the Summit API.
- Added a **calendar view** for seeing habit completions and schedules over time.
- Added a **notification scheduling system** with reusable templates.
- Introduced **offline image support for habits**:
  - Attach photos to habits or completions.
  - Images are stored **locally on-device only**, not uploaded to the Summit API.
- Added **theme support** (Material 3):
  - Modern M3 styling.
  - Foundation for dark/light theming and future customization.
- Added **background sync workers** for automatic data synchronization when connectivity is available.
- Enhanced **biometric authentication** with safer fallback options (PIN/password) when biometrics fail or are unavailable.

### UI / UX Improvements
- Integrated **Material 3 theming** across core screens (lists, detail views, dialogs).
- Added **animated transitions** between key navigation flows for smoother screen changes.
- Implemented a **24-hour time picker** for notification scheduling to avoid AM/PM confusion.
- Added **notification permission handling** for Android 13+:
  - Runtime `POST_NOTIFICATIONS` prompt.
  - In-app explanation and quick link to system settings.
- **Settings reorganisation**:
  - Grouped theme, notification, and privacy-related options under a clearer Settings screen.
  - Removed or renamed ambiguous toggles to better reflect their behaviour.
- **Removed deprecated / confusing menu options**:
  - Removed early experimental **export** options and other redundant actions that were not fully supported.
  - Simplified overflow menus to focus on features that actually work in Part 3.

### Data & Privacy Changes
- Switched habit **image storage from remote API uploads to local-only storage on device** for better privacy and control.
- Limited **Summit API** usage to **text-based habit data** (habit titles, schedules, completion flags, etc.) – no image blobs.
- Updated in-app wording to emphasise that Stride Habits is a **varsity project**, not a commercial or medical product.
- Aligned app copy with the new **privacy policy website**, clarifying:
  - Use of Firebase (Auth, Crashlytics).
  - Local-only storage for images.
  - Planned deletion of backend data when the project is shut down.

### Build & Infrastructure
- Updated **Gradle configuration** with Java toolchain support for more reproducible builds.
- Added **Room database migrations** to safely evolve the local schema from Part 2 to Part 3.
- Introduced **Room type converters** to support more complex data types (e.g., `LocalDate`, enums for habit frequency, etc.).
- Hardened **WorkManager** usage for background sync and scheduled notifications.

### Unit Testing
- Added **unit tests** for core business logic:
  - Habit scheduling and due/overdue calculations.
  - Streak calculations across calendar days.
  - Mapping between local Room entities and Summit API DTOs.
- Introduced **repository tests** using in-memory Room to validate:
  - Read/write behaviour.
  - Migrations for the new Part 3 schema.
- Added **basic ViewModel tests** to ensure:
  - UI state reflects changes in the underlying data.
  - Error and loading states are handled for sync and notifications.
- Set up a minimal **instrumentation / androidTest** harness (placeholder for future UI tests) to validate that the app boots and core flows can be exercised on device/emulator.

### Websites
#### Privacy Policy Website
- Created a dedicated **privacy policy page** (GitHub Pages) for Stride Habits:
  - Documents what data is collected (habits, reminders, limited technical data).
  - Explains local-only image storage and how Summit API is used as a temporary backend for this project.
  - Clarifies that Stride Habits is a **student / varsity project**, not a medical app, and is provided “as is”.
  - Linked from the Google Play listing for store compliance.

#### Help & FAQ Website
- Created a **Help & FAQ page** (GitHub Pages) to support users:
  - Covers getting started (adding, editing, deleting habits).
  - Explains calendar, streaks, reminders, and offline image behaviour.
  - Documents common troubleshooting steps (notifications not firing, slow sync, etc.).
  - Reiterates that Summit API and Firebase are used in a minimal way, and what happens when the project ends.

---

### Removed / Deprecated Features
- Removed early **export** features and other experimental / redundant options that were not fully implemented or reliable in Part 2.
- Cleaned up legacy code paths tied to the old remote image upload approach and unused UI entry points.
