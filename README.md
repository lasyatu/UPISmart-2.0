# UPISmart 2.0 — Personal Money OS

UPISmart 2.0 is a native Android fintech application built entirely with **Kotlin** and **Jetpack Compose**. It provides intelligent personal money management with deterministic **Safe-to-Spend** velocity tracking, native **Camera-based QR Scan & Pay**, **Bills Radar**, **Money Missions** savings vaults, **Recurring Payments**, an explainable **Spending Detective** rule engine, Android **Biometric Authentication**, and **Bucks** — a witty American Gen Z AI financial companion grounded in local data.

---

## 🎨 Visual Design & Theme

- **Background:** Dark Charcoal (`#101016`)
- **Primary Accent:** Neon Lime (`#C5FF65`)
- **Secondary Accent:** Violet (`#B7A0FF`)
- **Surface & Cards:** Dark Midnight Charcoal (`#171723`, `#1D1C2C`, `#222234`)
- **Status Accents:** Coral Warning (`#FFB347`), Neon Teal (`#48CAE4`), Emerald Success (`#2EC4B6`), Crimson Danger (`#FF5C5C`)
- **Typography & Components:** Strict Material Design 3 spacing (8dp grid), rounded cards (16dp–24dp), custom adaptive vector launcher icon.

---

## 🚀 Key Features

### 1. Home Dashboard & Safe-to-Spend Engine
- **Monthly Budget:** Configurable monthly cap (default ₹50,000).
- **Total Spending:** Derived exclusively from stored Room transactions in the current cycle.
- **Remaining Budget:** `Monthly Budget - Current Month Spending`.
- **Bills Radar Reservations:** Sum of all unpaid bills and pending recurring obligations for the month.
- **Safe-to-Spend per Day:** `(Remaining Budget - Bills Reserved) / Remaining Days in Month`. Ensures users never accidentally spend money needed for upcoming bills.
- **Recent Transactions:** Live feed of debited transactions with category tags and timestamps.

### 2. Native Camera QR Scan & Pay (100% Simulated & Safe)
- **CameraX Native Integration:** Real-time camera viewfinder with animated scanner laser.
- **Torch Control:** Flashlight toggle button for low-light scanning.
- **Fictional Format Validation:** Exclusively accepts demo format:
  ```
  upismart://pay?recipient=merchant@demo&name=Demo%20Merchant
  ```
  Recipients **must** end with `@demo`. Real banking UPI IDs and external URLs are rejected.
- **Duplicate Callback Protection:** Atomic lock prevents accidental re-scans.
- **Sample Demo QR Modal:** Displayed on-screen so users or presenters can scan or click "Scan / Load Demo QR" immediately without needing a second device.
- **Manual Demo Recipient Entry:** Direct input fallback for devices or emulators without camera access.
- **Pre-Payment Review:** Displays recipient name, verified demo badge, amount input with quick chips (₹100, ₹250, ₹500, ₹1000), category selector, note, live safe-to-spend balance impact, and Spending Detective warnings.
- **Simulated Success Receipt:** Generates reference ID (`UPI/DEMO/2026/...`) and logs transaction directly into the persistent Room database.

### 3. Bucks — Witty Gen-Z AI Financial Companion
- **Personality:** Cool, friendly, casual American Gen-Z tone ("Yo!", "no cap", "fr", "chill", "vibes").
- **Grounded Intelligence:** Connected directly to Room database state. Never invents balances or transactions.
- **Capabilities:**
  - Reports current Safe-to-Spend pace and remaining budget.
  - Explains highest spending categories and biggest debits.
  - Summarizes upcoming bills and reservation amounts.
  - Tracks savings goal progress.
  - Guides users step-by-step through simulated QR scanning.
- **UI:** Dollar avatar, message bubbles, typing indicator animation, quick suggestion chips.

### 4. Spending Detective (Explainable Rule Engine)
- **High Impact Rule:** Flags any transaction exceeding 20% of monthly budget (e.g. ≥ ₹10,000 for ₹50,000 budget).
- **Budget Velocity Rule:** Warns when 80% or more of monthly budget has been consumed with days remaining.
- **Top Category Identification:** Highlights category with largest aggregate spend.
- **Peak Debit Identification:** Identifies largest individual debit.
- **Pre-Payment Warning:** Evaluates entered amount before payment confirmation to warn if it exceeds 20% of budget or remaining unreserved balance.
- **Transparency:** Explains every alert with explicit numbers; never pretends a black-box AI model exists.

### 5. Money Missions (Savings Goals)
- Goal name, target amount, saved amount, target date.
- Visual animated progress bar and percent completion.
- "+ Add Contribution" dialog with persistent contribution records.
- Completed goal badge when target is reached.

### 6. Bills Radar & Recurring Transactions
- Manage scheduled bills with due dates.
- Overdue status indicator when bill day has passed and is unpaid.
- Unpaid bills lock funds in the reservation calculation. Marking a bill paid immediately unlocks unreserved funds without reserving twice.
- **Recurring Transactions:** Define payees, amounts, and frequencies (DAILY, WEEKLY, MONTHLY, YEARLY). Automatically logs due recurring debits into transaction history and advances the next due date.

### 7. Biometric & Security Protection
- **Android BiometricPrompt API:** Locks sensitive financial details on launch using device fingerprint or face recognition.
- **PIN Fallback:** 4-digit PIN lock screen (default demo PIN: `1234`).
- **Quick Lock:** Instant lock button in the top bar.

### 8. Local Room Persistence
- `AppDatabase` stores:
  - `transactions`
  - `bills`
  - `savings_goals`
  - `savings_contributions`
  - `recurring_transactions`
  - `user_settings`
- Pre-seeded on first run with fictional demo data. Does not wipe existing user entries on restart.

---

## 🛠️ Build and Run Instructions

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- Android SDK 36 (Minimum SDK: 24)
- JDK 17 or JDK 21

### Building the Debug APK
To compile and assemble the debug APK:
```bash
gradle assembleDebug
```
The APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Running Automated Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

### Installing on an Android Phone or Emulator
1. Connect device via USB with USB Debugging enabled (or start Android Emulator).
2. Install the debug APK directly:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Or launch directly from Android Studio by clicking **Run 'app'**.

### Generating a Signed Release APK / AAB
1. Set the upload keystore path and passwords:
   ```bash
   export KEYSTORE_PATH=/path/to/my-upload-key.jks
   export STORE_PASSWORD=myStorePassword
   export KEY_PASSWORD=myKeyPassword
   ```
2. Build the release APK:
   ```bash
   gradle assembleRelease
   ```
   Or release Android App Bundle:
   ```bash
   gradle bundleRelease
   ```

---

## 🔒 Security & Privacy Notice
- **SIMULATED PAYMENT — NO REAL MONEY TRANSFERRED.**
- UPISmart 2.0 is an educational and demo money OS. It is **not** connected to the National Payments Corporation of India (NPCI) or any commercial banking infrastructure.
- The app never collects, stores, or transmits real UPI PINs, bank passwords, or debit card credentials.
