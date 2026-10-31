# EziPay Coin (Android)

## Release signing

The release keystore and its passwords are not stored in the repository. Provide them as gradle
properties (for example in `~/.gradle/gradle.properties`) or as environment variables:

| Name | Meaning |
|---|---|
| `EZIPAY_RELEASE_STORE_FILE` | Path to the keystore file |
| `EZIPAY_RELEASE_STORE_PASSWORD` | Keystore password |
| `EZIPAY_RELEASE_KEY_ALIAS` | Key alias |
| `EZIPAY_RELEASE_KEY_PASSWORD` | Key password |

When any of the four is missing, `assembleRelease` still builds, but the APK is unsigned and cannot be
installed or uploaded. CI jobs that only need to compile can run without them.

## Build types

| Build type | Application id | Chain | API | Notes |
|---|---|---|---|---|
| `debug` | `com.app.ezipaycoin.debug` when a Firebase config knows it, else `com.app.ezipaycoin` | BSC testnet (97), testnet RPC and test token contracts | `http://10.0.2.2:5009/api/v1/` by default (the .NET API on the host; override with `-PapiBaseUrl=...`) | Never mainnet by default. Profile > Network switches chain at runtime. |
| `staging` | `com.app.ezipaycoin.staging`, named "Ezipay Coin Staging" | **BSC testnet (97) by default**; `-PstagingChain=mainnet` for 56 | `http://18.142.108.53/api/v1/` (override with `-PstagingApiBaseUrl=...`) | Release-like (minified, not debuggable), installs next to production. Cleartext only to the staging host. Signed with the release key when configured, else the debug key. Profile > Network switches chain at runtime. |
| `release` | `com.app.ezipaycoin` | BSC mainnet (56) | `https://api.ezipaycoin.com/api/v1/` (override with `-PproductionApiBaseUrl=https://...`) | Minified, no logging, no cleartext, no chain switch. Needs the signing properties above. |
| `sandbox` | as debug | Local chain (31337) | The local sandbox in `ezipay-coin-platform` | Exists only on the `sandbox` branch, where `app/sandbox.properties` is present. Its values never reach a release build. |

The build fails at configuration if release is not chain 56, debug is chain 56, staging is not 97
or 56, or release could switch chain at runtime.

Building, installing and publishing each build type, step by step: [PUBLISH_GUIDE.md](PUBLISH_GUIDE.md).

Token contract addresses for balances and sends come from the dashboard response
(`contractAddress`), not only from the build. The backend's `Contracts` table for the
environment you point at must hold that environment's addresses.

## Firebase

Each build type uses its own Firebase project, so test pushes never reach production users.

- `release`: `app/google-services.json` (production project).
- `debug`: `app/src/debug/google-services.json`, the test project `ezipay-coin-001`. The Gradle
  plugin prefers it over the app-level file. The `.debug` application id is applied only when
  the file in use (the first of the two that exists) has a client for `com.app.ezipaycoin.debug`; otherwise the build warns and
  keeps the release id.
- `staging`: `app/src/staging/google-services.json` with a client for `com.app.ezipaycoin.staging`.

Firebase is optional except in release. When the file a variant uses has no client for its
application id, the variant builds with `BuildConfig.FIREBASE_ENABLED=false`: no push token and no
social sign-up, nothing else changes. Release still requires `app/google-services.json`.

Neither file is committed (see `.gitignore`). Get them from the project owner.

## Feature flags and `App/config`

Unfinished features are hidden in release builds. `config/Feature.kt` lists each feature with
its release default and whether the client can drive it. At start the app reads
`GET api/v1/App/config`; the `features` object overrides the defaults. A key missing from a
server answer means off. If the server cannot be reached the build defaults stay. A feature
the client cannot drive (KYC, NFT mint) stays hidden even when the server turns it on.

In a debug build, Profile > "Developer: show unfinished features" shows everything.

`App/config` also carries the Home banners, the minimum supported version and the support and
terms links. Banners are managed on the server; the app has no static promotions.

## Payments

The app does not talk to the payment vendor and holds no vendor credential. Deposits go
through `api/v1/Payments/*` on our backend, which adds the credential. The routes are
switched off on the server until the operator enables them, so the app treats
`apiMessage: FEATURE_DISABLED` as "not available". On-chain withdrawal is hidden
(`FEATURE_ONCHAIN_WITHDRAW=false`). Details: [docs/production-readiness.md](docs/production-readiness.md).

## Tests

JVM unit tests run without a device:

```bash
./gradlew testDebugUnitTest --no-daemon --max-workers=2
```

Coverage and the written test report, and the instrumented tests, are described in
[docs/testing.md](docs/testing.md); each run is recorded under `docs/test-reports/`.

## More

- [PUBLISH_GUIDE.md](PUBLISH_GUIDE.md): build types, the local server, staging builds for the team, signing, versions and Google Play
- [docs/production-readiness.md](docs/production-readiness.md): what changed, the security model and what is deferred
- [docs/feature-inventory.md](docs/feature-inventory.md): every screen and tile and its status
