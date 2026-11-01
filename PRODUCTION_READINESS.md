---
# AlertMesh Production Readiness

## Concept 4 interface and SOS verification — 2026-11-01

- Used `.artifacts/icons/alertmesh_icon_concept4_master_1024.png` as the exact launcher and in-app mark. Rebuilt the Compose palette around its navy, cyan mesh, and coral beacon; added a timed SOS progress indicator, quick actions, and a bounded live pressure chart.
- A normal SOS tap opens the report form. A physical 11-second hold on Samsung SM-M307F submitted an automatic SOS with condition `UNKNOWN`, one person, and a fresh location fix. Opening a new report now clears a previously displayed incoming SOS.
- The Pixel 8a has a barometer; the Samsung SM-M307F reports the sensor as unavailable. The field panel shows pressure only when measured and relative height only when a recent peer pressure exists. This is an indicative sensor comparison, not a calibrated floor or rescue location.
- `:protocol:test :server:build :app:assembleDebug :app:lintDebug` passed: 30 protocol tests, 0 failures. Debug APK: `.artifacts/AlertMesh-2026-11-01-concept4-debug.apk`, SHA-256 `F44C59584101AEC98412A0C72ABC290DC8D20DADBA20E4F3C8B495C94CFFF124`.
- **Readiness:** this is a tested debug build, not a certified production release. Release signing, multi-hop radio, sustained background operation, large-queue recovery, and independent security review remain open.

---

## Physical Nearby and audio verification — 2026-11-01

- Tested on Samsung SM-M307F (Android 11/API 30) and Pixel 8a (API 37). Both connected over Nearby and showed one live peer. Samsung text and SOS reached Pixel; the incoming SOS report displayed the chosen `TRAPPED` condition.
- A Samsung voice note reached Pixel. The first playback was silent/distorted. Fixed AMR-NB decoding to feed 20 ms frames separately and use the media speaker path; the user confirmed audible playback after reinstall.
- Live reconnect test: Pixel saved `OFFLINE_QUEUE_TEST_20261101_4` while Samsung was unavailable and displayed an honest no-peer warning. After Samsung rejoined, the exact message appeared in its Room database. Recovery now filters transient presence messages, batches responses under the 64 KB frame limit, and retries Nearby when the Activity resumes with permissions.
- Replaced the app mark with the selected dark shield/mesh design across the in-app mark, launcher icon, monochrome icon, and notification icon.
- Final debug APK: `.artifacts/AlertMesh-2026-11-01-nearby-verified-debug.apk`, SHA-256 `673A0D534C60D155380C7084860DBAB54530AC73867E49970508F0008BD91C5D`. `:protocol:test :app:assembleDebug :app:lintDebug` passed, 28 tests and 0 failures.
- **Readiness:** the tested two-phone paths work. This is still a debug test build. Multi-hop radio, long-duration/background resilience, large-queue recovery, release signing, and independent security review need evidence before production deployment.

---

## Delivery and recovery verification — 2026-11-01

- SOS and voice submissions now persist in Room, so they can be offered during digest sync after a connection. Newly synced signed messages reach the app's incoming SOS/voice observers; unknown or forged origins are rejected in the Android configuration. A 64 KB send limit prevents silently transmitting frames that receivers would drop.
- Signing and hybrid identity keysets now persist encrypted under Android Keystore, preserving the node identity across process restarts. Android backup is disabled to avoid restoring an encrypted keyset without its device-bound Keystore key.
- The protocol loopback test delivered TEXT, SOS, and VOICE across three nodes. Verified sync duplicate filtering and oversized voice rejection have dedicated tests. The Pixel 8a installed and launched the updated debug build, retained its keyset across force-stop/relaunch, recorded/submitted a voice note without a fatal exception, and submitted a local SOS that explicitly warned no peers were connected.
- Final gate: `:protocol:test :server:build :app:assembleDebug :app:lintDebug` passed; 27 protocol tests, 0 failures. The new debug APK is `.artifacts/AlertMesh-2026-11-01-verified-debug.apk`, SHA-256 `9C02DA0E0D81145CD7D27C9313E4D8AEC48DF0326957AB8BA26831B32CB78D68`.
- **Still blocked for production:** only one physical Nearby-capable phone was connected. Its Nearby transport and the emulator's socket transport cannot form a real radio peer. End-to-end peer receipt, audible playback of a received note, reconnect sync on devices, background/radio failures, and a security review remain unverified.

