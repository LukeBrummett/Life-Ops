# Git Workflow Quick Reference

This document provides quick commands for working with the Life-Ops repository's branch structure.

---

## 📊 Branch Structure

```
main (production)
  ├── Tagged releases (v1.0.0, v1.1.0, etc.)
  └── Protected - requires PR approval

develop (integration)
  ├── Latest development features
  └── Default target for feature PRs

test (QA validation)
  └── Testing before production release

feature/* (feature development)
  └── Individual features branch from develop

bugfix/* (bug fixes)
  └── Non-critical fixes branch from develop

hotfix/* (urgent fixes)
  └── Critical fixes branch from main
```

---

## 🚀 Common Workflows

### Starting a New Feature

```bash
# Update develop branch
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/your-feature-name

# Work on your feature
# ... make changes ...

# Commit your changes
git add .
git commit -m "Add feature: description"

# Push to remote
git push -u origin feature/your-feature-name

# Create PR to develop on GitHub
```

### Fixing a Bug

```bash
# For non-critical bugs (from develop)
git checkout develop
git pull origin develop
git checkout -b bugfix/issue-description

# ... fix the bug ...

git add .
git commit -m "Fix: description of fix"
git push -u origin bugfix/issue-description

# Create PR to develop
```

### Hotfix (Critical Production Bug)

```bash
# For critical bugs in production
git checkout main
git pull origin main
git checkout -b hotfix/critical-issue

# ... fix the bug ...

git add .
git commit -m "Hotfix: description"
git push -u origin hotfix/critical-issue

# Create PR to main
# After merging to main, also merge to develop
```

### Preparing a Release

```bash
# Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.1.0

# Update version numbers
# - app/build.gradle.kts (versionCode, versionName)
# - Update CHANGELOG.md

# Commit version bump
git add .
git commit -m "Bump version to 1.1.0"

# Push release branch
git push -u origin release/v1.1.0

# Merge to test for QA
git checkout test
git merge release/v1.1.0
git push origin test

# After testing, create PR to main
# After merging to main, tag and merge back to develop
```

### Tagging a Release

```bash
# After merging release PR to main
git checkout main
git pull origin main

# Create annotated tag
git tag -a v1.1.0 -m "Release version 1.1.0

Release notes here..."

# Push tag to remote
git push origin v1.1.0

# Merge back to develop
git checkout develop
git merge main
git push origin develop
```

---

## 🔄 Syncing Your Fork

If you're working from a fork:

```bash
# Add upstream remote (one time)
git remote add upstream https://github.com/LukeBrummett/Life-Ops.git

# Sync your fork's develop branch
git checkout develop
git fetch upstream
git merge upstream/develop
git push origin develop

# Update your feature branch
git checkout feature/your-feature
git rebase develop
```

---

## 📤 First-Time Repository Push

```bash
# After creating repository on GitHub

# Push main branch
git push -u origin main

# Push other branches
git push -u origin develop
git push -u origin test

# Push tags
git push --tags
```

---

## 🏷️ Version Tagging Convention

- **Format**: `vMAJOR.MINOR.PATCH` (e.g., `v1.0.0`, `v1.2.3`)
- **Tag Type**: Always use annotated tags (`-a`)
- **Tag Message**: Include release notes summary

```bash
git tag -a v1.0.0 -m "Release version 1.0.0

Summary of changes:
- Feature 1
- Feature 2
- Bug fixes"
```

---

## 🔍 Useful Commands

### View Current Branch
```bash
git branch
```

### View All Branches (including remote)
```bash
git branch -a
```

### View Tags
```bash
git tag -l
```

### View Tag Details
```bash
git show v1.0.0
```

### Delete Local Branch
```bash
git branch -d feature/old-feature
```

### Delete Remote Branch
```bash
git push origin --delete feature/old-feature
```

### Rebase Feature Branch on Develop
```bash
git checkout feature/your-feature
git rebase develop
```

### Cherry-Pick a Commit
```bash
git cherry-pick <commit-hash>
```

### View Commit History
```bash
git log --oneline --graph --all
```

---

## ⚠️ Important Notes

1. **Never force push to main or develop**
   - Use `git push --force-with-lease` only on feature branches if needed

2. **Always create PR for main merges**
   - Don't commit directly to main

3. **Keep commits atomic**
   - One logical change per commit
   - Write clear commit messages

4. **Rebase vs Merge**
   - Use rebase for feature branches to keep history clean
   - Use merge for releases to preserve history

5. **Branch Naming**
   - Use descriptive names: `feature/task-search`, not `feature/123`
   - Use kebab-case: `feature/add-dark-mode`

---

## 📋 Commit Message Guidelines

```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Tests
- `chore`: Maintenance

**Example:**
```
feat: Add task search functionality

- Implement search bar in All Tasks screen
- Add filtering by name, category, and tags
- Update TaskRepository with search methods

Fixes #42
```

---

## 🔗 Links

- [CONTRIBUTING.md](CONTRIBUTING.md) - Full contribution guidelines
- [GitHub Flow Guide](https://guides.github.com/introduction/flow/)
- [Git Flow Cheatsheet](https://danielkummer.github.io/git-flow-cheatsheet/)
