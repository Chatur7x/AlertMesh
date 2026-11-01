# AlertMesh Threat Model & Key Lifecycle

Stage 3 (Security) documentation. No source code changes are described here;
this document records the security design, verified properties, and remaining
release gates for the AlertMesh protocol module.

---

## 1. Threat Model

### Assets to protect

| Asset | Description |
|-------|-------------|
| Message content | Plaintext of direct and relayed messages (text, voice, SOS payloads) |
| Sender / recipient identity | `deviceId`, Ed25519 signing public key, ECIE hybrid public key bound to a device |
| Location data | Any location-bearing payloads carried in messages |
| Presence beacons | PRESENCE messages advertising a device's public keys and online state |

### Threat actors

| Actor | Capabilities / motivation |
|-------|---------------------------|
| Relay nodes | Forward frames between peers; see ciphertext and metadata but hold no private keys |
| Network attackers | Read, inject, reorder, delay, or drop frames on the transport |
| Device compromise | Full access to a single device's memory, storage, and keys |
| Physical theft | Loss or seizure of a running or powered-off device |

### Trust boundaries

| Boundary | What it separates | Trust assumption |
|----------|-------------------|------------------|
| Transport layer | Peer-to-peer radio / network frames | Untrusted: frames may be read, injected, or replayed |
| Protocol engine | Message processing, signature verification, key storage | Trusted to verify signatures before delivery/relay |
| App layer | UI, fingerprint display, presence provider | Trusted to display fingerprints correctly and invoke the engine |
| Device boundary | App process vs. OS / hardware | Trusted to protect in-memory keys; Android Keystore migration pending |

### Assumptions

- Tink is correctly implemented and its primitives (Ed25519, ECIE, AES-256-GCM) behave as specified.
- Android Keystore is secure when available (migration from in-memory storage is pending; see Key Lifecycle).
- Users verify fingerprints out-of-band (e.g. in person or via a separate trusted channel) before trusting a peer's binding.

### Threats NOT in scope

- Metadata traffic analysis (who talks to whom, when, and how often)
- Side-channel attacks (timing, power, electromagnetic, cache)
- Compromised endpoint devices (if a device is fully compromised, its keys and messages are lost)
- Denial of service / transport availability

---

## 2. Key Lifecycle

### Identity generation

On first launch, `TinkCryptoProvider.generateIdentity()` creates:

- An **Ed25519 signing keypair** (`SignatureKeyTemplates.ED25519`) — used for message signatures and user-visible fingerprints.
- An **ECIE hybrid keypair** (`HybridKeyTemplates.ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM`) — used for direct message encryption via `HybridEncrypt`/`HybridDecrypt`.

The `deviceId` is derived as the first 8 hex bytes of SHA-256 over the serialized Ed25519 public key. The fingerprint is generated from the same public key hash (see Fingerprint verification below).

### Key storage

Identity is currently held in memory via Tink `CleartextKeysetHandle`:

- `signKeysetHandle` / `hybridKeysetHandle` / `sessionAeadKeysetHandle` live as JVM `KeysetHandle` instances for the process lifetime.
- **Identity is lost on app restart** — no persistent backing store exists yet.
- **Android Keystore-backed persistence is PENDING** (release gate #1).

### Key exchange

Public keys are exchanged via **PRESENCE messages**, broadcast periodically and sent to newly discovered peers. A PRESENCE payload carries both the Ed25519 public key and the ECIE hybrid public key. `ProtocolEngine` stores discovered keys in:

- `knownPublicKeys[deviceId]` — Ed25519 key (signature verification)
- `knownHybridKeys[deviceId]` — ECIE key (encryption)

### Key binding

A `deviceId` is bound to both public keys through **proof of possession via signatures**:

- Every outgoing `MessageEnvelope` is signed over `envelope.signable()` (canonical header, excluding TTL) with the device's Ed25519 private key.
- On receipt, `ProtocolEngine` verifies the signature against the origin's known Ed25519 public key **before** delivery or relay. Unsigned or incorrectly signed frames are dropped.
- DigestSync likewise verifies signatures before saving synced messages (forgery protection).

### Key rotation

**Not yet implemented.** Defined behavior is PENDING (release gate #2). No protocol exists to retire, replace, or re-distribute keys after initial generation.

### Reinstall / key-loss behavior

- On reinstall, `generateIdentity()` runs again, producing a brand-new Ed25519 + ECIE keypair and a new `deviceId`/fingerprint.
- Old messages encrypted under the previous hybrid key are **undecryptable** (the old private key is gone).
- Contacts must **re-verify fingerprints** out-of-band, since the new identity is untrusted until bound and verified.

### Fingerprint verification

- `NodeIdentity.generateFingerprint()` hashes the Ed25519 public key with SHA-256 and maps the first 4 bytes to 4 words from a 256-word list, joined by `-` (e.g. `apple-brave-crane-delta`).
- The 4-word fingerprint is displayed in the UI for **out-of-band verification** by users. Verification itself is not yet implemented in UI (release gate #3).

---

## 3. Cryptographic Design

| Component | Algorithm / primitive | Details |
|-----------|----------------------|---------|
| Signing | Ed25519 | Tink `PublicKeySign` / `PublicKeyVerify` (`SignatureKeyTemplates.ED25519`) |
| Encryption | ECIE (hybrid) | Tink `HybridEncrypt` / `HybridDecrypt` (`ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM`) |
| Symmetric payload | AES-256-GCM | Tink `Aead` (`AeadKeyTemplates.AES256_GCM`) for link/session encryption |
| Message authentication | Ed25519 signature | Signature over canonical header (`envelope.signable()`), excluding TTL; verified before delivery/relay |
| Key derivation | Hybrid key agreement via Tink | Direct recipient encryption via ECIE; NOT public-key-only SHA-256 (that vulnerability was fixed) |

Note: `deriveSessionKey()` retains a legacy SHA-256-based combined-key hash for session AEAD setup, but direct message confidentiality relies on Tink ECIE, not public-key-only hashing.

---

## 4. Security Properties (verified vs. claimed)

| Property | Status | Evidence |
|----------|--------|----------|
| Message authenticity | Verified | Ed25519 signature verified before delivery/relay (ProtocolEngineTest, CryptoProviderTest) |
| Direct message confidentiality | Verified | ECIE: Alice→Bob decrypts, Mallory cannot (CryptoProviderTest.testCrossDeviceEciesRelayCannotDecrypt) |
| Relay cannot decrypt direct messages | Verified | Same test above |
| Replay protection | Verified | ReplayGuard with reordering window (ReplayGuardTest 6/6) |
| Duplicate suppression | Verified | DedupCache + SuppressionTracker |
| Sync response forgery protection | Verified | DigestSync verifies signatures before saving (DigestSyncTest 4/4) |
| Frame size limits | Verified | MAX_FRAME_SIZE 64KB in ProtocolEngine |
| Dedup time-based expiration | Verified | DedupCache 5-min TTL (DedupCacheTest 4/4) |
| Android Keystore persistence | NOT IMPLEMENTED | CleartextKeysetHandle in memory; migration PENDING |
| Key rotation | NOT IMPLEMENTED | Defined behavior PENDING |

---

## 5. Remaining Security Work (Release Gates)

The following must be completed before pilot deployment:

1. **Android Keystore-backed identity storage** — replace `CleartextKeysetHandle` in-memory storage with Android Keystore-backed persistence so identity survives app restart.
2. **Key rotation protocol** — define and implement behavior for retiring, replacing, and redistributing keys.
3. **Fingerprint verification UI** — implement the user-facing flow to display and confirm 4-word fingerprints out-of-band.
4. **Independent security review** — third-party review of the protocol, cryptography, and implementation.
5. **Physical-device penetration testing** — assess resistance to device compromise and physical theft scenarios.

---

*Document only verified claims; unverified items are marked PENDING or NOT IMPLEMENTED above. No source code was modified in producing this document.*
