package com.example.myapplication.Customer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.example.myapplication.R
import com.example.myapplication.WelcomeActivity

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityCustomerMapBinding
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CustomerMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener{

    private lateinit var googleApiClient : GoogleApiClient
    private lateinit var lastLocation : Location
    private lateinit var locationRequest: LocationRequest
    var radius : Int = 1

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCustomerMapBinding

    private lateinit var customer_logout_btn : Button
    private lateinit var customer_setting_btn : Button
    private lateinit var call_a_car_btn : Button
    private lateinit var customerID : String
    private lateinit var CustomerPickUpLocation : LatLng
    private var driverFound :Boolean = false
    private var requestType  = false
    private lateinit var driverFoundId : String


    private lateinit var mAuth : FirebaseAuth
    private lateinit var currentUser : FirebaseUser
    var DriverMarker : Marker? = null
    private lateinit var CustomerDatabaseRef : DatabaseReference
    private lateinit var DriverAvailableRef : DatabaseReference
    private lateinit var DriverRef : DatabaseReference
    private lateinit var   DriverLocationRef: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerMapBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        customerID = FirebaseAuth.getInstance().currentUser!!.uid
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests")
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available")

        //When the driver confirms customer request then that driver goes in Drivers working node
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working")


        customer_logout_btn = findViewById(R.id.customer_logout_btn)
        customer_setting_btn = findViewById(R.id.customer_settings_btn)
        call_a_car_btn = findViewById(R.id.customers_callcab_btn)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        customer_logout_btn.setOnClickListener {


            mAuth.signOut()
            LogoutCustomer()
        }

        call_a_car_btn.setOnClickListener {

            if(requestType){

            }
            else{
                requestType = true

                var geoFire = GeoFire(CustomerDatabaseRef)
                geoFire.setLocation(
                    customerID,
                    GeoLocation(lastLocation.latitude, lastLocation.longitude)
                )

                CustomerPickUpLocation = LatLng(lastLocation.latitude, lastLocation.longitude)

                // marker on customer location

                val customerMarkerBitmap = BitmapFactory.decodeResource(resources, R.drawable.customer_marker)
                val desiredWidth = 120
                val desiredHeight = 120

                val scaledMarkerBitmap = Bitmap.createScaledBitmap(customerMarkerBitmap, desiredWidth, desiredHeight, false)
                mMap.addMarker(
                    MarkerOptions()
                        .position(CustomerPickUpLocation)
                        .title("PickUp Customer From Here")
                        .icon(BitmapDescriptorFactory.fromBitmap(scaledMarkerBitmap))
                )
                //Change the text of btn
                call_a_car_btn.text = "Searching For Driver"

                // Get Closest Driver
                GetClosestDriverCab()
            }


        }

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

        buildGoogleApiClient()
        mMap.isMyLocationEnabled = true
    }



    override fun onConnected(p0: Bundle?) {

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
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,this)
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {

        lastLocation = location


        val latlng : LatLng = LatLng(location.latitude,location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13f))



    }

    private fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()


    }

    private fun LogoutCustomer() {


        var WelcomeIntent = Intent(this, WelcomeActivity::class.java)
        WelcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(WelcomeIntent)
        //kill this driver map activity
        finish()
    }



    private fun GetClosestDriverCab() {

        var geoFire = GeoFire(DriverAvailableRef)

        val geoQuery = geoFire.queryAtLocation(
            GeoLocation(CustomerPickUpLocation.latitude,CustomerPickUpLocation.longitude),
            radius.toDouble()
        )

        geoQuery.removeAllListeners()


        geoQuery.addGeoQueryEventListener(object: GeoQueryEventListener {

            override fun onKeyEntered(key: String?, location: GeoLocation?) {

                if(!driverFound)
                {
                    driverFound = true;
                    driverFoundId = key.toString();

                    DriverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child("Drivers")
                        .child(driverFoundId)
                    val driverMap = mutableMapOf<String ,String>()
                    driverMap.put("CustomerRideID",customerID)
                    DriverRef.updateChildren(driverMap as Map<String, Any>)

                    //show location of driver to customer
                    GettingDriverLocation();
                    //Change text in button
                    call_a_car_btn.text = "Looking for Driver Location..."

                }

            }

            override fun onKeyExited(key: String?) {

            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {

            }

            override fun onGeoQueryReady() {

                if(!driverFound){
                    radius = radius +1
                    GetClosestDriverCab()
                }
            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }

        })


    }

    private fun GettingDriverLocation() {

        DriverLocationRef.child(driverFoundId).child("l")
            .addValueEventListener(object  : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists())
                    {
                        val driverLocationMap : List<Object> = snapshot.getValue() as List<Object>
                        var LocationLat : Double = 0.0
                        var LocationLng : Double = 0.0

                        call_a_car_btn.text = "Driver Found"

                        //Check latitude and longitude in not null
                        if(driverLocationMap.get(0) != null)
                        {
                            LocationLat = driverLocationMap.get(0).toString().toDouble()
                        }
                        if(driverLocationMap.get(1) != null)
                        {
                            LocationLng = driverLocationMap.get(1).toString().toDouble()
                        }


                        // Marker for Driver's Location
                        var DriverLatLng : LatLng = LatLng(LocationLat, LocationLng)

                        //If any problem occur in driver's phone (ex - due to battery ) so notify customer to search for new driver
                        if(DriverMarker !=null){
                            DriverMarker?.remove()
                        }

                        //show distance of driver from customerr location  // location2 for driver //location1 for customer
                        val location2 : Location = Location("")
                        location2.latitude = DriverLatLng.latitude
                        location2.longitude = DriverLatLng.longitude

                        val location1 : Location = Location("")
                        location1.latitude = CustomerPickUpLocation.latitude
                        location1.longitude = CustomerPickUpLocation.longitude


                        var Distance : Float = location1.distanceTo(location2)
                        call_a_car_btn.text = "Driver At $Distance"


//                        val customerMarkerBitmap = BitmapFactory.decodeResource(resources, R.drawable.customer_marker
//                        )
//                        val desiredWidth = 120
//                        val desiredHeight = 120
//
//                        val scaledMarkerBitmap = Bitmap.createScaledBitmap(customerMarkerBitmap, desiredWidth, desiredHeight, false)
//

                        DriverMarker =    mMap.addMarker(
                            MarkerOptions()
                                .position(DriverLatLng)
                                .title("Your Driver is here")
                        )


                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


}