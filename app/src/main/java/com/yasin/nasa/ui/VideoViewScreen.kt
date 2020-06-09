package com.yasin.nasa.ui

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.commit451.youtubeextractor.Stream
import com.commit451.youtubeextractor.YouTubeExtractor
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.yasin.nasa.R
import com.yasin.nasa.databinding.ScreenVideoViewBinding
import com.yasin.nasa.getAppComponent
import com.yasin.nasa.ui.apod.NasaAPODViewModel
import com.yasin.nasa.ui.apod.NasaAPODViewModelFactory
import com.yasin.nasa.util.getYoutubeVideoIdFromUrl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import javax.inject.Inject


/**
 * Created by Yasin on 8/6/20.
 */
class VideoViewScreen : Fragment(R.layout.screen_video_view) {

    @Inject lateinit var nasaAPODViewModelFactory: NasaAPODViewModelFactory
    @Inject lateinit var picasso: Picasso
    private lateinit var binding: ScreenVideoViewBinding
    private lateinit var videoLoadDisposable : Disposable
    private val nasaAPODViewModel: NasaAPODViewModel by navGraphViewModels(R.id.nav_main) { nasaAPODViewModelFactory }
    private lateinit var player: SimpleExoPlayer
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        getAppComponent().injectVideoScreen(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ScreenVideoViewBinding.bind(view)
        insetWindow()
        makeSnackBar()
        makeVideoPlayer()
        subscribeToUrl()
    }

    private fun subscribeToUrl() {
        nasaAPODViewModel.hdUrl.observe(this.viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                if (it.contains("youtube")) {
                    extractYouTubeUrl(it)
                }else {
                    val videoUri: Uri = Uri.parse(it)
                    val videoSource: MediaSource? = buildMediaSource(videoUri)
                    player.prepare(videoSource, false, false)
                }
            }
        })
    }

    private fun extractYouTubeUrl(
        it: String
    ) {
        val extractor = YouTubeExtractor.Builder().build()
        videoLoadDisposable = extractor.extract(getYoutubeLink(it))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ extraction ->
                val videoUrl = extraction.streams
                    .filterIsInstance<Stream.VideoStream>()
                    .max()?.url
                Log.d("after extraction", videoUrl ?: "no url")
                val videoUri: Uri = Uri.parse(videoUrl)
                val videoSource: MediaSource? = buildMediaSource(videoUri)
                player.prepare(videoSource, false, false)
            }, { t ->
                t.printStackTrace()
                Log.e("Error extracting Url", t.toString())
                binding.progressBar.visibility = View.GONE
                showSnackBar(getString(R.string.cannot_retrieve_url))
            })
    }

    private fun makeSnackBar() {
        snackbar = Snackbar.make(
            binding.snackBarView,
            getString(R.string.no_internet),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(R.string.action_retry)) {
                nasaAPODViewModel.reloadVideo()
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


    private fun buildMediaSource(uri: Uri): MediaSource? {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(requireContext(), "nasa")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun makeVideoPlayer() {
        player = ExoPlayerFactory.newSimpleInstance(requireContext())
        player.playWhenReady = true
        player.addListener(playerEventListener)
        binding.video.player = player
    }

    private fun getYoutubeLink(url:String): String {
        Log.d("before extraction" , url)
        return getYoutubeVideoIdFromUrl(url) ?: ""
    }

    private fun insetWindow() {
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->

            val lpVideoView = binding.video.layoutParams as ViewGroup.MarginLayoutParams
            lpVideoView.apply {
                topMargin += windowInsets.systemWindowInsetTop
                bottomMargin += windowInsets.systemWindowInsetBottom
                rightMargin += windowInsets.systemWindowInsetRight
                leftMargin += windowInsets.systemWindowInsetLeft
            }
            binding.video.layoutParams = lpVideoView

            val lpSnackbarView = binding.snackBarView.layoutParams as ViewGroup.MarginLayoutParams
            lpSnackbarView.apply {
                topMargin += windowInsets.systemWindowInsetTop
                bottomMargin += windowInsets.systemWindowInsetBottom
                rightMargin += windowInsets.systemWindowInsetRight
                leftMargin += windowInsets.systemWindowInsetLeft
            }
            binding.snackBarView.layoutParams = lpSnackbarView

            // clear this listener so insets aren't re-applied
            binding.root.setOnApplyWindowInsetsListener(null)
            return@setOnApplyWindowInsetsListener windowInsets
        }
    }

    private val playerEventListener : Player.EventListener = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException?) {
            super.onPlayerError(error)
            binding.progressBar.visibility = View.GONE
            showSnackBar(getString(R.string.cannot_play))
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            when (playbackState) {
                Player.STATE_READY -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (snackbar.isShownOrQueued) hideSnackBar()
        releasePlayer()
        if(::videoLoadDisposable.isInitialized) videoLoadDisposable.dispose()
    }

    private fun releasePlayer() {
        player.stop()
        player.release()
    }

}