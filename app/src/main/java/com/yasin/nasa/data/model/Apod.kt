package com.yasin.nasa.data.model

import com.google.gson.annotations.SerializedName

data class Apod(
    @SerializedName("date")
    val date: String = "",

    @SerializedName("copyright")
    val copyright: String = "",

    @SerializedName("media_type")
    val mediaType: String = "",

    @SerializedName("hdurl")
    val hdurl: String = "",

    @SerializedName("service_version")
    val serviceVersion: String = "",

    @SerializedName("explanation")
    val explanation: String = "",

    @SerializedName("title")
    val title: String = "",

    @SerializedName("url")
    val url: String = ""
)