# Life-Ops

[![Version](https://img.shields.io/badge/version-1.0.2-blue.svg)](https://github.com/LukeBrummett/Life-Ops/releases)
[![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/license-MIT-purple.svg)](LICENSE)
[![Build Status](https://github.com/LukeBrummett/Life-Ops/actions/workflows/android-build.yml/badge.svg)](https://github.com/LukeBrummett/Life-Ops/actions/workflows/android-build.yml)

## 🧠 Detach, Act, and Enjoy.  
Let Life-Ops handle the mental overhead—your daily checklist appears when it’s needed, powered by interval scheduling, flexible relationships, and automatic inventory tracking.

---

## ✨ What is Life-Ops?

Life-Ops is an **offline-first Android app** for automating daily task management and inventory tracking, built on Clean Architecture principles.  
Designed for cognitive offload, it eliminates the stress of remembering schedules by surfacing only what you need to do today, organized by interval, category, relationships, and supply availability.

- **No cloud. No accounts. No network.** Your data stays private, stored on-device.
- **Deterministic scheduling:** Tasks show up because of _your rules_, not algorithms.
- **Atomic actions:** Each checklist is clear, actionable, and never duplicates tasks.

---

## 🚀 Key Features

- **Intelligent Scheduling**  
  - Daily, weekly, monthly, or ad-hoc intervals  
  - Flexible exclusions (“never schedule for Thursdays”)
  - Configurable overdue behavior (Postpone or Skip)

- **Task Relationships**  
  - Parent—child hierarchies  
  - Automatic or manual completion of parent based on child progress  
  - Triggered tasks (tasks that appear after others complete)

- **Inventory Integration**  
  - Tie tasks directly to inventory (with fixed, prompted, or recount consumption modes)
  - Shopping and restocking workflows  
  - Always advisory: tasks can complete even with low inventory

- **Powerful UI**  
  - Daily checklist (Today view) grouped by category, showing progress  
  - All Tasks view for timeline navigation  
  - Rich Task Detail and Task Edit screens  
  - Full Inventory and Settings screens  
  - Dark Mode and Material Design 3

- **Import/Export**  
  - Backup and restore your data (JSON)  
  - Portable task and inventory configuration

---

## 🏗️ Architecture Highlights

Life-Ops is engineered for longevity, testability, and adaptability.

- **Clean Architecture:**  
  Data Layer (Room, Repositories) ← Domain Layer (Use Cases, Business Rules) ← Presentation Layer (Compose UI, ViewModels)
- **MVVM Pattern:**  
  Composables observe immutable UI state via StateFlow—no direct database calls from UI.
- **Repository Pattern:**  
  All domain logic accesses data via interface abstractions, making the system easy to test and extend.
- **Kotlin Coroutines & Flow:**  
  Asynchronous updates keep the UI responsive, so changes are instantly reflected.

See [docs/Architecture.md](../docs/Architecture.md) and [docs/Project Overview.md](../docs/Project%20Overview.md) for deep detail.

---

## 🖥️ Screens

- **Today View:**  
  Checklist by category, real-time progress, collapse/expand, congratulation messages.
- **All Tasks:**  
  Search, filter, and edit any scheduled or ad-hoc tasks.
- **Task Detail:**  
  See relationships, schedule, inventory, completion streaks.
- **Task Edit/Create:**  
  Full configuration for schedules, relationships, and inventory.
- **Inventory Management:**  
  Adjust quantities, trigger shopping workflows, handle restock.
- **Settings:**  
  Data management, export/import, developer utilities.

---

## 📚 User Workflows

**1. Exercise Routine with Parent-Child Tasks:**  
Plan grouped workouts (stretch > lift > cardio), track child progress, get automatic parent completion and celebration.

**2. Flexible Scheduling & Triggers:**  
Edit recurring intervals, exclude days, configure triggers (“Clean Up” after “Cook Dinner”) for sequential workflows.

**3. Inventory Management:**  
Bundle recount tasks, prompt for inventory after completion, run shopping and restocking workflows that update supply quantities seamlessly.

More in [docs/Project Overview.md](../docs/Project%20Overview.md).

---

## 🔧 Installation

### Install via APK
1. Get latest APK from [GitHub Releases](https://github.com/LukeBrummett/Life-Ops/releases/latest)
2. Enable “Install from Unknown Sources” on your device
3. Tap the APK and install

### Build from Source

**Requirements:**  
- JDK 21+  
- Android Studio Hedgehog (2023.1.1+)  
- Android SDK API 34

```bash
git clone https://github.com/LukeBrummett/Life-Ops.git
cd Life-Ops
./gradlew assembleDebug          # Builds debug APK
./gradlew installDebug           # Installs to connected device
./gradlew test                   # Runs tests
```
For detailed developer workflow, see [git-workflow.md](../git-workflow.md).

---

## 🏢 Project Structure

```
Life-Ops/
├── app/
│   ├── data/         # Data layer (Room database, DAOs, repositories)
│   ├── domain/       # Pure business logic (Use Cases)
│   ├── presentation/ # Compose UI and ViewModels
│   └── di/           # Dependency injection (Hilt)
├── docs/
│   ├── Architecture.md
│   ├── Project Overview.md
│   └── design/...
├── build.gradle.kts / settings.gradle.kts
└── README.md
```

---

## 🧪 Testing

- **Unit Tests:**  
  Thorough coverage for scheduling, relationships, inventory logic.
- **Integration Tests:**  
  DAO and workflow tests using in-memory Room.
- **Manual UI Flows:**  
  All critical flows validated on Android Emulator and physical devices.

---

## 💡 Contributing

We welcome pull requests!  
- Follow branch strategy (`feature/*` → `develop`, hotfixes from `main`)
- Write clear commit messages, ensure all tests pass
- See [contributing.md](../contributing.md) for coding conventions, testing requirements, and PR templates.

---

## 🗺️ Roadmap

- Time-of-day scheduling
- Calendar integration (view-only)
- Advanced analytics/statistics
- Notifications
- Tablet/landscape optimization
- More in [changelog.md](../changelog.md)

---

## 📝 License

MIT License—see [LICENSE](../LICENSE).

---

## 👋 Support & Contact

- **Report Issues:** [GitHub Issues](https://github.com/LukeBrummett/Life-Ops/issues)
- **Discussions:** [GitHub Discussions](https://github.com/LukeBrummett/Life-Ops/discussions)
- **Contributing:** [contributing.md](../contributing.md)

Made with ❤️ for cognitive offload.
