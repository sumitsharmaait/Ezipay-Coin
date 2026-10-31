# Test report

Generated 2026-10-31T18:16:42Z from `dev` @ `7b9507e90`.

## Summary

| Type | Suites | Tests | Passed | Failed | Skipped | Duration |
|---|---:|---:|---:|---:|---:|---:|
| Unit (JVM) | 39 | 301 | 301 | 0 | 0 | 77.3s |
| Instrumented (device) | 1 | 32 | 32 | 0 | 0 | 86.2s |

## Coverage (debug unit tests)

| Scope | Line | Branch | Method |
|---|---:|---:|---:|
| **Overall** | 13.4% (1688/12611) | 9.4% (838/8916) | 20.9% (611/2921) |
| `data.remote.api` | 40.7% (55/135) | 54.1% (59/109) | 45.2% (19/42) |
| `data.remote.dto` | 27.6% (24/87) | 25.0% (4/16) | 22.2% (14/63) |
| `data.repository` | 42.2% (65/154) | 30.6% (15/49) | 49.4% (40/81) |
| `presentation.dashboard.pay` | 33.2% (331/997) | 19.7% (116/589) | 41.6% (79/190) |
| `presentation.profile` | 32.7% (136/416) | 34.2% (65/190) | 50.7% (36/71) |
| `presentation.shared` | 85.6% (101/118) | 49.5% (51/103) | 78.9% (30/38) |
| `presentation.transactions` | 34.0% (69/203) | 13.3% (22/166) | 38.3% (18/47) |
| `utils.validation` | 97.9% (142/145) | 79.6% (148/186) | 93.1% (27/29) |

Classes with no executed code are included in the overall figure. The HTML report is at `app/build/reports/jacoco/jacocoTestReport/html/index.html` after a run.

## Environment

| | |
|---|---|
| Git | `dev` @ `7b9507e90` |
| JDK | openjdk version "17.0.18" 2026-01-20 |
| Gradle | 8.9 |
| Android Gradle Plugin | 8.6.0 |
| Kotlin | 2.1.0 |
| OS | Darwin 25.6.0 (arm64) |
| Device (instrumented) | EziPay_E2E (API 37, emulator) |

## Unit tests

### BannerPolicyTest

`com.app.ezipaycoin.config.BannerPolicyTest` - 9 tests, 9 passed, 0 failed, 0 skipped, 0.69s

| Test | Status | Time |
|---|---|---:|
| empty list shows nothing | passed | 0.279s |
| plain destinations resolve without a feature | passed | 0.119s |
| only https images are used | passed | 0.000s |
| zone-less dates are read as UTC | passed | 0.234s |
| Z and offset dates give the same result as zone-less UTC | passed | 0.003s |
| orders by priority then id and caps the count | passed | 0.001s |
| cta follows the destination feature | passed | 0.049s |
| unknown unsupported or label-less routes hide the button | passed | 0.002s |
| drops untitled and out of window banners | passed | 0.000s |

### FeatureResolverTest

`com.app.ezipaycoin.config.FeatureResolverTest` - 9 tests, 9 passed, 0 failed, 0 skipped, 0.63s

| Test | Status | Time |
|---|---|---:|
| absent route keeps defaults | passed | 0.006s |
| refresh keeps the banners from the server | passed | 0.610s |
| developer mode shows everything | passed | 0.000s |
| server can turn staking pools on and checkin off | passed | 0.001s |
| release defaults show only verified features | passed | 0.001s |
| features without a server key never come from the server | passed | 0.000s |
| a key the server does not list is off | passed | 0.001s |
| server cannot expose a feature the client cannot drive | passed | 0.001s |
| refresh applies the server map and failure leaves it alone | passed | 0.006s |

### SocialSignInTest

`com.app.ezipaycoin.config.SocialSignInTest` - 9 tests, 9 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| a fresh answer wins over the stored value | passed | 0.002s |
| server key is socialSignIn | passed | 0.000s |
| server off, missing or unknown keeps them hidden | passed | 0.000s |
| before a fetch the last known server value decides | passed | 0.000s |
| server on shows the buttons | passed | 0.000s |
| developer mode shows the buttons | passed | 0.001s |
| release default is off | passed | 0.000s |
| stored values do not turn on other features | passed | 0.000s |
| buttons are hidden when the feature is not in the enabled set | passed | 0.000s |

