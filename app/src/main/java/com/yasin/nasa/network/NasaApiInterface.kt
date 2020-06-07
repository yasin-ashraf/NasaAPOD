package com.yasin.nasa.network

import com.yasin.nasa.data.model.Apod
import com.yasin.nasa.util.APOD_ENDPOINT
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Yasin on 7/6/20.
 */
interface NasaApiInterface {

    @GET(APOD_ENDPOINT)
    fun getApod(@Query("api_key") apiKey : String) : Single<Apod>

}