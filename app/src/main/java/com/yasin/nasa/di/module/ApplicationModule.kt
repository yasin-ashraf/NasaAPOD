package com.yasin.nasa.di.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yasin.nasa.data.SessionManager
import com.yasin.nasa.di.AppScope
import com.yasin.nasa.di.ApplicationContext
import com.yasin.nasa.network.NasaApiInterface
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

/**
 * Created by Yasin on 7/6/20.
 */
@Module()
class ApplicationModule {

    @Provides
    @AppScope
    fun newsApiServices(retrofit: Retrofit): NasaApiInterface {
        return retrofit.create(NasaApiInterface::class.java)
    }

    @Provides
    @AppScope
    fun gson(): Gson {
        val gsonBuilder = GsonBuilder()
//            .registerTypeAdapter(Int::class.java, IntegerTypeAdapter())
//            .registerTypeAdapter(Integer::class.java, IntegerTypeAdapter())
            .setDateFormat("yyyy-MM-dd")
        return gsonBuilder.create()
    }

    @Provides
    @AppScope
    fun sessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }
}