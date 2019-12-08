package com.sensoguard.hunter.controler

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class ViewModelListener(application: Application) : AndroidViewModel(application) {

    private var t: ScheduledFuture<*>?=null
    private var scheduleTaskExecutor: ScheduledExecutorService?=null


    //listener to changes when the timer interval is time out
    private var currentCalendar: MutableLiveData<Calendar>?=null

    fun startCurrentCalendarListener(): LiveData<Calendar>? {

        if (currentCalendar == null) {
            currentCalendar=MutableLiveData()
        }

        return currentCalendar
    }


    fun startTimer(){
        //if there is already at least one alarm ,it is not necessary to initial the timer
        if(scheduleTaskExecutor==null
            || (scheduleTaskExecutor?.isShutdown!=null && scheduleTaskExecutor?.isShutdown!!)
        ) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1)
            executeTimer()
        }
    }

    // execute the time
    private fun executeTimer() {
        //Log.d("testTimer", "initial timer")

        // This schedule a task to run every 1 second:
        scheduleTaskExecutor?.scheduleAtFixedRate({
            Log.d("testTimer", "tick")
            try {
                this.currentCalendar?.postValue(Calendar.getInstance())
            } catch (ex: Exception) {
                Log.d("testTimer", "exception:" + ex.message)
            }

        }, 0, 1, TimeUnit.SECONDS)
    }

    //shut down the timer
    fun shutDownTimer(){
        Log.d("testTimer", "shutDownTimer")
        scheduleTaskExecutor?.shutdownNow()
    }
}