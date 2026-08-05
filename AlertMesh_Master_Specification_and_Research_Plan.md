# 🛜 AlertMesh: Deep Architecture Specification, 10-Week Engineering Blueprint & Academic Research Roadmap

---

## 1. Executive Summary & System Vision

**AlertMesh** is a zero-infrastructure, ad-hoc disaster response mesh networking protocol and Android mobile application designed to maintain critical communication when central networks (cellular towers, internet backbones, and power grids) collapse due to natural disasters, war, or infrastructure failure.

### Core Mission
During acute emergency scenarios, public safety is compromised by the single point of failure in centralized cellular architectures. AlertMesh leverages everyday consumer Android smartphones to form a localized, self-healing peer-to-peer (P2P) mesh topology using **Bluetooth Low Energy (BLE)** and **Wi-Fi Direct** via Google Nearby Connections (`P2P_CLUSTER`).

### Key Accomplishments in Version 3.0
* **End-to-End Cryptography**: Autonomous Ed25519 identity generation, payload signature verification, and AES-256-GCM AEAD encryption.
* **Automatic Key Bootstrap & Presence**: Decentralized public key exchange embedded in heartbeats (`PresencePayload`), eliminating centralized PKI requirements.
* **Store-and-Forward Sync (`DigestSync`)**: Vector-digest exchange for late-joining node reconciliation.
* **Universal Command Engine**: Fluid command routing allowing seamless execution of plain text broadcasts, `/peers`, `/ping`, `/status`, `/fix`, and `/sos`.
* **Resource-Conscious Power Management**: Adaptive heartbeat duty-cycling dynamically adjusted based on battery level and network density.

---

## 2. Deep-Dive Architecture & Core Pipelines

The AlertMesh codebase is organized as a modular, three-tier Gradle project:

```
                  ┌─────────────────────────────────────────┐
                  │              :app Module                │
                  │   Jetpack Compose UI (Terminal & SOS)   │
                  │        Foreground MeshService           │
                  │       Room DB & AppContainer DI         │
                  └────────────────────┬────────────────────┘
                                       │
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │            :protocol Module             │
                  │         Pure Kotlin / JVM Core          │
                  │   ProtocolEngine & Transport Interfaces │
                  │   TinkCryptoProvider & Routing Logic    │
                  └────────────────────┬────────────────────┘
                                       │
                    ┌──────────────────┴──────────────────┐
                    ▼                                     ▼
      ┌───────────────────────────┐         ┌───────────────────────────┐
      │  NearbyTransport (P2P)    │         │  SocketTransport (TCP)    │
      │  Physical Device Layer    │         │  Simulation / Emulator    │
      └───────────────────────────┘         └─────────────┬─────────────┘
                                                          │
                                                          ▼
                                            ┌───────────────────────────┐
                                            │      :server Module       │
                                            │  Rendezvous Simulation    │
                                            └───────────────────────────┘
```

### 2.1 The Protocol Layer (`:protocol`)

Written in pure Kotlin/JVM without Android dependencies, ensuring 100% unit-testability and cross-platform porting potential.

#### A. Data Models & Wire Format
Messages are serialized using `kotlinx.serialization` with **Protocol Buffers (Protobuf)** for minimal byte footprint over lossy radio channels:
* **`MessageEnvelope`**: The universal outer carrier containing:
  * `msgId`: 16-byte UUID.
  * `origin`: Sender device ID (SHA-256 fingerprint prefix).
  * `counter`: Monotonically increasing sequence number for replay protection.
  * `type`: Enum (`TEXT`, `VOICE`, `SOS`, `PRESENCE`, `SYNC_REQ`, `SYNC_RESP`).
  * `ttl`: Time-To-Live hop limit (default 8, SOS=12, Sync=1).
  * `payload`: Encrypted byte payload.
  * `signature`: Ed25519 signature over canonical header fields.
* **`PresencePayload`**: Self-describing heartbeat broadcast every 10–30s containing:
  * `deviceId` & `fingerprint`.
  * `publicKey`: 32-byte Ed25519 public key (bootstraps peer key lookup).
  * `batteryPercent`, `neighborCount`, `timestamp`.
  * GPS fields: `latitude`, `longitude`, `accuracy`.

