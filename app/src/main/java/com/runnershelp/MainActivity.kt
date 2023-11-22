package com.runnershelp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationsRecorder
    private lateinit var paceAdapter: PaceAdapter

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val permissionList = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
        for (i in permissionList.indices){
            if (ActivityCompat.checkSelfPermission(this, permissionList[i]) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, permissionList, 1)
            }
        }

        //Location Services
        paceAdapter = PaceAdapter(mutableListOf())
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationsRecorder(paceAdapter)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view) // Set the view of the main activity to the main view

        //Initializing main_view components
        val rvPaceItems = findViewById<RecyclerView>(R.id.rvPaceTimings)
        val etBPM = findViewById<EditText>(R.id.etBPM)
        val btnVib = findViewById<Button>(R.id.btnVibrate)
        val btnRunning = findViewById<Button>(R.id.btnRun)
        val btnSavePace = findViewById<Button>(R.id.btnSavePace)
        val btnSendData = findViewById<Button>(R.id.btnSendData)
        val btnDelete = findViewById<ImageButton>(R.id.btnDelete)
        var runIsClicked = true
        var vibIsClicked = true

        //Pace View things
        rvPaceItems.adapter = paceAdapter
        rvPaceItems.layoutManager = LinearLayoutManager(this)

        //functions for Vibration
        fun startVibrate (){
            //Service Components
            val startIntent = Intent(this, VibrationService::class.java)
            val bpm :Int = etBPM.text.toString().toInt()
            startIntent.action = "startVibration" //Set a Start Signal
            startIntent.putExtra("bpm", bpm) //Set bpm
            startForegroundService(startIntent) // Send Signal
        }

        fun stopVibrate (){
            //Service Component
            val stopIntent = Intent(this, VibrationService::class.java)
            stopIntent.action = "stopVibration" // Set a Stop Signal
            startForegroundService(stopIntent) //Send Signal
        }

        //btn for vibration
        btnVib.setOnClickListener {
            if (vibIsClicked) {
                btnVib.setBackgroundResource(R.drawable.btn_stop_vib)
                btnVib.text = getString(R.string.stop_vibrate)
                startVibrate()
            } else {
                btnVib.setBackgroundResource(R.drawable.btn_start_vib)
                btnVib.text = getString(R.string.start_vibrate)
                stopVibrate()
            }
            vibIsClicked = !vibIsClicked
        }

        //btn for location
        btnRunning.setOnClickListener {
            for (i in permissionList.indices){
                if (ActivityCompat.checkSelfPermission(this, permissionList[i]) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, permissionList, 1)
                }
            }
            if (runIsClicked) {
                btnRunning.setBackgroundResource(R.drawable.btn_stop_vib)
                btnRunning.text = getString(R.string.stop_run)
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        10000, //(10 sec) Duration in ms
                        0f, //Distance
                        locationListener
                    )
                }
            } else {
                btnRunning.setBackgroundResource(R.drawable.btn_start_vib)
                btnRunning.text = getString(R.string.start_run )
                locationManager.removeUpdates(locationListener)
            }
            runIsClicked = !runIsClicked
        }

        btnSavePace.setOnClickListener {
            savePaceAlertDialog()
        }

        btnSendData.setOnClickListener {
            val fileHelper = FileHelper()
            fileHelper.compressCsvFiles(applicationContext.filesDir.toString())
            startFileShareIntent(applicationContext.filesDir.toString() + "/compressed.zip")
        }

        btnDelete.setOnClickListener {
            if (paceAdapter.empty()){
                Toast.makeText(this, "No Pace Data to delete", Toast.LENGTH_SHORT).show()
            } else {
                deletePaceAlertDialog()
            }
        }
    }

    private fun deletePaceAlertDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Pace Data?")
        builder.setPositiveButton("Yes"){dialog, _ ->
            paceAdapter.deletePace()
            Toast.makeText(this, "Deleted Pace Current Pace Data", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("No") {dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun savePaceAlertDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Type of Run and Name of Run")
        val options = arrayOf("Silence and No Vibration", "Vibration Only", "Music Only", "Vibration and Music")
        val input = layoutInflater.inflate(R.layout.dialog_input, null)
        val editText = input.findViewById<EditText>(R.id.editText)
        var type :String = null.toString()
        builder.setView(input)

        builder.setSingleChoiceItems(options, -1 ) { dialogInterface: DialogInterface, i: Int ->
            type = when (i) {
                0 -> "_NoMusNoVib"
                1 -> "_NoMusYeVib"
                2 -> "_YeMusNoVib"
                3 -> "_YeMusYeVib"
                else -> null.toString()
            }
        }

        builder.setPositiveButton("OK") { dialog, _ ->
            if (type == null.toString()){
                Toast.makeText(this, "Please Select a Type of Run", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }
            val fileName = editText.text.toString().trim() + type
            if (fileName.isNotEmpty()) {
                makeFile(fileName, locationListener.getPacesSeconds())
                Log.d("Path to file", applicationContext.filesDir.toString())
            } else {
                makeFile("Pace_data", locationListener.getPacesSeconds())
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            // Handle cancellation if needed
        }

        builder.show()
    }

    private fun makeFile(filename:String, paces: MutableList<Pair<Int,Double>>){
        val HEADER = "PACE_NUM,PACE_IN_SECONDS"

        val newFilename = "$filename.csv"
        try {
            val fileOut: FileOutputStream = openFileOutput(newFilename, Context.MODE_PRIVATE)

            fileOut.write(HEADER.toByteArray())
            fileOut.write("\n".toByteArray())
            for (i in paces.indices){
                val text = paces[i].first.toString()
                val num = paces[i].second.toString()
                fileOut.write("$text,$num".toByteArray())
                fileOut.write("\n".toByteArray())
            }
            fileOut.close()

        } catch (_: Exception) {}
    }

    private fun startFileShareIntent(filePath: String) { // pass the file path where the actual file is located.
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"  // "*/*" will accepts all types of files, if you want specific then change it on your need.
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Sharing file from the Runner's Help"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "All the pace Data that has been collected"
            )
            val fileURI = FileProvider.getUriForFile(
                applicationContext, applicationContext.packageName + ".provider",
                File(filePath)
            )
            putExtra(Intent.EXTRA_STREAM, fileURI)
        }
        startActivity(shareIntent)
    }
}