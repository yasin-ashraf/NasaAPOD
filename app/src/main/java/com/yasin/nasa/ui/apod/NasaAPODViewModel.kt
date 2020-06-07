package com.yasin.nasa.ui.apod

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yasin.nasa.data.model.Apod
import com.yasin.nasa.network.NetworkState
import com.yasin.nasa.network.NetworkState.*
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

    private val calendar = getInstance()
    private val decimalFormat: DecimalFormat by lazy { DecimalFormat("00") }
    private val defaultDate: String =
        "${decimalFormat.format(calendar.get(YEAR))}-${decimalFormat.format(
            calendar.get(MONTH))}-${decimalFormat.format(
            calendar.get(DAY_OF_MONTH)
        )}"
    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val _selectedDate: MutableLiveData<String> =
        MutableLiveData(defaultDate) // date in yyyy-mm-dd
    private val _apodData: MutableLiveData<NetworkState<Apod>> = MutableLiveData()
    val apodData: LiveData<NetworkState<Apod>> get() = _apodData

    init {
        getApod()
    }

    fun getApod() {
        _apodData.value = Loading
        compositeDisposable.add(
            nasaAPODRepository.getApod(_selectedDate.value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null) {
                        _apodData.value = Success(it)
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
        this._selectedDate.value = "${decimalFormat.format(year)}-${decimalFormat.format(
           month)}-${decimalFormat.format(day)}"
        calendar.set(YEAR,year)
        calendar.set(MONTH,month)
        calendar.set(DAY_OF_MONTH,day)
        getApod()
    }

    fun getDate() : Calendar = calendar

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}