#### B. Protocol Engine Execution Loop (`ProtocolEngine.kt`)
The engine operates as a concurrent, event-driven state machine managed by a dedicated Kotlin `CoroutineScope`:
1. **Discovery Listener**: Monitors `transport.discoveredPeers()`. When a new peer appears:
   * Dispatches an immediate `PRESENCE` payload to exchange identity.
   * Sends a `SYNC_REQ` with local message digests to initiate store-and-forward reconciliation.
2. **Ingress Processing (`processIncoming`)**:
   * **Deduplication Check**: Drops packets whose `msgId` is in `DedupCache` (sliding window of 1,000 IDs).
   * **Replay Protection**: Validates `counter > lastSeenCounter` for the packet's `origin` in `ReplayGuard`.
   * **Rate Limiting**: Enforces token-bucket rate limits per peer (max 5 pkts/sec).
   * **Key Bootstrap & Signature Verification**:
     * If public key is unknown for `origin` and message is `PRESENCE`, extracts `presence.publicKey`.
     * Verifies `envelope.signature` against `originPubKey` using Tink Ed25519.
     * If valid, stores `knownPublicKeys[origin] = originPubKey`.
   * **Peer Persistence**: Invokes `peerPresenceListener(presence, originPubKey)` to update Room DB.
   * **Flooding Relay**: Decrements `ttl`. If `ttl > 0` and packet has not exceeded suppression threshold (`SuppressionTracker`), schedules a jittered broadcast (50–250ms random delay) to all neighbors except sender.

---

### 2.2 The Application Layer (`:app`)

The Android application implements a robust single-activity, MVVM architecture with manual Dependency Injection (`AppContainer`).

#### A. Foreground Mesh Service (`MeshService.kt`)
* Runs as an Android Foreground Service with type `connectedDevice` to prevent OS background execution limits (Doze mode suppression).
* Manages continuous presence beacons (`startPresenceBroadcast`).
* **Adaptive Power Controller**: Checks battery level every 60 seconds.
  * Battery ≥ 20%: Presence beacon interval = 10,000 ms.
  * Battery < 20%: Expands beacon interval to 30,000 ms to preserve power.

#### B. Terminal & Emergency UI System
* **Terminal HUD (`TerminalScreen.kt`)**: Built with Jetpack Compose using custom HSL colors (`BgPrimary`, `AccentCyan`, `AccentGreen`, `AccentRed`).
* **Universal Command Engine (`CommandParser.kt` & `TerminalViewModel.kt`)**:
  * Plain text input $\rightarrow$ Broadcasts automatically on active channel.
  * Slash commands parsed with alias fallback (`/msg`, `/send`, `/peers`, `/ping`, `/status`, `/fix`, `/sos`).
  * Command History Navigation supported via `▲` and `▼` state controls.
* **Audio Voice Engine (`MeshAudioManager.kt`)**: Captures microphone input via `AudioRecord`, encodes stream using hardware `MediaCodec` (**AMR-NB @ 8kHz / 12.2 kbps**), and streams chunked audio frames over `VOICE` payloads.

---

### 2.3 The Rendezvous Server Layer (`:server`)

A standalone Kotlin JVM console application designed for deterministic network simulation without requiring physical Android devices.
* **TCP Multiplexer (`RendezvousServer.kt`)**: Listens on port `9999`. Relays Base64-JSON frames between simulated `SocketTransport` instances.
* **Topology Injector (`TopologyManager.kt`)**: Dynamically configures graph edges (`line`, `star`, `ring`, `mesh`, `partition`, `heal`).
* **Loss Simulation (`PacketLossInjector.kt`)**: Introduces stochastic packet drop rates ($0.0 \rightarrow 1.0$) to benchmark link degradation response.

---

## 3. Cryptographic & Network Security Specifications

```
  +-----------------------------------------------------------------------+
  |                        MessageEnvelope Header                         |
  |  msgId | origin | counter | type | channel | ttl | payload | signature |
  +-----------------------------------------------------------------------+
                                                               |
                                            Ed25519 Sign / Verify
                                                               v
  +-----------------------------------------------------------------------+
  |                          AES-256-GCM AEAD                            |
  |  Plaintext Payload  <---> Encrypted Bytes + 128-bit Authentication Tag|
  +-----------------------------------------------------------------------+
```