### ApiContractTest

`com.app.ezipaycoin.data.ApiContractTest` - 10 tests, 10 passed, 0 failed, 0 skipped, 1.97s

| Test | Status | Time |
|---|---|---:|
| everyAppRouteIsInTheBackendBaselineOrAKnownGap | passed | 0.155s |
| loginBodyMatchesTheBackendDto | passed | 0.011s |
| baselineResourceIsPresentAndNonTrivial | passed | 0.019s |
| transactionsBodyFieldsAreAcceptedByTheBackend | passed | 0.015s |
| kycSubmitFormFieldNamesMatchTheBackendForm | passed | 1.739s |
| knownGapsAreNotHidingRoutesThatTheBaselineHas | passed | 0.003s |
| apiServiceDeclaresRoutes | passed | 0.001s |
| mintBodiesMatchTheBackendDtos | passed | 0.002s |
| updateProfileBodyMatchesTheBackendDto | passed | 0.005s |
| coreRoutesTheMoneyAndSessionFlowsNeedAreInTheBaseline | passed | 0.015s |

### EarnRepoImplTest

`com.app.ezipaycoin.data.EarnRepoImplTest` - 11 tests, 11 passed, 0 failed, 0 skipped, 0.41s

| Test | Status | Time |
|---|---|---:|
| a 401 asks the user to unlock again | passed | 0.090s |
| a 404 from an older server becomes NOT_FOUND | passed | 0.057s |
| check-in status parses dates streak and next reward | passed | 0.060s |
| learn list parses and an empty list is success | passed | 0.037s |
| tasks parse with reward and status | passed | 0.034s |
| complete task posts date and watched seconds with a key | passed | 0.030s |
| a refused request keeps the server code | passed | 0.021s |
| staking pools parse | passed | 0.021s |
| check-in sends the date and an idempotency key | passed | 0.016s |
| app config parses features and banners | passed | 0.014s |
| summary sends status and chain id and parses the total | passed | 0.029s |

### KycSubmitFormTest

`com.app.ezipaycoin.data.KycSubmitFormTest` - 1 tests, 1 passed, 0 failed, 0 skipped, 2.20s

| Test | Status | Time |
|---|---|---:|
| submitSendsTheBackendFieldNames | passed | 2.202s |

### RepositoryApiTest

`com.app.ezipaycoin.data.RepositoryApiTest` - 17 tests, 17 passed, 0 failed, 0 skipped, 1.79s

| Test | Status | Time |
|---|---|---:|
| updateProfilePostsNameAndEmail | passed | 0.032s |
| nonceIsRequestedWithThePublicKeyQuery | passed | 0.045s |
| logoutIsAGetWithTheBearerToken | passed | 0.009s |
| featureDisabledIsAnEnvelopeWithNoData | passed | 0.037s |
| jsonRpcCallsGoToTheRootWithTheRpcBody | passed | 0.238s |
| estimateGasBodyCarriesTheCallObject | passed | 0.037s |
| anEmptyTokenSendsNoAuthorizationHeader | passed | 0.024s |
| noAuthorizationHeaderWhenThereIsNoSession | passed | 0.043s |
| unauthorizedResponseThrowsHttpException | passed | 0.042s |
| rpcErrorIsParsed | passed | 0.084s |
| applicationErrorEnvelopeIsReturnedNotThrown | passed | 0.229s |
| transactionsRequestBodyAndPaging | passed | 0.355s |
| dashboardParsesBalancesAndHandlesNulls | passed | 0.181s |
| usersMeIsParsedAndUnknownFieldsAreIgnored | passed | 0.113s |
| receiptStatusAndPendingReceiptAreParsed | passed | 0.204s |
| loginPostsTheSignedNonceAndParsesTheSession | passed | 0.072s |
| upstreamFailureWithTextDataIsReturnedAsARefusedEnvelope | passed | 0.033s |

### TransactionDisplayTest

