# Contributing to Life-Ops

Thank you for your interest in contributing to Life-Ops! This document provides guidelines and instructions for contributing to the project.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Pull Request Process](#pull-request-process)
- [Branch Strategy](#branch-strategy)
- [Testing Guidelines](#testing-guidelines)

---

## 🤝 Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors. We expect respectful and constructive communication in all interactions.

### Expected Behavior

- Be respectful and considerate
- Provide constructive feedback
- Focus on what is best for the project
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment or discriminatory language
- Personal attacks or insults
- Publishing others' private information
- Any conduct that could reasonably be considered inappropriate

---

## 🚀 Getting Started

### Prerequisites

1. **Install Required Tools**
   - JDK 21 or higher
   - Android Studio Hedgehog (2023.1.1) or newer
   - Git for version control

2. **Fork and Clone**
   ```bash
   # Fork the repository on GitHub
   # Then clone your fork
   git clone https://github.com/YOUR_USERNAME/Life-Ops.git
   cd Life-Ops
   ```

3. **Set Up Remotes**
   ```bash
   # Add upstream remote
   git remote add upstream https://github.com/LukeBrummett/Life-Ops.git
   
   # Verify remotes
   git remote -v
   ```

4. **Build the Project**
   ```bash
   # Build debug APK
   ./gradlew assembleDebug
   
   # Run tests
   ./gradlew test
   ```

---

## 🔄 Development Workflow

### 1. Create a Feature Branch

Always branch from `develop`:

```bash
# Update develop branch
git checkout develop
git pull upstream develop

# Create feature branch
git checkout -b feature/your-feature-name
```

### 2. Make Your Changes

- Write clean, readable code following our [Coding Standards](#coding-standards)
- Add tests for new functionality
- Update documentation as needed
- Keep commits focused and atomic

### 3. Commit Your Changes

Use clear, descriptive commit messages:

```bash
git add .
git commit -m "Add feature: brief description

- Detailed point 1
- Detailed point 2
- Fixes #123"
```

**Commit Message Format:**
```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### 4. Push and Create Pull Request

```bash
# Push to your fork
git push origin feature/your-feature-name

# Create PR on GitHub targeting the 'develop' branch
```

---

## 📝 Coding Standards

### Kotlin Style Guide

Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Prefer `val` over `var` when possible
- Use type inference where it improves readability

### Compose Guidelines

- Keep composables focused and single-purpose
- Extract reusable UI components
- Use `remember` appropriately to avoid recomposition
- Prefer stateless composables with hoisting
- Use Material3 components consistently

### Architecture Patterns

- **MVVM**: ViewModels manage UI state, Composables observe
- **Clean Architecture**: Separate data, domain, and presentation layers
- **Repository Pattern**: Abstract data sources
- **Use Cases**: Single-responsibility business logic
- **Dependency Injection**: Use Hilt for all dependencies

### Code Organization

```kotlin
// 1. Imports (grouped: Android, Third-party, Local)
import androidx.compose.runtime.*
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lifeops.app.domain.model.Task

// 2. Class declaration with annotations
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {
    
    // 3. Properties (state first, then private)
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    // 4. Init block
    init {
        loadTasks()
    }
    
    // 5. Public methods
    fun onEvent(event: TaskEvent) {
        // Handle events
    }
    
    // 6. Private methods
    private fun loadTasks() {
        // Implementation
    }
}
```

### Documentation

- Add KDoc comments for public APIs
- Document complex logic with inline comments
- Keep comments up-to-date with code changes

```kotlin
/**
 * Calculates the next occurrence date for a recurring task.
 *
 * @param task The task to calculate for
 * @param fromDate The date to calculate from (defaults to today)
 * @return The next occurrence date, or null if task has no schedule
 */
fun calculateNextOccurrence(task: Task, fromDate: LocalDate = LocalDate.now()): LocalDate? {
    // Implementation
}
```

---

## 🔀 Branch Strategy

We use Git Flow with three main branches:

### Main Branches

- **`main`**: Production-ready code only
  - Tagged with version numbers (v1.0.0, v1.1.0, etc.)
  - Only accepts merges from `develop` via release PRs
  - Protected: requires PR approval

- **`develop`**: Integration branch for features
  - Default target for feature PRs
  - Contains latest development changes
  - Should always build successfully

- **`test`**: QA and validation branch
  - Used for testing releases before production
  - Merges from `develop` for testing cycles
  - Feedback goes back to `develop`

### Supporting Branches

- **`feature/*`**: New features or enhancements
  - Branch from: `develop`
  - Merge into: `develop`
  - Naming: `feature/task-search`, `feature/dark-mode`

- **`bugfix/*`**: Bug fixes for develop
  - Branch from: `develop`
  - Merge into: `develop`
  - Naming: `bugfix/task-completion-crash`

- **`hotfix/*`**: Urgent production fixes
  - Branch from: `main`
  - Merge into: `main` AND `develop`
  - Naming: `hotfix/critical-data-loss`

- **`release/*`**: Release preparation
  - Branch from: `develop`
  - Merge into: `main` AND `develop`
  - Naming: `release/v1.1.0`

### Workflow Example

```bash
# Feature development
git checkout develop
git pull upstream develop
git checkout -b feature/new-feature
# ... make changes ...
git push origin feature/new-feature
# Create PR to develop

# Release process
git checkout develop
git pull upstream develop
git checkout -b release/v1.1.0
# ... version bump, changelog ...
git push origin release/v1.1.0
# Create PR to main (after testing in test branch)
# After merge, tag main with v1.1.0
# Merge release branch back to develop
```

---

## ✅ Pull Request Process

### Before Submitting

1. **Update your branch**
   ```bash
   git checkout develop
   git pull upstream develop
   git checkout your-branch
   git rebase develop
   ```

2. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew assembleDebug
   ```

3. **Check for issues**
   - No compiler warnings
   - Code follows style guidelines
   - All tests pass
   - Documentation updated

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issues
Fixes #123

## Changes Made
- Change 1
- Change 2
- Change 3

## Testing
- [ ] Unit tests added/updated
- [ ] Manual testing completed
- [ ] No regressions found

## Screenshots (if applicable)
[Add screenshots for UI changes]

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests pass locally
```

### Review Process

1. **Automated Checks**: CI/CD runs tests (when configured)
2. **Code Review**: At least one maintainer reviews
3. **Feedback**: Address review comments
4. **Approval**: PR approved by maintainer
5. **Merge**: Squash and merge to target branch

---

## 🧪 Testing Guidelines

### Unit Tests

- Write tests for all business logic
- Use descriptive test names
- Follow Arrange-Act-Assert pattern
- Mock dependencies appropriately

```kotlin
@Test
fun `calculateNextOccurrence returns correct date for weekly task`() {
    // Arrange
    val task = Task(
        id = "test-1",
        name = "Test Task",
        intervalUnit = IntervalUnit.WEEK,
        intervalQty = 1
    )
    val fromDate = LocalDate.of(2025, 1, 1)
    
    // Act
    val result = taskScheduler.calculateNextOccurrence(task, fromDate)
    
    // Assert
    assertEquals(LocalDate.of(2025, 1, 8), result)
}
```

### Integration Tests

- Test data layer interactions
- Verify repository behavior
- Test database migrations

### UI Tests (future)

- Test user workflows
- Verify navigation
- Check accessibility

---

## 🐛 Reporting Bugs

### Before Reporting

1. Check existing issues
2. Verify it's reproducible
3. Test on latest version

### Bug Report Template

```markdown
**Describe the bug**
Clear description of the issue

**To Reproduce**
Steps to reproduce:
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What should happen

**Actual behavior**
What actually happens

**Screenshots**
If applicable

**Device Information**
- Device: [e.g., Pixel 7]
- Android Version: [e.g., 14]
- App Version: [e.g., 1.0.0]

**Additional context**
Any other relevant information
```

---

## 💡 Feature Requests

We welcome feature suggestions! Please:

1. Check if already requested
2. Describe the use case
3. Explain expected behavior
4. Consider implementation complexity

---

## 📞 Questions?

- Open a [Discussion](https://github.com/LukeBrummett/Life-Ops/discussions)
- Comment on relevant issues
- Reach out to maintainers

---

Thank you for contributing to Life-Ops! 🎉
