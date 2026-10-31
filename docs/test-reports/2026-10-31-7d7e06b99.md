# Test report

Generated 2026-10-31T12:57:02Z from `test/unit-integration-ui` @ `7d7e06b99`.

## Summary

| Type | Suites | Tests | Passed | Failed | Skipped | Duration |
|---|---:|---:|---:|---:|---:|---:|
| Unit (JVM) | 36 | 282 | 282 | 0 | 0 | 7.9s |
| Instrumented (device) | 1 | 32 | 32 | 0 | 0 | 73.0s |

## Coverage (debug unit tests)

| Scope | Line | Branch | Method |
|---|---:|---:|---:|
| **Overall** | 13.2% (1654/12553) | 8.8% (795/9057) | 20.6% (598/2904) |
| `data.remote.api` | 40.7% (55/135) | 54.1% (59/109) | 45.2% (19/42) |
| `data.remote.dto` | 27.6% (24/87) | 25.0% (4/16) | 22.2% (14/63) |
| `data.repository` | 42.2% (65/154) | 30.6% (15/49) | 49.4% (40/81) |
| `presentation.dashboard.pay` | 32.3% (318/985) | 16.8% (103/613) | 40.2% (74/184) |
| `presentation.profile` | 32.7% (136/416) | 31.0% (65/210) | 50.7% (36/71) |
| `presentation.shared` | 85.6% (101/118) | 49.5% (51/103) | 78.9% (30/38) |
| `presentation.transactions` | 34.3% (69/201) | 13.0% (21/162) | 38.3% (18/47) |
| `utils.validation` | 97.9% (142/145) | 79.6% (148/186) | 93.1% (27/29) |

Classes with no executed code are included in the overall figure. The HTML report is at `app/build/reports/jacoco/jacocoTestReport/html/index.html` after a run.

## Environment

| | |
|---|---|
| Git | `test/unit-integration-ui` @ `7d7e06b99` |
| JDK | openjdk version "17.0.18" 2026-01-20 |
| Gradle | 8.9 |
| Android Gradle Plugin | 8.6.0 |
| Kotlin | 2.1.0 |
| OS | Darwin 25.6.0 (arm64) |
| Device (instrumented) | EziPay_E2E AVD, API 37, arm64 (emulator-5558) |

## Unit tests

### BannerPolicyTest

