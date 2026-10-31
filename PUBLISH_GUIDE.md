# Building and publishing the Android app

This guide takes you from a fresh checkout to an app on Google Play. It covers the four build
types, how each one reaches its server, signing, version numbers and the Play Console steps.

| Build type | Server | Use it for |
|---|---|---|
| `debug` | The Development Server running on your Mac | Day-to-day development |
| `staging` | The Staging Server | Team testing before a release |
| `uat` | The UAT server once one exists; the Staging Server until then | User-acceptance testing before production |
| `release` | The Production Server | Google Play |

## 1. Prerequisites

- **JDK 21.** Point `JAVA_HOME` at it, or choose it in Android Studio under
  *Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK*. The repository does
  not pin a JDK path; if you want one, put `org.gradle.java.home=<path>` in your own
  `~/.gradle/gradle.properties`.
- **Android Studio** (current stable) with **Android SDK Platform 36** and the SDK build tools.
  Command-line builds need `local.properties` with `sdk.dir=<path to the SDK>` (Android Studio
  writes it on first open).
- **GitHub Packages credentials** for Trust Wallet Core (`com.trustwallet:wallet-core`). Create a
  GitHub *classic* personal access token with the `read:packages` scope (fine-grained tokens are
  rejected) and add to `~/.gradle/gradle.properties`:

  ```properties
  gpr.user=<your GitHub user name>
  gpr.key=<the token>
  ```

  The environment variables `GPR_USER` / `GPR_KEY` work as well. Never put them in the repository.
- **Firebase config files** (`google-services.json`), from the project owner. See section 6.
- `adb` (part of the SDK platform tools) to install builds on a device.

Check the setup:

```bash
./gradlew :app:testDebugUnitTest
```

On a machine with little memory, add `--max-workers=2`.

## 2. Build types at a glance

| | `debug` | `staging` | `uat` | `release` |
|---|---|---|---|---|
| Application id | `com.app.ezipaycoin.debug` once a Firebase config lists it, otherwise `com.app.ezipaycoin` | `com.app.ezipaycoin.staging` | `com.app.ezipaycoin.uat` | `com.app.ezipaycoin` |
| App name on the launcher | Ezipay Coin | **Ezipay Coin Staging** (orange icon background) | **Ezipay Coin UAT** (purple icon background) | Ezipay Coin |
| Server (`API_BASE_URL`) | `http://10.0.2.2:5009/api/v1/` (override `-PapiBaseUrl=...`) | `http://18.142.108.53/api/v1/` (override `-PstagingApiBaseUrl=...`) | `-PuatApiBaseUrl=...`; **until it is set, the staging server** `http://18.142.108.53/api/v1/` | `https://api.ezipaycoin.com/api/v1/` (override `-PproductionApiBaseUrl=https://...`; the build warns, never upload such a build) |
| Chain | BSC Testnet (97) | **BSC Testnet (97) by default**; `-PstagingChain=mainnet` for BSC Mainnet (56) | **BSC Testnet (97) by default**; `-PuatChain=mainnet` for BSC Mainnet (56) | BSC Mainnet (56) only |
| In-app network switch | Yes (Profile > Network) | Yes (Profile > Network) | Yes (Profile > Network) | No |
| Cleartext HTTP | `10.0.2.2`, `127.0.0.1`, `18.142.108.53` | `18.142.108.53` only | `18.142.108.53` only | None (HTTPS only) |
| Minified, not debuggable | No | Yes | Yes | Yes |
| Signing | Debug key | Release key when configured, otherwise the debug key | Release key when configured, otherwise the debug key | Release key (unsigned without it) |
| Developer options in Profile | Yes | No | No | No |

All of these values live in `app/build.gradle.kts` (BuildConfig fields) and the per-build-type
resources under `app/src/debug`, `app/src/staging` and `app/src/uat`. No URL or chain id is written
in the Kotlin code, so you never edit source to change environment.

The build refuses to configure if release is not chain 56, if debug is chain 56, if staging or uat
is not 97 or 56, or if release could switch chain at runtime.

