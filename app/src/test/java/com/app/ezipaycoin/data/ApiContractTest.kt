package com.app.ezipaycoin.data

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.LoginRequest
import com.app.ezipaycoin.data.remote.dto.request.MintAllowlistRequest
import com.app.ezipaycoin.data.remote.dto.request.MintRequest
import com.app.ezipaycoin.data.remote.dto.request.TransactionsRequest
import com.app.ezipaycoin.data.remote.dto.request.UpdateProfileRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.POST
import java.lang.reflect.Modifier

/**
 * Guards the app against drifting from the backend contract.
 *
 * Source of truth: src/test/resources/contract/backend-baseline.txt, copied from the backend's
 * contract-baseline.txt. Every route declared in ApiService must exist there, except the gaps
 * listed in [KNOWN_GAPS]. A new route that is neither in the baseline nor in the list fails the build.
 * When a gap is fixed, delete its entry (a stale entry is harmless but misleading).
 */
class ApiContractTest {

    private data class Route(val method: String, val path: String) {
        override fun toString() = "$method $path"
    }

    private fun normalise(path: String) =
        path.trim('/').lowercase().replace(Regex("\\{[^}]*\\}"), "{}").let { "/$it" }

    private val baseline: List<String> by lazy {
        checkNotNull(javaClass.getResource("/contract/backend-baseline.txt")).readText().lines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
    }

    private val baselineRoutes: Set<Route> by lazy {
        baseline.filter { it.matches(Regex("^(GET|POST|PUT|DELETE|PATCH) .*")) }.map {
            val (m, p) = it.split(" ", limit = 2)
            Route(m, normalise(p.removePrefix("/api/v1")))
        }.toSet()
    }

    private val appRoutes: List<Route> by lazy {
        ApiService::class.java.declaredMethods.mapNotNull { m ->
            m.getAnnotation(GET::class.java)?.let { Route("GET", normalise(it.value)) }
                ?: m.getAnnotation(POST::class.java)?.let { Route("POST", normalise(it.value)) }
        }.sortedBy { it.toString() }
    }

    /** JSON-RPC calls go to the BSC node, not to the backend. */
    private val chainRpc = Route("POST", "/")

    /**
     * Routes the app calls that the backend baseline does not have, with the reason.
     * Payment routes are served by the vendor today (the backend exposes them under Payments/);
     * the others have no backend route yet.
     */
    private val KNOWN_GAPS = mapOf(
        Route("POST", "/networkinfobytokenid") to "vendor host today; backend route is Payments/NetworkInfobyTokenId",
        Route("POST", "/cryptopayindeposittoken") to "vendor host today; backend route is Payments/CryptoPayinDeposittoken",
        Route("POST", "/cryptopayouttransfertoken") to "vendor host today; backend route is Payments/CryptoPayoutTransfertoken",
        Route("POST", "/cryptopayintransactionstatuscheck") to "vendor host today; backend route is Payments/CryptoPayinTransactionStatusCheck",
        Route("POST", "/cardtxn") to "vendor host today; backend route is Payments/CardTxn",
        Route("POST", "/kyc/video/upload") to "no backend route",
        Route("GET", "/kyc/video/status/{}") to "no backend route",
        Route("POST", "/kyc/liveness/session") to "no backend route",
        Route("GET", "/contract/validatetoken") to "no backend route",
        Route("POST", "/contract/addtoken") to "no backend route",
    )

    @Test
    fun baselineResourceIsPresentAndNonTrivial() {
        assertTrue("expected the copied backend routes", baselineRoutes.size > 50)
        assertTrue(baselineRoutes.contains(Route("GET", "/dashboard")))
    }

    @Test
    fun apiServiceDeclaresRoutes() {
        assertTrue(appRoutes.size > 20)
    }

    @Test
    fun everyAppRouteIsInTheBackendBaselineOrAKnownGap() {
        val unknown = appRoutes.filter { it != chainRpc && it !in baselineRoutes && it !in KNOWN_GAPS }
        assertTrue("routes the backend does not define: $unknown", unknown.isEmpty())
    }

    @Test
    fun coreRoutesTheMoneyAndSessionFlowsNeedAreInTheBaseline() {
        val core = listOf(
            Route("GET", "/auth/nonce"), Route("POST", "/auth/login"), Route("GET", "/auth/logout"),
            Route("GET", "/dashboard"), Route("POST", "/wallet/transactions"),
            Route("GET", "/users/me"), Route("POST", "/users/updateprofile"),
            Route("POST", "/nft/mint/public"), Route("GET", "/kyc/status"), Route("POST", "/kyc/submit"),
        )
        core.forEach { assertTrue("$it must be a backend route", it in baselineRoutes) }
        core.forEach { assertTrue("$it must be called by the app", it in appRoutes) }
    }

