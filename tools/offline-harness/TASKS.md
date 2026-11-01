# AlertMesh local implementation queue

Inference: local-code:latest (Phi-4-mini, 3.84B, Q4_K_M), Ollama loopback only.
Use one focused task per run. Do not use cloud models or download dependencies.
Read relevant source before editing; preserve unrelated work. Do not commit or publish.
Never mark a task complete without recording changed files and verification evidence.
If verification is blocked, record the actual blocker. Never report production readiness from code changes alone.
Cryptographic changes require a separate security review before release.

- [ ] 1. Correct mesh transport lifecycle and permission sequencing. Avoid duplicate collectors on repeated service starts. Handle startup failure and stop the transport on shutdown.
- [ ] 2. Align simulator registration/frame JSON with SocketTransport and verify registration plus A-B-C forwarding.
- [ ] 3. Observe received TEXT messages in the terminal, preserving origin and channel; do not duplicate local echoes.
- [ ] 4. Resolve direct-message targets to connected transport endpoints. Report unknown/unreachable targets instead of silently broadcasting. Define multi-hop recipient routing separately.
- [ ] 5. Connect received SOS to alarm state and notifications; display position, accuracy, and fix age, including no-location cases.
- [ ] 6. Persist outgoing messages and implement honest queued/sent/acknowledged states. Add a real acknowledgment protocol before claiming delivery.
- [ ] 7. Verify each synced original signature, bound sync transfer sizes and retention, and validate partition recovery and restart recovery.
- [ ] 8. Repair suppression counting, dedup admission, replay ordering, counter concurrency, and identity-to-public-key binding. Add adversarial regression tests.
- [ ] 9. Design and review secure key persistence, recipient encryption, group-key membership, and rotation. Implement cross-device encryption only after documenting the protocol. Obtain independent security review.
- [ ] 10. Complete voice-note receive/playback, recording synchronization, duration/size limits, interruption handling, and cleanup.
- [ ] 11. Implement real ping/sync/trace operations, automatic stale-peer updates, real hop counts, and channel filtering. Do not print false success.
- [ ] 12. Make background operation and battery adaptation robust; test process death, radio toggles, denied permissions, missing sensors, and database upgrades.
- [ ] 13. Add CI and reproducible measurements for hop delivery, latency, loss, suppression, partition recovery, and crypto throughput. Physical-device tests remain required.
- [ ] 14. Prepare signed release, upgrade tests, pilot checklist, accurate documentation, and recorded release gates. Keep barometer experimental pending calibration.

Live voice streaming is deferred until text, SOS, recovery, and voice notes pass validation.

## Execution evidence

No implementation tasks have been completed by this queue yet.