An optional fourth build type, `sandbox`, appears only when `app/sandbox.properties` exists (local
zero-cost sandbox, chain 31337). It is not part of the release process.

## 3. Debug build against the local Development Server

1. Start the backend (repository `Ezipay-Coin-Backend-API`) with the profile that listens on all
   interfaces, port 5009:

   ```bash
   dotnet watch run --launch-profile android-emulator
   ```

   Port 5000 is not used because macOS keeps it for the AirPlay receiver.

2. **Emulator.** Build and install; the emulator reaches the Mac at `10.0.2.2`:

   ```bash
   ./gradlew :app:installDebug
   ```

   Or press Run in Android Studio with the `debug` variant selected (*Build > Select Build
   Variant*).

3. **Physical device over USB** (simplest, no configuration change). Forward the port and point the
   build at the device's own loopback address, which is already allowed for cleartext:

   ```bash
   adb reverse tcp:5009 tcp:5009
   ./gradlew :app:installDebug -PapiBaseUrl=http://127.0.0.1:5009/api/v1/
   ```

4. **Physical device over Wi-Fi** (same network as the Mac).
   - Find the Mac's address: `ipconfig getifaddr en0` (for example `192.168.1.20`).
   - Debug builds allow cleartext HTTP only to the hosts listed in
     `app/src/debug/res/xml/network_security_config.xml`, so a LAN address is blocked until you add
     it. Add one line inside the existing `<domain-config>` and do not commit it:

     ```xml
     <domain includeSubdomains="false">192.168.1.20</domain>
     ```

   - Build with that address:

     ```bash
     ./gradlew :app:installDebug -PapiBaseUrl=http://192.168.1.20:5009/api/v1/
     ```

   - If the app cannot connect, allow incoming connections for `dotnet` in the macOS firewall.

Debug builds run on BSC Testnet (97). Profile > Network switches to Mainnet and back (see
section 4, "Switching chain in the app"); Profile > "Developer: show unfinished features" shows features that are off in release.

## 4. Staging build for the team

The staging app installs next to the production app (different application id, name and icon),
so testers can keep both.

> **Staging runs on BSC TESTNET (chain 97) by default.** Every staging build prints which chain it
> targets, for example:
> `Staging build targets BSC TESTNET (97) at http://18.142.108.53/api/v1/ - pass -PstagingChain=mainnet to switch`

### Build

```bash
./gradlew :app:assembleStaging      # APK for direct install
./gradlew :app:bundleStaging        # AAB, e.g. for a Play internal testing track
```

Outputs:

| Artifact | Path |
|---|---|
| APK | `app/build/outputs/apk/staging/app-staging.apk` |
| AAB | `app/build/outputs/bundle/staging/app-staging.aab` |

Options (combine as needed):

| Option | Effect |
|---|---|
| `-PstagingChain=mainnet` | Build for BSC Mainnet (56): mainnet RPC and token contracts. **Real funds.** Default is `testnet`. |
| `-PstagingApiBaseUrl=http://host/api/v1/` | Another staging server. Keep the trailing `/`. A plain-HTTP host other than `18.142.108.53` also needs a line in `app/src/staging/res/xml/network_security_config.xml`. |

### Signing

When the four `EZIPAY_RELEASE_*` values (section 7) are set, staging is signed with the release
key. Without them it is signed with the debug key of the machine that built it, and the build
prints a line saying so. That APK installs fine, but Android only accepts an update signed with the
same key, so let one machine build the staging APKs the team installs (or uninstall before
installing a build from another machine). A debug-key AAB cannot be uploaded to Play.

### Install

- With a cable: `adb install -r app/build/outputs/apk/staging/app-staging.apk`
- Without one: share the APK file (drive, chat). On the phone, open it and allow "Install unknown
  apps" for the app that opened it.

### Switching chain in the app

Profile > **Network** shows the chain in use and lets you pick BSC Testnet (97) or BSC Mainnet (56).
The choice is stored and the app restarts to apply it. Signing, the RPC node, token contracts and
explorer links follow the choice. Balances, history and token lists come from the server, which
stays on its own chain, so pick the chain the staging server runs on. The row exists only in debug,
staging and UAT builds.