---

## Light interface and motion pass — 2026-11-01

- Rebuilt the Android Compose visual system around warm white surfaces, dark readable type, a coral SOS action, and restrained teal status accents. The home and emergency screens now share consistent spacing, grouped choices, and clear action hierarchy.
- Replaced the launcher, adaptive, monochrome, notification, and in-app mark with one beacon-inspired AlertMesh identity. Added spring/fade screen transitions and brief state feedback that respect Android's disabled-animation setting.
- Removed channel-change command noise from Activity. The emergency form requires an explicit condition before submission, exposes the selected choice visually and semantically, and keeps the delivery warning visible.
- Consulted online Apple design and Taste skill guidance, adapted for Android Compose. No iOS-only controls or bundled Apple fonts were used.
- Verification: `:protocol:test :app:assembleDebug :app:lintDebug` passed; 25 protocol tests passed. The final debug APK SHA-256 is `DEEA5A80D60B548CFAD59B510F169731FB127884CEA94E8B24E7A7A996D884CE`. Installed on Pixel 8a and emulator. Emulator SOS submission was disabled before condition selection and enabled afterward. Screenshots and navigation motion are in `.artifacts/`.
- This pass verifies UI behavior and build health. Physical peer delivery, incoming SOS, voice delivery/playback, and recovery across devices still require end-to-end testing before production use.

---

## Design polish — 2026-11-01

- Consulted the GitHub `SudewaJay/apple-design-skill` and `Hmbown/taste` skills, and Apple's Human Interface Guidelines. Applied the relevant hierarchy, plain-language copy, touch-target, purposeful-motion, and visual-restraint guidance to Android Compose; did not embed Apple-only fonts or copy iOS controls.
- Added an adaptive launcher icon and Android 13 themed monochrome icon. The home screen now hides startup diagnostics behind a visible control and opens with a concise activity empty state. Status color and SOS press use brief animations that respect Android's disabled-animations setting.
- Emergency condition is no longer preselected. Submission stays disabled until a condition is chosen; reducing total people clamps injured count. Selection also has a checkmark and radio-button semantics.
- Final debug APK SHA-256: `72190CF0F7BF27A19473C4246EB87DC658DE2F3B095B2C567073EAAF7A9D3D9F`. Protocol tests 25/25, Android lint, and APK assembly passed. The final build installed and launched on the Pixel 8a and emulator; the emulator confirmed disabled/enabled SOS submission states.
- This is a UI and interaction pass. Peer delivery, received voice, and incoming SOS remain unverified between physical devices.

---

## Android interface redesign — 2026-11-01

- Replaced the terminal-first presentation with a Compose home screen showing peer status, a prominent SOS action, quick diagnostics, activity cards, message input, channel switching, and hold-to-record voice. Existing command handling remains accessible through the input field.
- Reworked the emergency screen into a readable request form and a report/receipt state. The form uses large visible condition choices, larger counters, and explicit delivery language.
- Added a vector AlertMesh mark to the home screen and Android launcher icon. Updated the color palette and used sans-serif typography for actions and body text, retaining monospace for technical activity.
- Added a matching monochrome foreground-service notification icon. Final redesign APK SHA-256: `FAD461BDBF1EA8D3719A009060C27A758B2B40CA953726A433E02F57D3E187FF`; 25 protocol tests, app assembly, and lint passed.
- Built and inspected the new home screen on a Pixel 8a and the emergency form on the emulator. Navigation, scrolling to the submit action, and process survival were observed. This is UI verification, not evidence of peer delivery or production readiness.

---

## Follow-up verification and UI work — 2026-11-01

