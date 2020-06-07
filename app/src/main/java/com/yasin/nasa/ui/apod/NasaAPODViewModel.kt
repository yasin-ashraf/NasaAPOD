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
import javax.inject.Inject

/**
 * Created by Yasin on 7/6/20.
 */
class NasaAPODViewModel @Inject constructor(
    private val nasaAPODRepository: NasaAPODRepository
) : ViewModel() {

    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val _apodData: MutableLiveData<NetworkState<Apod>> = MutableLiveData()
    val apodData: LiveData<NetworkState<Apod>> get() = _apodData

    init {
        getApod()
    }

    fun getApod() {
        _apodData.value = Loading
        compositeDisposable.add(
            nasaAPODRepository.getApod()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null) {
                        _apodData.value = Success(it)
                    } else {
                        _apodData.value = Error("Error fetching data!")
                    }
                }, {
                    if(it is IOException) {
                        _apodData.value = NetworkError
                    }else {
                        _apodData.value = Error("${it.message}")
                    }
                })
        )
    }

}