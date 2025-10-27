# Life-Ops

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/LukeBrummett/Life-Ops/releases)
[![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)
[![License](https://img.shields.io/badge/license-MIT-purple.svg)](LICENSE)
[![Build Status](https://github.com/LukeBrummett/Life-Ops/actions/workflows/android-build.yml/badge.svg)](https://github.com/LukeBrummett/Life-Ops/actions/workflows/android-build.yml)

> Produce a daily checklist that you can execute without thinking. Tasks are atomic. Scheduling is interval-based. Inventory gates execution.

**Life-Ops** is an offline-first Android application that eliminates the mental overhead of daily task management through automated scheduling, inventory tracking, and intelligent task relationships.

---

## 📋 Features

### Core Task Management
- **📅 Intelligent Scheduling**: Interval-based recurring tasks (daily, weekly, monthly) with specific days of week
- **🔗 Task Relationships**: Parent-child hierarchies with automatic completion and task triggering
- **📦 Inventory Integration**: Gate task execution on supply availability with automatic consumption tracking
- **⏭️ Flexible Overdue Behavior**: Choose between postponing tasks day-by-day or skipping to next occurrence
- **🎯 Next-Occurrence Timeline**: Each task appears only once at its next scheduled date - no duplicate instances

### Advanced Features
- **🚫 Schedule Exclusions**: Never schedule tasks on specific days, dates, or months
- **🏷️ Categories & Tags**: Organize and filter tasks with custom categorization
- **📊 Task Details**: Track completion history, streaks, and upcoming occurrences
- **🔄 Import/Export**: Backup and restore your entire task configuration
- **🌙 Dark Mode**: Full Material3 theming with system-wide dark mode support

### Design Philosophy
- **Offline-Only**: Zero network dependencies, complete privacy
- **Deterministic**: Tasks appear based on explicit rules, never algorithmic guessing
- **Configuration Over Motivation**: Set up once, execute without thinking
- **Cognitive Offload**: The app remembers, you just complete visible tasks

---

## 📱 Screenshots

| Today View | Task Details | Task Edit |
|------------|--------------|-----------|
| *Daily checklist with all scheduled tasks* | *Completion history and relationships* | *Comprehensive task configuration* |

| All Tasks | Settings | Inventory |
|-----------|----------|-----------|
| *Complete task catalog with search* | *Data management and preferences* | *Supply tracking and consumption* |

---

## 🚀 Installation

### Prerequisites
- Android device running Android 8.0 (API 26) or higher
- ~20MB of storage space

### Install via APK
1. Download the latest `life-ops-v1.0.0.apk` from [Releases](https://github.com/LukeBrummett/Life-Ops/releases/latest)
2. Enable "Install from Unknown Sources" in your device settings
3. Open the downloaded APK file and follow the installation prompts

### Install via USB (Developer Mode)
```bash
# Connect your Android device via USB with Developer Mode enabled
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛠️ Building from Source

> **Cross-Platform Support**: This project builds successfully on Linux, macOS, and Windows.
> The Gradle wrapper is included and configured to work on all platforms without additional setup.

### Prerequisites
- **JDK**: OpenJDK 21 or higher
- **Android Studio**: Hedgehog (2023.1.1) or newer (optional, for IDE development)
- **Android SDK**: API Level 34 (Android 14)
- **Gradle**: 8.2+ (included via wrapper)

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/LukeBrummett/Life-Ops.git
   cd Life-Ops
   ```

2. **Make gradlew executable (Linux/macOS only)**
   ```bash
   chmod +x gradlew
   ```

3. **Build the APK**
   ```bash
   # Debug build
   ./gradlew assembleDebug
   
   # Release build (requires signing key)
   ./gradlew assembleRelease
   ```
   
   On Windows, use `gradlew.bat` instead:
   ```cmd
   gradlew.bat assembleDebug
   ```

4. **Install on connected device**
   ```bash
   ./gradlew installDebug
   ```

5. **Run tests**
   ```bash
   ./gradlew test
   ```

### Important Notes
- **Do not** set `org.gradle.java.home` in the project's `gradle.properties` file
- If you need to specify a custom JDK path, use your user-level `gradle.properties` file:
  - Linux/macOS: `~/.gradle/gradle.properties`
  - Windows: `%USERPROFILE%\.gradle\gradle.properties`
- Or set the `JAVA_HOME` environment variable before building

---

## 🏗️ Tech Stack

### Core Technologies
- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM with Clean Architecture principles
- **Database**: Room (SQLite) with TypeConverters
- **Dependency Injection**: Hilt (Dagger)
- **Build System**: Gradle with Kotlin DSL

### Key Libraries
- **AndroidX Compose**: UI toolkit (BOM 2024.02.02)
- **Navigation Compose**: Type-safe navigation
- **Room**: 2.6.1 with KSP code generation
- **Hilt**: 2.50 for dependency injection
- **Kotlin Coroutines**: Async/reactive programming
- **StateFlow**: Reactive state management

---

## 📐 Architecture

Life-Ops follows Clean Architecture with clear separation of concerns:

```
app/
├── data/           # Data layer (Room entities, DAOs, repositories)
├── domain/         # Business logic (use cases, domain models)
├── ui/             # Presentation layer (Compose screens, ViewModels)
│   ├── today/      # Daily checklist screen
│   ├── tasks/      # Task management (list, detail, edit)
│   ├── inventory/  # Supply tracking
│   └── settings/   # App configuration
└── di/             # Dependency injection modules
```

### Key Design Patterns
- **Repository Pattern**: Data abstraction layer
- **Use Case Pattern**: Single-responsibility business logic
- **State Management**: Unidirectional data flow with StateFlow
- **Type Safety**: Sealed classes for events and navigation

See [docs/Architecture.md](docs/Architecture.md) for detailed architecture documentation.

---

## 📖 Documentation

- **[Project Overview](docs/Project%20Overview.md)**: Complete feature specification and user workflows
- **[Architecture](docs/Architecture.md)**: System design and technical decisions
- **[Today Screen Design](docs/design/Today%20Screen%20Design.md)**: Daily checklist UI/UX
- **[All Tasks Design](docs/design/All%20Tasks%20Design.md)**: Task catalog interface

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Workflow
1. Fork the repository
2. Create a feature branch from `develop`
   ```bash
   git checkout -b feature/your-feature-name develop
   ```
3. Make your changes with clear commit messages
4. Write/update tests as needed
5. Submit a Pull Request to the `develop` branch

### Branch Structure
- `main`: Production-ready releases only
- `develop`: Integration branch for features
- `test`: Testing and QA validation
- `feature/*`: Individual feature development

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🗺️ Roadmap

### Future Features
- ⏰ Time-based scheduling (specific times of day)
- 📅 Calendar integration (view-only)
- 📈 Advanced statistics and insights
- 🔔 Customizable notifications
- 🎨 Custom themes and color schemes
- 📱 Tablet/landscape layout optimization

---

## 💬 Support

- **Issues**: [GitHub Issues](https://github.com/LukeBrummett/Life-Ops/issues)
- **Discussions**: [GitHub Discussions](https://github.com/LukeBrummett/Life-Ops/discussions)

---

## 🙏 Acknowledgments

Built with:
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [Material Design 3](https://m3.material.io/) - Google's design system
- [Dagger Hilt](https://dagger.dev/hilt/) - Dependency injection framework

---

<div align="center">
Made with ❤️ for cognitive offload
</div>
