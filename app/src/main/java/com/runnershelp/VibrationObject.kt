package com.runnershelp

import android.util.Log
import kotlin.math.log

data class VibrationObject (
    val bpm:Int,
    val measure:Int = 4,
    val note:Int = 4
    ) {
    private val timeBPM = 60000 / bpm.toLong()
    private val timeMS = timeBPM / (note/4)
    private val timings: LongArray = LongArray(measure*2+1)

    private fun fillTimings(){
        val on = if(timeMS/4 > 125) 125 else timeMS/4
        val off = timeMS - on

        for (i in timings.indices) {
            if (i%2 == 0){
                timings[i] = off
            } else {
                timings[i] = on
            }
        }
    }

    fun getTimings() : LongArray{
        fillTimings()
        return timings
    }
}