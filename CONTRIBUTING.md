# Contributing to DAVY

Thanks for your interest in contributing to DAVY.

## Project Overview

DAVY is an Android anime app built with Kotlin, Jetpack Compose, Hilt, and ExoPlayer. The app currently focuses on browsing anime from the current season, opening anime details, and playing episodes.

## Before You Start

- Make sure you have Android Studio or the Android SDK installed.
- Install JDK 17 or newer.
- Ensure you have Git configured locally.
- If you plan to work on player parsers, note that parser implementations live in the separate repository:
  - https://github.com/AsfhtgkDavid/davy-parsers

## Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/AsfhtgkDavid/davy.git
   cd davy
   ```

2. Sync Gradle dependencies:
   ```bash
   ./gradlew :app:dependencies
   ```

3. Build the debug app:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run the app from Android Studio or with:
   ```bash
   ./gradlew installDebug
   ```

## Recommended Workflow

1. Fork the repository.
2. Create a new branch for your change:
   ```bash
   git checkout -b feature/your-change
   ```
3. Make your changes.
4. Run the relevant checks and build:
   ```bash
   ./gradlew assembleDebug
   ```
5. Commit your work with a clear message.
6. Open a pull request with a summary of the change.

## Coding Guidelines

- Follow the existing Kotlin style in the project.
- Keep changes focused and avoid unrelated refactors.
- Prefer small, well-tested changes.
- If you add new UI, keep it consistent with the current Compose-based design.
- If you change behavior that affects playback, verify it manually if possible.

## Adding New Player Parsers

If you want to add support for new player sources or episode parsers, please work in the parser repository:

- https://github.com/AsfhtgkDavid/davy-parsers

This project consumes parser implementations from that repository, so parser-related changes should be made there unless the integration layer in DAVY itself must also be updated.

## Pull Request Guidelines

Please include:
- A short description of the problem you solved
- The motivation behind the change
- Screenshots or screen recordings when the UI changes
- Notes about any manual testing you performed

## Questions

If you are unsure where to start, open an issue or contact the maintainers before starting significant work.
