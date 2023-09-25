package com.example.myapplication.Driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.myapplication.R
import com.example.myapplication.WelcomeActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityDriverMapBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DriverMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDriverMapBinding

    private lateinit var googleApiClient : GoogleApiClient
    private lateinit var lastLocation : Location
    private lateinit var locationRequest: LocationRequest

    private lateinit var LogoutDriverButton : Button
    private lateinit var SettingDriverButton : Button
    private lateinit var mAuth : FirebaseAuth
    private lateinit var currentUser : FirebaseUser
    private  var currentLogoutDriverStatus : Boolean = false
    private lateinit var AssignedCustomerRef : DatabaseReference
    private lateinit var AssignedCustomerPickUpRef : DatabaseReference
    private lateinit var driverID : String
    private  var customerID : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriverMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        driverID = mAuth.currentUser!!.uid

        LogoutDriverButton  = findViewById(R.id.driver_logout_btn)
        SettingDriverButton = findViewById(R.id.driver_settings_btn)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        //Logout Click
        LogoutDriverButton.setOnClickListener {

            currentLogoutDriverStatus = true
            DisconnectDriver()
            mAuth.signOut()

            LogoutDriver()


        }

        // To get the customer Requeste
        GetAssignedCustomerRequest()


    }


    private fun GetAssignedCustomerRequest() {

        AssignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users")
            .child("Drivers")
            .child(driverID).child("CustomerRideID")
        AssignedCustomerRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    customerID = snapshot.value.toString()

                    GetAssignedCustomerPickUpLocation()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    private fun GetAssignedCustomerPickUpLocation() {

        AssignedCustomerPickUpRef = FirebaseDatabase.getInstance().getReference()
            .child("Customer Requests")
            .child(customerID)
            .child("l")

        AssignedCustomerPickUpRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists())
                {
                    val  customerLocationMap : List<Object> = snapshot.getValue() as List<Object>

                    var LocationLat : Double = 0.0
                    var LocationLng : Double = 0.0

                    //Check latitude and longitude in not null
                    if(customerLocationMap.get(0) != null)
                    {
                        LocationLat = customerLocationMap.get(0).toString() as Double
                    }
                    if(customerLocationMap.get(1) != null)
                    {
                        LocationLng = customerLocationMap.get(1).toString() as Double
                    }

                    var DriverLatLng : LatLng = LatLng(LocationLat, LocationLng)

                    mMap.addMarker(MarkerOptions().position(DriverLatLng).title("Pick Up Location"))!!

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        buildGoogleApiClient()


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true


    }


    override fun onConnected(p0: Bundle?) {
        // request for location and update location
        locationRequest = LocationRequest()
        locationRequest.setInterval(1000)
        locationRequest.setFastestInterval(1000)
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY




        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        //if this gives error add permission check above code.
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,this)

    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {

        if(applicationContext !=null){
            lastLocation = location



            val latlng : LatLng = LatLng(location.latitude, location.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13f))


            //save location to firebase so customer can see the driver

            var userID : String? = FirebaseAuth.getInstance().currentUser?.uid
            var DriverAvailibilityRef : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("Drivers Available")

            //get latitude and longitude of driver location
            var geoFireAvailibility : GeoFire = GeoFire(DriverAvailibilityRef)

            // -> Now for driver working
            var DriverWorkingRef : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("Drivers Working")
            var geoFireWorking : GeoFire = GeoFire(DriverWorkingRef)



            when(customerID){

                "" -> {
                    geoFireWorking.removeLocation(userID)
                    geoFireAvailibility.setLocation(
                        userID,
                        GeoLocation(location.latitude, location.longitude)
                    )
                }
                else ->{
                    geoFireAvailibility.removeLocation(userID)
                    geoFireWorking.setLocation(userID, GeoLocation(location.latitude, location.longitude))
                }




            }

        }

    }

    protected fun buildGoogleApiClient(){
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()

        if(!currentLogoutDriverStatus){
            DisconnectDriver()
        }

    }

    private fun DisconnectDriver() {

        var userID : String? = FirebaseAuth.getInstance().currentUser?.uid
        //parnet ref for driver availability
        var DriverAvailibilityRef : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("Drivers Available")

        //get latitude and longitude of driver location
        var geoFire : GeoFire = GeoFire(DriverAvailibilityRef)
        geoFire.removeLocation(userID)
    }

    private fun LogoutDriver() {


        var WelcomeIntent = Intent(this, WelcomeActivity::class.java)
        WelcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(WelcomeIntent)
        //kill this driver map activity
        finish()

    }
}