`com.app.ezipaycoin.config.BannerPolicyTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.19s

| Test | Status | Time |
|---|---|---:|
| empty list shows nothing | passed | 0.036s |
| plain destinations resolve without a feature | passed | 0.035s |
| only https images are used | passed | 0.000s |
| orders by priority then id and caps the count | passed | 0.098s |
| cta follows the destination feature | passed | 0.014s |
| unknown unsupported or label-less routes hide the button | passed | 0.001s |
| drops untitled and out of window banners | passed | 0.005s |

### FeatureResolverTest

`com.app.ezipaycoin.config.FeatureResolverTest` - 9 tests, 9 passed, 0 failed, 0 skipped, 0.21s

| Test | Status | Time |
|---|---|---:|
| absent route keeps defaults | passed | 0.001s |
| refresh keeps the banners from the server | passed | 0.201s |
| developer mode shows everything | passed | 0.000s |
| server can turn staking pools on and checkin off | passed | 0.000s |
| release defaults show only verified features | passed | 0.000s |
| features without a server key never come from the server | passed | 0.000s |
| a key the server does not list is off | passed | 0.000s |
| server cannot expose a feature the client cannot drive | passed | 0.001s |
| refresh applies the server map and failure leaves it alone | passed | 0.003s |

### ApiContractTest

`com.app.ezipaycoin.data.ApiContractTest` - 10 tests, 10 passed, 0 failed, 0 skipped, 0.44s

| Test | Status | Time |
|---|---|---:|
| everyAppRouteIsInTheBackendBaselineOrAKnownGap | passed | 0.029s |
| loginBodyMatchesTheBackendDto | passed | 0.003s |
| baselineResourceIsPresentAndNonTrivial | passed | 0.002s |
| transactionsBodyFieldsAreAcceptedByTheBackend | passed | 0.001s |
| kycSubmitFormFieldNamesMatchTheBackendForm | passed | 0.401s |
| knownGapsAreNotHidingRoutesThatTheBaselineHas | passed | 0.001s |
| apiServiceDeclaresRoutes | passed | 0.001s |
| mintBodiesMatchTheBackendDtos | passed | 0.002s |
| updateProfileBodyMatchesTheBackendDto | passed | 0.000s |
| coreRoutesTheMoneyAndSessionFlowsNeedAreInTheBaseline | passed | 0.002s |

### EarnRepoImplTest

`com.app.ezipaycoin.data.EarnRepoImplTest` - 11 tests, 11 passed, 0 failed, 0 skipped, 0.11s

| Test | Status | Time |
|---|---|---:|
| a 401 asks the user to unlock again | passed | 0.021s |
| a 404 from an older server becomes NOT_FOUND | passed | 0.008s |
| check-in status parses dates streak and next reward | passed | 0.012s |
| learn list parses and an empty list is success | passed | 0.010s |
| tasks parse with reward and status | passed | 0.008s |
| complete task posts date and watched seconds with a key | passed | 0.013s |
| a refused request keeps the server code | passed | 0.008s |
| staking pools parse | passed | 0.008s |
| check-in sends the date and an idempotency key | passed | 0.006s |
| app config parses features and banners | passed | 0.007s |
| summary sends status and chain id and parses the total | passed | 0.006s |

### KycSubmitFormTest

`com.app.ezipaycoin.data.KycSubmitFormTest` - 1 tests, 1 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| submitSendsTheBackendFieldNames | passed | 0.013s |

### RepositoryApiTest

`com.app.ezipaycoin.data.RepositoryApiTest` - 17 tests, 17 passed, 0 failed, 0 skipped, 0.12s

| Test | Status | Time |
|---|---|---:|
| updateProfilePostsNameAndEmail | passed | 0.007s |
| nonceIsRequestedWithThePublicKeyQuery | passed | 0.006s |
| logoutIsAGetWithTheBearerToken | passed | 0.004s |
| featureDisabledIsAnEnvelopeWithNoData | passed | 0.008s |
| jsonRpcCallsGoToTheRootWithTheRpcBody | passed | 0.010s |
| estimateGasBodyCarriesTheCallObject | passed | 0.007s |
| anEmptyTokenSendsNoAuthorizationHeader | passed | 0.004s |
| noAuthorizationHeaderWhenThereIsNoSession | passed | 0.004s |
| unauthorizedResponseThrowsHttpException | passed | 0.004s |
| rpcErrorIsParsed | passed | 0.006s |
| applicationErrorEnvelopeIsReturnedNotThrown | passed | 0.004s |
| transactionsRequestBodyAndPaging | passed | 0.018s |
| dashboardParsesBalancesAndHandlesNulls | passed | 0.009s |
| usersMeIsParsedAndUnknownFieldsAreIgnored | passed | 0.006s |
| receiptStatusAndPendingReceiptAreParsed | passed | 0.009s |
| loginPostsTheSignedNonceAndParsesTheSession | passed | 0.008s |
| upstreamFailureWithTextDataIsReturnedAsARefusedEnvelope | passed | 0.005s |

### AuthInterceptorTest

`com.app.ezipaycoin.data.remote.api.AuthInterceptorTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| nullTokenSendsNoAuthorizationHeader | passed | 0.003s |
| emptyTokenSendsNoAuthorizationHeader | passed | 0.002s |
| realTokenIsSentAsBearer | passed | 0.003s |
| blankTokenSendsNoAuthorizationHeader | passed | 0.002s |

### EnvelopeParsingTest

`com.app.ezipaycoin.data.remote.api.EnvelopeParsingTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| parseErrorsGetAFriendlyMessage | passed | 0.002s |
| refusedEnvelopeWithArrayDataKeepsTheMessage | passed | 0.001s |
| successfulEnvelopeParsesExactlyAsBefore | passed | 0.000s |
| refusedEnvelopeWithTextDataKeepsTheMessage | passed | 0.001s |
| successfulEnvelopeWithBadDataStillFails | passed | 0.001s |

### HttpLoggingTest

`com.app.ezipaycoin.data.remote.api.HttpLoggingTest` - 3 tests, 3 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| authorizationHeaderIsRedacted | passed | 0.000s |
| releaseLogsNothing | passed | 0.001s |
| debugLogsHeadersOnly | passed | 0.000s |

### PaymentsProxyTest

`com.app.ezipaycoin.data.remote.api.PaymentsProxyTest` - 10 tests, 10 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| vendorSuccessBodyIsDecodedUnchanged | passed | 0.003s |
| featureDisabledEnvelopeBecomesFriendlyError | passed | 0.000s |
| vendor4xxKeepsVendorMessage | passed | 0.001s |
| emptyUnauthorizedBodyIsSessionExpired | passed | 0.000s |
| idempotencyKeysAreUniqueAndPrintableAscii | passed | 0.000s |
| walletMismatchHasOwnMessage | passed | 0.001s |
| garbageSuccessBodyIsUnavailableNotACrash | passed | 0.000s |
| rateLimitIsReported | passed | 0.000s |
| vendorUnavailableAndTimeoutShowTheSameMessage | passed | 0.000s |
| networkInfoListIsDecoded | passed | 0.003s |

### DecodePreferencesTest

`com.app.ezipaycoin.data.remote.dto.DecodePreferencesTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.04s

