package com.yasin.nasa

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import com.yasin.nasa.di.DaggerMainComponent
import com.yasin.nasa.di.MainComponent

/**
 * Created by Yasin on 7/6/20.
 */
class Nasa : Application() {

    lateinit var appComponent: MainComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = getComponent()
    }

    private fun getComponent(): MainComponent {
        return DaggerMainComponent
            .builder()
            .context(this.applicationContext)
            .build()
    }

    companion object {
        fun getApp(context: Context): Nasa {
            return context.applicationContext as Nasa
        }
    }
}

fun Fragment.getAppComponent(): MainComponent = Nasa.getApp(this.requireContext()).appComponent