- Final checked debug APK SHA-256: `9F28B800BAC9E5CA15EB9653FED27A6CD3D0ECC214CAE4DF1BBF938244DE7BB7`. `:protocol:test :server:build :app:assembleDebug :app:lintDebug` passed with JDK 21; 25 protocol tests passed. The APK installed and launched on the Pixel 8a and emulator.
- Emulator permission denial: after revoking precise location and choosing to keep approximate location, the app remained alive, precise location stayed denied, and `MeshService` did not start. Other permission combinations and mid-session revocation remain untested.
- Emulator restart persistence: a text submission with zero peers showed a local queue warning; after force-stop and relaunch, the database contained one `TEXT` row with `queued` status and the boot screen showed one stored message. Resend and peer receipt were not demonstrated.
- Incoming SOS now decodes from stored protobuf messages in the emergency view and can be dismissed as seen. Incoming voice notes now appear in the terminal with a playback action. These receive paths compile, but no second physical peer sent an SOS or voice note, and playback quality has not been verified.
- Emergency form choices are now fully visible as large vertical controls; counter controls and SOS action are enlarged. The terminal explicitly shows when no peers are connected, and submission copy avoids implying confirmed delivery.
- **Readiness remains prototype.** One physical Android phone and one emulator were connected; the phone uses Nearby while the emulator uses SocketTransport. A second physical phone is needed for a real Nearby sender/receiver test. Store-and-forward resend, SOS receipt, voice playback, multi-hop radio behavior, accessibility, and security review remain open.

---

## Device and UI check — 2026-11-01

- Artemis/Ollama was unavailable at `127.0.0.1:11434`; these checks were performed by Codex and Gradle/ADB, not by Artemis.
- Emulator `Novaro_QA` booted. The isolated `com.alertmesh.devtest` debug APK installed and launched without a fatal exception. Terminal and SOS form were visually inspected. This proves first launch and screen navigation only, not network delivery.
- Pixel 8a (Android API 37, serial `43051JEKB12919`): the isolated `com.alertmesh.devtest` build installed and launched. Terminal and SOS triage screens were inspected. The existing `com.alertmesh` version `0.1.0-rc1` and its database were preserved.
- Physical push-to-talk testing exposed an `AudioRecord` initialization crash. `MeshAudioManager` now checks initialization and handles recording failure; after installing the rebuilt APK, push-to-talk left the app process alive with no fatal exception in the captured logcat. Actual voice delivery and playback remain unverified.
- Debug builds use the `.devtest` application ID suffix for side-by-side testing. Final checked APK SHA-256: `748C7F5763195688E7A7B854474FB1CF645CF53248C45100E461A4740327BCC4`.
- Terminal boot copy was corrected to avoid false Keystore/router/listening claims and a stale version string. Incoming non-TEXT payloads are no longer rendered as UTF-8 chat. `/ping` now states that it is not implemented instead of claiming success.
- UI finding: the condition row needs horizontal scrolling to reveal all choices. Triage labels were enlarged to 12–13sp and a swipe hint was added. Text scaling, touch targets, and accessibility semantics still need verification.
- Final build checks passed: 25 protocol tests, app assembly, and Android lint. End-to-end receipt, incoming SOS, received voice, restart recovery, permission denial/revocation, and multi-device radio behavior remain unverified. Do not mark them complete.

---

## Earlier verification snapshot — 2026-11-01 (superseded where values differ above)

- Reproduced the protocol tests, server build, Android compile, lint, and debug assembly using JDK 21 and the configured SDK. After the fixes below, `:protocol:test :app:compileDebugKotlin :app:lintDebug :app:assembleDebug` passed (exit 0); 25 protocol tests passed with 0 failures and 0 errors. New debug APK SHA-256: `D11FFC7022573EE4B57BDCB06C313FB9B1AE7B012A70415740128FBBF3D16499`.
- Fixed targeted TEXT fail-open behavior: if the recipient encryption key is unknown, sending now throws instead of transmitting plaintext. Added a regression test. This does not yet establish multi-hop private delivery: target IDs still need endpoint resolution.
- Restored and registered a Room v1-to-v2 migration for the `delivered` to `status` schema change. The migration has not yet been exercised against a populated v1 database.
- `D:\Dev\AndroidSdk\emulator\emulator.exe` and platform-tools `adb.exe` exist; the `Novaro_QA` AVD is listed. No emulator or device was connected during this check. Earlier claims that no emulator is available are inaccurate.
- Feature audit: incoming TEXT is observed through the database, but incoming SOS is not connected to `EmergencyViewModel.onSosReceived`, and incoming VOICE is not connected to `MeshAudioManager.playAudio`. `/ping` and `/fix` remain incomplete. Store-and-forward verification is optional in `DigestSync` and the app constructs it without a verifier, so synced messages remain a trust gap. These features are not verified end to end.
- Release status remains **prototype / locally buildable**, not pilot-ready. No physical-device testing or independent security review has occurred.

---

