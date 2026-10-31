package com.app.ezipaycoin.data

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.KycSubmitRequest
import com.app.ezipaycoin.data.repository.KycRepoImpl
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** The form field names must match the backend's KycSubmitRequestDTO (model binding ignores unknown names). */
class KycSubmitFormTest {

    @get:Rule val tmp = TemporaryFolder()
    private lateinit var server: MockWebServer
    private lateinit var repo: KycRepoImpl

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        repo = KycRepoImpl(api)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun submitSendsTheBackendFieldNames() = runTest {
        server.enqueue(
            MockResponse().setHeader("Content-Type", "application/json")
                .setBody("""{"apiStatus":true,"apiMessage":"ok","error":null,"apiData":null}""")
        )
        val front = tmp.newFile("front.jpg").apply { writeText("a") }
        val selfie = tmp.newFile("selfie.jpg").apply { writeText("b") }
        repo.submitKyc(
            KycSubmitRequest(
                fullName = "A B", dateOfBirth = "01/01/1990", nationality = "IN",
                addressLine = "1 Road", city = "C", postalCode = "1", country = "IN",
                documentType = "PASSPORT", documentNumber = "X1",
                frontImagePath = front.path, backImagePath = null,
                selfieImagePath = selfie.path, consentAccepted = true
            )
        )
        val body = server.takeRequest().body.readUtf8()
        for (name in listOf(
            "FullName", "DateOfBirth", "Nationality", "AddressLine", "City", "PostalCode", "Country",
            "DocumentTypeId", "DocumentNumber", "ConsentAccepted", "FrontImage", "Selfie"
        )) {
            assertTrue("missing form field $name", body.contains("name=\"$name\"", ignoreCase = true))
        }
        assertTrue(body.contains("name=\"DocumentTypeId\"") && body.contains("name=\"Selfie\""))
        assertFalse(body.contains("name=\"documentType\""))
        assertFalse(body.contains("name=\"selfieImage\""))
    }
}
