package com.yasin.nasa.ui.apod

import com.yasin.nasa.network.NasaApiInterface
import javax.inject.Inject

/**
 * Created by Yasin on 7/6/20.
 */
class NasaAPODRepository @Inject constructor(
    private val nasaApiInterface: NasaApiInterface
) {
}