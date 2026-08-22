# Sahnur Nursery - Android App

A modern, production-grade Android application for nursery management built with Kotlin, Jetpack Compose, Material 3, and Room Database.

## Features
- **Plant & Inventory Management**: Track species, varieties, stock quantities, and low stock thresholds.
- **Customer Directory & History**: Complete records of contractors, retail buyers, and farm clients.
- **Sales & Billing**: Quick invoicing with payment methods (UPI, Cash, Card) and stock auto-deduction.
- **Expense Tracking**: Categorized farm and nursery expenses with profit & loss calculation.
- **Stock Audit Logs**: Real-time tracking of stock in, sales, and damage logs.
- **Search & Filter**: Global searching across plants, sales, expenses, and customers.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose & Material 3
- **Architecture**: MVVM with Repository Pattern
- **Persistence**: Room Database with KSP
- **Asynchronous**: Kotlin Coroutines & Flow
- **Build System**: Gradle 8.7 with Version Catalog (`libs.versions.toml`)
- **CI/CD**: GitHub Actions for automated APK builds on JDK 17

## Building the App
```bash
chmod +x gradlew
./gradlew assembleDebug
```
The resulting APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