`com.app.ezipaycoin.data.remote.TransactionDisplayTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.03s

| Test | Status | Time |
|---|---|---:|
| dashboard crypto with null logo and symbol parses | passed | 0.001s |
| transaction row with null fields does not throw | passed | 0.020s |
| zero or absent fiat value is hidden | passed | 0.007s |
| amount label carries sign and symbol | passed | 0.001s |
| amount label survives a missing symbol | passed | 0.000s |

### AuthInterceptorTest

`com.app.ezipaycoin.data.remote.api.AuthInterceptorTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.22s

| Test | Status | Time |
|---|---|---:|
| nullTokenSendsNoAuthorizationHeader | passed | 0.080s |
| emptyTokenSendsNoAuthorizationHeader | passed | 0.032s |
| realTokenIsSentAsBearer | passed | 0.014s |
| blankTokenSendsNoAuthorizationHeader | passed | 0.083s |

### EnvelopeParsingTest

`com.app.ezipaycoin.data.remote.api.EnvelopeParsingTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.06s

| Test | Status | Time |
|---|---|---:|
| parseErrorsGetAFriendlyMessage | passed | 0.035s |
| refusedEnvelopeWithArrayDataKeepsTheMessage | passed | 0.001s |
| successfulEnvelopeParsesExactlyAsBefore | passed | 0.018s |
| refusedEnvelopeWithTextDataKeepsTheMessage | passed | 0.006s |
| successfulEnvelopeWithBadDataStillFails | passed | 0.001s |

### HttpLoggingTest

`com.app.ezipaycoin.data.remote.api.HttpLoggingTest` - 3 tests, 3 passed, 0 failed, 0 skipped, 0.02s

| Test | Status | Time |
|---|---|---:|
| authorizationHeaderIsRedacted | passed | 0.012s |
| releaseLogsNothing | passed | 0.007s |
| debugLogsHeadersOnly | passed | 0.000s |

### PaymentsProxyTest

`com.app.ezipaycoin.data.remote.api.PaymentsProxyTest` - 10 tests, 10 passed, 0 failed, 0 skipped, 0.04s

| Test | Status | Time |
|---|---|---:|
| vendorSuccessBodyIsDecodedUnchanged | passed | 0.018s |
| featureDisabledEnvelopeBecomesFriendlyError | passed | 0.001s |
| vendor4xxKeepsVendorMessage | passed | 0.002s |
| emptyUnauthorizedBodyIsSessionExpired | passed | 0.000s |
| idempotencyKeysAreUniqueAndPrintableAscii | passed | 0.001s |
| walletMismatchHasOwnMessage | passed | 0.000s |
| garbageSuccessBodyIsUnavailableNotACrash | passed | 0.000s |
| rateLimitIsReported | passed | 0.001s |
| vendorUnavailableAndTimeoutShowTheSameMessage | passed | 0.000s |
| networkInfoListIsDecoded | passed | 0.022s |

### DecodePreferencesTest

`com.app.ezipaycoin.data.remote.dto.DecodePreferencesTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.18s

| Test | Status | Time |
|---|---|---:|
| invalidJsonIsCorruption | passed | 0.065s |
| garbageBytesAreCorruption | passed | 0.000s |
| validRecordRoundTrips | passed | 0.109s |
| emptyFileIsAFreshInstall | passed | 0.000s |
| decryptionFailureIsCorruptionNotAnEmptyRecord | passed | 0.002s |

### EarnViewModelTest

`com.app.ezipaycoin.presentation.EarnViewModelTest` - 14 tests, 14 passed, 0 failed, 0 skipped, 0.08s

| Test | Status | Time |
|---|---|---:|
| a network failure keeps the idempotency key for the retry | passed | 0.050s |
| a task with no time requirement completes at once | passed | 0.005s |
| a timed task cannot be completed before it was opened | passed | 0.002s |
| a failed summary is an error state and others still load | passed | 0.002s |
| a timed task needs the required time and sends the watched seconds | passed | 0.002s |
| an already completed task reloads the list | passed | 0.001s |
| check-in is not sent when the server says it is done for today | passed | 0.002s |
| describe tells server codes from transport messages | passed | 0.001s |
| check-in success shows the reward and reloads | passed | 0.007s |
| loads totals check-in and tasks on start | passed | 0.002s |
| an empty summary shows zero not fake numbers | passed | 0.001s |
| pools load once on demand | passed | 0.002s |
| a rule refusal drops the key and maps the code | passed | 0.001s |
| tasks failure is reported with a readable message | passed | 0.000s |

