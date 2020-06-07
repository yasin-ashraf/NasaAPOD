package com.yasin.nasa.ui.apod

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.yasin.nasa.R
import com.yasin.nasa.data.model.Apod
import com.yasin.nasa.databinding.ScreenFirstBinding
import com.yasin.nasa.getAppComponent
import com.yasin.nasa.network.NetworkState.*
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by Yasin on 7/6/20.
 */
class NasaAPODScreen : Fragment(R.layout.screen_first) {

    @Inject lateinit var nasaAPODViewModelFactory: NasaAPODViewModelFactory
    @Inject lateinit var picasso: Picasso
    private lateinit var binding: ScreenFirstBinding
    private val nasaAPODViewModel: NasaAPODViewModel by navGraphViewModels(R.id.nav_main) { nasaAPODViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        getAppComponent().injectApodScreen(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ScreenFirstBinding.bind(view)
        insetWindow()
        init()
    }

    private fun init() {
        nasaAPODViewModel.apodData.observe(this.viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> { binding.progressBar.visibility = View.VISIBLE }
                is Success -> {render(it.data)}
                is NetworkError -> {}
                is Error -> {}
            }
        })
    }

    private fun render(apod: Apod?) {
        binding.tvHeadline.text = apod?.title
        binding.tvDescription.text = apod?.explanation
        picasso.load(apod?.hdurl)
            .fit()
            .into(binding.ivApod,picassoCallback)
    }

    private val picassoCallback : Callback = object : Callback {
        override fun onSuccess() {

        }

        override fun onError(e: Exception?) {

        }

    }

    private fun insetWindow() {
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->

            val lpCalendar = binding.buttonCalendar.layoutParams as ViewGroup.MarginLayoutParams
            lpCalendar.apply {
                rightMargin += windowInsets.systemWindowInsetRight
                leftMargin += windowInsets.systemWindowInsetLeft
            }
            binding.buttonCalendar.layoutParams = lpCalendar

            val lpTvTitle = binding.tvHeadline.layoutParams as ViewGroup.MarginLayoutParams
            lpTvTitle.apply {
                topMargin += windowInsets.systemWindowInsetTop
                rightMargin += windowInsets.systemWindowInsetRight
                leftMargin += windowInsets.systemWindowInsetLeft
            }
            binding.tvHeadline.layoutParams = lpTvTitle

            val lpTvDescription = binding.tvDescription.layoutParams as ViewGroup.MarginLayoutParams
            lpTvDescription.apply {
                rightMargin += windowInsets.systemWindowInsetRight
                leftMargin += windowInsets.systemWindowInsetLeft
            }
            binding.tvDescription.layoutParams = lpTvDescription

            val lpCvZoomPlay = binding.zoomPlay.layoutParams as ViewGroup.MarginLayoutParams
            lpCvZoomPlay.apply {
                bottomMargin += windowInsets.systemWindowInsetBottom
                rightMargin += windowInsets.systemWindowInsetRight
                leftMargin += windowInsets.systemWindowInsetLeft
            }
            binding.zoomPlay.layoutParams = lpCvZoomPlay

            // clear this listener so insets aren't re-applied
            binding.root.setOnApplyWindowInsetsListener(null)
            return@setOnApplyWindowInsetsListener windowInsets
        }
    }


}