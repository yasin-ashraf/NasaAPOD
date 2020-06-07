package com.yasin.nasa.ui.apod

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.yasin.nasa.R
import com.yasin.nasa.data.model.Apod
import com.yasin.nasa.databinding.ScreenFirstBinding
import com.yasin.nasa.getAppComponent
import com.yasin.nasa.network.NetworkState.*
import com.yasin.nasa.util.TYPE_IMAGE
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
    private lateinit var snackbar: Snackbar

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
        makeSnackBar()
        nasaAPODViewModel.apodData.observe(this.viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> { binding.progressBar.visibility = View.VISIBLE }
                is Success -> {
                    render(it.data)
                    hideSnackBar()
                }
                is NetworkError -> {showSnackBar()}
                is Error -> {}
            }
        })
    }

    private fun makeSnackBar() {
        snackbar = Snackbar.make(binding.snackBarView, getString(R.string.no_internet), Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.action_retry)) {
                nasaAPODViewModel.getApod()
            }
            .setTextColor(ContextCompat.getColor(requireContext(),R.color.nasa_white))
            .setActionTextColor(ContextCompat.getColor(requireContext(),R.color.nasa_white))
            .setBackgroundTint(ContextCompat.getColor(requireContext(),R.color.snackBar_color))
    }

    private fun showSnackBar() {
        if(snackbar.isShownOrQueued) snackbar.dismiss()
        Handler().postDelayed({ snackbar.show() },500) //delay snackBar
    }

    private fun hideSnackBar() {
        if(snackbar.isShownOrQueued) snackbar.dismiss()
    }

    private fun render(apod: Apod?) {
        binding.tvHeadline.text = apod?.title
        binding.tvDescription.text = apod?.explanation
        if (apod?.mediaType == TYPE_IMAGE) binding.tvButton.text = getString(R.string.zoom)
        else binding.tvButton.text = getString(R.string.play)
        picasso.load(apod?.hdurl)
            .fit()
            .into(binding.ivApod,imageLoadCallBack)
    }

    private val imageLoadCallBack : Callback = object : Callback {
        override fun onSuccess() {
            binding.progressBar.visibility = View.GONE
            animateViewsIn()
        }

        override fun onError(e: Exception?) {
            binding.progressBar.visibility = View.GONE
        }

    }

    private fun animateViewsIn() {
        for (i in 0 until binding.container.childCount) {
            animateEachViewIn(binding.container.getChildAt(i), i)
        }
    }

    private fun animateEachViewIn(child: View, i: Int) {
        child.animate()
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(500)
            .setDuration((200 * i).toLong())
            .setInterpolator(DecelerateInterpolator(2f))
            .start()
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