**Status:** Draft (verified build state recorded)  
**Branch:** main (20 modified files this session incl. PRODUCTION_READINESS.md; untracked: `alertmesh_corrected.pdf`, `extracted_images/`, `tools/`)  
**Toolchain:** JAVA_HOME=JDK 21 required for all Gradle commands (JDK 25 on PATH crashes Gradle 8.11.1)

---

## Verified Build & Test Results (JAVA_HOME=JDK 21)

| Task | Command | Result | Exit code | Notes |
|------|---------|--------|-----------|-------|
| Protocol tests | `.\gradlew :protocol:cleanTest :protocol:test` | BUILD SUCCESSFUL | 0 | **24/24 tests pass** |
| Server build | `.\gradlew :server:build` | BUILD SUCCESSFUL | 0 | |
| App Kotlin compile | `.\gradlew :app:compileDebugKotlin` | BUILD SUCCESSFUL | 0 | |
| App lint | `.\gradlew :app:lintDebug` | BUILD SUCCESSFUL | 0 | 0 errors |
| App assemble | `.\gradlew :app:assembleDebug` | BUILD SUCCESSFUL | 0 | APK produced |

**APK:** `app/build/outputs/apk/debug/app-debug.apk`
- Size: 13,073,382 bytes (~12.5 MB)
- SHA-256: `2309D4560F703EADB9FF60DE0F7EC719DBD4C683A803BB801EC871F82D6FA646`

**Test breakdown (24 total, 0 failures):**

| Test class | tests | failures | errors |
|---|---|---|---|
| CryptoProviderTest | 6 | 0 | 0 |
| DedupCacheTest | 4 | 0 | 0 |
| DigestSyncTest | 4 | 0 | 0 |
| MultiNodeTest | 1 | 0 | 0 |
| ProtocolEngineTest | 3 | 0 | 0 |
| ReplayGuardTest | 6 | 0 | 0 |
| **Total** | **24** | **0** | **0** |