    @Test
    fun knownGapsAreNotHidingRoutesThatTheBaselineHas() {
        // A gap that now exists in the baseline should be removed from the list.
        val stale = KNOWN_GAPS.keys.filter { it in baselineRoutes }
        assertTrue("remove from KNOWN_GAPS: $stale", stale.isEmpty())
    }

    // ---- request bodies ----

    private fun dtoFields(name: String): Set<String> {
        val line = baseline.first { it.startsWith("DTO $name ") }
        return line.substringAfter("| ").split(",").map { it.substringBefore(":").lowercase() }.toSet()
    }

    private fun fieldsOf(c: Class<*>): Set<String> =
        c.declaredFields.filter { !Modifier.isStatic(it.modifiers) }.map {
            (it.getAnnotation(com.google.gson.annotations.SerializedName::class.java)?.value ?: it.name).lowercase()
        }.toSet()

    @Test
    fun loginBodyMatchesTheBackendDto() {
        assertEquals(dtoFields("LoginInputDTO"), fieldsOf(LoginRequest::class.java))
    }

    @Test
    fun updateProfileBodyMatchesTheBackendDto() {
        assertEquals(dtoFields("UpdateProfileInfo"), fieldsOf(UpdateProfileRequest::class.java))
    }

    @Test
    fun transactionsBodyFieldsAreAcceptedByTheBackend() {
        val unknown = fieldsOf(TransactionsRequest::class.java) - dtoFields("TransactionFilterRequestDTO")
        assertTrue("backend ignores: $unknown", unknown.isEmpty())
    }

    @Test
    fun mintBodiesMatchTheBackendDtos() {
        assertEquals(dtoFields("MintRequestDTO"), fieldsOf(MintRequest::class.java))
        assertEquals(dtoFields("MintAllowlistRequestDTO"), fieldsOf(MintAllowlistRequest::class.java))
    }

    /**
     * Form fields KycRepoImpl sends that the backend form (KycSubmitRequestDTO) has no property for,
     * so model binding drops them. Both are [Required] on the backend, so submit is rejected.
     * Remove an entry when the app sends the backend's name (DocumentTypeId, Selfie).
     */
    private val KNOWN_KYC_FORM_MISMATCHES = setOf("documenttype", "selfieimage")

    @Test
    fun kycSubmitFormFieldNamesMatchTheBackendForm() {
        val harness = ApiHarness()
        try {
            val front = java.io.File.createTempFile("front", ".jpg").apply { writeBytes(byteArrayOf(1)); deleteOnExit() }
            val selfie = java.io.File.createTempFile("selfie", ".jpg").apply { writeBytes(byteArrayOf(2)); deleteOnExit() }
            harness.enqueueJson("""{"apiStatus":true,"apiMessage":"ok","error":null,"apiData":null}""")
            kotlinx.coroutines.runBlocking {
                com.app.ezipaycoin.data.repository.KycRepoImpl(harness.api).submitKyc(
                    com.app.ezipaycoin.data.remote.dto.request.KycSubmitRequest(
                        fullName = "A B", dateOfBirth = "01 / 01 / 1990", nationality = "IN", addressLine = "1 Road",
                        city = "C", postalCode = "1", country = "IN", documentType = "PASSPORT", documentNumber = "X1",
                        frontImagePath = front.path, backImagePath = null, selfieImagePath = selfie.path,
                        consentAccepted = true
                    )
                )
            }
            val request = harness.server.takeRequest()
            assertEquals("/api/v1/Kyc/submit", request.path)
            val names = Regex("(?<!file)name=\"([^\"]+)\"").findAll(request.body.readUtf8()).map { it.groupValues[1].lowercase() }.toSet()
            assertTrue(names.containsAll(listOf("fullname", "documentnumber", "frontimage", "consentaccepted")))
            val unknown = names - dtoFields("KycSubmitRequestDTO")
            assertTrue("new form fields the backend does not bind: ${unknown - KNOWN_KYC_FORM_MISMATCHES}", KNOWN_KYC_FORM_MISMATCHES.containsAll(unknown))
        } finally {
            harness.shutdown()
        }
    }
}