| Test | Status | Time |
|---|---|---:|
| invalidJsonIsCorruption | passed | 0.023s |
| garbageBytesAreCorruption | passed | 0.000s |
| validRecordRoundTrips | passed | 0.014s |
| emptyFileIsAFreshInstall | passed | 0.000s |
| decryptionFailureIsCorruptionNotAnEmptyRecord | passed | 0.001s |

### EarnViewModelTest

`com.app.ezipaycoin.presentation.EarnViewModelTest` - 14 tests, 14 passed, 0 failed, 0 skipped, 0.02s

| Test | Status | Time |
|---|---|---:|
| a network failure keeps the idempotency key for the retry | passed | 0.012s |
| a task with no time requirement completes at once | passed | 0.003s |
| a timed task cannot be completed before it was opened | passed | 0.000s |
| a failed summary is an error state and others still load | passed | 0.001s |
| a timed task needs the required time and sends the watched seconds | passed | 0.001s |
| an already completed task reloads the list | passed | 0.001s |
| check-in is not sent when the server says it is done for today | passed | 0.001s |
| describe tells server codes from transport messages | passed | 0.000s |
| check-in success shows the reward and reloads | passed | 0.001s |
| loads totals check-in and tasks on start | passed | 0.001s |
| an empty summary shows zero not fake numbers | passed | 0.000s |
| pools load once on demand | passed | 0.001s |
| a rule refusal drops the key and maps the code | passed | 0.002s |
| tasks failure is reported with a readable message | passed | 0.000s |

### LearnViewModelTest

`com.app.ezipaycoin.presentation.LearnViewModelTest` - 3 tests, 3 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| failure can be retried | passed | 0.003s |
| shows active titled items in the server order | passed | 0.001s |
| an empty list is an empty state not an error | passed | 0.000s |

### AccountDeletionTest

`com.app.ezipaycoin.presentation.deleteaccount.AccountDeletionTest` - 6 tests, 6 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| successWipesDeviceAfterServerConfirms | passed | 0.005s |
| wrongPasswordCallsNothing | passed | 0.001s |
| networkErrorKeepsTheWalletAndRetryReusesTheKey | passed | 0.003s |
| unauthorizedKeepsTheWallet | passed | 0.002s |
| lockoutStopsAttempts | passed | 0.001s |
| serverRefusalKeepsTheWallet | passed | 0.002s |

### PayViewModelTest

`com.app.ezipaycoin.presentation.pay.PayViewModelTest` - 39 tests, 39 passed, 0 failed, 0 skipped, 3.47s

