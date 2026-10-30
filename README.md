# Airtel ODU Monitor (ZLT W154R Plus) — v1.0.0

A native Android mobile application built with **Kotlin** and **Jetpack Compose** to monitor and manage broadband data usage, connected clients, live telemetry, carrier frequency bands, and SMS/USSD services for the **Airtel ZLT W154R Plus** outdoor/indoor LTE-A CPE router.

---

## 🚀 Version 1.0.0 Features

### 1. Glanceable Home Dashboard (`HomeScreen.kt`)
- **Unified Hero Card**: Real-time remaining data volume in GB with tabular digits, billing cycle days left, animated gradient progress track, active LTE band, and live RSRP signal quality indicator (`• B7 • -81 dBm`).
- **User-Configurable Data Plan Allowance**: Allows users to set their custom Airtel data plan allowance (with presets: `30 GB`, `50 GB`, `75 GB`, `100 GB`, `150 GB`, `250 GB`, and custom input). Direct quick-edit button (`✏️`) right on the hero card.
- **Compact Live Telemetry Strip**: 52dp real-time streaming bar displaying downstream bandwidth (`↓ Mbps`), upstream bandwidth (`↑ Mbps`), latency (`⚡ ms`), and router uptime.
- **Quick Action Tiles**: One-tap access to SIM & SMS Center and Carrier Band Lock.

### 2. Connected Devices Management (`DevicesScreen.kt`)
- **Client Inventory**: Lists all active devices on 5 GHz Wi-Fi, 2.4 GHz Wi-Fi, and Ethernet LAN from the router DHCP table (`/xml/default_ap_station_info.xml`).
- **Device Identification & Nicknames**: Shows device vendor, IP address, MAC address, connection type, and allows custom friendly device nicknames.
- **Access Control**: Instant device block/unblock actions directly communicated to router security tables.
- **Search & Filter**: Search clients by name, IP, or MAC; filter by connection category.

### 3. Real-Time Speed & Latency Benchmark (`SpeedTestScreen.kt`)
- **Direct Bandwidth Engine**: Multi-threaded socket-level download, upload, ping, and jitter benchmarking.
- **Clean Speedometer Gauge**: Custom Compose Canvas dial gauge with peak speed tracking and latency jitter analysis.
- **Zero Distraction UI**: Clean interface directly focused on speed benchmarking without confusing routing modals.

### 4. SIM Services & USSD Terminal (`SmsCenterScreen.kt`)
- **SMS Inbox**: Reads OTPs, verification codes, data balance notifications, and carrier alerts sent to the router's SIM card.
- **Interactive USSD Terminal**: Run standard Airtel USSD codes (such as `*310#`, `*141#`, `*140#`) to check balances or purchase bundles directly through the modem.

### 5. Carrier Frequency Band Locking (`BandLockScreen.kt`)
- **Frequency Locking**: Lock the modem to specific 4G LTE bands (`Band 1`, `Band 3`, `Band 7`, `Band 20`, `Band 28`) or 5G NR bands to maximize speed or stability.
- **One-Tap Presets**: Includes presets like **Max Speed (High Bands)**, **Strongest Indoors (Low Bands)**, and **Auto All Bands**.

### 6. Settings & Diagnostics (`SettingsScreen.kt`)
- **Progressive Disclosure**: Clean semantic organization dividing controls into Data Plan Allowance, App Theme, and Router Connection.
- **Obsidian Dark / Clean Light Mode**: Adaptive color tokens with dedicated dual-pill toggle ensuring maximum contrast and legibility in bright sunlight or dark OLED environments.
- **Collapsible Advanced Gateway**: Technical settings (Gateway IP, HTTP port, Admin password, 4G Only/Auto mode, polling interval) kept neatly inside an expandable accordion.
- **Remote Gateway Reboot**: Safely reboot the router hardware with a confirmation dialog.
- **Persistent Local Caching (`AppPreferences`)**: User preferences, custom data limits, and telemetry are cached in Android `SharedPreferences` for instant launch without flickering loading spinners.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Modern Jetpack Compose with Material 3 design system and Emil Kowalski design engineering principles (concentric radii, `scale(0.96f)` tactile micro-interactions, responsive press feedback).
- **Architecture**: MVVM with Unidirectional Data Flow (`UiState`, `StateFlow`).
- **Networking**: Direct HTTP gateway communication with Airtel ZLT W154R+ firmware endpoints (`/goform/goform_get_cmd_process`, `/xml/default_ap_station_info.xml`, `/goform/set_band_lock_action`) over local Wi-Fi without cloud dependencies.
- **Simulation Engine**: Full mock router engine (`DemoRouterApiClient`) allowing complete UI and functional testing in environments without physical router hardware.
- **Test Suite**: Automated unit tests covering repository accounting, simulation modes, ViewModels, and state transitions (16/16 tests passing).

---

## 📱 How to Build and Run

### Prerequisites
- **JDK**: Java 17
- **Android SDK**: API 36 / Build-Tools 36.0.0
- **Android Device**: Android 7.0 (API 24) or higher

### 1. Build Debug APK
```bash
./gradlew assembleDebug
```
The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Run Automated Unit Tests
```bash
./gradlew testDebugUnitTest
```

### 3. Install on Connected Device via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.airteldatamonitor/.MainActivity
```
