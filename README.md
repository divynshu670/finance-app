# ?? Finance Companion

A powerful, feature-rich Android finance tracking app built with Kotlin, Jetpack Compose, and Material Design 3. Track your income and expenses, monitor your financial goals, and gain insights into your spending habits with OCR-powered receipt scanning.

---

## ? Features

### ?? Core Transaction Management
- **Track Income & Expenses** - Categorize transactions with 8 intelligent categories
- **Real-time Balance** - View current balance, total income, and total expenses at a glance
- **Transaction History** - Browse all transactions with filtering and sorting options
- **Quick Add** - Add transactions via floating action button with minimal taps

### ?? Smart Features
- **OCR Receipt Scanner** - Capture or upload receipts and auto-extract amounts using Google ML Kit
- **Auto Category Detection** - AI-powered category suggestions based on extracted text
- **Merchant Recognition** - Optional merchant/category hints from receipt OCR

### ?? Goal Tracking
- **Emergency Fund** - Set savings targets with visual progress tracking
- **7-Day Streak** - Build spending discipline with daily check-ins
- **No Spend Challenge** - Challenge yourself to go days without spending
- **Auto-Completion** - Goals automatically mark as complete when targets are reached

### ?? Insights & Analytics
- **Weekly Comparison** - Compare this week vs. last week spending with percentages
- **Monthly Breakdown** - Category-wise expense analysis with graphical representation
- **Top Category** - Identify your biggest spending category
- **Average Transaction** - Track spending patterns and averages

### ?? Customization
- **Dark Mode** - Complete dark theme with Material Design 3 colors
- **Currency Selection** - Switch between multiple currencies (?, $, �, etc.)
- **Sound & Haptics** - Optional haptic feedback for interactions
- **Biometric Lock** - Fingerprint authentication for app security

### ?? Settings & Utilities
- **Daily Notifications** - Customizable spending reminders (7 unique messages)
- **Export Data** - Download transactions as CSV or PDF
- **Clear All Data** - Factory reset with confirmation
- **Push Notifications** - Real-time alerts for transactions

---

## ?? Screens & Modules

| Screen | Description |
|--------|-------------|
| **Home** | Dashboard with balance summary, recent transactions, and quick access to goals |
| **Transactions** | Full transaction history with search, filter, and sort capabilities |
| **Add Transaction** | Expense/income form with category selection and optional OCR scanning |
| **Receipt Scanner** | Capture/pick image ? Preview ? Extract data ? Confirm ? Save |
| **Goals** | Emergency fund, streaks, and no-spend challenges management |
| **Insights** | Weekly/monthly analytics, category breakdown, and spending trends |
| **Settings** | App preferences, security, notifications, export, and data management |
| **Onboarding** | First-time user setup and feature introduction |

---

## ??? Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin 2.x |
| **UI Framework** | Jetpack Compose + Material Design 3 |
| **Architecture** | MVVM + Clean Architecture |
| **Local Database** | Room (SQLite) |
| **Data Storage** | DataStore (Preferences) |
| **Background Work** | WorkManager |
| **Security** | Biometric API |
| **ML/AI** | Google ML Kit (Text Recognition) |
| **Build System** | Gradle (Kotlin DSL) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 (Android 15) |

### Key Dependencies
- `androidx-navigation-compose` - Type-safe navigation
- `androidx-room` - Local database with coroutines support
- `androidx-work-runtime-ktx` - Background task scheduling
- `androidx-biometric` - Fingerprint authentication
- `google-mlkit-text-recognition` - On-device OCR
- `androidx-datastore-preferences` - Encrypted preferences

---

## ?? Installation & Setup

### Prerequisites
- Android Studio (2024.1 or later)
- Android SDK 36 (automatically installed by AS)
- JDK 11 or later
- Git

### Steps

1. **Clone the Repository**
   \\\ash
   git clone https://github.com/yourusername/finance-companion.git
   cd finance-companion
   \\\

2. **Open in Android Studio**
   - Launch Android Studio
   - Click "Open" and select the project folder
   - Wait for Gradle sync to complete

3. **Configure SDK**
   - Android Studio will prompt to install missing SDK components
   - Approve installations and wait for completion

4. **Run the App**
   - Connect an Android device (API 24+) or use an emulator
   - Click "Run" (??) or press \Shift + F10\
   - App will build and launch

## ?? Usage Guide

### Adding a Transaction
1. Tap **"+"** floating action button
2. Select **Expense** or **Income**
3. Enter amount and optional description
4. Select a category from the 8 available options
5. Confirm the date/time
6. Tap **"Save"**