### Firebase in staging

Until a Firebase app is registered for `com.app.ezipaycoin.staging`, staging builds with Firebase
switched off: no push notifications (no push token is sent at sign-in) and no Google/Facebook
sign-up buttons. Everything else works. To turn it on, register the Android app
`com.app.ezipaycoin.staging` in the (non-production) Firebase project, download its
`google-services.json` and save it as:

```
app/src/staging/google-services.json
```

The file is git-ignored. The next build detects it (`BuildConfig.FIREBASE_ENABLED=true`). The same
rule applies to every build type except release: a variant whose Firebase file does not list its
application id builds with Firebase off and prints a line saying so.

## 4a. UAT build for user-acceptance testing

UAT is the last check before production: business users and testers accept a release candidate
on a build that behaves like production (minified, not debuggable) but points at a test server.
UAT builds are made from the `uat` branch. The app installs next to staging and production
(`com.app.ezipaycoin.uat`, named **Ezipay Coin UAT**, purple icon background), so one device can
hold all three.

> **UAT runs on BSC TESTNET (chain 97) by default**, like staging.

**Server.** There is no UAT server yet. Until there is, UAT uses the staging server
`http://18.142.108.53/api/v1/`, and every UAT build says so:

```
UAT has no server of its own yet: using the staging server http://18.142.108.53/api/v1/. Set -PuatApiBaseUrl=... (or uatApiBaseUrl in ~/.gradle/gradle.properties) once a UAT server exists.
```

Once a UAT server exists, pass `-PuatApiBaseUrl=https://<uat host>/api/v1/` (keep the trailing `/`)
or put `uatApiBaseUrl=...` in `~/.gradle/gradle.properties`. An HTTPS host needs nothing else. A
plain-HTTP host other than `18.142.108.53` also needs a `<domain>` line in
`app/src/uat/res/xml/network_security_config.xml`.

### Build

```bash
./gradlew :app:assembleUat      # APK: app/build/outputs/apk/uat/app-uat.apk
./gradlew :app:bundleUat        # AAB: app/build/outputs/bundle/uat/app-uat.aab
```

| Option | Effect |
|---|---|
| `-PuatChain=mainnet` | Build for BSC Mainnet (56). **Real funds.** Default is `testnet`. |
| `-PuatApiBaseUrl=https://host/api/v1/` | The UAT server (see above). |

Signing, installing, the in-app network switch and Firebase work exactly as for staging (section 4),
with `uat` in place of `staging`: the Firebase file is `app/src/uat/google-services.json` and must
list `com.app.ezipaycoin.uat`; until then UAT builds with push and social sign-up switched off.

## 5. Production release

### Build

```bash
./gradlew :app:bundleRelease        # AAB for Google Play
./gradlew :app:assembleRelease      # APK, only for checks on a device
```

| Artifact | Path |
|---|---|
| AAB | `app/build/outputs/bundle/release/app-release.aab` |
| APK | `app/build/outputs/apk/release/app-release.apk` (`app-release-unsigned.apk` without signing) |

Release always uses the Production Server and BSC Mainnet, has no network switch and allows no
cleartext traffic. It needs `app/google-services.json` with a client for `com.app.ezipaycoin`; the
build fails without it, so a release never ships without push.

Before uploading, install the release APK on a device and check sign-in, balances and a small send.

## 6. Firebase files

| Build type | File (the first one that exists is used) | Must list |
|---|---|---|
| `debug` | `app/src/debug/google-services.json`, then `app/google-services.json` | `com.app.ezipaycoin.debug` (otherwise debug keeps `com.app.ezipaycoin`) |
| `staging` | `app/src/staging/google-services.json`, then `app/google-services.json` | `com.app.ezipaycoin.staging` |
| `uat` | `app/src/uat/google-services.json`, then `app/google-services.json` | `com.app.ezipaycoin.uat` |
| `release` | `app/src/release/google-services.json`, then `app/google-services.json` | `com.app.ezipaycoin` |

