# Testing

Three kinds of tests, one report.

| Kind | Where | Runs on | What it covers |
|---|---|---|---|
| Unit | `app/src/test` | JVM | View models, validators, repositories against a mock web server, the backend contract |
| Instrumented | `app/src/androidTest` | emulator or device | Compose screens with fake repositories, real wallet-core signing |
| Report | `scripts/test-report.sh` | host | Turns the results above into `docs/test-reports/latest.md` |

## Running

Unit tests and coverage:

```bash
./gradlew testDebugUnitTest jacocoTestReport --no-daemon --max-workers=2 \
  -Dorg.gradle.parallel=false -Dorg.gradle.jvmargs=-Xmx2048m --console=plain
```

- Results: `app/build/test-results/testDebugUnitTest/*.xml`
- Coverage: `app/build/reports/jacoco/jacocoTestReport/html/index.html` (and `jacocoTestReport.xml`)
- One class: add `--tests 'com.app.ezipaycoin.presentation.pay.PayViewModelTest'`

Instrumented tests (emulator or device attached; pick the serial explicitly if more than one is connected):

```bash
ANDROID_SERIAL=emulator-5556 ./gradlew connectedDebugAndroidTest
```

- Results: `app/build/outputs/androidTest-results/connected/`
- The tests use the real app process and its encrypted preferences store, and wipe the store before and
  after each test. Use an emulator, not a device with a wallet you care about.
- No test touches the network. Fake repositories replace Retrofit; the chain is a fake too.

Report (after either run, or both):

```bash
scripts/test-report.sh
```

It writes `docs/test-reports/latest.md` and `docs/test-reports/<UTC-date>-<git-sha>.md`: totals per kind, every test
with status and time, line/branch coverage overall and for the key packages, and the environment (git, JDK,
Gradle, AGP, Kotlin, device). It reads existing results and never runs tests. `DEVICE_NAME="Pixel 7 API 34"`
labels the device when the result files do not.

## What is covered

Unit tests, by area:

- **Send flow** (`PayViewModelTest`): amount parsing, address checks (format, EIP-55, own address), coin resolution,
  balance and fee checks, the confirm step, sign, broadcast, receipt polling (mined, reverted, still pending),
  double-submit guards, error mapping. ERC-20 `transfer` calldata and wei scaling are asserted against fixed
  vectors through the gas-estimate request; the chain id and nonce are read from the signing input.
- **Validators** (`utils/validation/*Test`): amount, address and checksum, coin kind, error mapper, receipt backoff, throttle.
- **Dashboard refresh** (`WalletSharedViewModelTest`): load at unlock, resume throttle, push refresh, pull to refresh,
  last good data kept on failure.
- **Profile** (`MyProfileViewModelTest`, `ProfileMergeTest`): Users/me, cache fallback, rename, logout, picture upload.
- **History** (`AllTransactionsViewModelTest`): paging and filter behaviour, including two pinned defects.
- **HTTP** (`RepositoryApiTest`): each repository against `MockWebServer`; request line, headers (Bearer), body and
  envelope parsing, `apiStatus=false`, `FEATURE_DISABLED`, 401, and the JSON-RPC calls to the node.
- **Contract** (`ApiContractTest`): every route in `ApiService` must exist in the backend baseline copy at
  `app/src/test/resources/contract/backend-baseline.txt`, request DTO fields must match the backend DTOs, and the KYC
  form field names are compared with the backend form.

Instrumented tests:

- `UnlockScreenTest`, `HomeScreenTest`, `SendScreenTest`, `ReceiveScreenTest`, `TransactionsScreenTest`,
  `ProfileScreenTest`: what the user sees and the messages shown; pull to refresh; the confirmation dialog.
- `DialogueAccessibilityTest`, plus content-description assertions in the screen tests.
- `PaymentSigningTest`: real wallet-core signs a token transfer and a BNB transfer; the broadcast bytes are checked
  for calldata, recipient, amount and the EIP-155 chain id.

## Conventions

- No `Thread.sleep`, no real clock, no real network. View models run on `MainDispatcherRule` (virtual time);
  advance with `main.scheduler.runCurrent()`, `advanceTimeBy`, `advanceUntilIdle`.
- Repositories are MockK mocks in unit tests and small fakes in `androidTest/.../support/Fakes.kt`.
- `UserPreferencesRepository` is a singleton; unit tests swap it with `FakeDataStore.install(...)` and always
  `unmockkAll()` in `@After`.
- wallet-core is native and cannot load on the JVM. Unit tests replace `AnySigner.sign`; anything that needs the
  real library goes in `androidTest`.
- Golden JSON lives in `app/src/test/resources/golden`. Shapes follow the recordings in
  `ezipay-coin-platform/contracts/golden`; keep them short and free of real addresses, tokens and keys.
- Tests that pin a defect are named `*KnownIssue*` and carry a comment saying what the fixed behaviour should be.
  When the defect is fixed the test is changed on purpose, not deleted.
- `ApiContractTest.KNOWN_GAPS` lists routes the app calls that the backend baseline lacks, each with a reason.
  A new route that is in neither place fails the build. Remove an entry when its gap closes.
- Refresh the baseline copy by re-copying the route column from the backend's `contract-baseline.txt`.
- Tests may not be `@Ignore`d to get a green run.

## Adding tests for new code

New view model: inject its repository through the constructor, build it in a test with `MainDispatcherRule`, assert
`uiState.value` after `runCurrent()`. New endpoint: add a golden file and a case in `RepositoryApiTest`; the
contract test picks up the route automatically. New screen: add a `ScreenTest` subclass in `androidTest/.../ui`.

## Known limits

- The refresh throttles in `WalletSharedViewModel` read the system clock, so tests only check "a second call right
  away is throttled". Checking that the window expires needs a clock parameter on the view model.
- The signing step of the token path cannot run on the JVM (`AnyAddress` is native); it is covered on a device by
  `PaymentSigningTest`.
