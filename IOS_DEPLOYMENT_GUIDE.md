# UPISmart 2.0 — iOS Cross-Platform Deployment Guide

This guide documents the official, maintainable path to deliver **UPISmart 2.0** to **iPhone and iPad (iOS)** users without duplicating financial business logic across platforms.

---

## 1. Architectural Strategy for iOS

Because UPISmart 2.0's native Android application is built in **Kotlin + Jetpack Compose** (which relies on Android OS libraries, `BiometricPrompt`, and Android CameraX), **an Android APK cannot run natively on iOS**.

To deliver UPISmart 2.0 to iOS with zero duplicated business logic and complete maintenance parity, we provide two recommended paths:

### Path A: Progressive Web App (PWA) via Safari (Instant & Free)
* **Cost:** $0 (No Apple Developer account required).
* **How users install:**
  1. Open Safari on iPhone or iPad and navigate to the AWS App Runner backend URL.
  2. Tap the **Share** button (box with upward arrow).
  3. Scroll down and tap **"Add to Home Screen"**.
  4. The app installs as a standalone, full-screen icon with custom splash screen and offline support.

### Path B: Native iOS Application via Capacitor Wrapper (App Store / TestFlight)
* **Packaging Tool:** **Capacitor 6** (`@capacitor/ios`).
* **Mechanism:** Wraps the responsive UPISmart 2.0 web application and REST API client into a native WebKit iOS application (`.xcworkspace`), allowing compilation to an iOS `.ipa` and distribution through Apple TestFlight or the App Store.

---

## 2. Prerequisites for Native iOS Compilation & Signing

To compile, code-sign, and publish an iOS application, Apple requires:
1. **Hardware:** A computer running **macOS** (MacBook, Mac mini, or Mac Studio).
2. **Software:** **Xcode 15+** installed from the Mac App Store.
3. **Apple Developer Account:** Enrolled at [developer.apple.com](https://developer.apple.com) ($99 USD/year) to generate provisioning profiles and distribute to TestFlight or App Store.
4. **Node.js:** v18 or v20+ with `npm`.

---

## 3. Step-by-Step Instructions to Generate & Build the iOS Project

### Step 1: Install Capacitor in Workspace
From the project root on your Mac:
```bash
# Initialize npm package if not already present
npm init -y

# Install Capacitor Core, CLI, and iOS platform library
npm install @capacitor/core @capacitor/cli @capacitor/ios
```

### Step 2: Add the iOS Platform
```bash
# Generate the native Xcode iOS project using capacitor.config.json
npx cap add ios

# Sync static web assets to the native iOS app
npx cap sync ios
```

### Step 3: Open Project in Xcode
```bash
# Opens the native Xcode workspace
npx cap open ios
```

### Step 4: Configure Signing & Capabilities in Xcode
1. In Xcode's left sidebar, select the top-level **App** project.
2. Go to the **Signing & Capabilities** tab.
3. Check **Automatically manage signing**.
4. Select your **Team** (associated with your Apple Developer account).
5. Choose your target device (e.g., iPhone 15 simulator or connected physical iPhone).

### Step 5: Build & Run
- Click the **Play / Run (⌘R)** button in Xcode.
- The UPISmart 2.0 application launches inside the iOS Simulator or on your physical device, displaying the Dark & Neon Lime interface and connecting securely to your AWS backend over HTTPS!

---

## 4. Native iOS Features Supported by Capacitor
- **Face ID & Touch ID:** Can be enabled using `@capacitor/biometrics`.
- **Camera Scanning:** Native iOS Camera access using `@capacitor/camera`.
- **Haptic Feedback:** Tactile vibrations on transfer confirmation via `@capacitor/haptics`.
