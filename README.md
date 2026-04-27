# SecureVision

SecureVision is a modular Android surveillance intelligence application focused on real-time visual monitoring, face recognition workflows, threat detection, and on-device event management.

It combines CameraX, ML-based detection pipelines, local persistence, and a Compose-first UI into a clean architecture that is ready for extension with backend services and admin tooling.

## Table of Contents
- [Overview](#overview)
- [Current Status](#current-status)
- [Core Features](#core-features)
- [Architecture](#architecture)
- [Repository Structure](#repository-structure)
- [Android Module Breakdown](#android-module-breakdown)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Build, Lint, and Test](#build-lint-and-test)
- [Security and Privacy](#security-and-privacy)
- [Product Roadmap](#product-roadmap)
- [License](#license)

## Overview
SecureVision is designed as a security-oriented mobile platform that can:
- stream live camera frames,
- detect and classify visual entities,
- match face embeddings against known profiles,
- raise and persist alerts,
- present operational data across dashboard, history, and settings screens.

The codebase currently centers on the Android app and shared domain/data/ML modules, with backend and admin-web directories reserved for expansion.

## Current Status
- **Implemented focus:** Android application (multi-module)
- **Backend:** placeholder directory with scaffold documentation
- **Admin web:** placeholder directory with scaffold documentation
- **ML experiments:** notebook workspace under `ml/notebooks`

## Core Features
- **Live Monitoring:** Camera preview with frame analysis and visual overlays
- **Face Workflows:** face detection, embedding generation, profile matching, save-as-profile flow
- **Threat Detection Hooks:** detection mapping for weapon/unknown-face alert generation
- **Alerting:** cooldown-based alert emission with local persistence and feedback triggers
- **Operational UI:** dashboard metrics, alert list, profile management, history, and settings
- **Offline-first Local Data:** Room-based storage for alerts, detection events, and profiles
- **Configurable Behavior:** user settings for notification behavior, thresholds, retention, and camera preferences

## Architecture
SecureVision follows a layered modular architecture:

- **Presentation Layer:** Jetpack Compose feature modules and navigation
- **Domain Layer:** business models, repositories, and use cases
- **Data Layer:** Room entities/DAOs/repositories and dependency wiring
- **ML Layer:** detection abstractions and model-specific analyzers/detectors

See detailed architecture notes in:
- `docs/architecture/system_overview.md`

## Repository Structure
```text
securevision/
├── README.md
├── android/                  # Main Android application and modules
├── backend/                  # Backend placeholder
├── admin-web/                # Admin dashboard placeholder
├── docs/
│   ├── architecture/
│   ├── product/
│   └── security/
└── ml/
    └── notebooks/            # ML experimentation notebooks
```

## Android Module Breakdown
Key modules under `android/`:

- `app` – app entry point, navigation graph, app wiring
- `core-domain` – domain models, repository contracts, use cases
- `core-data` – Room DB, DAOs, repository implementations, DI modules
- `core-ui` – shared UI components/theme
- `feature-dashboard` – system summary and quick actions
- `feature-live` – camera preview, frame analysis, live detections
- `feature-alerts` – alerts UI and management flows
- `feature-profiles` – known profile list and profile operations
- `feature-history` – detection history browsing
- `feature-settings` – runtime behavior and preference configuration
- `ml-common` – common ML contracts and data objects
- `ml-face` – face detector and embedding generator integration
- `ml-weapon` – weapon detection module scaffolding/integration
- `ml-attributes` – attribute analysis scaffolding/integration

## Technology Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Navigation:** Navigation Compose
- **DI:** Hilt
- **Database:** Room
- **Camera:** CameraX
- **ML:** Google ML Kit + modular ML abstractions
- **Async/State:** Coroutines + Flow + ViewModel
- **Build:** Gradle Kotlin DSL

## Getting Started
### Prerequisites
- Android Studio (latest stable recommended)
- JDK 17
- Android SDK with API 34
- Gradle wrapper (already included)

### Setup
1. Clone the repository.
2. Open the `android/` directory in Android Studio.
3. Sync Gradle dependencies.
4. Run the `app` module on an emulator or physical device (Android 8.0+, API 26+).

### Permissions Used
The Android app requests:
- `CAMERA`
- `INTERNET`
- `POST_NOTIFICATIONS`
- `VIBRATE`
- `RECEIVE_BOOT_COMPLETED`

## Build, Lint, and Test
From the Android project root:

```bash
cd /home/runner/work/securevision/securevision/android
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

If dependency or plugin resolution fails, verify internet/proxy access to `google()`, `mavenCentral()`, and the Gradle Plugin Portal.

## Security and Privacy
SecureVision handles biometric workflows and includes policy guidance for:
- consent-aware capture,
- embedding-first storage preference,
- secure transport/storage expectations,
- retention and audit considerations.

See:
- `docs/security/privacy.md`

## Product Roadmap
Planned high-level progression is documented in:
- `docs/product/roadmap.md`

## License
This project is licensed under the terms in `LICENSE`.
