package com.app.ezipaycoin.data.repository

import com.app.ezipaycoin.data.remote.api.ApiService
import com.app.ezipaycoin.data.remote.dto.request.MintAllowlistRequest
import com.app.ezipaycoin.data.remote.dto.request.MintRequest
import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.app.ezipaycoin.data.remote.dto.response.NftCollectionResponse
import com.app.ezipaycoin.data.remote.dto.response.NftHoldingsResponse
import com.app.ezipaycoin.data.remote.dto.response.NftMintResponse
import com.app.ezipaycoin.data.remote.dto.response.NftTokenResponse
import com.app.ezipaycoin.domain.repository.NftRepository
import com.app.ezipaycoin.utils.ResponseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class NftRepoImpl(
    private val apiService: ApiService
) : NftRepository {

    override suspend fun getCollection(): ResponseState<NftCollectionResponse> =
        safeApiCall { apiService.getNftCollection() }

    override suspend fun getHoldings(owner: String): ResponseState<NftHoldingsResponse> =
        safeApiCall { apiService.getNftHoldings(owner) }

    override suspend fun getToken(tokenId: Long): ResponseState<NftTokenResponse> =
        safeApiCall { apiService.getNftToken(tokenId) }

    override suspend fun mintPublic(quantity: Int): ResponseState<NftMintResponse> =
        safeApiCall { apiService.mintNftPublic(MintRequest(quantity)) }

    override suspend fun mintAllowlist(
        quantity: Int,
        proof: List<String>
    ): ResponseState<NftMintResponse> =
        safeApiCall { apiService.mintNftAllowlist(MintAllowlistRequest(quantity, proof)) }

    /**
     * Unwraps the standard { apiStatus, apiMessage, error, apiData } envelope.
     *
     * The API methods return BaseResponse<T> directly rather than
     * Response<BaseResponse<T>>, so Retrofit throws HttpException on a non-2xx status
     * instead of returning a failed response — hence the explicit catch below.
     */
    private suspend fun <T> safeApiCall(
        call: suspend () -> BaseResponse<T>
    ): ResponseState<T> = withContext(Dispatchers.IO) {
        try {
            val body = call()

            when {
                body.apiStatus != true ->
                    ResponseState.Error(
                        body.apiMessage ?: body.error ?: "Request failed."
                    )

                body.apiData == null ->
                    ResponseState.Error("The server returned no data.")

                else -> ResponseState.Success(body.apiData!!)
            }
        } catch (e: HttpException) {
            ResponseState.Error(
                when (e.code()) {
                    401 -> "Your session has expired. Please unlock your wallet again."
                    else -> "Request failed (${e.code()})."
                }
            )
        } catch (e: IOException) {
            ResponseState.Error("Can't reach the network. Check your connection and try again.")
        } catch (e: Exception) {
            ResponseState.Error(e.message ?: "Something went wrong.")
        }
    }
}
