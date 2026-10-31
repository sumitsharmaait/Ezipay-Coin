# Production readiness

What was changed in the Android client in the production-readiness round, why, and what is
left. Each item names the branch it was merged from. The audit behind it assigned the IDs
(AND-A-nn).

## Release blockers closed

| Area | Change | Branch |
|---|---|---|
| Vendor credential | Payment vendor calls go through the backend `Payments/*` proxy. The basic-auth credential and vendor URLs are gone from the app. The on-device card deposit module is deleted. (AND-A-01, 30) | `fix/no-secrets-in-app` |
| Merchant payout | On-chain withdraw is hidden by `FEATURE_ONCHAIN_WITHDRAW=false`; the vendor payout let any user move merchant funds. (AND-A-02) | `fix/no-secrets-in-app` |
| Logging | Network logging is `HEADERS` in debug and `NONE` in release, with `Authorization` redacted. Token logs are removed; R8 strips `Log` in release. (AND-A-03, 22) | `fix/release-logging-and-signing`, `fix/no-secrets-in-app` |
| Signing | Release signing reads `EZIPAY_RELEASE_*` properties or environment, not a tracked file. (AND-A-04) | `fix/release-logging-and-signing` |
| Network and backup | No cleartext traffic in release, `allowBackup=false` with exclusions, no exported Facebook provider. (AND-A-17, 21, 41) | `fix/release-logging-and-signing` |
| Wrong chain in debug | Debug builds use BSC testnet with test contracts and a separate application id; a build check stops a mainnet debug build. | `fix/build-variants` |
| Sending | Amount and address validation (EIP-55 checksum, self-send), balance and fee check before signing, confirm dialog, in-flight guard, readable error messages, longer receipt polling, coin resolved by contract instead of defaulting to USDT. (AND-A-05 to 10) | `fix/send-validation` |
| Freshness | Balances refresh on resume (10 s throttle), pull to refresh and on a foreground push. | `fix/home-refresh` |
| Permissions | Launch asks only for notifications; camera is asked when the scanner opens; the profile picture uses the system picker. | `fix/permissions-on-demand` |
| Profile | `Users/me` is loaded and cached for offline use. | `fix/profile-load` |
| Unfinished features | Feature flags with release defaults and an `App/config` override. Earn (summary, check-in, tasks) and Learn use the backend; fake APY, airdrop, leaderboard, spin and static promotions are removed or dev-only. | `feat/feature-gating-and-app-config`, `feat/earn-wiring`, `fix/remove-static-promos` |
| Banners | Home banners come from `App/config`; the Pay "Utilities" tab is hidden behind a flag. | `feat/dynamic-banners` |
| Seed and key safety | Seed and key reveal need the password, use `FLAG_SECURE`, give no key copy and warn on seed copy (cleared after 60 s). Social sign-up no longer saves a wallet before the seed is confirmed. A decrypt failure no longer becomes an empty record: the file is copied aside and a recovery screen shows. (AND-A-11 to 14) | `fix/key-storage-and-seed-safety` |
| Account deletion | In-app delete (store requirement), calling `DELETE api/v1/Users/me` with `{"confirm":"DELETE"}`. | `feat/delete-account` |

## Security model

- The wallet key never leaves the device. The server holds only an encrypted keystore backup it
  cannot read. Signing happens in the app.
- No vendor secret is in the build. The release dex was checked for the old credential.
- Money-moving screens that the server cannot protect are off: payout, card deposit, NFT mint
  (the server would sign it), KYC (payload mismatch).
- The server decides which features exist (`App/config`); the client hides what it cannot
  drive whatever the server says.
- Release builds log nothing sensitive, allow no cleartext traffic, and are not backed up.

## Needs action outside the code

- The old payment vendor credential and the old release signing passwords are in git history.
  Rotate both. Removing them from the code does not make them secret again.
- The backend must have the payments proxy enabled and the vendor credential set before this
  build ships; otherwise deposits show as unavailable.
- Register the `.debug` application id and the release id in the right Firebase projects.
  The backend must push to both projects (see the backend's deploy checklist).

## Deferred, with reasons

| Item | Reason |
|---|---|
| Password stored inside the encrypted blob, no auto-relock, biometric not bound to a key, CBC mode (AND-A-18, 19, 20, 45) | Needs a key-derivation redesign and a migration of existing wallets. Being done as separate work, not part of this round's merges. |
| 24-word seed import (AND-A-31 rest) | Import takes 12 words only; wider support is a feature. |
| Failed-login handling in the import and confirm view models (AND-A-25) | Social sign-up fixed; the others need the same pattern. |
| 401 token refresh, certificate pinning, seed in navigation arguments (AND-A-26, 40, 44) | Refresh needs backend work; pinning needs a decision on pins and rotation; the nav change is a refactor. |
| Staking writes, reward claim | The backend routes are off and the staking prepare call returns no transaction. |
| KYC upload | Backend route is off and the payload does not match (AND-A-16). |
| NFT mint | Server-signed on the backend; the client must sign it before it can be offered. |
| Add custom token | The backend routes do not exist. |
| Earn task timer | Enforced in the app only; the server trusts the reported seconds. |
