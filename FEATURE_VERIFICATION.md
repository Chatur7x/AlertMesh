# AlertMesh — Feature Verification Matrix

## Delivery and recovery pass — 2026-11-01

- Protocol loopback test: three nodes relayed TEXT, SOS, and VOICE in order; the destination received the original voice bytes. This tests protocol routing, not Android Nearby radio or audio decoding.
- Verified digest sync now rejects unknown/forged origins in the app configuration, stores only new messages, and forwards newly synced messages into the incoming UI path. SOS and voice submissions now persist locally for reconnect sync.
- The Pixel 8a debug build launched after installing Android Keystore-backed identity keys. The encrypted keyset file had the same SHA-256 across force-stop and relaunch. A physical microphone hold produced a voice submission without crashing. A local SOS showed the honest no-peers warning.
- Final gate: 27 protocol tests passed, server build, Android assembly, and lint passed. Debug APK SHA-256: `9C02DA0E0D81145CD7D27C9313E4D8AEC48DF0326957AB8BA26831B32CB78D68`.
- One physical Pixel 8a was available. Its Nearby transport cannot directly peer with the emulator's socket transport. Physical peer receipt, audible playback of a received note, and actual reconnect delivery remain **unverified**; the app is not production-ready on this evidence.

---

## Interface redesign check — 2026-11-01

- Home screen, launcher mark, and emergency form were redesigned in Compose. Pixel 8a home screen and emulator emergency screen were visually inspected; the emergency submit action was reachable by scrolling.
- Existing protocol and transport limitations still apply. The redesigned interface does not establish that SOS, voice, or queued messages reach another device.

---

## Follow-up result — 2026-11-01

- Latest build: 25 protocol tests, server build, app assembly, and lint passed. APK SHA-256 `9F28B800BAC9E5CA15EB9653FED27A6CD3D0ECC214CAE4DF1BBF938244DE7BB7`.
- Pixel 8a and emulator: latest APK installed and launched. Emulator precise-location denial kept the app alive without starting the mesh service.
- Emulator: a locally queued text row survived force-stop and relaunch (`status=queued`). Reconnection delivery was not tested.
- Incoming SOS display/dismiss and voice playback controls were added, but no real inbound SOS or voice payload was received from another physical device. Those features are **implemented, not end-to-end verified**.
- SOS form controls were enlarged and condition choices made fully visible. Accessibility and text scaling remain untested.
- The historical matrix below contains superseded per-feature device-blocked entries. Use this follow-up and PRODUCTION_READINESS.md for the current result.

---

## Live check — 2026-11-01

- Pixel 8a (Android API 37): isolated `com.alertmesh.devtest` build installed and launched. Terminal screen and SOS triage form displayed. The old `com.alertmesh` installation and its database were preserved. Physical push-to-talk initially crashed because `AudioRecord` was uninitialized; after the fix and reinstall, push-to-talk left the process alive with no fatal exception in captured logcat. No SOS was submitted and no peer delivery was observed.
- `Novaro_QA` emulator: the same isolated build installed and launched; terminal and SOS form were visually inspected. Boot copy was revised to stop claiming Keystore persistence, an online router, or a current location fix without evidence.
- UI review: terminal monospace theme is legible on the Pixel 8a. The SOS condition row needs horizontal scrolling; triage labels were enlarged and a swipe hint added. Touch target sizes, text scaling, and screen-reader behavior remain untested.
- Artemis/Ollama was unavailable on localhost during this check. Gradle/ADB and direct source review produced the evidence here.
- Incoming SOS, received voice, actual destination delivery, partition/restart recovery, and multi-device Nearby behavior remain unverified. Do not infer those from launch or compile success.
- Final checked build: 25 protocol tests passed, app assembly and lint passed; APK SHA-256 `748C7F5763195688E7A7B854474FB1CF645CF53248C45100E461A4740327BCC4`.

---