| Test | Status | Time |
|---|---|---:|
| signerErrorIsReportedAndNothingIsBroadcast | passed | 1.885s |
| pendingPaymentIsReleasedOnceItsReceiptArrives | passed | 0.091s |
| unknownCoinIsNotSentToAGuessedContract | passed | 0.058s |
| typingAnAddressValidatesLive | passed | 0.042s |
| tokenEstimateTargetsTheContractWithErc20TransferCalldata | passed | 0.042s |
| feeOnTopOfAFullBnbBalanceIsRefused | passed | 0.039s |
| amountAboveBalanceIsRejected | passed | 0.041s |
| rejectedBroadcastShowsAFriendlyReason | passed | 0.044s |
| receiptLookupErrorsAreTreatedAsNotYetMined | passed | 0.041s |
| confirmWithoutARequestDoesNothing | passed | 0.039s |
| networkFailureWhileQuotingIsMappedToAConnectionMessage | passed | 0.038s |
| historyFailureIsShownNotThrown | passed | 0.042s |
| loadsTransactionHistoryOnInit | passed | 0.042s |
| canRetryAfterAFailedQuote | passed | 0.045s |
| minedSuccessClearsTheFormAndReloadsHistory | passed | 0.049s |
| sendingToOwnAddressIsRejectedEvenInDifferentCase | passed | 0.039s |
| revertedEstimateShowsAFriendlyMessage | passed | 0.044s |
| tokenSendWithoutBnbForGasIsRefused | passed | 0.047s |
| validBnbRequestStopsAtConfirmationWithTheFee | passed | 0.041s |
| revertedTransactionShowsFailureAndKeepsTheForm | passed | 0.042s |
| nonceIsReadAsPendingForTheSender | passed | 0.037s |
| emptyAddressIsRejectedBeforeAnyNetworkCall | passed | 0.041s |
| invalidAmountsAreRejected | passed | 0.043s |
| badEip55ChecksumIsRejected | passed | 0.041s |
| qrScanFillsTheAddressAndStopsScanning | passed | 0.041s |
| malformedAddressIsRejected | passed | 0.035s |
| withoutAWalletNothingHappens | passed | 0.038s |
| editingTheAmountClearsItsError | passed | 0.040s |
| confirmSignsWithTheQuotedNonceAndFeeAndBroadcastsTheSignedBytes | passed | 0.044s |
| cancelDropsTheConfirmationAndConfirmThenDoesNothing | passed | 0.036s |
| unminedPaymentIsReportedPendingAndBlocksASecondSend | passed | 0.037s |
| missingNonceStopsTheFlow | passed | 0.035s |
| confirmFetchesAFreshNonceAndFee | passed | 0.034s |
| dashboardContractIsUsedForTheTokenWhenListed | passed | 0.033s |
| dismissDialogResetsTheResult | passed | 0.034s |
| doubleTapOnConfirmBroadcastsOnce | passed | 0.036s |
| secondPayWhileTheFirstIsPreparingIsIgnored | passed | 0.035s |
| nativeEstimateCarriesValueAndNoCalldata | passed | 0.033s |
| tokenConfirmSignsAContractCallToTheTokenWithTransferCalldata | passed | 0.039s |

### MyProfileViewModelTest

`com.app.ezipaycoin.presentation.profile.MyProfileViewModelTest` - 11 tests, 11 passed, 0 failed, 0 skipped, 0.62s

| Test | Status | Time |
|---|---|---:|
| renameCallsTheApiWithNameAndEmailAndUpdatesTheCache | passed | 0.097s |
| logoutSucceeds | passed | 0.038s |
| renameFailureIsShownAndCacheIsUntouched | passed | 0.033s |
| refusedResponseLeavesTheCacheAlone | passed | 0.030s |
| showsCachedValuesWhenTheServerIsOffline | passed | 0.029s |
| blankServerFieldsKeepTheCachedOnes | passed | 0.029s |
| serverProfileReplacesAndIsCached | passed | 0.029s |
| withoutAProfileRepositoryOnlyTheCacheIsUsed | passed | 0.028s |
| emptyCacheShowsPlaceholdersUntilTheServerAnswers | passed | 0.029s |
| uploadedPictureUrlIsCached | passed | 0.240s |
| logoutFailureIsShown | passed | 0.035s |

### ProfileMergeTest

