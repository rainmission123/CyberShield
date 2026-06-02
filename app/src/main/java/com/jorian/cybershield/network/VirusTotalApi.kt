package com.jorian.cybershield.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

interface VirusTotalApi {

    @FormUrlEncoded
    @POST("urls")
    suspend fun submitUrl(
        @Header("x-apikey") apiKey: String,
        @Field("url") url: String
    ): Response<VirusTotalSubmitResponse>

    @GET("analyses/{analysisId}")
    suspend fun getAnalysis(
        @Header("x-apikey") apiKey: String,
        @Path("analysisId") analysisId: String
    ): Response<VirusTotalAnalysisResponse>
}