### 3.1 Identity & Key Generation
* On first launch, `TinkCryptoProvider` generates a **CleartextKeysetHandle** containing an **Ed25519** private/public keypair.
* `getDeviceId()` derives a stable 16-character hexadecimal node identity by hashing the Ed25519 public key bytes with SHA-256.
* `getFingerprint()` extracts a 4-character formatted fingerprint (e.g., `A3F9`) for visual verification in the UI.

### 3.2 Confidentiality & Authentication
* **Payload Encryption**: Text payloads are encrypted using **AES-256-GCM** (Galois/Counter Mode) with 12-byte initialization vectors (IVs) and 16-byte authentication tags.
* **Signatures**: Every envelope header (`signable()`) is signed with the node's Ed25519 private key. Receiving nodes verify the signature before processing or relaying.
* **HKDF Key Derivation (`deriveSessionKey`)**:
  Computes a pairwise symmetric key between Node A ($K_A$) and Node B ($K_B$) using SHA-256 HKDF over lexicographically sorted public key bytes:
  $$\text{SessionKey} = \text{SHA-256}(\min(K_A, K_B) \mathbin{\Vert} \max(K_A, K_B))$$

---

## 4. Transport Layer & Google Nearby Connections

```
[Phone A] --(BLE Discovery)--> [Endpoint Found] --(Accept Connection)--> [Connected]
    │                                                                          │
    └─────────────────────── Bi-directional Payload Stream ────────────────────┘
```

### 4.1 Peer-to-Peer Topology (`P2P_CLUSTER`)
AlertMesh configures Google Nearby Connections in `P2P_CLUSTER` mode. This strategy enables M-to-N multi-point connections supporting ad-hoc mesh clustering without requiring an existing Wi-Fi Access Point.

### 4.2 Non-Blocking Handshake Lifecycle
* **Advertising & Discovery**: Launched concurrently on `MeshService` startup under Service ID `"com.alertmesh.mesh"`.
* **Immediate Non-Blocking Acceptance**: `onConnectionInitiated` immediately accepts incoming requests asynchronously via `connectionsClient.acceptConnection(endpointId, payloadCallback)`.
* **Logging & Error Diagnostics**: Comprehensive `Log.d("AlertMesh-Nearby")` output tracks endpoint discovery, connection success, rejection, and loss.

---

## 5. Universal Command Engine Specifications

The Universal Command Engine processes input string $S$ according to the following state tree:

```
                          Input String (S)
                                 │
                   Starts with '/' character?
                                 │
                 ┌───────────────┴───────────────┐
                YES                             NO
                 │                               │
       Split by whitespace              Command.UniversalBroadcast(S)
                 │                               │
        Check Command Token                      ▼
                 │                      Broadcast to All Peers
    ┌────────────┼────────────┐           on Current Channel
    ▼            ▼            ▼
 /peers       /status       /fix
    │            │            │
  Query        Output       Trigger
 PeerDB        Node        Diagnostic
 display      Metrics       Rescan
```

| Command Token | Aliases | Parameters | Description |
|---------------|---------|------------|-------------|
| *Plain Text* | N/A | `<text>` | Broadcasts text message to all channel peers |
| `/peers` | `/nodes`, `/list`, `/l` | None | Displays online peers, battery, fingerprint & location |
| `/send` | `/msg`, `/broadcast`, `/b` | `[target] <text>` | Universal message sender |
| `/status` | `/info`, `/metrics`, `/sys` | None | Displays node diagnostics & location fix status |
| `/ping` | `/p` | `[target]` | Pings specific target or broadcasts to all peers |
| `/fix` | `/repair`, `/rescan` | None | Triggers 1-tap mesh repair, permission check & rescan |
| `/sos` | `/emergency` | `[note]` | Broadcasts high-priority SOS emergency alert |
| `/channel` | `/ch`, `/chan` | `<1-8>` | Switches active communication channel |
| `/clear` | `/cls` | None | Clears terminal screen log |
| `/help` | `/?` | None | Displays universal help menu |

---

## 6. Comprehensive 10-Week Implementation Plan

This 10-week roadmap breaks down the complete end-to-end development of AlertMesh from scratch. Follow this week-by-week guide if recreating or expanding the project:

```
[W1: Architecture] ──► [W2: Protocol & Serialization] ──► [W3: Cryptography & Security]
                                                                  │
[W6: Nearby Transport] ◄── [W5: Storage & Room DB] ◄──────────────┘
         │
         ▼
[W7: Engine & Relay] ──► [W8: Terminal & UI] ──► [W9: Audio & GPS] ──► [W10: Hardening]
```

### Week 1: Project Setup & Architecture Skeleton
* **Task 1.1**: Initialize multi-module Gradle project (`:app`, `:protocol`, `:server`). Configure Kotlin 2.0, Compose Compiler, and KSP.
* **Task 1.2**: Define standard namespace dependencies (`room-runtime`, `tink-android`, `play-services-nearby`, `kotlinx-serialization-protobuf`).
* **Task 1.3**: Set up repository structure, `.gitignore`, and Git branches (`main`, `dev`).

### Week 2: Core Data Protocol & Serialization
* **Task 2.1**: Implement `MessageEnvelope.kt` with Protobuf annotation `@Serializable`.
* **Task 2.2**: Implement payload models: `PresencePayload.kt`, `SosPayload.kt`, `MessageType.kt`.
* **Task 2.3**: Write unit tests for encoding and decoding binary Protobuf payloads (`ProtoBuf.encodeToByteArray`).

### Week 3: Cryptography & Identity Subsystem
* **Task 3.1**: Create `CryptoProvider` interface and implement `TinkCryptoProvider.kt`.
* **Task 3.2**: Implement Ed25519 keypair initialization and canonical header signing/verification.
* **Task 3.3**: Implement AES-256-GCM payload encryption (`encryptAead` / `decryptAead`).
* **Task 3.4**: Implement HKDF-SHA256 session key derivation (`deriveSessionKey`).

### Week 4: Routing, Caching & Deduplication
* **Task 4.1**: Implement `DedupCache.kt` using a bounded ConcurrentLinkedQueue sliding window.
* **Task 4.2**: Implement `ReplayGuard.kt` tracking monotonic sequence counters per origin.
* **Task 4.3**: Implement `SuppressionTracker.kt` with automatic 60-second time-based entry eviction (`evict()`).
* **Task 4.4**: Implement `RateLimiter.kt` enforcing 5 tokens/sec bucket capacity per peer.

### Week 5: Local Persistence & Synchronization Store
* **Task 5.1**: Define Room entities: `MessageEntity.kt` and `PeerEntity.kt`.
* **Task 5.2**: Create DAOs (`MessageDao.kt`, `PeerDao.kt`) with Kotlin `Flow` queries.
* **Task 5.3**: Implement `RoomSyncStore.kt` bridging Room DAOs to the `SyncStore` interface.
* **Task 5.4**: Implement `DigestSync.kt` for vector-digest calculation and missing message extraction.

### Week 6: Transport Layer & Nearby Connections
* **Task 6.1**: Implement `Transport.kt` interface and `TransportFrame` container.
* **Task 6.2**: Implement `NearbyTransport.kt` using Google Nearby Connections API (`P2P_CLUSTER`).
* **Task 6.3**: Add non-blocking connection acceptance callbacks and logcat diagnostics.
* **Task 6.4**: Implement `SocketTransport.kt` for local TCP simulation.

### Week 7: Protocol Engine Assembly
* **Task 7.1**: Build `ProtocolEngine.kt` orchestrating Transport, Crypto, Cache, Routing, and Sync.
* **Task 7.2**: Implement public key bootstrapping on `PRESENCE` message ingress.
* **Task 7.3**: Implement immediate presence beacon transmission upon peer connection discovery.
* **Task 7.4**: Wire `peerPresenceListener` to update `PeerRepository` in Room.

### Week 8: Terminal HUD & Universal Command Engine
* **Task 8.1**: Design retro cyber-tactical theme (`Color.kt`, `Theme.kt`, `TerminalFont`).
* **Task 8.2**: Build Compose UI (`TerminalScreen.kt`, `KeyRow.kt`, `TerminalText.kt`).
* **Task 8.3**: Implement `CommandParser.kt` supporting Universal Broadcast fallback and slash commands.
* **Task 8.4**: Build `TerminalViewModel.kt` handling history navigation (`▲` / `▼`) and peer list state.

