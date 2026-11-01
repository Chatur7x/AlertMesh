# AlertMesh release build

The Android release variant uses a dedicated signing key when these environment variables are set:

| Variable | Value |
|---|---|
| `ALERTMESH_RELEASE_STORE_FILE` | Absolute path to the PKCS#12 keystore |
| `ALERTMESH_RELEASE_STORE_PASSWORD` | Keystore password |
| `ALERTMESH_RELEASE_KEY_ALIAS` | Signing key alias |
| `ALERTMESH_RELEASE_KEY_PASSWORD` | Signing key password |

Set `ALERTMESH_REQUIRE_SIGNED_RELEASE=1` to fail the build if any signing value is missing. Then run `./gradlew :protocol:test :server:build :app:assembleRelease :app:lintRelease` with JDK 21. Verify the output with Android SDK `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`.

The first dedicated release key and its environment file were generated locally under `.artifacts/release-key/`, which Git ignores. **Back up both files securely and separately from the source repository.** Losing this key prevents in-place updates to apps signed with it. Never commit the key or passwords. The release certificate SHA-256 is `86823314FCB367ED2A5A9668B479EF76850273410A2EDE1101C24E55EB4A3511`.

The Samsung test phone already has `com.alertmesh` version `0.1.0-rc1` signed with a different Android debug key (certificate SHA-256 `3C382FF80BF4FFAE967C60BB65A85E3376F416E4C4DAF9FB656C00DF9E08BF70`). Android correctly rejects an in-place update with the new release key. Preserve that installation and its data until its original key is found or the data is exported and a reinstall is explicitly approved. The Pixel accepted the new release package.

This signed APK is a release candidate. A signed build alone does not establish disaster-response reliability or independent security assurance.
