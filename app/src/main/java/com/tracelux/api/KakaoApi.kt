package com.tracelux.api

import com.tracelux.models.KakaoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoApi {
    @GET("v2/local/search/address.json")
    suspend fun searchAddress(
        @Header("Authorization") auth: String,
        @Query("query") query: String
    ): KakaoSearchResponse

    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Header("Authorization") auth: String,
        @Query("query") query: String
    ): KakaoSearchResponse
}
