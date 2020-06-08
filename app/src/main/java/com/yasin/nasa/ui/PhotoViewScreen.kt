package com.yasin.nasa.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.yasin.nasa.R
import com.yasin.nasa.databinding.ScreenPhotoViewBinding
import com.yasin.nasa.getAppComponent
import com.yasin.nasa.ui.apod.NasaAPODViewModel
import com.yasin.nasa.ui.apod.NasaAPODViewModelFactory
import javax.inject.Inject

/**
 * Created by Yasin on 8/6/20.
 */
class PhotoViewScreen : Fragment(R.layout.screen_photo_view) {

    @Inject lateinit var nasaAPODViewModelFactory: NasaAPODViewModelFactory
    @Inject lateinit var picasso: Picasso
    private lateinit var binding: ScreenPhotoViewBinding
    private val nasaAPODViewModel: NasaAPODViewModel by navGraphViewModels(R.id.nav_main) { nasaAPODViewModelFactory }
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        getAppComponent().injectPhotoScreen(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ScreenPhotoViewBinding.bind(view)
        insetWindow()
        nasaAPODViewModel.hdUrl.observe(this.viewLifecycleOwner, Observer {
            if (it != null) {
                picasso.load(it)
                    .into(binding.zoomView, imageLoadCallBack)
            }
        })
    }

    private val imageLoadCallBack: Callback = object : Callback {
        override fun onSuccess() {
            binding.progressBar.visibility = View.GONE
        }

        override fun onError(e: Exception?) {
            Log.e("Error loading APOD",e.toString())
            binding.progressBar.visibility = View.GONE
//            showSnackBar(getString(R.string.error_loading_image))
        }
    }

    private fun insetWindow() {
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }


}