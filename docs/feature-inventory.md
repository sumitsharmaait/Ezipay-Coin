# Feature inventory

Status of every screen and tile in the release build. "Hidden" means shown only in debug builds
with "Developer: show unfinished features". Flags are in `config/Feature.kt` and can be
overridden by the server's `App/config.features`.

| Screen / tile | Final status | Reason / note |
|---|---|---|
| Earn header (Total / Claimable / Locked) | fixed | `GET Earn/summary` x2 (CLAIMABLE, LOCKED); loading/error/retry; zero shown as 0.0000 |
| Earn claim / lock buttons | hidden (removed) | claim route switched off and the client has no claim flow; Lock did nothing |
| Earn > Check-in | fixed | `GET Earn/checkin/status` + `GET Earn/checkin` with Idempotency-Key; calendar marks real dates; UTC day |
| Earn > Tasks (new) | fixed | `GET Earn/checkin/tasks`, `POST .../complete` with watchedSeconds; timer enforced client side (server trusts the claim, backend point) |
| Earn > Staking | hidden in release | `GET earn/Staking/pools` read-only list, shown when `features.staking` is true (or dev mode). Stake/claim/unstake not in client (writes off, prepare returns no tx) |
| Earn > Airdrops, Leaderboard, Spin & Win | hidden in release | invented data / client RNG, no backend; dev mode only |
| Home "Stake USDT & Earn 12% APY" card | removed | replaced by App/config banners |
| Home tiles Pay Bills, Gift Cards | removed | no feature behind them |
| Home tile Staking | hidden in release | shown when staking flag on |
| Home tiles Add Funds, Daily Rewards, More | works | navigation / external link |
| Learn | fixed | lessons from `GET learn` (active, sorted, https links only), empty/error states; dead hero card, "How to stake" card and third-party video card removed; topic grid, Terms, Privacy, social links kept (static official links) |
| Wallet > NFT holdings/detail | works | unchanged read routes |
| Wallet > Mint an NFT | hidden in release | backend mint off and server-signed; client-unsupported so a server flag cannot reveal it |
| Profile > VIP Privilege, Verifications, Twitter | hidden in release | placeholders (TODO) |
| Profile > KYC Upload | hidden in release | route off, payload mismatch (AND-A-16) |
| Profile > Security, Logout | unchanged | not mine |
| Add Custom Token | hidden (no entry) | backend routes absent; route registered but nothing navigates to it |
| KYC status, WebView screens | unreachable | unchanged |
| Pay > Utilities tab | hidden | Hidden behind `Feature.PAY_UTILITIES` (default off); no feature behind it |
| Create wallet, import (12 words), Google/Facebook sign-up | works | Social sign-up now confirms the seed before saving |
| Unlock, biometric, password | works, weak | Key-derivation redesign deferred |
| Home balance card, coin selector, market snapshot | works | Dashboard; EZP value uses the server's static price |
| Home banners | works | From `App/config` |
| Wallet tab token list, NFT collection and detail | works | |
| Wallet > Deposit | works when the server enables the proxy | Via `Payments/*`; shows unavailable otherwise |
| Wallet > Withdraw (on-chain) | hidden | Vendor payout is unsafe |
| Wallet > Buy with card | removed | Card module deleted |
| Pay > Direct Pay (BNB, EZPT, USDT) | works | Validated, confirmed, tracked |
| Pay > transactions list and detail | works | |
| Receive | works | Local address and QR |
| Profile name, photo | works | |
| Profile > Security | works | Password gate, FLAG_SECURE |
| Profile > Delete account | works | `DELETE Users/me` |
| Notification bell | works | Opens transactions; push display works |

## Tests

JVM unit tests cover feature resolution, Earn and Learn (against recorded JSON), send validation,
payments proxy, logging, key storage and account deletion. Run `./gradlew testDebugUnitTest`.
