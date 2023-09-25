package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.myapplication.Customer.CustomerLoginRegisterActivity
import com.example.myapplication.Driver.DriverLoginRegisterActivity
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WelcomeActivity : AppCompatActivity() {

    lateinit var driverBtn : Button
    lateinit var customerBtn : Button
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        driverBtn = findViewById(R.id.driverbtn)
        customerBtn = findViewById(R.id.customerbtn)


        driverBtn.setOnClickListener {
            startActivity(Intent(this, DriverLoginRegisterActivity::class.java))
        }

        customerBtn.setOnClickListener {
            startActivity(Intent(this, CustomerLoginRegisterActivity::class.java))
        }


//        Check location permission

        if (checkLocationPermission()) {
            // You have permission, you can access the location here
            // Example: startLocationUpdates()
        } else {
            // Request permission
            requestLocationPermission()
        }

    }

    private fun checkLocationPermission(): Boolean {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        return (ContextCompat.checkSelfPermission(this, fineLocationPermission) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, coarseLocationPermission) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can access the location now
                // Example: startLocationUpdates()
            } else {
                // Permission denied
                // You may want to show a message or take appropriate action here
            }
        }
    }
}