`com.app.ezipaycoin.presentation.profile.ProfileMergeTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| blankOrMissingServerValuesKeepCache | passed | 0.000s |
| noResponseKeepsCache | passed | 0.000s |
| emptyCacheTakesServer | passed | 0.000s |
| serverValuesOverrideCache | passed | 0.000s |

### WalletSharedViewModelTest

`com.app.ezipaycoin.presentation.shared.WalletSharedViewModelTest` - 24 tests, 24 passed, 0 failed, 0 skipped, 1.02s

| Test | Status | Time |
|---|---|---:|
| selectionFallsBackToTheFirstCoinWhenItIsNoLongerListed | passed | 0.082s |
| serverRefusalOnFirstLoadShowsItsMessage | passed | 0.030s |
| unlockLoadsTheDashboardAndSelectsTheFirstCoin | passed | 0.030s |
| pullRefreshAlwaysFetchesAndTogglesTheIndicator | passed | 0.038s |
| pullRefreshIsIgnoredBeforeUnlock | passed | 0.037s |
| socialLinksMapToTheirPages | passed | 0.079s |
| firstLoadFailureShowsAnError | passed | 0.047s |
| resumeRefreshIsAllowedAgainOnceTheWindowHasPassed | passed | 0.044s |
| pushRefreshRestartsTheResumeWindow | passed | 0.040s |
| pushBeforeUnlockIsIgnored | passed | 0.038s |
| openUrlEventIsRelayedToCollectors | passed | 0.038s |
| pullRefreshBypassesTheThrottle | passed | 0.041s |
| selectedCoinSurvivesARefreshEvenWithDifferentCase | passed | 0.040s |
| silentRefreshDoesNotShowTheFullScreenLoader | passed | 0.042s |
| failedPullRefreshKeepsLastGoodData | passed | 0.044s |
| fetchBalanceEventReloadsWithLoader | passed | 0.043s |
| serverRefusalDuringRefreshKeepsLastGoodData | passed | 0.039s |
| resumeRefreshIsIgnoredBeforeUnlock | passed | 0.036s |
| nothingIsFetchedBeforeUnlock | passed | 0.035s |
| pushRefreshIsAllowedAgainAfterTwoSeconds | passed | 0.042s |
| resumeRefreshIsThrottledAndSilent | passed | 0.041s |
| transactionSelectionIsStored | passed | 0.035s |
| pushWhileUnlockedRefreshesOnce | passed | 0.043s |
| unlockShowsTheLoaderWhileFirstLoading | passed | 0.039s |

### AllTransactionsViewModelTest

`com.app.ezipaycoin.presentation.transactions.AllTransactionsViewModelTest` - 6 tests, 6 passed, 0 failed, 0 skipped, 0.02s

| Test | Status | Time |
|---|---|---:|
| exceptionResetsLoadingAndKeepsEarlierRows | passed | 0.007s |
| serverRefusalResetsLoadingAndRetryAsksForTheSamePage | passed | 0.001s |
| filterChangeRestartsAtPageOne | passed | 0.002s |
| aShortFirstPageEndsPaging | passed | 0.000s |
| counterpartyIsTheOtherSide | passed | 0.005s |
| pagesAdvanceFromOneAndStopWhenTheTotalIsReached | passed | 0.001s |

### EnvelopeCipherTest

`com.app.ezipaycoin.utils.EnvelopeCipherTest` - 6 tests, 6 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| newWritesAreGcmAndRoundTrip | passed | 0.006s |
| existingCbcBlobsStillDecrypt | passed | 0.001s |
| fallsBackToLegacyWhenGcmKeyIsUnusable | passed | 0.001s |
| legacyBlobWhoseIvStartsWithTheMagicStillDecrypts | passed | 0.001s |
| eachWriteUsesAFreshIv | passed | 0.001s |
| tamperedGcmRecordIsRejected | passed | 0.000s |

### PasswordGateTest

`com.app.ezipaycoin.utils.PasswordGateTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| correctPasswordIsAccepted | passed | 0.000s |
| successResetsFailureCount | passed | 0.000s |
| missingStoredPasswordNeverMatches | passed | 0.000s |
| matcherOverloadUsesTheCallerDecision | passed | 0.001s |
| lockoutEndsAndCountersReset | passed | 0.000s |
| wrongPasswordCountsDown | passed | 0.000s |
| lockoutAfterMaxAttemptsEvenForCorrectPassword | passed | 0.000s |

### PasswordMigrationTest

