package com.yasin.nasa.util

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler

/**
 * Created by Yasin on 28/1/20.
 */
class VideoRequestHandler : RequestHandler() {

    companion object {
        const val SCHEME_VIDEO = "video"
    }

    override fun canHandleRequest(data: Request?): Boolean {
        return (SCHEME_VIDEO == data?.uri?.scheme)
    }

    override fun load(request: Request?, networkPolicy: Int): Result? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(request?.uri?.path)
        val offsetString = request?.uri?.fragment
        val offset : Long = offsetString?.toLong() ?: 0L
        val bitmap : Bitmap = mediaMetadataRetriever.getFrameAtTime(offset)
        return Result(bitmap, Picasso.LoadedFrom.DISK)
    }
}