package com.yasin.nasa.ui.apod

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yasin.nasa.data.model.Apod
import com.yasin.nasa.network.NetworkState
import com.yasin.nasa.network.NetworkState.*
import com.yasin.nasa.util.TYPE_IMAGE
import com.yasin.nasa.util.TYPE_VIDEO
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import java.util.Calendar.*
import javax.inject.Inject

/**
 * Created by Yasin on 7/6/20.
 */
class NasaAPODViewModel @Inject constructor(
    private val nasaAPODRepository: NasaAPODRepository
) : ViewModel() {

    private val selectedDateCalenar = getInstance(TimeZone.getTimeZone("UTC"))
    private val currentDateCalendar = getInstance(TimeZone.getTimeZone("UTC"))
    private val decimalFormat: DecimalFormat by lazy { DecimalFormat("00") }
    private val _hdUrl : MutableLiveData<String> = MutableLiveData()
    val hdUrl : LiveData<String> get() = _hdUrl
    private val _mediaType : MutableLiveData<String> = MutableLiveData()
    val mediaType : MutableLiveData<String> get() = _mediaType
    private val defaultDate: String =
        "${decimalFormat.format(selectedDateCalenar.get(YEAR))}-${decimalFormat.format(
            selectedDateCalenar.get(MONTH) + 1)}-${decimalFormat.format(
            selectedDateCalenar.get(DAY_OF_MONTH)
        )}"
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val _selectedDate: MutableLiveData<String> =
        MutableLiveData(defaultDate) // date in yyyy-mm-dd
    private val _apodData: MutableLiveData<NetworkState<Apod>> = MutableLiveData()
    val apodData: LiveData<NetworkState<Apod>> get() = _apodData

    init {
        getDefaultApod()
    }

    private fun getDefaultApod() {
        _apodData.value = Loading
        compositeDisposable.add(
            nasaAPODRepository.getDefaultApod()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null) {
                        _apodData.value = Success(it)
                        if (it.mediaType == TYPE_IMAGE){
                            _mediaType.value = TYPE_IMAGE
                            _hdUrl.value = it.hdurl
                        }
                        else {
                            _mediaType.value = TYPE_VIDEO
                            _hdUrl.value = it.url
                        }
                    } else {
                        _apodData.value = Error("Error fetching data!")
                    }
                }, {
                    if (it is IOException) {
                        _apodData.value = NetworkError
                    } else {
                        _apodData.value = Error("${it.message}")
                    }
                })
        )
    }


    fun getApodForDate() {
        _apodData.value = Loading
        compositeDisposable.add(
            nasaAPODRepository.getApod(_selectedDate.value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null) {
                        _apodData.value = Success(it)
                        if (it.mediaType == TYPE_IMAGE){
                            _mediaType.value = TYPE_IMAGE
                            _hdUrl.value = it.hdurl
                        }
                        else {
                            _mediaType.value = TYPE_VIDEO
                            _hdUrl.value = it.url
                        }
                    } else {
                        _apodData.value = Error("Error fetching data!")
                    }
                }, {
                    if (it is IOException) {
                        _apodData.value = NetworkError
                    } else {
                        _apodData.value = Error("${it.message}")
                    }
                })
        )
    }

    fun setDate(year : Int, month : Int, day : Int) {
        selectedDateCalenar.set(YEAR,year)
        selectedDateCalenar.set(MONTH,month)
        selectedDateCalenar.set(DAY_OF_MONTH,day)
        if(selectedDateCalenar.timeInMillis == currentDateCalendar.timeInMillis) {
            getDefaultApod()
        }else {
            this._selectedDate.value = "${decimalFormat.format(year)}-${decimalFormat.format(
                month + 1)}-${decimalFormat.format(day)}"
            getApodForDate()
        }
    }

    fun getDate() : Calendar = selectedDateCalenar

    fun reloadVideo() {
        this._hdUrl.value = _hdUrl.value
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}