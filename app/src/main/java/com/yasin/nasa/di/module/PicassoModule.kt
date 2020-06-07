package com.yasin.nasa.di.module

import android.content.Context

import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.yasin.nasa.di.AppScope
import com.yasin.nasa.di.ApplicationContext
import com.yasin.nasa.util.VideoRequestHandler

import dagger.Module
import dagger.Provides

/**
 * Created by Yasin on 7/6/20.
 */
@Module(includes = [NetworkModule::class])
class PicassoModule {

    @Provides
    @AppScope
    fun picasso(@ApplicationContext context: Context): Picasso {
        return Picasso.Builder(context)
            .loggingEnabled(true)
            .addRequestHandler(VideoRequestHandler())
            .downloader(OkHttp3Downloader(context))
            .build()
    }

}
