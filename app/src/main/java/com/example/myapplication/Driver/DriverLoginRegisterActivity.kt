package com.example.myapplication.Driver

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DriverLoginRegisterActivity : AppCompatActivity() {

    lateinit var driverLoginTv : TextView
    lateinit var driverRegisterTv: TextView
    lateinit var driverStatus : TextView
    lateinit var driverRegisterLinkTv : TextView
    lateinit var etEmail : EditText
    lateinit var etPassword : EditText
    private lateinit var loadingBar : ProgressDialog

    //FIREBASE CREDENTIALS
    private lateinit var mAuth : FirebaseAuth
    private lateinit var DriverDatabaseRef : DatabaseReference
    private lateinit var onlineDriverID : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login_register)



        driverLoginTv = findViewById(R.id.driver_login_txt)
        driverRegisterTv = findViewById(R.id.driver_register_txt)
        driverStatus = findViewById(R.id.driver_status)
        driverRegisterLinkTv = findViewById(R.id.driver_account)
        etEmail = findViewById(R.id.et_driver_login_email)
        etPassword = findViewById(R.id.et_driver_password)
        loadingBar = ProgressDialog(this)

        mAuth = FirebaseAuth.getInstance()

        //HIDE REGISTER BTN
        // driverRegisterTv.visibility = View.INVISIBLE
        // driverRegisterTv.isEnabled = false

        //CLICK LISTENER ON DONT HAVE ACCOUNT
        driverRegisterLinkTv.setOnClickListener {

            driverLoginTv.visibility = View.INVISIBLE
            // driverRegisterLinkTv.visibility = View.INVISIBLE
            driverStatus.text = "Register Driver"

            driverRegisterTv.visibility = View.VISIBLE
            driverRegisterTv.isEnabled = true
        }



        //REGISTER THE  NEW DRIVER
        driverRegisterTv.setOnClickListener {

            var email = etEmail.text.toString()
            var password = etPassword.text.toString()

            RegisterDriver(email, password)
        }

        //LOGIN FOR DRIVERS
        driverLoginTv.setOnClickListener {

            var email = etEmail.text.toString()
            var password = etPassword.text.toString()

            SignInDriver(email, password)
        }


    }



    //FUNCTION FOR LOGIN DRIVERS
    private fun SignInDriver(email: String, password: String) {
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email or password", Toast.LENGTH_SHORT).show()
        }
        else
        {
            loadingBar.setTitle("Signing In")
            loadingBar.setMessage("Please wait , verifyig driver")
            loadingBar.show()

            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(it.isSuccessful){

                        //CHANGE ACTIVITY FROM THIS TO MAP ACTIVITY
                        startActivity(Intent(this,DriverMapActivity::class.java))

                        Toast.makeText(this, "Now you can search your Customer", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()


                    }else{
                        Toast.makeText(this, "Sign In  Failed 😒, Try Again...", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()
                    }
                }
        }

    }

    //TO REGISTER DRIVER ON FIREBASE
    private fun RegisterDriver(email: String, password: String) {
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email or password", Toast.LENGTH_SHORT).show()
        }
        else {

            loadingBar.setTitle("Registration")
            loadingBar.setMessage("Please wait . registring driver")
            loadingBar.show()

            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(it.isSuccessful){

                        onlineDriverID = mAuth.currentUser!!.uid
                        DriverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(onlineDriverID)



                        DriverDatabaseRef.setValue(true)

                        //send driver to driver map activity
                        startActivity(Intent(this,DriverMapActivity::class.java))

                        Toast.makeText(this, "Congratulations !! you are a cab driver now !!😊", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()

                        //CHANGE ACTIVITY FROM THIS TO MAP ACTIVITY
                        startActivity(Intent(this,DriverMapActivity::class.java))
                    }else{
                        Toast.makeText(this, "Registeration Failed 😒", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()
                    }
                }
        }
    }



}