**Environment:**
- JDK 25 on PATH (`java -version` → 25.0.4.1 Temurin) — do NOT use for Gradle
- JDK 21 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`
- Must set `$env:JAVA_HOME` to JDK 21 before Gradle commands (not persisted)
- Android SDK at `D:\Dev\AndroidSdk` (platforms/android-36, build-tools 35.0.0+36.0.0)
- `local.properties` sdk.dir = `D:\Dev\AndroidSdk`
- `compileSdk` changed 35→36 in `app/build.gradle.kts`

---

## Acceptance Standard (pilot-ready)

1. Reproducible build and passing relevant automated checks.
2. Demonstrated three-node relay (A → B → C).
3. Verified encrypted private communication (independent recipient decryption).
4. Verified reconnect and restart recovery.
5. Working incoming text, SOS, and voice notes.
6. Accurate UI delivery/error states.
7. Bounded resource use.
8. Documented physical-device testing and independent security-review gates.

**Do not claim production-ready while required verification remains incomplete.**

---

## Confirmed Issues (17 total)

| # | Issue | Milestone | Severity | Resolved | Evidence |
|---|-------|-----------|----------|----------|----------|
| 1 | SocketTransport sends `fromPeerId`/`toPeerId`/`dataBase64`; RendezvousServer reads `from`/`to` and ignores `dataBase64`. Simulator relay does not work. | A | Critical | Yes | SocketTransport.kt:60-64, RendezvousServer.kt:82-84 |
| 2 | MainActivity calls `startMeshService()` before permissions are granted (line 57, no wait for callback). | A | Critical | Yes | MainActivity.kt:48-57 |
| 3 | ProtocolEngine.start() is not idempotent — repeated starts launch duplicate collectors. | A | High | Yes | ProtocolEngine.kt:46-76 |
| 4 | MeshService starts transport/radios without permission check. | A | High | Yes | MeshService.kt:67-95 |
| 5 | Direct `/msg target text` ignores `target` — broadcasts to all peers (plaintext). | B | High | Yes | TerminalViewModel.kt:255-261 |
| 6 | `sendMessage()` signs but never encrypts payloads. `encryptForRecipient` exists but is unused. | B | High | Yes | ProtocolEngine.kt:186-213, TinkCryptoProvider.kt:109-120 |
| 7 | Outgoing messages not persisted before transmission. | B | High | Yes | TerminalViewModel.kt:247-261 |
| 8 | No delivery/ack states (queued, submitted, delivered, failed, acked). | B | High | Yes | MessageEntity.status field; ACK message type; sendAck; updateMessageStatus |
| 9 | SocketTransport ignores TOPOLOGY messages from server. | A | Medium | No | SocketTransport.kt:78-86 |
| 10 | NearbyTransport requests connection on every `onEndpointFound` without dedup. | A | Medium | Yes | NearbyTransport.kt:74-79 |
| 11 | RoomSyncStore uses `runBlocking` inside coroutines. | B | Medium | Partial | RoomSyncStore.saveMessages now stores status="sent" instead of delivered=true, but runBlocking still used | RoomSyncStore.kt:13,17,33 |
| 12 | MeshAudioManager.stopRecording() is racy (isRecording=false but codec coroutine may still write). | F | High | No | MeshAudioManager.kt:87-93 |
| 13 | `deriveSessionKey()` derived AES keys from public-key-only material; public keys are broadcast in PRESENCE, so any relay with both public keys could decrypt private direct messages. | C | Critical | Yes | Replaced with dual-key ECIE; verified by CryptoProviderTest.testCrossDeviceEciesRelayCannotDecrypt |
| 14 | ReplayGuard had no reordering window for out-of-order / concurrent delivery. | C | High | Yes | Rewritten with reordering window (default 100); ReplayGuardTest 6/6 |
| 15 | DigestSync accepted sync responses without signature verification. | C | High | Yes | Rewritten with optional signature verifier on sync responses (backward-compatible); DigestSyncTest 4/4 |
| 16 | ProtocolEngine had no frame-size bound before protobuf decode. | C | High | Yes | MAX_FRAME_SIZE 64KB limit added before decode |
| 17 | DedupCache had no time-based expiration or entry cap. | C | High | Yes | Rewritten with time-based expiration (default 5 min TTL) + 5000-entry LRU cap; DedupCacheTest 4/4 |
| 18 | Document threat model and key lifecycle | C | — | Yes | THREAT_MODEL.md |

**Summary:** Issues 1-8 resolved; issue 9 not implemented (TOPOLOGY); issue 10 resolved; issue 11 partial (runBlocking still present, status stored correctly); issue 12 not implemented (racy stopRecording, deferred to Milestone F). Security fixes 13-17 resolved (Milestone C, protocol layer); Issue 18 resolved (THREAT_MODEL.md created).

---

## Build Commands

| Command | Result | Exit code | Notes |
|---------|--------|-----------|-------|
| `.\gradlew :protocol:cleanTest :protocol:test` | BUILD SUCCESSFUL | 0 | 24/24 protocol tests pass |
| `.\gradlew :server:build` | BUILD SUCCESSFUL | 0 | Server module builds |
| `.\gradlew :app:compileDebugKotlin` | BUILD SUCCESSFUL | 0 | |
| `.\gradlew :app:lintDebug` | BUILD SUCCESSFUL | 0 | 0 errors |
| `.\gradlew :app:assembleDebug` | BUILD SUCCESSFUL | 0 | APK produced |

**Gradle wrapper:** 8.11.1 (requires JDK 8-21; crashes on JDK 25).  
**Workaround:** `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"` before Gradle commands (not persisted).

---

## Test Results (protocol module, JDK 21)

| Test Class | Tests | Failures | Errors |
|---|---|---|---|
| CryptoProviderTest | 6 | 0 | 0 |
| DedupCacheTest | 4 | 0 | 0 |
| DigestSyncTest | 4 | 0 | 0 |
| MultiNodeTest | 1 | 0 | 0 |
| ProtocolEngineTest | 3 | 0 | 0 |
| ReplayGuardTest | 6 | 0 | 0 |
| **Total** | **24** | **0** | **0** |

- CryptoProviderTest: 6/6 including `testCrossDeviceEciesRelayCannotDecrypt` (Alice→Bob works, Mallory cannot decrypt).
- DedupCacheTest: 4/4 including new TTL expiration test.
- DigestSyncTest: 4/4 (new — signature verification on sync).
- MultiNodeTest: 1/1 (A→B→C relay via LoopbackTransport).
- ProtocolEngineTest: 3/3 including `testSendAndReceiveAck`.
- ReplayGuardTest: 6/6 (new — out-of-order delivery handling).

---

## Critical Security Fix