### LearnViewModelTest

`com.app.ezipaycoin.presentation.LearnViewModelTest` - 3 tests, 3 passed, 0 failed, 0 skipped, 0.20s

| Test | Status | Time |
|---|---|---:|
| failure can be retried | passed | 0.194s |
| shows active titled items in the server order | passed | 0.002s |
| an empty list is an empty state not an error | passed | 0.000s |

### AccountDeletionTest

`com.app.ezipaycoin.presentation.deleteaccount.AccountDeletionTest` - 6 tests, 6 passed, 0 failed, 0 skipped, 0.10s

| Test | Status | Time |
|---|---|---:|
| successWipesDeviceAfterServerConfirms | passed | 0.032s |
| wrongPasswordCallsNothing | passed | 0.009s |
| networkErrorKeepsTheWalletAndRetryReusesTheKey | passed | 0.015s |
| unauthorizedKeepsTheWallet | passed | 0.016s |
| lockoutStopsAttempts | passed | 0.011s |
| serverRefusalKeepsTheWallet | passed | 0.020s |

### PayViewModelTest

`com.app.ezipaycoin.presentation.pay.PayViewModelTest` - 39 tests, 39 passed, 0 failed, 0 skipped, 36.69s

| Test | Status | Time |
|---|---|---:|
| signerErrorIsReportedAndNothingIsBroadcast | passed | 29.541s |
| pendingPaymentIsReleasedOnceItsReceiptArrives | passed | 0.191s |
| unknownCoinIsNotSentToAGuessedContract | passed | 0.117s |
| typingAnAddressValidatesLive | passed | 0.159s |
| tokenEstimateTargetsTheContractWithErc20TransferCalldata | passed | 0.354s |
| feeOnTopOfAFullBnbBalanceIsRefused | passed | 0.135s |
| amountAboveBalanceIsRejected | passed | 0.115s |
| rejectedBroadcastShowsAFriendlyReason | passed | 0.156s |
| receiptLookupErrorsAreTreatedAsNotYetMined | passed | 0.165s |
| confirmWithoutARequestDoesNothing | passed | 0.079s |
| networkFailureWhileQuotingIsMappedToAConnectionMessage | passed | 0.077s |
| historyFailureIsShownNotThrown | passed | 0.106s |
| loadsTransactionHistoryOnInit | passed | 0.129s |
| canRetryAfterAFailedQuote | passed | 0.104s |
| minedSuccessClearsTheFormAndReloadsHistory | passed | 0.222s |
| sendingToOwnAddressIsRejectedEvenInDifferentCase | passed | 0.410s |
| revertedEstimateShowsAFriendlyMessage | passed | 0.161s |
| tokenSendWithoutBnbForGasIsRefused | passed | 0.103s |
| validBnbRequestStopsAtConfirmationWithTheFee | passed | 0.140s |
| revertedTransactionShowsFailureAndKeepsTheForm | passed | 0.129s |
| nonceIsReadAsPendingForTheSender | passed | 0.382s |
| emptyAddressIsRejectedBeforeAnyNetworkCall | passed | 0.127s |
| invalidAmountsAreRejected | passed | 0.352s |
| badEip55ChecksumIsRejected | passed | 0.137s |
| qrScanFillsTheAddressAndStopsScanning | passed | 0.199s |
| malformedAddressIsRejected | passed | 0.315s |
| withoutAWalletNothingHappens | passed | 0.565s |
| editingTheAmountClearsItsError | passed | 0.206s |
| confirmSignsWithTheQuotedNonceAndFeeAndBroadcastsTheSignedBytes | passed | 0.152s |
| cancelDropsTheConfirmationAndConfirmThenDoesNothing | passed | 0.140s |
| unminedPaymentIsReportedPendingAndBlocksASecondSend | passed | 0.146s |
| missingNonceStopsTheFlow | passed | 0.122s |
| confirmFetchesAFreshNonceAndFee | passed | 0.081s |
| dashboardContractIsUsedForTheTokenWhenListed | passed | 0.132s |
| dismissDialogResetsTheResult | passed | 0.234s |
| doubleTapOnConfirmBroadcastsOnce | passed | 0.251s |
| secondPayWhileTheFirstIsPreparingIsIgnored | passed | 0.189s |
| nativeEstimateCarriesValueAndNoCalldata | passed | 0.117s |
| tokenConfirmSignsAContractCallToTheTokenWithTransferCalldata | passed | 0.188s |