None of these files is committed. Ask the project owner for them.

## 7. Signing and the keystore

The release keystore and its passwords never go into the repository. Gradle reads four values,
from `~/.gradle/gradle.properties` or from environment variables of the same name:

| Name | Meaning |
|---|---|
| `EZIPAY_RELEASE_STORE_FILE` | Absolute path to the keystore file |
| `EZIPAY_RELEASE_STORE_PASSWORD` | Keystore password |
| `EZIPAY_RELEASE_KEY_ALIAS` | Key alias |
| `EZIPAY_RELEASE_KEY_PASSWORD` | Key password |

- Keep the keystore outside the repository (for example in a password manager or a secure folder
  under your home directory) with a backup. `*.jks` and `*.keystore` are git-ignored as a safety net.
- If any value is missing, `bundleRelease` and `assembleRelease` still build, but the output is
  unsigned and Play rejects it.
- **Play App Signing.** Google Play holds the key that signs what users install. The key above is
  the *upload key*: Play checks that each upload is signed with it. If the upload key is lost or
  leaked, the account owner can request an upload key reset in Play Console (*Test and release >
  App integrity*); the app's signing key is unaffected.
- Check a signed AAB with `jarsigner -verify -verbose app/build/outputs/bundle/release/app-release.aab`.

## 8. Version numbers

Both live in `app/build.gradle.kts` under `defaultConfig`:

```kotlin
versionCode = 3
versionName = "1.1.0"
```

- `versionCode` is an integer that **must be higher than every version code already uploaded to
  Play** for this app, on any track. Increase it by one for every upload, including internal
  testing uploads and re-uploads after a rejected build.
- `versionName` is what users see. Use `MAJOR.MINOR.PATCH`: patch for fixes, minor for features,
  major for breaking changes.
- Debug, staging and UAT add `-debug` / `-staging` / `-uat` to the version name automatically.
- Change both in one commit on `dev` (for example "Bump version to 1.2.0 (4)") before building the
  release.

## 9. Publishing on Google Play Console

You need access to the app in [Google Play Console](https://play.google.com/console) with release
permissions.

1. **Build** a signed `app-release.aab` (sections 5 and 7) with a new `versionCode`.
2. **Internal testing first.** *Test and release > Testing > Internal testing > Create new
   release*. Upload the AAB. Play shows the version code and name it read; check them.
3. **Release name and notes.** The release name defaults to the version name. Add release notes
   per language in the `<en-US>...</en-US>` form Play shows.
4. **Review and roll out** to internal testing. Add testers (email list) under the *Testers* tab
   and share the opt-in link. Install from Play on a device and run the smoke checks.
5. **Closed testing** (optional but recommended): *Testing > Closed testing*, create a release by
   promoting the same build (*Promote release*) or uploading it again. New personal developer
   accounts must run a closed test with enough testers for the required period before production
   access is granted.
6. **App content** (once, then whenever something changes): under *Policy and programs > App
   content* complete or review the **Data safety** form (the app collects email, name and push
   token, and handles wallet addresses; keys never leave the device), **Content rating**
   questionnaire, **Target audience and content**, **Ads** declaration, **Financial features**
   declaration (the app is a crypto wallet), privacy policy URL and the **account deletion** link
   (the app has in-app deletion).
7. **Store listing**: check title, descriptions, screenshots and icon under *Grow users > Store
   presence > Main store listing*.
8. **Production.** *Test and release > Production > Create new release*, promote the tested build
   or upload it, add release notes, then *Next*. Fix every error the review page lists; warnings are
   worth reading.
9. **Staged rollout.** Set a rollout percentage (for example 10 %), then *Start rollout to
   Production*. Watch *Quality > Android vitals* (crashes, ANRs) and reviews; raise the percentage
   in steps to 100 %. *Halt rollout* stops new installs if something is wrong.
10. **Review.** Google reviews the release before it goes live, from hours to several days. The
    status is on the *Publishing overview* page; with *Managed publishing* on, you choose when an
    approved release goes live.

After the rollout, tag the released commit (for example `v1.2.0`) and record the version code.
