# Development Guide

This document defines the coding standards, Git workflow, commit conventions, and other rules to ensure consistent and high-quality development within our team.

---

## 1. Code Conventions

### 1.1 General
- **Language:** Kotlin (Spring Boot)
- **Kotlin Version:** 21 (or defined in project)
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

### 1.2 Naming
- **Classes & Interfaces:** `PascalCase` (e.g., `UserService`)
- **Methods & Variables:** `camelCase` (e.g., `calculatePrice`)
- **Constants:** `UPPER_SNAKE_CASE` (e.g., `MAX_USERS`)
- **Packages:** lowercase, dot-separated (e.g., `com.example`)

### 1.3 Code Style
- Use `id("org.jlleitschuh.gradle.ktlint") version "12.1.0"` plugin.
  - Format before commiting.
- Max 120 line length.
- Follow the Single Responsibility Principle – each function should do one thing.
- Avoid magic numbers; use named constants.

### 1.4 Architecture
- **Controllers**: thin, return DTOs.
- **Services**: business logic, transactional boundaries.
- **Repos**: Spring Data JPA, no business logic.
- **DTO** vs **Entity**: do not expose entities on the API.

### 1.5 Testing
- Use **JUnit5** + **AssertJ** -> no `kotlin.test`
- Test method naming: `should ${expected behavior} when condition()`

---

## 2. Branching Strategy

We use **Gitflow**:

- `main` → Production-ready code only.
- `dev` → Integration branch for next release.
- `feat/*` → New features (branched from `dev`).
- `fix/*` → Fixes in development (branched from `dev`).

**Example:**
```bash
git checkout dev
git checkout -b feat/user-authentication
```

---

## 3. Commit Messages
- Follow [AngularJS commit conventions](https://gist.github.com/stephenparish/9941e89d80e2bc58a153)
  Commit Message Format:
```bash
<type>(<scope>): <short summary>

[optional body]

[optional footer(s)]
```

#### Types
- feat → New feature
- fix → Bug fix
- docs → Documentation changes
- style → Formatting, no code change
- refactor → Code restructuring without changing behavior
- test → Adding or fixing tests
- chore → Build process or auxiliary tools changes
- ci -> Changes to our CI configuration files and scripts (example scopes: Circle, BrowserStack, SauceLabs)
- build -> Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)


Example
```bash 
// https://github.com/angular/angular/blob/main/CONTRIBUTING.md#commit-message-header

// Commit message header
<type>(<scope>): <short summary>
  │       │             │
  │       │             └─⫸ Summary in present tense. Not capitalized. No period at the end.
  │       │
  │       └─⫸ Commit Scope: animations|bazel|benchpress|common|compiler|compiler-cli|core|
  │                          elements|forms|http|language-service|localize|platform-browser|
  │                          platform-browser-dynamic|platform-server|router|service-worker|
  │                          upgrade|zone.js|packaging|changelog|docs-infra|migrations|
  │                          devtools
  │
  └─⫸ Commit Type: build|ci|docs|feat|fix|perf|refactor|test

// Example 
fix(router): fix payload parameter in post request // header
```

Example with footer:
```fix(router): fix payload parameter in post request // header

Access first name parameter correctly from request payload // body

#19 // footer
```

---

## 4. Pull Request Guidelines
- PRs must be small, focused, and linked to an issue/task.
- Include a clear description of changes and motivation.
- Must pass all tests before merging.
- At least 1 approval from 1-2 other developers required.
- See: [Template](PULL_REQUEST_TEMPLATE.md)

---

## 5. Code Review Process
- Checklist for reviewers:
  - [ ] Code follows conventions and is readable.
  - [ ] No unused imports or dead code.
  - [ ] All tests pass locally.
  - [ ] No sensitive data in logs or commits.
  - [ ] SOLID principles and DRY are applied.
  
---

## 6. Environment Setup
- Kotlin 21
- Gradle
- MySQL
- IDE: IntelliJ IDEA