### RecentPayeesTest

`com.app.ezipaycoin.presentation.pay.RecentPayeesTest` - 3 tests, 3 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| own address is excluded even on a sent row | passed | 0.012s |
| payees are de-duplicated case-insensitively and keep order | passed | 0.001s |
| received transfers never produce payees | passed | 0.000s |

### MyProfileViewModelTest

`com.app.ezipaycoin.presentation.profile.MyProfileViewModelTest` - 11 tests, 11 passed, 0 failed, 0 skipped, 15.35s

| Test | Status | Time |
|---|---|---:|
| renameCallsTheApiWithNameAndEmailAndUpdatesTheCache | passed | 0.423s |
| logoutSucceeds | passed | 0.311s |
| renameFailureIsShownAndCacheIsUntouched | passed | 0.307s |
| refusedResponseLeavesTheCacheAlone | passed | 2.807s |
| showsCachedValuesWhenTheServerIsOffline | passed | 0.470s |
| blankServerFieldsKeepTheCachedOnes | passed | 0.603s |
| serverProfileReplacesAndIsCached | passed | 0.656s |
| withoutAProfileRepositoryOnlyTheCacheIsUsed | passed | 0.907s |
| emptyCacheShowsPlaceholdersUntilTheServerAnswers | passed | 1.183s |
| uploadedPictureUrlIsCached | passed | 6.733s |
| logoutFailureIsShown | passed | 0.916s |

### ProfileMergeTest

`com.app.ezipaycoin.presentation.profile.ProfileMergeTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| blankOrMissingServerValuesKeepCache | passed | 0.002s |
| noResponseKeepsCache | passed | 0.001s |
| emptyCacheTakesServer | passed | 0.000s |
| serverValuesOverrideCache | passed | 0.000s |

### WalletSharedViewModelTest

`com.app.ezipaycoin.presentation.shared.WalletSharedViewModelTest` - 24 tests, 24 passed, 0 failed, 0 skipped, 9.11s

| Test | Status | Time |
|---|---|---:|
| selectionFallsBackToTheFirstCoinWhenItIsNoLongerListed | passed | 1.183s |
| serverRefusalOnFirstLoadShowsItsMessage | passed | 0.751s |
| unlockLoadsTheDashboardAndSelectsTheFirstCoin | passed | 0.178s |
| pullRefreshAlwaysFetchesAndTogglesTheIndicator | passed | 0.827s |
| pullRefreshIsIgnoredBeforeUnlock | passed | 1.223s |
| socialLinksMapToTheirPages | passed | 0.747s |
| firstLoadFailureShowsAnError | passed | 0.416s |
| resumeRefreshIsAllowedAgainOnceTheWindowHasPassed | passed | 0.413s |
| pushRefreshRestartsTheResumeWindow | passed | 0.311s |
| pushBeforeUnlockIsIgnored | passed | 0.434s |
| openUrlEventIsRelayedToCollectors | passed | 0.134s |
| pullRefreshBypassesTheThrottle | passed | 0.262s |
| selectedCoinSurvivesARefreshEvenWithDifferentCase | passed | 0.306s |
| silentRefreshDoesNotShowTheFullScreenLoader | passed | 0.096s |
| failedPullRefreshKeepsLastGoodData | passed | 0.088s |
| fetchBalanceEventReloadsWithLoader | passed | 0.082s |
| serverRefusalDuringRefreshKeepsLastGoodData | passed | 0.096s |
| resumeRefreshIsIgnoredBeforeUnlock | passed | 0.066s |
| nothingIsFetchedBeforeUnlock | passed | 0.061s |
| pushRefreshIsAllowedAgainAfterTwoSeconds | passed | 0.163s |
| resumeRefreshIsThrottledAndSilent | passed | 0.140s |
| transactionSelectionIsStored | passed | 0.784s |
| pushWhileUnlockedRefreshesOnce | passed | 0.168s |
| unlockShowsTheLoaderWhileFirstLoading | passed | 0.151s |

### AllTransactionsViewModelTest

`com.app.ezipaycoin.presentation.transactions.AllTransactionsViewModelTest` - 6 tests, 6 passed, 0 failed, 0 skipped, 0.07s

| Test | Status | Time |
|---|---|---:|
| exceptionResetsLoadingAndKeepsEarlierRows | passed | 0.029s |
| serverRefusalResetsLoadingAndRetryAsksForTheSamePage | passed | 0.002s |
| filterChangeRestartsAtPageOne | passed | 0.007s |
| aShortFirstPageEndsPaging | passed | 0.001s |
| counterpartyIsTheOtherSide | passed | 0.032s |
| pagesAdvanceFromOneAndStopWhenTheTotalIsReached | passed | 0.001s |

### EnvelopeCipherTest

`com.app.ezipaycoin.utils.EnvelopeCipherTest` - 6 tests, 6 passed, 0 failed, 0 skipped, 0.04s

| Test | Status | Time |
|---|---|---:|
| newWritesAreGcmAndRoundTrip | passed | 0.023s |
| existingCbcBlobsStillDecrypt | passed | 0.001s |
| fallsBackToLegacyWhenGcmKeyIsUnusable | passed | 0.002s |
| legacyBlobWhoseIvStartsWithTheMagicStillDecrypts | passed | 0.002s |
| eachWriteUsesAFreshIv | passed | 0.003s |
| tamperedGcmRecordIsRejected | passed | 0.005s |

### PasswordGateTest

`com.app.ezipaycoin.utils.PasswordGateTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| correctPasswordIsAccepted | passed | 0.002s |
| successResetsFailureCount | passed | 0.000s |
| missingStoredPasswordNeverMatches | passed | 0.000s |
| matcherOverloadUsesTheCallerDecision | passed | 0.002s |
| lockoutEndsAndCountersReset | passed | 0.000s |
| wrongPasswordCountsDown | passed | 0.000s |
| lockoutAfterMaxAttemptsEvenForCorrectPassword | passed | 0.000s |