### Scanning a Receipt (Expense Only)
1. Open **Add Expense** form
2. Tap **"?? Scan Receipt"**
3. Choose:
   - **Take Photo** - Use device camera
   - **Pick from Gallery** - Select existing image
4. Wait for text extraction (2-3 seconds)
5. Review extracted data:
   - Amount (highest valid number found)
   - Optional merchant/category hints
6. Edit if needed and tap **"Confirm"**
7. Select category (auto-filled if detected)
8. Tap **"Save Expense"**

### Viewing Insights
1. Go to **Insights** tab
2. Scroll through:
   - **Weekly Comparison** - Current vs. previous week
   - **Monthly Breakdown** - Category-wise chart
   - **Top Category** - Highest spending category
   - **All 8 Categories** - Individual breakdown

### Setting Up Goals
1. Navigate to **Goals** tab
2. **Emergency Fund**: Tap to set target amount and target date
3. **7-Day Streak**: Starts automatically, resets if no expenses for 2 days
4. **No Spend Challenge**: Tap "Create Challenge" ? Set duration ? Start

### Enabling Dark Mode
1. Go to **Settings**
2. Find **Theme** option
3. Toggle **"Dark Mode"** ON
4. App instantly switches to dark theme

### Setting Up Biometric Lock
1. Go to **Settings** ? **Security**
2. Enable **"Fingerprint Lock"**
3. Scan your fingerprint when prompted
4. App will require fingerprint on next launch

### Exporting Data
1. Go to **Settings** ? **Data**
2. Tap **"Export to CSV"** or **"Export to PDF"**
3. Choose destination folder
4. File downloads with timestamp

---


## ?? Architecture Overview

\\\
finance-companion/
+-- app/
�   +-- src/main/
�   �   +-- java/com/example/financecompanion/
�   �   �   +-- domain/
�   �   �   �   +-- model/          # Data models
�   �   �   �   +-- repository/     # Repository interfaces
�   �   �   +-- viewmodel/          # MVVM ViewModels
�   �   �   +-- views/
�   �   �   �   +-- screen/         # Compose screens
�   �   �   �   +-- components/     # Reusable UI components
�   �   �   +-- database/           # Room entities & DAOs
�   �   �   +-- notifications/      # WorkManager & notifications
�   �   �   +-- feature/            # Feature-specific logic
�   �   �   �   +-- receipt/        # OCR receipt scanning
�   �   �   +-- security/           # Biometric authentication
�   �   �   +-- utils/              # Utilities (formatting, etc.)
�   �   �   +-- dev/                # Developer mode
�   �   +-- res/                    # Resources (strings, colors, etc.)
�   +-- build.gradle.kts
�   +-- AndroidManifest.xml
+-- gradle/
    +-- libs.versions.toml          # Dependency management
\\\

---

## ?? Performance Optimizations

- **Lazy Loading** - Transactions and insights load on-demand
- **Background Processing** - OCR and data export run on IO coroutines
- **Efficient Recomposition** - Compose optimizations to prevent unnecessary UI updates
- **Smooth Scrolling** - LazyColumn with proper item keys
- **Database Indexing** - Room queries optimized with indices
- **WorkManager** - Lightweight background notifications

---

## ?? Security Features

- **Encrypted Local Storage** - DataStore stores data with encryption
- **Biometric Authentication** - Fingerprint lock using AndroidX Biometric API
- **No Network Transmission** - All data stays on device
- **Permission Handling** - Proper Android 13+ permission requests
- **File Provider** - Safe file access for receipts and exports

---

## ?? Future Roadmap

- [ ] Cloud Sync (Firebase Firestore)
- [ ] Multi-device Support
- [ ] Budget Alerts (notify when category overspent)
- [ ] Recurring Transactions
- [ ] Bill Reminders
- [ ] Expense Sharing
- [ ] Receipt OCR for Income
- [ ] Custom Categories
- [ ] More Chart Types (Pie, Line graphs)
- [ ] Voice Input for Transactions
- [ ] Integration with Bank APIs
- [ ] Data Backup & Restore

---



### Code Style
- Follow Kotlin conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions small and focused

---



## ????? Author

Created with ?? for financial awareness and smart money management.

---


## ?? Acknowledgments

- Material Design 3 for modern UI guidelines
- Google ML Kit for OCR capabilities
- Jetpack libraries for robust Android development


---

**Made with Kotlin & Jetpack Compose** ??
