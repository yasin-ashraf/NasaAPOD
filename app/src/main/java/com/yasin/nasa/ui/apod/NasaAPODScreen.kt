package com.yasin.nasa.ui.apod

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
import com.yasin.nasa.util.TYPE_VIDEO
import com.yasin.nasa.util.getYoutubeVideoIdFromUrl
import com.yasin.nasa.util.retrieveVideoFrame
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.Calendar.*
import javax.inject.Inject

/**
 * Created by Yasin on 7/6/20.
 */
class NasaAPODScreen : Fragment(R.layout.screen_first) {

    @Inject lateinit var nasaAPODViewModelFactory: NasaAPODViewModelFactory
    @Inject lateinit var picasso: Picasso
    private lateinit var binding: ScreenFirstBinding
    private lateinit var videoLoadDisposable : Disposable
    private val nasaAPODViewModel: NasaAPODViewModel by navGraphViewModels(R.id.nav_main) { nasaAPODViewModelFactory }
    private lateinit var snackbar: Snackbar
    private var animated : Boolean = false
    private var buttonActionId : Int = 0

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
        subscribeApodData()
        subscribeHdUrl()
        subscribeMediaType()
        binding.zoomPlay.setOnClickListener(buttonCLickListener)
        binding.buttonCalendar.setOnClickListener { showDatePicker() }
    }

    private fun subscribeMediaType() {
        nasaAPODViewModel.mediaType.observe(this.viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    TYPE_IMAGE -> {
                        binding.zoomPlay.text = getString(R.string.zoom)
                        buttonActionId = R.id.action_nasaAPODScreen_to_photoViewScreen
                    }

                    TYPE_VIDEO -> {
                        binding.zoomPlay.text = getString(R.string.play)
                        buttonActionId = R.id.action_nasaAPODScreen_to_videoViewScreen
                    }
                }
            }
        })
    }

    private fun subscribeApodData() {
        nasaAPODViewModel.apodData.observe(this.viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Success -> {
                    render(it.data)
                    hideSnackBar()
                }
                is NetworkError -> {
                    showSnackBar(getString(R.string.no_internet))
                }
                is Error -> {
                    showSnackBar(it.message)
                }
            }
        })
    }

    private fun subscribeHdUrl() {
        nasaAPODViewModel.hdUrl.observe(this.viewLifecycleOwner, Observer {
            binding.zoomPlay.isEnabled = it != null
        })
    }

    private fun makeSnackBar() {
        snackbar = Snackbar.make(
            binding.snackBarView,
            getString(R.string.no_internet),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(R.string.action_retry)) {
                nasaAPODViewModel.getApod()
            }
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.nasa_white))
            .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.nasa_white))
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.snackBar_color))
    }

    private fun showSnackBar(text : String) {
        if (snackbar.isShownOrQueued) snackbar.dismiss()
        snackbar.setText(text)
        Handler().postDelayed({ snackbar.show() }, 500) //delay snackBar
    }

    private fun hideSnackBar() {
        if (snackbar.isShownOrQueued) snackbar.dismiss()
    }

    private fun render(apod: Apod?) {
        binding.tvHeadline.text = apod?.title
        binding.tvDescription.text = apod?.explanation
        loadImage(apod)
    }

    private fun loadImage(apod: Apod?) {
        if (!apod?.url.isNullOrEmpty()) {
            if (apod?.mediaType == TYPE_IMAGE) {
                picasso.load(apod.url)
                    .fit().centerCrop().into(binding.ivApod, imageLoadCallBack)
            } else {
                if (apod?.url?.contains("youtube") == true) {
                    // if youtube url, get thumbnail from url
                    val thumbnailUrl : String = getYoutubeThumbnail(apod.url)
                    picasso.load(thumbnailUrl)
                        .fit().centerCrop().into(binding.ivApod, imageLoadCallBack)
                }else {
                    // if not youtube url, try getting the frame from video
                    videoLoadDisposable = Single.fromCallable { retrieveVideoFrame(apod?.url) }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ bitmap ->
                            binding.ivApod.setImageBitmap(bitmap)
                            Log.d("LOAD VIDEO THUMBNAIL","SUCCESS!")
                        }, { throwable ->
                            showSnackBar(getString(R.string.cannot_get_thumbnail))
                            Log.e("LOAD VIDEO THUMBNAIL",throwable.toString())
                        })
                }
                binding.progressBar.visibility = View.GONE
                if (!animated) animateViewsIn()
            }
        }
    }

    private val buttonCLickListener : View.OnClickListener = View.OnClickListener {
        animated = false
        findNavController().navigate(buttonActionId)
    }

    private fun getYoutubeThumbnail(url:String): String {
        return "http://img.youtube.com/vi/${getYoutubeVideoIdFromUrl(url)}/hqdefault.jpg"
    }

    private val imageLoadCallBack: Callback = object : Callback {
        override fun onSuccess() {
            binding.progressBar.visibility = View.GONE
            if (!animated) animateViewsIn()
        }

        override fun onError(e: Exception?) {
            Log.e("Error loading APOD",e.toString())
            binding.progressBar.visibility = View.GONE
            showSnackBar(getString(R.string.error_loading_image))
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            //set date
            nasaAPODViewModel.setDate(
                year = year,
                month = month,
                day = dayOfMonth
            )
        }
        val datePicker = DatePickerDialog(
            requireContext(),
            R.style.Nasa_DatePickerTheme,
            dateSetListener,
            nasaAPODViewModel.getDate().get(YEAR),
            nasaAPODViewModel.getDate().get(MONTH),
            nasaAPODViewModel.getDate().get(DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = getInstance().timeInMillis
        datePicker.show()
    }

    /** Animation **/

    private fun animateViewsIn() {
        for (i in 0 until binding.container.childCount) {
            animateEachViewIn(binding.container.getChildAt(i), i)
        }
        animated = true
    }

    private fun animateEachViewIn(child: View, i: Int) {
        child.animate()
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f).scaleY(1f)
            .setStartDelay(500).setDuration((200 * i).toLong())
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

            val lpTvDescription = binding.scrollDescription.layoutParams as ViewGroup.MarginLayoutParams
            lpTvDescription.apply {
                rightMargin += windowInsets.systemWindowInsetRight
                leftMargin += windowInsets.systemWindowInsetLeft
            }
            binding.scrollDescription.layoutParams = lpTvDescription

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

    override fun onDestroy() {
        super.onDestroy()
        if(::videoLoadDisposable.isInitialized) videoLoadDisposable.dispose()
    }

}