### PasswordMigrationTest

`com.app.ezipaycoin.utils.PasswordMigrationTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.94s

| Test | Status | Time |
|---|---|---:|
| plaintextIsNeverClearedWhenVerifierCannotBeConfirmed | passed | 0.068s |
| migratesPlaintextToVerifierAndKeepsEverythingElse | passed | 0.095s |
| staleVerifierForADifferentPasswordIsReplaced | passed | 0.327s |
| crashBeforeVerifierWriteLeavesPlaintextUsable | passed | 0.090s |
| freshInstallWithoutPasswordIsUntouched | passed | 0.009s |
| crashBetweenVerifierAndPlaintextDeletionKeepsBothAndRecovers | passed | 0.269s |
| secondRunDoesNothing | passed | 0.082s |

### PasswordVerifierTest

`com.app.ezipaycoin.utils.PasswordVerifierTest` - 9 tests, 9 passed, 0 failed, 0 skipped, 5.97s

| Test | Status | Time |
|---|---|---:|
| withPasswordDropsPlaintext | passed | 4.126s |
| correctPasswordVerifies | passed | 0.062s |
| credentialsPreferVerifierOverLegacyPlaintext | passed | 0.013s |
| defaultsMeetTheMinimumAndRecordTheIterationCount | passed | 1.683s |
| wrongOrEmptyPasswordIsRejected | passed | 0.021s |
| credentialsAcceptLegacyPlaintextBeforeMigration | passed | 0.001s |
| malformedRecordsNeverThrow | passed | 0.003s |
| recordDoesNotContainThePassword | passed | 0.001s |
| saltIsRandomPerRecord | passed | 0.001s |

### RelockPolicyTest

`com.app.ezipaycoin.utils.RelockPolicyTest` - 6 tests, 6 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| foregroundWithoutBackgroundNeverLocks | passed | 0.004s |
| timerResetsAfterReturn | passed | 0.002s |
| defaultTimeoutIsTwoMinutes | passed | 0.001s |
| staysUnlockedOnQuickReturn | passed | 0.000s |
| repeatedStopsDoNotRestartTheClock | passed | 0.000s |
| locksAtTheTimeout | passed | 0.000s |

### SeedHolderTest

`com.app.ezipaycoin.utils.SeedHolderTest` - 3 tests, 3 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| keepsACopy | passed | 0.004s |
| holdsAndClears | passed | 0.001s |
| seedPhraseRouteCarriesNoArguments | passed | 0.003s |

### SeedPhraseRulesTest

`com.app.ezipaycoin.utils.SeedPhraseRulesTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.04s

