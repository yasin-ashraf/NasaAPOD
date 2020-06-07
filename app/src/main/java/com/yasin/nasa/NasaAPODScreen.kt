package com.yasin.nasa

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yasin.nasa.databinding.ScreenFirstBinding

/**
 * Created by Yasin on 7/6/20.
 */
class NasaAPODScreen : Fragment(R.layout.screen_first) {

    private lateinit var binding: ScreenFirstBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ScreenFirstBinding.bind(view)
        insetWindow()
    }

    private fun insetWindow() {
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->

            val lpTvTitle = binding.tvHeadline.layoutParams as ViewGroup.MarginLayoutParams
            lpTvTitle.apply {
                topMargin += windowInsets.systemWindowInsetTop
                rightMargin += windowInsets.systemWindowInsetRight
                leftMargin += windowInsets.systemWindowInsetLeft
            }
            binding.tvHeadline.layoutParams = lpTvTitle

            val lpTvDescription = binding.tvDescription.layoutParams as ViewGroup.MarginLayoutParams
            lpTvDescription.apply {
                bottomMargin += windowInsets.systemWindowInsetBottom
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