**Vulnerability — `deriveSessionKey()` derived AES keys from public key material only.** It computed SHA-256 over the concatenation of both devices' public keys. Public keys are broadcast in PRESENCE messages, so any relay (or eavesdropper) possessing both devices' public keys could derive the same AES key and decrypt all "private" direct messages.

**Fix — Dual-key ECIE (hybrid encryption).**
- Identity now carries two keys: an Ed25519 signing key (`identity.publicKey`) plus a separate ECIE hybrid public key (`identity.hybridPublicKey`).
- `encryptForRecipient` uses Tink `HybridEncrypt` with the recipient's hybrid public key.
- `decryptFromSender` uses Tink `HybridDecrypt` with the local hybrid private key.
- `CryptoProvider.getHybridPublicKey()` added; `NodeIdentity` and `PresencePayload` gained a `hybridPublicKey` field; `ProtocolEngine` stores known hybrid keys and encrypts on send / decrypts on receive.
- Verified by `testCrossDeviceEciesRelayCannotDecrypt`: Alice→Bob decrypts; Mallory (with both public keys) cannot.

**Additional Milestone C hardening (protocol layer):**
- ReplayGuard: reordering window (default 100) added for out-of-order / concurrent delivery (Issue #14).
- DigestSync: signature verification on sync responses, optional verifier, backward-compatible (Issue #15).
- ProtocolEngine: MAX_FRAME_SIZE 64KB limit enforced before protobuf decode (Issue #16).
- DedupCache: time-based expiration (default 5 min TTL) + 5000-entry LRU cap (Issue #17).

**Threat model documentation: COMPLETE — THREAT_MODEL.md created (Stage 3 documentation).**

---

## Milestones Status

### A. Network & Android Lifecycle — Core fixes complete
- [x] Fix SocketTransport ↔ RendezvousServer frame format alignment
- [x] Gate service start on granted permissions (MainActivity + MeshService)
- [x] Make ProtocolEngine.start() idempotent
- [x] Prevent duplicate collectors / connections
- [ ] Stop transport/location/audio cleanly on shutdown (observation below)
- [x] Verify A → B → C relay via LoopbackTransport/MultiNodeTest (protocol level)
- [ ] End-to-end client receipt via SocketTransport + server NOT yet verified (env socket hang)

### B. Text Messaging & Honest UI — Protocol layer complete; app-layer UI integration compile-verified
- [x] Wire encryption into message flows (recipient encryption via dual-key ECIE/Tink HybridEncrypt)
- [x] Direct targeting: resolve to transport endpoint, report unknown/unreachable
- [x] Persist outgoing messages before transmission (status="sent")
- [x] Distinguish device identity from Nearby endpoint IDs
- [x] Show honest delivery/error states only when evidence supports them
- [x] Add acknowledgment mechanism (ACK message type, sendAck, updateMessageStatus)
- [x] Make channels work on reception
- [x] Show real connection state and actionable errors

### C. Security — Protocol layer complete; threat model documentation PENDING
- [ ] Document threat model and key lifecycle before modifying encryption
- [ ] Persist identity/private keys in Android Keystore (currently CleartextKeysetHandle in memory)
- [ ] Bind device identities to public keys + fingerprint verification
- [ ] Verify signatures before admitting to trusted storage
- [ ] Validate each original message inside sync responses
- [ ] Fix replay for out-of-order / concurrent sends
- [ ] Add malformed-input, size, resource limits
- [ ] Avoid logging private content / keys / precise location
- [ ] Independent security review = release gate

### D. Store-and-Forward and Routing — PARTIAL (suppression/TTL/dedup done; bounded retention pending app-layer)
- [ ] Recover queued messages after disconnect/reconnect/restart
- [ ] Bound retention: 24-hour / 500-message policy
- [ ] Preserve original authenticated message data during sync
- [ ] Prevent duplicate display and unbounded retransmission
- [ ] Repair suppression so duplicate rebroadcasts cancel pending forwarding
- [ ] TTL, split horizon, dedup, rate limiting, replay checks working together
- [ ] Auto-expire stale peers, meaningful hop/neighbor counts

### E. SOS — Remaining
- [ ] Connect received SOS to emergency UI + notifications
- [ ] Display sender, triage, coordinates, accuracy, fix age
- [ ] Handle missing/stale location honestly
- [ ] Prioritize SOS over ordinary traffic
- [ ] Explicit ack/dismissal behavior
- [ ] Never present local send as proof of receipt

### F. Voice Notes — Remaining (blocked by Issue #12)
- [ ] Complete record → encode → transmit → receive → decode → playback
- [ ] Fix recording/stop synchronization + codec draining/cleanup (stopRecording racy — Issue #12)
- [ ] Duration, payload size, memory limits
- [ ] Missing mic permission, interruptions, malformed audio, playback errors
- [ ] Live streaming deferred (document honestly)

### G. Commands, Location, Power — Remaining
- [ ] Real /ping, /sync, /trace or mark unsupported
- [ ] Remove fake success messages
- [ ] GPS distance only with freshness/accuracy
- [ ] RSSI estimates unavailable, not fabricated
- [ ] Power adaptation on battery change (not just startup)
- [ ] Screen lock, background, process death, radio toggles, absent sensors
- [ ] Barometric floor detection experimental until calibrated
- [ ] Database migrations for schema changes

### H. Testing, Measurements, Packaging — Remaining
- [ ] Tests: direct/multi-hop text, independent-device encryption, tampering/replay/identity substitution/forged sync, TTL/loops/suppression/concurrent/out-of-order, partition/heal/restart recovery, target resolution/unknown recipients, SOS receipt/location edge cases, voice lifecycle/payload limits, DB migrations
- [ ] Simulator: delivery ratio 1-5 hops, E2E latency, suppression on/off, loss 0/10/20/30%, partition recovery, crypto throughput
- [ ] CI for build/tests/lint
- [ ] Usable APK, release signing secrets outside Git
- [ ] No publish/push/deploy/distribute
- [ ] Update setup instructions, capabilities, limitations, pilot checklist

E–H: REMAINING — mostly app-layer work, blocked by lack of physical devices for testing.

---

## Files Modified This Session

- `app/src/main/java/com/alertmesh/data/db/MeshDatabase.kt` — fixed `PeerDao()`→`PeerDao` syntax; removed unresolvable `Migration`/dead code
- `app/src/main/java/com/alertmesh/service/MeshService.kt` — transport start/stop with idempotency flag + `runBlocking`; added `import android.Manifest`; `val`→`var` for permissions list
- `local.properties` — sdk.dir fixed to `D:\Dev\AndroidSdk`
- `app/build.gradle.kts` — compileSdk 35→36; added `androidx.fragment:fragment:1.6.2`

**Files modified pre-existing (prior agent, preserved):** 16 source files across app + protocol + server (hybrid encryption, ACK messages, status field, etc.)

**No source code files were modified as part of this documentation update.**

---

## Known Blockers

| Blocker | Impact | Workaround / Status |
|---------|--------|---------------------|
| No physical Android devices | App-layer verification (D–H) cannot be completed end-to-end; client receipt via SocketTransport+server not verified | Mitigate with code review + protocol tests; defer physical-device gates |
| End-to-end client receipt via SocketTransport + server | Socket hang in environment prevents full A→B→C client verification | Protocol-level relay verified (MultiNodeTest); client path unverified |
| JDK 25 on active PATH | Gradle 8.11.1 crashes parsing JavaVersion 25 | Set `$env:JAVA_HOME` to JDK 21 before Gradle commands |
| Threat model documentation missing | Milestone C documentation incomplete | Resolved — THREAT_MODEL.md created |

---

## Current Milestone

**Build pipeline green; C (security protocol layer) complete; Stage 3 threat model documentation complete (THREAT_MODEL.md created); moving to Stage 2 end-to-end verification.**

## Documentation Artifacts

| Artifact | Contents |
|----------|----------|
| `THREAT_MODEL.md` | Threat model, key lifecycle, cryptographic design, security properties, remaining work |
| `FEATURE_VERIFICATION.md` | 18-feature verification matrix with status counts (5 verified, 11 partial, 2 deferred, 0 blocked/failed) |
| `PRODUCTION_READINESS.md` | This file — verified build/test state, confirmed issues, milestones, blockers, next actions |

## Exact Next Action

1. End-to-end A→B→C client receipt via SocketTransport + server (blocked by env socket hang — protocol-level relay verified via MultiNodeTest)
2. Android Keystore-backed identity persistence (currently CleartextKeysetHandle in memory — identity lost on restart)
3. Stages D–H app-layer work (SOS, voice notes, commands, store-and-forward recovery, etc.) — requires physical devices