| Test | Status | Time |
|---|---|---:|
| messagesAreSpecificPerCase | passed | 0.018s |
| validatorSeesTheNormalisedPhrase | passed | 0.009s |
| otherLengthsAreRejected | passed | 0.008s |
| everyBip39LengthIsAccepted | passed | 0.001s |
| checksumFailureFromWalletCoreIsInvalid | passed | 0.001s |
| pastedWhitespaceAndCaseAreNormalised | passed | 0.000s |
| emptyAndBlankInput | passed | 0.001s |

### SessionRecoveryTest

`com.app.ezipaycoin.utils.SessionRecoveryTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.32s

| Test | Status | Time |
|---|---|---:|
| exceptionDuringSignInIsAGiveUp | passed | 0.145s |
| aTokenRefreshedMeanwhileIsReusedWithoutAnotherSignIn | passed | 0.004s |
| expiredSignalFiresOnceUntilANewTokenIsSet | passed | 0.011s |
| concurrentRejectionsShareOneSignIn | passed | 0.023s |
| onlyAUN001BodyOn401TriggersRecoveryAndOnlyOnce | passed | 0.019s |
| failedSignInSendsTheAppToUnlockAndKeepsGoingNowhere | passed | 0.096s |
| rejectedSessionSignsInAgainAndRetries | passed | 0.023s |

### SocialProfileTest

`com.app.ezipaycoin.utils.SocialProfileTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| missingPhotoIsEmptyNotTheWordNull | passed | 0.004s |
| missingNameFallsBackToTheEmailPrefix | passed | 0.000s |
| fullProfileIsReady | passed | 0.000s |
| missingEmailIsReportedWithAMessage | passed | 0.001s |

### WalletSignInTest

`com.app.ezipaycoin.utils.WalletSignInTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.04s

| Test | Status | Time |
|---|---|---:|
| networkErrorsBecomeFailures | passed | 0.014s |
| failedLoginIsReported | passed | 0.006s |
| blankMessageGetsADefault | passed | 0.005s |
| failedNonceIsReportedAndLoginIsNotAttempted | passed | 0.004s |
| successSignsTheServerNonce | passed | 0.007s |

### AddressValidatorTest

`com.app.ezipaycoin.utils.validation.AddressValidatorTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| lowerAndUpperCaseHaveNoChecksum | passed | 0.002s |
| ownAddressRejectedCaseInsensitive | passed | 0.001s |
| whileTypingOnlyFlagsImpossibleInput | passed | 0.000s |
| malformedRejected | passed | 0.001s |
| validChecksummedAddressesPass | passed | 0.001s |
| badChecksumRejected | passed | 0.000s |
| keccakKnownVectors | passed | 0.002s |

### AmountValidatorTest

`com.app.ezipaycoin.utils.validation.AmountValidatorTest` - 9 tests, 9 passed, 0 failed, 0 skipped, 0.02s

| Test | Status | Time |
|---|---|---:|
| rejectsMalformedZeroAndNegative | passed | 0.002s |
| tokenAmountCannotExceedBalance | passed | 0.000s |
| bnbAmountPlusFeeMustFit | passed | 0.000s |
| parsesPlainDecimals | passed | 0.000s |
| decimalLimitIsEighteen | passed | 0.005s |
| feeSkippedWhenUnknown | passed | 0.006s |
| tokenNeedsBnbForFee | passed | 0.000s |
| weiConversionRoundTrips | passed | 0.004s |
| blankAndInvalidMessages | passed | 0.000s |

### CoinKindTest

