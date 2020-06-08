package com.yasin.nasa.util

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by Yasin on 8/6/20.
 */
@Throws(Throwable::class)
fun retrieveVideoFrame(videoPath: String?): Bitmap? {
    val bitmap: Bitmap?
    var mediaMetadataRetriever: MediaMetadataRetriever? = null
    try {
        mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(
            videoPath,
            HashMap()
        )
        bitmap = mediaMetadataRetriever.getFrameAtTime(3000000, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC)
    } catch (e: Exception) {
        e.printStackTrace()
        throw Throwable("Exception in retrieve video frame:$e")
    } finally {
        mediaMetadataRetriever?.release()
    }
    return bitmap
}

fun getYoutubeVideoIdFromUrl(inUrl: String): String? {
    inUrl.replace("&feature=youtu.be", "")
    if (inUrl.toLowerCase().contains("youtu.be")) {
        return inUrl.substring(inUrl.lastIndexOf("/") + 1)
    }
    val pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
    val compiledPattern: Pattern = Pattern.compile(pattern)
    val matcher: Matcher = compiledPattern.matcher(inUrl)
    return if (matcher.find()) {
        matcher.group()
    } else null
}
