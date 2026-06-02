package com.jorian.cybershield.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://www.virustotal.com/api/v3/"

    val virusTotalApi: VirusTotalApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VirusTotalApi::class.java)
    }
}