`com.app.ezipaycoin.utils.validation.CoinKindTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| knownTokensFallBackToConfiguredContracts | passed | 0.001s |
| malformedListedContractIsNotTrusted | passed | 0.000s |
| listedContractWins | passed | 0.000s |
| unknownCoinWithoutContractIsUnsupported | passed | 0.000s |
| bnbIsNativeEvenIfAContractIsListed | passed | 0.001s |

### CoinResolverTest

`com.app.ezipaycoin.utils.validation.CoinResolverTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| knownSymbolsFallBackToConfiguredContracts | passed | 0.000s |
| bnbIsNative | passed | 0.000s |
| listedContractWins | passed | 0.000s |
| unknownCoinWithoutContractIsRefused | passed | 0.000s |

### ReceiptPollingTest

`com.app.ezipaycoin.utils.validation.ReceiptPollingTest` - 1 tests, 1 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| scheduleSumsToTotalAndBacksOff | passed | 0.001s |

### RefreshThrottleTest

`com.app.ezipaycoin.utils.validation.RefreshThrottleTest` - 2 tests, 2 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| forceBypassesAndRestartsWindow | passed | 0.000s |
| allowsFirstThenBlocksWithinWindow | passed | 0.001s |

### TxErrorMapperTest

`com.app.ezipaycoin.utils.validation.TxErrorMapperTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| gasAndNonceErrors | passed | 0.001s |
| networkErrors | passed | 0.003s |
| erc20InsufficientBalanceRevert | passed | 0.000s |
| unknownTextIsNeverShownRaw | passed | 0.001s |

## Instrumented tests

### DialogueAccessibilityTest

`com.app.ezipaycoin.ui.DialogueAccessibilityTest` - 32 tests, 32 passed, 0 failed, 0 skipped, 86.18s

| Test | Status | Time |
|---|---|---:|
| errorDialogAnnouncesAnErrorAndDismisses | passed | 4.921s |
| successDialogAnnouncesSuccess | passed | 1.862s |
| switchingCoinShowsItsBalance | passed | 3.247s |
| currencyPickerIconIsLabelled | passed | 2.504s |
| pullToRefreshFetchesTheDashboardAgain | passed | 1.333s |
| failedLoadShowsTheReasonAndRetryRecovers | passed | 1.797s |
| showsTheSelectedCoinBalanceAndFiatValue | passed | 3.924s |
| profileImageAndEditControlsAreLabelled | passed | 2.579s |
| showsTheNameFromTheServer | passed | 2.551s |
| showsTheCachedNameWhenOffline | passed | 2.760s |
| withoutAWalletNoQrCodeIsShown | passed | 2.000s |
| copyButtonIsLabelledAndClickable | passed | 2.040s |
| showsTheWalletAddressAndItsQrCode | passed | 1.299s |
| blankAddressIsReportedOnPay | passed | 3.671s |
| amountAboveBalanceIsReported | passed | 3.327s |
| cancelClosesTheConfirmationWithoutSending | passed | 4.810s |
| tokenPickerAndSlideControlAreLabelled | passed | 2.725s |
| garbageAddressIsFlaggedWhileTyping | passed | 2.500s |
| validRequestAsksForConfirmationShowingRecipientAmountAndFee | passed | 4.608s |
| networkRejectionShowsAnErrorDialogWithAnAccessibleIcon | passed | 3.736s |
| ownAddressIsRejected | passed | 2.813s |
| tappingARowReportsTheTransaction | passed | 1.343s |
| listsEachTransactionWithSignedAmount | passed | 1.225s |
| choosingAFilterRequestsThatType | passed | 1.201s |
| showsTheThreeFilters | passed | 1.358s |
| passwordFieldCanBeRevealedAndHasAnAccessibleToggle | passed | 1.438s |
| showsTitlePasswordFieldAndUnlockButton | passed | 0.909s |
| correctPasswordUnlocksWhenNoWalletIsSetUpYet | passed | 2.214s |
| wrongPasswordShowsAnErrorAndStaysLocked | passed | 2.300s |
| tokenTransferIsSignedAsAnErc20CallOnTheConfiguredChain | passed | 3.047s |
| nativeTransferCarriesTheRecipientAndAmount | passed | 2.977s |
| signatureIsDeterministicForTheSameRequest | passed | 5.000s |

