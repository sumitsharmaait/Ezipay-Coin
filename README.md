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
