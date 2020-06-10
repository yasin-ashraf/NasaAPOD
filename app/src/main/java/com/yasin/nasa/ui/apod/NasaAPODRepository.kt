package com.yasin.nasa.ui.apod

import com.yasin.nasa.data.model.Apod
import com.yasin.nasa.network.NasaApiInterface
import com.yasin.nasa.util.API_KEY
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Yasin on 7/6/20.
 */
class NasaAPODRepository @Inject constructor(
    private val nasaApiInterface: NasaApiInterface
) {

    fun getApod(date : String?) : Single<Apod> = nasaApiInterface.getApod(API_KEY,date ?: "2019-01-01")

    fun getDefaultApod() : Single<Apod> = nasaApiInterface.getDefaultApod(API_KEY)
}