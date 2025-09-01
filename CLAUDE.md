# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application called "First Application" that tracks card payments by parsing SMS messages. The app uses SMS broadcast receivers to automatically detect and parse payment notifications from Korean banking systems, storing payment data in a Room database.

## Build System & Commands

### Build Commands
```bash
# Build debug version
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Install on connected device
./gradlew installDebug
```

### Key Configuration Files
- `build.gradle.kts` (project-level): Contains common configuration
- `app/build.gradle.kts`: Main app build configuration
- `gradle/libs.versions.toml`: Version catalog for dependencies
- `settings.gradle.kts`: Project settings and module declarations

## Architecture

### Core Components

**Application Class**: `PaymentsApplication.kt`
- Initializes database and repository instances
- Uses lazy initialization pattern for database and repository

**Database Layer** (Room):
- `AppDatabase.kt`: Room database with singleton pattern
- `CardPayment.kt`: Entity representing payment records
- `PaymentDao.kt`: Data access object for database operations
- `PaymentRepository.kt`: Repository pattern implementation
- `Converters.kt`: Type converters for Room database

**SMS Processing**:
- `SmsReceiver.kt`: BroadcastReceiver for SMS_RECEIVED_ACTION
- Parses Korean payment SMS messages using regex patterns
- Automatically extracts card name and payment amount
- Stores payments in Room database using GlobalScope

**UI Layer**:
- `MainActivity.kt`: Main activity with Navigation Component setup
- `FirstFragment.kt` & `SecondFragment.kt`: Navigation fragments
- `PaymentViewModel.kt`: ViewModel with ViewModelFactory
- `PaymentListAdapter.kt`: RecyclerView adapter for payment list

### Key Patterns Used
- MVVM architecture with ViewModel and LiveData
- Repository pattern for data access
- Room database with type converters for Date objects
- Navigation Component for fragment navigation
- View Binding enabled for layout access

## Dependencies

### Core Libraries
- **Room**: `2.6.1` for local database
- **Navigation**: `2.6.0` for fragment navigation
- **Material Design**: `1.10.0` for UI components
- **KSP**: `2.0.21-1.0.20` for annotation processing

### Development Configuration
- **Target SDK**: 36
- **Min SDK**: 24
- **Compile SDK**: 36
- **Java**: Version 11
- **Kotlin**: 2.0.21

## Permissions & Features

The app requires `RECEIVE_SMS` permission for automatic payment detection from SMS messages. The SMS parsing logic is specifically designed for Korean banking SMS formats using regex patterns for "카드" (card) and "결제/승인" (payment/approval) keywords.

## Testing

- Unit tests: `app/src/test/java/com/example/firstapplication/`
- Instrumented tests: `app/src/androidTest/java/com/example/firstapplication/`
- Test runner: AndroidJUnitRunner