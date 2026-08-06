# 🛜 AlertMesh

**Offline-first disaster response mesh network for Android** — enabling encrypted peer-to-peer communication when cellular and internet infrastructure fails.

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?logo=android" />
  <img src="https://img.shields.io/badge/Min%20SDK-24-blue" />
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue?logo=jetpackcompose" />
  <img src="https://img.shields.io/badge/Crypto-Google%20Tink-orange" />
  <img src="https://img.shields.io/badge/Transport-Nearby%20Connections-red?logo=google" />
</p>

---

## 🚨 The Problem

When earthquakes, floods, or other disasters strike, traditional communication infrastructure (cellular towers, internet) often goes down first. Victims become isolated — unable to call for help, coordinate rescues, or warn others of danger.

## 💡 The Solution

**AlertMesh** creates an ad-hoc mesh network between nearby Android devices using **Bluetooth Low Energy** and **Wi-Fi Direct** (via Google Nearby Connections). No internet, no cell towers, no central server required.

Messages hop from device to device, extending the network's reach far beyond any single radio's range.

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🔗 **Mesh Networking** | Automatic peer discovery & multi-hop message relay via BLE/Wi-Fi Direct |
| 🔐 **End-to-End Encryption** | Ed25519 signatures + AES-256-GCM payload encryption via Google Tink |
| 🆘 **Emergency SOS** | One-tap SOS broadcast with GPS coordinates, triage info, and condition status |
| 🎙️ **Voice Notes** | Push-to-talk AMR-NB encoded audio transmitted over the mesh |
| 💬 **Encrypted Chat** | Text messaging with AEAD encryption across the network |
| 📍 **GPS Location** | Automatic location embedding in presence beacons and SOS alerts |
| 📡 **Presence Beacons** | Periodic heartbeats with battery, location, and neighbor count |
| 🔄 **Store & Forward** | Digest-based sync ensures messages reach peers who join late |
| 🛡️ **Replay Protection** | Counter-based replay guard + message deduplication cache |
| ⚡ **Adaptive Power** | Battery-aware duty cycling — conserves power when battery is low |
| 📊 **Distance Estimation** | Haversine GPS distance + RSSI signal strength + barometric altitude |
| 🖥️ **Terminal UI** | Retro cyber-tactical terminal interface with command system |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    :app (Android)                    │
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐  │
│  │ Terminal  │  │ Emergency │  │   MeshService    │  │
│  │   UI      │  │    UI     │  │  (Foreground)    │  │
│  └────┬─────┘  └─────┬─────┘  └────────┬─────────┘  │
│       │              │                  │            │
│  ┌────┴──────────────┴──────────────────┴─────────┐  │
│  │              AppContainer (DI)                  │  │
│  │  MessageRepo │ PeerRepo │ AudioMgr │ LocationMgr│  │
│  └──────────────────────┬─────────────────────────┘  │
│                         │                            │
├─────────────────────────┼────────────────────────────┤
│                  :protocol (JVM)                     │
│  ┌──────────────────────┴─────────────────────────┐  │
│  │              ProtocolEngine                     │  │
│  │  ┌─────────┐ ┌──────────┐ ┌────────────────┐   │  │
│  │  │ Crypto  │ │ Routing  │ │   Transport    │   │  │
│  │  │  Tink   │ │ Dedup    │ │ Nearby/Socket  │   │  │
│  │  │ Ed25519 │ │ Replay   │ │  BLE+WiFi      │   │  │
│  │  │ AES-GCM │ │ RateLimit│ │  TCP/JSON      │   │  │
│  │  └─────────┘ └──────────┘ └────────────────┘   │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
├──────────────────────────────────────────────────────┤
│                  :server (JVM CLI)                   │
│  RendezvousServer │ TopologyManager │ PacketLoss     │
│  (Testing/Simulation Only)                           │
└──────────────────────────────────────────────────────┘
```

### Module Breakdown

| Module | Purpose |
|--------|---------|
| **`:app`** | Android application — UI (Jetpack Compose), services, Room DB, DI |
| **`:protocol`** | Pure Kotlin/JVM — protocol engine, crypto, routing, transport interfaces |
| **`:server`** | Testing tool — TCP rendezvous server with configurable topologies & packet loss |

---

## 🔒 Security Model

| Layer | Mechanism |
|-------|-----------|
| **Identity** | Ed25519 key pair generated on first launch via Google Tink |
| **Authentication** | Every message signed with origin's Ed25519 private key |
| **Confidentiality** | AES-256-GCM AEAD encryption for payloads |
| **Integrity** | Signature verification on every hop before relay |
| **Replay Protection** | Per-origin monotonic counter + deduplication cache |
| **Rate Limiting** | Token-bucket rate limiter per peer (5 tokens/sec) |
| **Flood Control** | Suppression tracker — stops relay after 2+ rebroadcasts |
| **Session Keys** | HKDF-derived session keys from combined public key material |

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Crypto | Google Tink (Ed25519, AES-256-GCM, ECIES) |
| Transport | Google Nearby Connections API (P2P_CLUSTER) |
| Database | Room (SQLite) |
| Serialization | kotlinx.serialization (Protobuf wire format) |
| Concurrency | Kotlin Coroutines + Flows |
| Audio | MediaCodec (AMR-NB @ 8kHz) |
| Location | Google Play Services FusedLocationProvider |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

---

## 📱 Screens

### Terminal Interface
A retro cyber-tactical terminal with command system:
- `/help` — Show available commands
- `/peers` — List connected mesh peers
- `/msg <target> <text>` — Send encrypted message
- `/clear` — Clear terminal output
- **Push-to-Talk** button for voice notes
- Command history navigation (↑/↓)

### Emergency SOS
- One-tap SOS alarm with pulsing animation
- Triage form: people count, injured count, condition, notes
- Auto-attaches GPS coordinates
- Broadcasts with high TTL (12 hops) for maximum reach

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

### Build
```bash
git clone https://github.com/Chatur7x/AlertMesh.git
cd AlertMesh
./gradlew assembleDebug
```

### Install
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Testing with Simulation Server
```bash
# Terminal 1: Start the rendezvous server
./gradlew :server:run

