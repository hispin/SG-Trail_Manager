package com.sensoguard.trailmanager.controler

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class ViewModelListener : ViewModel() {

    private var scheduleTaskExecutor: ScheduledExecutorService? = null


    //listener to the ticks of the timer
    private var currentCalendar: MutableLiveData<Calendar>? = null

    fun startCurrentCalendarListener(): LiveData<Calendar>? {

        if (currentCalendar == null) {
            currentCalendar = MutableLiveData()
        }

        return currentCalendar
    }


    fun startTimer() {
        //if there is already a running timer ,it is not necessary to initial the timer
        if (scheduleTaskExecutor == null || scheduleTaskExecutor?.isShutdown == true) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1)
            executeTimer()
        }
    }

    // execute the timer every 1 second
    // (fixed delay: after the device sleeps the missed ticks are not executed all at once)
    private fun executeTimer() {
        scheduleTaskExecutor?.scheduleWithFixedDelay({
            try {
                this.currentCalendar?.postValue(Calendar.getInstance())
            } catch (ex: Exception) {
                Log.d("testTimer", "exception:" + ex.message)
            }

        }, 0, 1, TimeUnit.SECONDS)
    }

    //shut down the timer
    fun shutDownTimer() {
        scheduleTaskExecutor?.shutdownNow()
    }

    override fun onCleared() {
        shutDownTimer()
    }
}
