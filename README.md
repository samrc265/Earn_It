# Earn It: Gamified Productivity 🏆

**Earn It** turns your daily to-do list into a game. It combines task management with a "Tamagotchi-style" virtual plant that grows—or dies—based on your real-world consistency.

## 📱 Download & Test
**[Download the Latest APK Here](./release/app_dec25.apk)**
*(Note: Ensure you allow installation from unknown sources if testing on a physical device)*

## ✨ Key Features
* **Quest System:** Tasks are categorized into Daily (2 XP), Short Term (10 XP), and Long Term (25 XP).
* **XP & Rewards:** Every 100 XP levels you up, granting 1 Reward Point (★) to spend on real-life treats.
* **The Plant Companion:** A virtual plant that grows only if you complete 2/3rds of your daily quests.
* **Procedural Forest:** Every tree is generated using a custom algorithm based on a unique seed.
* **Widgets:** Interactive home screen widgets to view tasks and track watering streaks.
* **Persistent Tracking:** Local storage via Room Database to track long-term scores and streaks.

## 🛠 Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Local Data:** Room Database (SQLite)
* **Widgets:** Jetpack Glance
* **Background Tasks:** WorkManager
* **Serialization:** Gson

## 🚀 How to Build
1. Clone the repository:
   `git clone https://github.com/samrc265/earn-it.git`
2. Open the project in **Android Studio**.
3. Let Gradle sync dependencies.
4. Run on an Emulator or connected Android device.

---
