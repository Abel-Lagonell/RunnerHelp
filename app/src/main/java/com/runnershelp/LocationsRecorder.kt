package com.runnershelp

import android.location.Location
import android.location.LocationListener
import android.util.Log

class LocationsRecorder(
    private val paceAdapter: PaceAdapter
) : LocationListener {
    private var longitude:Double = 0.0
    private var startLng:Double? = null
    private var latitude:Double = 0.0
    private var startLat: Double? = null
    private val size = 3
    private var distance = Array(size) { -1.0 }
    private val totalDistance = mutableListOf<Double>()

    override fun onLocationChanged(location: Location) {
        longitude = location.longitude
        latitude = location.latitude
        recordDistance(latitude, longitude)
    }

    private fun recordDistance(lat:Double, lng:Double) {
        //If there are no starting points make one
        if (startLat == null && startLng == null) {
            startLng = lng
            startLat = lat
            return
        }
        //Holder of the distanceBetween results
        val distanceBet= FloatArray(3)
        //Calculate two points
        startLat?.let { startLng?.let { it1 ->
            Location.distanceBetween(it, it1, lat, lng, distanceBet)
        } }
        startLat = lat
        startLng = lng
        //Add it to an array
        if (addToArray(distance, distanceBet[0], size) == -1) { //Catch Completion Error
            addToPaceList()
            distance = Array(size){-1.0}
            addToArray(distance, distanceBet[0], size)
        }
    }

    private fun addToArray(arr:Array<Double>, e:Float, size:Int): Int {
        var i = 0
        while (i<size){
            if (arr[i] == -1.0){
                arr[i] = e.toDouble()
                return 0
            }
            i++
        }
        return -1 //throw Error of completion
    }

    private fun addToPaceList(){
        totalDistance.add(distance.sum())
        val pace = Pace(
            "Pace time " + totalDistance.size.toString() + ":",
            calculatePace() //"min:sec"/km
        )
        paceAdapter.addPace(pace)
    }

    private fun calculatePace(): String {
        val timeTotalSeconds: Int = if (totalDistance.size == 1) {
            (size +1)* 10 //seconds total for the first iteration
        } else {
            size * 10 //seconds total
        }

        val kilometersCovered = totalDistance[totalDistance.size - 1] / 1000 //kilometers covered

        val paceInSeconds = timeTotalSeconds / kilometersCovered
        val timeMinutes = (paceInSeconds / 60).toInt() //Minutes part
        val timeSeconds = kotlin.math.round((paceInSeconds / 60.0 - timeMinutes) * 100).toInt() //Seconds Part

        if (kilometersCovered == 0.0){
            return "0:0"
        }
        if (timeSeconds<10){
            return "$timeMinutes:0$timeSeconds"
        }
        return "$timeMinutes:$timeSeconds"
    }

    fun getPacesSeconds(): MutableList<Pair<Int, Double>> {

        val pacesSeconds = mutableListOf<Pair<Int, Double>>()
        var timeTotalSeconds: Int

        for (i in 0 until totalDistance.size) {
            timeTotalSeconds = if (i == 0) {
                (size +1)* 10 //seconds total for the first iteration
            } else {
                size * 10 //seconds total
            }
            val kilometersCovered = totalDistance[i] / 1000 //kilometers covered
            var temp = 0.0 //Pace in seconds
            if (kilometersCovered !=0.0) { temp = timeTotalSeconds / kilometersCovered }
            pacesSeconds.add(Pair(i,temp))
        }

        return pacesSeconds
    }

    fun clearDistances(){
        totalDistance.clear()
    }
}