### Week 9: Hardware Sensors, Audio & Foreground Service
* **Task 9.1**: Implement `MeshAudioManager.kt` with AMR-NB hardware codec encoding (8kHz / 12.2 kbps).
* **Task 9.2**: Implement `MeshLocationManager.kt` using FusedLocationProviderClient.
* **Task 9.3**: Implement `MeshService.kt` as an Android Foreground Service (`connectedDevice`).
* **Task 9.4**: Add battery monitoring and adaptive presence duty-cycling (10s $\rightarrow$ 30s when battery < 20%).

### Week 10: System Integration, Verification & Field Testing
* **Task 10.1**: Update `MainActivity.kt` with runtime permission request launcher (Location, Bluetooth, Nearby Wi-Fi).
* **Task 10.2**: Perform multi-device physical field testing (connection handshake, message relay, voice playback, SOS broadcast).
* **Task 10.3**: Run Gradle `assembleDebug` build verification.
* **Task 10.4**: Package APK releases (`v1.0`, `v2.0`, `v3.0`).

---

## 7. Academic Research Paper Strategy & Publication Roadmap

AlertMesh presents an excellent foundation for an empirical computer science or mobile systems research paper.

### 7.1 Proposed Paper Title
> **"AlertMesh: A Secure, Battery-Aware P2P Mesh Protocol for Infrastructure-Free Emergency Disaster Response on Commodity Android Devices"**

### 7.2 Key Research Questions (RQs)
* **RQ1 (Scalability & Range)**: How does multi-hop flooding latency scale as node density increases from 2 to 50 devices under lossy radio conditions?
* **RQ2 (Security Overhead)**: What is the computational and throughput overhead of per-packet Ed25519 signatures and AES-256-GCM AEAD encryption on low-end Android hardware?
* **RQ3 (Energy Conservation)**: How much battery life is preserved by dynamic presence duty-cycling (10s vs 30s) compared to static heartbeat protocols?
* **RQ4 (Data Reconciliation)**: What is the delivery success rate of store-and-forward digest synchronization (`DigestSync`) for intermittent nodes encountering network partitions?

### 7.3 Target Publication Venues
* **IEEE INFOCOM** (IEEE International Conference on Computer Communications)
* **ACM MobiCom** (International Conference on Mobile Computing and Networking)
* **IEEE PerCom** (IEEE International Conference on Pervasive Computing and Communications)
* **ACM International Conference on Systems for Energy-Efficient Built Environments (BuildSys / Disaster Response track)**

### 7.4 Evaluation Methodology & Experimental Setup
To write a high-impact paper, collect empirical metrics using the `:server` simulation harness and physical Android devices:

```
                               Experimental Metrics
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        ▼                                ▼                                ▼
[Packet Delivery Ratio]          [End-to-End Latency]           [Energy Consumption]
  % of SOS alerts delivered        Time (ms) from SOS              mWh consumed per hour
  vs Hop Count (1 to 10)           trigger to remote receipt       in background mesh mode
```

### 7.5 LaTeX Paper Outline Structure
```latex
\documentclass[sigconf]{acmart}

\title{AlertMesh: A Secure, Battery-Aware P2P Mesh Protocol for Emergency Response}

\begin{document}

\begin{abstract}
When central telecom infrastructure fails during natural disasters, victim isolation increases mortality rates. We present AlertMesh, a decentralized, infrastructure-free peer-to-peer mesh communication system for commodity Android smartphones...
\end{abstract}

\section{Introduction}
\section{Background \& Related Work} % Serves as comparison against Serval Project, Briar, DisasterNet
\section{System Architecture \& Protocol Design}
   \subsection{Cryptographic Identity \& Key Bootstrap}
   \subsection{P2P Transport Adaptation layer}
   \subsection{Store-and-Forward Vector Synchronization}
\section{Implementation Details}
\section{Experimental Evaluation}
   \subsection{Multi-hop Packet Delivery Ratio (PDR)}
   \subsection{Latency under Simulated Link Loss}
   \subsection{Battery Duty-Cycle Energy Savings}
\section{Discussion \& Limitations}
\section{Conclusion}

\end{document}
```

---

*Specification Document Generated for AlertMesh v3.0 Production Architecture.*