**Historical baseline below:** this matrix predates the live checks above; its device-blocked cells and 24-test count are superseded by the live check. Individual peer-delivery features remain unverified.  
**Stage:** 10 (historical baseline)  
**Project:** AlertMesh — offline-first disaster response mesh network for Android  
**Build baseline (green):** compile ✅, lint ✅ (0 errors), assemble ✅ (APK at `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `2309D4560F703EADB9FF60DE0F7EC719DBD4C683A803BB801EC871F82D6FA646`), protocol tests 24/24 passing, server build successful  
**Test command (requires JDK 21 for Gradle):** `.\gradlew :protocol:cleanTest :protocol:test`  
**Android SDK:** `D:\Dev\AndroidSdk` (platforms/android-36)  
**Devices:** no physical Android devices, no emulator available  

## Status conventions

| Status | Meaning |
|--------|---------|
| `verified` | Covered by an automated test that passes, or by a green build step that proves the behavior. |
| `partially verified` | Code exists and compiles; behavior is plausible by inspection, but no automated or device evidence covers it end-to-end. |
| `blocked` | Cannot be executed in this environment (no device / emulator / socket hang). Never reported as "passed" for physical-device tests. |
| `deferred` | Known open work (issue in PRODUCTION_READINESS.md); not implemented or not yet testable. |
| `failed` | Evidence shows the behavior does not work. (None in this matrix.) |

**Simulator:** no network simulator is built (milestone H remaining). `LoopbackTransport` is a protocol test harness, not a simulator; it is referenced under Automated Test where used.  
**Physical-device result:** marked `blocked — no device available` for every device-dependent feature, never "passed".  
**SocketTransport + server:** end-to-end client receipt is `blocked — environment socket hang` (not "verified"); protocol-level relay A→B→C is verified via `LoopbackTransport`/`MultiNodeTest`.

## Build baseline evidence

| Task | Command | Result | Evidence |
|------|---------|--------|----------|
| Protocol tests | `.\gradlew :protocol:cleanTest :protocol:test` | BUILD SUCCESSFUL, 24/24 pass | PRODUCTION_READINESS.md test table |
| Server build | `.\gradlew :server:build` | BUILD SUCCESSFUL | PRODUCTION_READINESS.md |
| App Kotlin compile | `.\gradlew :app:compileDebugKotlin` | BUILD SUCCESSFUL | PRODUCTION_READINESS.md |
| App lint | `.\gradlew :app:lintDebug` | BUILD SUCCESSFUL, 0 errors | PRODUCTION_READINESS.md |
| App assemble | `.\gradlew :app:assembleDebug` | BUILD SUCCESSFUL, APK produced | `app/build/outputs/apk/debug/app-debug.apk` |

Protocol test breakdown: CryptoProviderTest 6, DedupCacheTest 4, DigestSyncTest 4, MultiNodeTest 1, ProtocolEngineTest 3, ReplayGuardTest 6 (total 24, 0 failures).

## Feature verification matrix

| # | Feature | Expected Behavior | Code Entry Points | Automated Test | Simulator Result | Physical-Device Result | Failure Cases | Status |
|---|---------|-------------------|-------------------|----------------|------------------|------------------------|---------------|--------|
| 1 | Install and first launch | APK installs; app launches, registers Tink, creates container, starts MeshService foreground service with notification channel | `AlertMeshApp.kt:onCreate`, `MainActivity.kt:onCreate`→`requestPermissions`→`startMeshService`, `MeshService.kt:onCreate`/`onStartCommand` | none | none | blocked — no device available | Crash on launch if Tink registration fails; if permissions missing, service starts sticky without transport and logs warning | partially verified |
| 2 | Permission grant/denial/revocation | Grant enables transport start; denial keeps service running without mesh; revocation has no crash | `MainActivity.kt:88-104` (`requestPermissions`), `MeshService.kt:132-152` (`allPermissionsGranted`) | none | none | blocked — no device available | Denied location/Bluetooth → transport not started (warning logged, `START_STICKY` returned) | partially verified |
| 3 | Discovery and connection | Peers advertise/discover, connect, exchange presence; new peer gets PRESENCE + SYNC_REQ | `NearbyTransport.kt:start()` (advertise + discovery, `onEndpointFound`→`requestConnection`), `ProtocolEngine.kt:start()` (collects `discoveredPeers`, sends PRESENCE to new peers), `LoopbackTransport.connectTo()` (test harness) | `MultiNodeTest` (Loopback-based connection/relay; Nearby discovery not exercised) | none | blocked — no device available | Advertising/discovery failures logged; connection failures logged; duplicate endpoint found skipped | partially verified |
| 4 | Two-node text | A sends TEXT to B; B receives and decrypts the exact payload | `TerminalViewModel.kt:sendTextMessage`/`sendBroadcastMessage` → `ProtocolEngine.kt:sendMessage(MessageType.TEXT, ...)` | `ProtocolEngineTest.testSendAndReceive` ("Hello B", LoopbackTransport A→B) | none | blocked — no device available | Direct `/msg` targeting path exists at protocol layer; app-layer delivery/ACK states not yet verified | verified |
| 5 | Three-node multi-hop text | A→B→C relay; C receives, TTL decremented per hop; originator does not echo its own message | `ProtocolEngine.kt:sendMessage` (ttl decrement + relay), `SuppressionTracker.kt`, `MultiNodeTest` | `MultiNodeTest.testLineTopologyRelay` (A→B→C, ttl 3, C receives with ttl 2, `receivedA.size == 0`) | none | blocked — no device available | TTL exhaustion drops the message; suppression stops re-relay after ≥2 peers | verified |
| 6 | Direct-message privacy against relay | Only the intended recipient decrypts; a relay knowing both public keys cannot decrypt | `ProtocolEngine.kt:sendMessage` (encrypts via `encryptForRecipient` when target hybrid key known), `TinkCryptoProvider.kt:encryptForRecipient`/`decryptFromSender` | `CryptoProviderTest.testCrossDeviceEciesRelayCannotDecrypt` (Alice→Bob works, Mallory cannot decrypt) | none | blocked — no device available | Relay with both public keys cannot decrypt (dual-key ECIE); previous `deriveSessionKey` vulnerability fixed (KNOWN ISSUE #13 resolved) | verified |
| 7 | Broadcast rules and channel separation | Messages tagged with a channel; UI displays only messages on the active channel; voice uses channel 1 | `ProtocolEngine.kt:sendMessage(channel=)`, `sendVoiceMessage(channel=1)`, `TerminalViewModel.kt:observeMessages` (filters `it.channel == currentChannel`), `sendBroadcastMessage` | none dedicated (channel field exercised indirectly) | none | blocked — no device available | Messages on non-current channel not displayed; separation is reception-side only | partially verified |
| 8 | Forgery and tampering rejection | Unsigned/forged envelopes dropped; unknown origin dropped; oversized frames (>64 KB) rejected before decode | `ProtocolEngine.kt:processIncoming` (signature verify via `cryptoProvider.verify`, missing public key → drop, `MAX_FRAME_SIZE`), `DigestSync.kt` (signature verification on sync responses) | `DigestSyncTest.testInvalidSignatureDropped`, `testUnknownOriginDropped` (4/4 pass) | none | blocked — no device available | Forged signature dropped; unknown origin dropped; malformed frame caught and dropped | verified |
| 9 | Replay and out-of-order delivery | Exact duplicates rejected; out-of-order counters within the 100-window accepted; counters outside the window rejected; origins tracked independently | `ProtocolEngine.kt:processIncoming` (`replayGuard.isReplay`/`record`), `ReplayGuard.kt` | `ReplayGuardTest` 6/6 (first accepted, in-order, out-of-order within window accepted, duplicate rejected, outside window rejected, origins independent) | none | blocked — no device available | Exact duplicate rejected; counter outside window rejected | verified |
| 10 | Ring loop prevention and suppression | Messages relayed with TTL-1; relay stops when TTL ≤ 0 or a msgId has been rebroadcast by ≥2 peers (60 s max age); originator does not re-deliver its own message | `SuppressionTracker.kt:shouldSuppress`/`recordRebroadcast`, `ProtocolEngine.kt:processIncoming` (relay block), `MultiNodeTest` (originator does not echo) | none dedicated (SuppressionTracker has no unit test; MultiNodeTest asserts no self-echo) | none | blocked — no device available | Loop persists until TTL expires; suppression threshold default 2; split horizon not implemented | partially verified |
| 11 | Offline queue and reconnect | Sent messages persisted with status; digest sync (`SYNC_REQ`/`SYNC_RESP`) recovers missing messages after reconnect | `DigestSync.kt`, `RoomSyncStore.kt`, `MessageEntity.kt` (status field), `MeshService.kt` (`engine.start()` idempotent on restart) | `DigestSyncTest` (sync response handling); no disconnect/reconnect test | none | blocked — no device available | Queued message recovery after restart not implemented (Milestone D open); `RoomSyncStore` uses `runBlocking` (KNOWN ISSUE #11 partial) | partially verified |
| 12 | Process restart and database migration | App restarts and Room DB upgrades cleanly via migration | `MeshDatabase.kt` (`version = 2`, entities `MessageEntity`/`PeerEntity`); no `Migration` implemented | none | none | blocked — no device available | Schema change on upgrade crashes without a migration | deferred |
| 13 | Incoming SOS and location honesty | SOS broadcast with ttl 12; location/accuracy/fix-age/pressure attached honestly; incoming SOS surfaces in emergency UI | `EmergencyViewModel.kt:triggerSos`/`onSosReceived`, `ProtocolEngine.kt:sendSos` (ttl=12), `MeshLocationManager.kt:getCurrentLocation` (`fixAgeMs`), `TerminalViewModel.kt:/sos` command | none | none | blocked — no device available | Missing/stale location carried without freshness gating; SOS not yet connected to notifications (Milestone E open) | partially verified |
| 14 | Voice-note send and receive | Record (AMR-NB, 8 kHz mono) → encode → transmit (MessageType.VOICE, channel 1) → receive → decode → playback | `MeshAudioManager.kt:startRecording`/`stopRecording`/`playAudio`, `TerminalViewModel.kt:startVoice`/`stopVoice` → `sendVoiceMessage` | none | none | blocked — no device available | `stopRecording` racy (KNOWN ISSUE #12, deferred to Milestone F); missing mic permission, codec errors, payload limits untested | deferred |
| 15 | Low battery and screen-off behavior | Battery < 20% → presence interval 30 s (else 10 s); foreground service keeps process alive | `MeshService.kt:getBatteryPercent` + `presenceIntervalMs` adjustment (lines 109-113); foreground notification (`onStartCommand`) | none | none | blocked — no device available | Battery checked only at service start, not on change; no screen-off/doze/background handling | partially verified |
| 16 | Missing sensors and disabled radios | App continues without barometer (`hasBarometer=false`, logged); radio failures logged, no crash | `MeshBarometerManager.kt` (`hasBarometer`, `startListening` no-op when absent), `NearbyTransport.kt` (start/connection failure logging) | none | none | blocked — no device available | Radios off → no discovery/connection (logged only); no graceful degradation test | partially verified |
| 17 | Error reporting and recovery | Errors surfaced to UI (`errLine`) or logged; malformed frames dropped silently; transport start failure does not kill service | `TerminalViewModel.kt` (catch → `errLine`), `MeshService.kt:96-101` (transport start catch), `ProtocolEngine.kt:207-209` (catch → drop malformed) | none dedicated | none | blocked — no device available | Silent drops in `ProtocolEngine` catch block; transport start failure leaves service running without mesh | partially verified |
| 18 | APK installation and upgrade | Debug APK installs and upgrades; database at version 2 | `assembleDebug` → `app-debug.apk`; `AlertMeshApp.kt:onCreate`; `MeshDatabase.kt` (version 2, no migration) | none (no install/instrumentation test) | none | blocked — no device available | Upgrade without migration may fail; install/upgrade on device not executed | partially verified |

## Feature details

### 1. Install and first launch
- **Status:** partially verified
- **Evidence location:** APK assembled and SHA-256 recorded in PRODUCTION_READINESS.md; `AlertMeshApp.kt`, `MainActivity.kt`, `MeshService.kt` compile clean; lint 0 errors
- **Automated test:** none
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Tink registration failure at `AlertMeshApp.onCreate`; missing permissions → service starts sticky without transport (`MeshService.kt:84-87`)
- **What's missing:** install + first-launch execution on a device or emulator; Robolectric test for `MainActivity` permission flow

### 2. Permission grant/denial/revocation
- **Status:** partially verified
- **Evidence location:** `MainActivity.kt:88-104`, `MeshService.kt:132-152`
- **Automated test:** none
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Denied location or Bluetooth permissions → `allPermissionsGranted()` false → service returns `START_STICKY` without starting transport, warning logged
- **What's missing:** runtime grant/deny/revoke test (device or Robolectric); permission-revocation mid-session behavior

### 3. Discovery and connection
- **Status:** partially verified
- **Evidence location:** `NearbyTransport.kt`, `ProtocolEngine.kt:start()`, `MultiNodeTest.kt`
- **Automated test:** `MultiNodeTest` exercises LoopbackTransport connection + relay; Nearby advertising/discovery is not exercised by any test
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Advertising/discovery failures logged; connection failures logged; duplicate endpoint found skipped (`NearbyTransport.kt:75-78`)
- **What's missing:** Nearby discovery/connection test on devices; `P2P_CLUSTER` strategy behavior; reconnect after lost endpoint

### 4. Two-node text
- **Status:** verified
- **Evidence location:** `ProtocolEngineTest.testSendAndReceive`; `TerminalViewModel.kt:sendTextMessage`
- **Automated test:** `ProtocolEngineTest.testSendAndReceive` — LoopbackTransport A→B, "Hello B", payload equality asserted
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Protocol-level direct `targetPeer` send implemented; app-layer delivery/ACK states not yet verified
- **What's missing:** device-to-device text delivery receipt test

### 5. Three-node multi-hop text
- **Status:** verified
- **Evidence location:** `MultiNodeTest.testLineTopologyRelay`; `ProtocolEngine.kt:processIncoming` relay block
- **Automated test:** `MultiNodeTest.testLineTopologyRelay` — A→B→C via LoopbackTransport, ttl 3, C receives with ttl 2, originator A receives 0 messages
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** TTL exhaustion drops message; suppression stops re-relay after ≥2 peers rebroadcast same `msgId`
- **What's missing:** multi-hop over real transport (Nearby/Socket); lossy-link multi-hop test

### 6. Direct-message privacy against relay
- **Status:** verified
- **Evidence location:** `CryptoProviderTest.testCrossDeviceEciesRelayCannotDecrypt`; `ProtocolEngine.kt:sendMessage` encryption branch
- **Automated test:** `CryptoProviderTest.testCrossDeviceEciesRelayCannotDecrypt` — Alice→Bob decrypts; Mallory (both public keys, own private key only) cannot
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Relay with both public keys cannot decrypt (dual-key ECIE); prior `deriveSessionKey` vulnerability fixed (KNOWN ISSUE #13 resolved)
- **What's missing:** end-to-end private delivery receipt on device; identity/private-key persistence in Android Keystore (Milestone C open)

### 7. Broadcast rules and channel separation
- **Status:** partially verified
- **Evidence location:** `TerminalViewModel.kt:observeMessages` (channel filter, lines 85-89), `sendBroadcastMessage` (uses `currentChannel`), `ProtocolEngine.kt:sendVoiceMessage(channel=1)`
- **Automated test:** none dedicated
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Messages on non-current channel are not displayed; separation is reception-side only
- **What's missing:** dedicated channel-separation test; cross-channel isolation test

### 8. Forgery and tampering rejection
- **Status:** verified
- **Evidence location:** `DigestSyncTest.testInvalidSignatureDropped`, `testUnknownOriginDropped`; `ProtocolEngine.kt:processIncoming` signature verification
- **Automated test:** `DigestSyncTest` 4/4 (forged signature dropped, unknown origin dropped); `ProtocolEngine` verifies `cryptoProvider.verify(originPubKey, signable, signature)` before delivery
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Forged signature dropped; unknown origin dropped; malformed/oversized frame (>64 KB) caught and dropped
- **What's missing:** tampered-payload-in-transit test; threat model documentation (Milestone C open)

### 9. Replay and out-of-order delivery
- **Status:** verified
- **Evidence location:** `ReplayGuardTest` 6/6; `ProtocolEngine.kt:processIncoming` (`replayGuard.isReplay`/`record`)
- **Automated test:** `ReplayGuardTest` — first accepted, in-order accepted, out-of-order within window accepted, duplicate rejected, outside window rejected, origins independent
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Exact duplicate rejected; counter outside the 100-window rejected
- **What's missing:** network replay injection test over real transport

### 10. Ring loop prevention and suppression
- **Status:** partially verified
- **Evidence location:** `SuppressionTracker.kt` (`shouldSuppress` ≥2 peers, 60 s max age); `ProtocolEngine.kt:processIncoming` (relay stops when `newTtl <= 0` or suppressed); `MultiNodeTest` asserts originator receives 0 messages
- **Automated test:** none dedicated (no unit test for `SuppressionTracker`)
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Loop persists until TTL expires; suppression threshold default 2; split horizon not implemented
- **What's missing:** dedicated ring-loop/loop-suppression test; concurrent rebroadcast race test

### 11. Offline queue and reconnect
- **Status:** partially verified
- **Evidence location:** `DigestSync.kt` (`SYNC_REQ`/`SYNC_RESP`), `RoomSyncStore.kt`, `MessageEntity.kt` (`status` field), `MeshService.kt` (idempotent `engine.start()`)
- **Automated test:** `DigestSyncTest` (sync response handling); no disconnect/reconnect test exists
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Queued message recovery after restart not implemented (Milestone D open); `RoomSyncStore` uses `runBlocking` (KNOWN ISSUE #11 partial)
- **What's missing:** store-and-forward after reconnect test; bounded retention policy (24 h / 500 messages)

### 12. Process restart and database migration
- **Status:** deferred
- **Evidence location:** `MeshDatabase.kt` (`version = 2`, entities `MessageEntity`/`PeerEntity`); no `Migration` implemented
- **Automated test:** none
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Schema change on upgrade crashes Room without a migration
- **What's missing:** `RoomDatabase.Migration` implementation; restart recovery test

### 13. Incoming SOS and location honesty
- **Status:** partially verified
- **Evidence location:** `EmergencyViewModel.kt:triggerSos`/`onSosReceived`; `ProtocolEngine.kt:sendSos` (ttl=12); `MeshLocationManager.kt:getCurrentLocation` (`fixAgeMs`); `TerminalViewModel.kt:/sos`
- **Automated test:** none
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Missing/stale location carried without freshness gating; SOS not yet connected to notifications (Milestone E open)
- **What's missing:** SOS receipt UI/notification test; location freshness/accuracy gating test

### 14. Voice-note send and receive
- **Status:** deferred
- **Evidence location:** `MeshAudioManager.kt` (record/encode/decode/play); `TerminalViewModel.kt:startVoice`/`stopVoice` → `sendVoiceMessage`
- **Automated test:** none
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** `stopRecording` racy (KNOWN ISSUE #12, deferred to Milestone F); missing mic permission, codec errors, payload/duration limits untested
- **What's missing:** record→transmit→decode→playback test; fix for issue #12

### 15. Low battery and screen-off behavior
- **Status:** partially verified
- **Evidence location:** `MeshService.kt:getBatteryPercent` + `presenceIntervalMs` adjustment (30 s if < 20%, else 10 s); foreground service notification
- **Automated test:** none
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Battery checked only at service start, not on change; no screen-off/doze/background handling
- **What's missing:** battery-change listener (`ACTION_BATTERY_CHANGED` receiver); screen-off/doze behavior test

### 16. Missing sensors and disabled radios
- **Status:** partially verified
- **Evidence location:** `MeshBarometerManager.kt` (`hasBarometer` false when no pressure sensor; `startListening` no-op); `NearbyTransport.kt` failure logging
- **Automated test:** none
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Radios off → no discovery/connection (logged only); app continues without barometer
- **What's missing:** sensor-absence test; radio-toggle/airplane-mode test

### 17. Error reporting and recovery
- **Status:** partially verified
- **Evidence location:** `TerminalViewModel.kt` catch blocks → `errLine`; `MeshService.kt:96-101` transport start catch; `ProtocolEngine.kt:207-209` catch → drop malformed
- **Automated test:** none dedicated
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Silent drops in `ProtocolEngine` catch block; transport start failure leaves service running without mesh
- **What's missing:** error-injection tests; user-facing recovery actions

### 18. APK installation and upgrade
- **Status:** partially verified
- **Evidence location:** `app/build/outputs/apk/debug/app-debug.apk` (SHA-256 `2309D4560F703EADB9FF60DE0F7EC719DBD4C683A803BB801EC871F82D6FA646`); `MeshDatabase.kt` version 2
- **Automated test:** none (no install/instrumentation test)
- **Simulator result:** none
- **Physical-device result:** blocked — no device available
- **Failure cases:** Upgrade without migration may fail; install/upgrade on device not executed
- **What's missing:** install/upgrade test on device; database migration test

## Summary

| Status | Count | Features |
|--------|-------|----------|
| verified | 5 | 4 Two-node text, 5 Three-node multi-hop text, 6 Direct-message privacy against relay, 8 Forgery and tampering rejection, 9 Replay and out-of-order delivery |
| partially verified | 11 | 1 Install and first launch, 2 Permission grant/denial/revocation, 3 Discovery and connection, 7 Broadcast rules and channel separation, 10 Ring loop prevention and suppression, 11 Offline queue and reconnect, 13 Incoming SOS and location honesty, 15 Low battery and screen-off behavior, 16 Missing sensors and disabled radios, 17 Error reporting and recovery, 18 APK installation and upgrade |
| deferred | 2 | 12 Process restart and database migration, 14 Voice-note send and receive |
| blocked | 0 | none at feature level (physical-device columns are blocked for all device-dependent features) |
| failed | 0 | none |
| **Total** | **18** | |

### Blockers
- No physical Android devices and no emulator: all device-dependent physical-device results are `blocked — no device available`, never "passed".
- End-to-end client receipt via `SocketTransport` + server: `blocked — environment socket hang` (protocol-level relay A→B→C verified via `LoopbackTransport`/`MultiNodeTest`).
- No network simulator is built (milestone H remaining); simulator results are "none" across the matrix.
- JDK 25 on PATH crashes Gradle 8.11.1; Gradle commands require `$env:JAVA_HOME` set to JDK 21.
