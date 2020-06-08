package com.yasin.nasa.di

import android.content.Context
import com.yasin.nasa.di.module.ApplicationModule
import com.yasin.nasa.di.module.NetworkModule
import com.yasin.nasa.di.module.PicassoModule
import com.yasin.nasa.di.module.RetrofitModule
import com.yasin.nasa.ui.PhotoViewScreen
import com.yasin.nasa.ui.VideoViewScreen
import com.yasin.nasa.ui.apod.NasaAPODScreen
import dagger.BindsInstance
import dagger.Component

/**
 * Created by Yasin on 23/5/20.
 */
@AppScope
@Component(
    modules = [ApplicationModule::class, NetworkModule::class, PicassoModule::class, RetrofitModule::class]
)
interface MainComponent {
    fun injectApodScreen(nasaAPODScreen: NasaAPODScreen)
    fun injectPhotoScreen(photoViewScreen: PhotoViewScreen)
    fun injectVideoScreen(videoViewScreen: VideoViewScreen)

    @Component.Builder
    interface Builder {
        fun build() : MainComponent
        @BindsInstance fun context(@ApplicationContext context : Context) : Builder
    }
}