`com.app.ezipaycoin.utils.PasswordMigrationTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.19s

| Test | Status | Time |
|---|---|---:|
| plaintextIsNeverClearedWhenVerifierCannotBeConfirmed | passed | 0.016s |
| migratesPlaintextToVerifierAndKeepsEverythingElse | passed | 0.035s |
| staleVerifierForADifferentPasswordIsReplaced | passed | 0.047s |
| crashBeforeVerifierWriteLeavesPlaintextUsable | passed | 0.026s |
| freshInstallWithoutPasswordIsUntouched | passed | 0.001s |
| crashBetweenVerifierAndPlaintextDeletionKeepsBothAndRecovers | passed | 0.040s |
| secondRunDoesNothing | passed | 0.022s |

### PasswordVerifierTest

`com.app.ezipaycoin.utils.PasswordVerifierTest` - 9 tests, 9 passed, 0 failed, 0 skipped, 1.35s

| Test | Status | Time |
|---|---|---:|
| withPasswordDropsPlaintext | passed | 1.220s |
| correctPasswordVerifies | passed | 0.003s |
| credentialsPreferVerifierOverLegacyPlaintext | passed | 0.004s |
| defaultsMeetTheMinimumAndRecordTheIterationCount | passed | 0.116s |
| wrongOrEmptyPasswordIsRejected | passed | 0.001s |
| credentialsAcceptLegacyPlaintextBeforeMigration | passed | 0.000s |
| malformedRecordsNeverThrow | passed | 0.001s |
| recordDoesNotContainThePassword | passed | 0.000s |
| saltIsRandomPerRecord | passed | 0.001s |

### RelockPolicyTest

`com.app.ezipaycoin.utils.RelockPolicyTest` - 6 tests, 6 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| foregroundWithoutBackgroundNeverLocks | passed | 0.001s |
| timerResetsAfterReturn | passed | 0.000s |
| defaultTimeoutIsTwoMinutes | passed | 0.000s |
| staysUnlockedOnQuickReturn | passed | 0.000s |
| repeatedStopsDoNotRestartTheClock | passed | 0.000s |
| locksAtTheTimeout | passed | 0.001s |

### SeedHolderTest

`com.app.ezipaycoin.utils.SeedHolderTest` - 3 tests, 3 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| keepsACopy | passed | 0.001s |
| holdsAndClears | passed | 0.000s |
| seedPhraseRouteCarriesNoArguments | passed | 0.000s |

### SeedPhraseRulesTest

`com.app.ezipaycoin.utils.SeedPhraseRulesTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| messagesAreSpecificPerCase | passed | 0.001s |
| validatorSeesTheNormalisedPhrase | passed | 0.001s |
| otherLengthsAreRejected | passed | 0.000s |
| everyBip39LengthIsAccepted | passed | 0.001s |
| checksumFailureFromWalletCoreIsInvalid | passed | 0.000s |
| pastedWhitespaceAndCaseAreNormalised | passed | 0.000s |
| emptyAndBlankInput | passed | 0.000s |

### SessionRecoveryTest

`com.app.ezipaycoin.utils.SessionRecoveryTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.02s

| Test | Status | Time |
|---|---|---:|
| exceptionDuringSignInIsAGiveUp | passed | 0.007s |
| aTokenRefreshedMeanwhileIsReusedWithoutAnotherSignIn | passed | 0.002s |
| expiredSignalFiresOnceUntilANewTokenIsSet | passed | 0.003s |
| concurrentRejectionsShareOneSignIn | passed | 0.004s |
| onlyAUN001BodyOn401TriggersRecoveryAndOnlyOnce | passed | 0.001s |
| failedSignInSendsTheAppToUnlockAndKeepsGoingNowhere | passed | 0.002s |
| rejectedSessionSignsInAgainAndRetries | passed | 0.001s |

### SocialProfileTest

`com.app.ezipaycoin.utils.SocialProfileTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| missingPhotoIsEmptyNotTheWordNull | passed | 0.001s |
| missingNameFallsBackToTheEmailPrefix | passed | 0.000s |
| fullProfileIsReady | passed | 0.000s |
| missingEmailIsReportedWithAMessage | passed | 0.001s |

### WalletSignInTest