# Terminal 2: Run the app on emulator (auto-connects to 10.0.2.2:9999)
./gradlew :app:installDebug
```

Server CLI commands:
- `topology line` / `ring` / `star` / `mesh` — Change network shape
- `loss 0.3` — Set 30% packet loss
- `partition A B` — Partition peers A and B
- `heal` — Restore full connectivity

---

## 📦 Releases

| Version | Status | Description |
|---------|--------|-------------|
| **v1.0** | 🟡 Alpha | Core architecture, UI shells, partial protocol wiring |
| **v2.0** | 🟢 Production | All features complete, fully wired, production-ready |

Download APKs from the [Releases](https://github.com/Chatur7x/AlertMesh/releases) page.

---

## 📁 Project Structure

```
AlertMesh/
├── app/
│   └── src/main/java/com/alertmesh/
│       ├── AlertMeshApp.kt          # Application init + Tink registration
│       ├── MainActivity.kt          # Permissions + Navigation
│       ├── data/
│       │   ├── db/                   # Room database, DAOs, entities
│       │   └── repository/           # Message & Peer repositories
│       ├── di/
│       │   └── AppContainer.kt       # Manual DI / Service Locator
│       ├── service/
│       │   ├── MeshService.kt        # Foreground service + presence beacons
│       │   ├── MeshAudioManager.kt   # AMR-NB voice record/playback
│       │   ├── MeshLocationManager.kt# GPS via FusedLocationProvider
│       │   └── NearbyTransport.kt    # Nearby Connections transport driver
│       └── ui/
│           ├── terminal/             # Terminal screen + ViewModel
│           └── emergency/            # Emergency SOS screen + ViewModel
├── protocol/
│   └── src/main/java/com/alertmesh/protocol/
│       ├── ProtocolEngine.kt         # Core message processing pipeline
│       ├── cache/                    # DedupCache, ReplayGuard
│       ├── crypto/                   # CryptoProvider, TinkCryptoProvider
│       ├── model/                    # MessageEnvelope, SosPayload, PresencePayload
│       ├── routing/                  # RateLimiter, SuppressionTracker, DistanceEstimator
│       ├── sync/                     # DigestSync (store-and-forward)
│       └── transport/                # Transport interface, SocketTransport, LoopbackTransport
└── server/
    └── src/main/java/com/alertmesh/server/
        ├── RendezvousServer.kt       # TCP relay for testing
        ├── ServerCli.kt              # Interactive topology CLI
        ├── TopologyManager.kt        # Network shape management
        └── PacketLossInjector.kt     # Simulated packet loss
```

---

## 📄 License

This project is part of an academic research initiative on offline disaster response communication systems.

---

<p align="center">
  <b>Built for when everything else goes down. 🛜</b>
</p>