`com.app.ezipaycoin.utils.WalletSignInTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.01s

| Test | Status | Time |
|---|---|---:|
| networkErrorsBecomeFailures | passed | 0.003s |
| failedLoginIsReported | passed | 0.001s |
| blankMessageGetsADefault | passed | 0.001s |
| failedNonceIsReportedAndLoginIsNotAttempted | passed | 0.001s |
| successSignsTheServerNonce | passed | 0.002s |

### AddressValidatorTest

`com.app.ezipaycoin.utils.validation.AddressValidatorTest` - 7 tests, 7 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| lowerAndUpperCaseHaveNoChecksum | passed | 0.000s |
| ownAddressRejectedCaseInsensitive | passed | 0.000s |
| whileTypingOnlyFlagsImpossibleInput | passed | 0.001s |
| malformedRejected | passed | 0.000s |
| validChecksummedAddressesPass | passed | 0.000s |
| badChecksumRejected | passed | 0.000s |
| keccakKnownVectors | passed | 0.001s |

### AmountValidatorTest

`com.app.ezipaycoin.utils.validation.AmountValidatorTest` - 9 tests, 9 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| rejectsMalformedZeroAndNegative | passed | 0.000s |
| tokenAmountCannotExceedBalance | passed | 0.000s |
| bnbAmountPlusFeeMustFit | passed | 0.000s |
| parsesPlainDecimals | passed | 0.000s |
| decimalLimitIsEighteen | passed | 0.000s |
| feeSkippedWhenUnknown | passed | 0.000s |
| tokenNeedsBnbForFee | passed | 0.000s |
| weiConversionRoundTrips | passed | 0.000s |
| blankAndInvalidMessages | passed | 0.000s |

### CoinKindTest

`com.app.ezipaycoin.utils.validation.CoinKindTest` - 5 tests, 5 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| knownTokensFallBackToConfiguredContracts | passed | 0.000s |
| malformedListedContractIsNotTrusted | passed | 0.000s |
| listedContractWins | passed | 0.000s |
| unknownCoinWithoutContractIsUnsupported | passed | 0.000s |
| bnbIsNativeEvenIfAContractIsListed | passed | 0.000s |

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
| scheduleSumsToTotalAndBacksOff | passed | 0.000s |

### RefreshThrottleTest

`com.app.ezipaycoin.utils.validation.RefreshThrottleTest` - 2 tests, 2 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| forceBypassesAndRestartsWindow | passed | 0.001s |
| allowsFirstThenBlocksWithinWindow | passed | 0.000s |

### TxErrorMapperTest

`com.app.ezipaycoin.utils.validation.TxErrorMapperTest` - 4 tests, 4 passed, 0 failed, 0 skipped, 0.00s

| Test | Status | Time |
|---|---|---:|
| gasAndNonceErrors | passed | 0.000s |
| networkErrors | passed | 0.000s |
| erc20InsufficientBalanceRevert | passed | 0.001s |
| unknownTextIsNeverShownRaw | passed | 0.000s |

## Instrumented tests

### DialogueAccessibilityTest

`com.app.ezipaycoin.ui.DialogueAccessibilityTest` - 32 tests, 32 passed, 0 failed, 0 skipped, 72.95s

| Test | Status | Time |
|---|---|---:|
| errorDialogAnnouncesAnErrorAndDismisses | passed | 5.199s |
| successDialogAnnouncesSuccess | passed | 2.712s |
| switchingCoinShowsItsBalance | passed | 2.974s |
| currencyPickerIconIsLabelled | passed | 2.025s |
| pullToRefreshFetchesTheDashboardAgain | passed | 1.764s |
| failedLoadShowsTheReasonAndRetryRecovers | passed | 1.538s |
| showsTheSelectedCoinBalanceAndFiatValue | passed | 1.754s |
| profileImageAndEditControlsAreLabelled | passed | 1.961s |
| showsTheNameFromTheServer | passed | 2.078s |
| showsTheCachedNameWhenOffline | passed | 2.241s |
| withoutAWalletNoQrCodeIsShown | passed | 1.269s |
| copyButtonIsLabelledAndClickable | passed | 1.550s |
| showsTheWalletAddressAndItsQrCode | passed | 1.469s |
| blankAddressIsReportedOnPay | passed | 2.468s |
| amountAboveBalanceIsReported | passed | 2.736s |
| cancelClosesTheConfirmationWithoutSending | passed | 2.590s |
| tokenPickerAndSlideControlAreLabelled | passed | 2.623s |
| garbageAddressIsFlaggedWhileTyping | passed | 1.423s |
| validRequestAsksForConfirmationShowingRecipientAmountAndFee | passed | 2.499s |
| networkRejectionShowsAnErrorDialogWithAnAccessibleIcon | passed | 2.570s |
| ownAddressIsRejected | passed | 1.583s |
| tappingARowReportsTheTransaction | passed | 2.078s |
| listsEachTransactionWithSignedAmount | passed | 1.491s |
| choosingAFilterRequestsThatType | passed | 1.944s |
| showsTheThreeFilters | passed | 1.241s |
| passwordFieldCanBeRevealedAndHasAnAccessibleToggle | passed | 1.665s |
| showsTitlePasswordFieldAndUnlockButton | passed | 1.429s |
| correctPasswordUnlocksWhenNoWalletIsSetUpYet | passed | 1.679s |
| wrongPasswordShowsAnErrorAndStaysLocked | passed | 2.136s |
| tokenTransferIsSignedAsAnErc20CallOnTheConfiguredChain | passed | 3.247s |
| nativeTransferCarriesTheRecipientAndAmount | passed | 2.804s |
| signatureIsDeterministicForTheSameRequest | passed | 4.829s |

