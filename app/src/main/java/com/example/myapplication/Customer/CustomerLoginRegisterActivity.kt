package com.example.myapplication.Customer

import android.annotation.SuppressLint
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

class CustomerLoginRegisterActivity : AppCompatActivity() {

    lateinit var customerLoginTV : TextView
    lateinit var customerRegisterTV : TextView
    lateinit var customerRegisterLinkTv : TextView
    lateinit var customerStatusTv : TextView
    lateinit var etEmail : EditText
    lateinit var etPassword : EditText
    lateinit var loadingBar : ProgressDialog



    //FIREBASE CREDENTIALS
    private lateinit var mAuth : FirebaseAuth
    private lateinit var CustomerDatabaseRef : DatabaseReference
    private lateinit var onLineCustomerID : String



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_login_register)


        customerLoginTV = findViewById(R.id.customer_login_txt)
        customerRegisterTV  = findViewById(R.id.customer_register_txt)
        customerRegisterLinkTv = findViewById(R.id.customer_acount)
        customerStatusTv = findViewById(R.id.customer_status)
        etEmail = findViewById(R.id.et_customer_login_email)
        etPassword = findViewById(R.id.et_customer_login_password)
        loadingBar = ProgressDialog(this)

        mAuth = FirebaseAuth.getInstance()

        //Hide Register TextView
        //  customerRegisterTV.visibility = View.INVISIBLE
        //customerRegisterTV.isEnabled = false

        //Click Listner on  don't have account option
        customerRegisterLinkTv.setOnClickListener {
            customerLoginTV.visibility = View.INVISIBLE
            customerRegisterLinkTv.visibility = View.INVISIBLE
            customerStatusTv.text = "Register Customer"

            customerRegisterTV.visibility = View.VISIBLE
            customerRegisterTV.isEnabled = true
        }

        //CLICK ON REGISTER BUTTON FOR CUSTOMER
        customerRegisterTV.setOnClickListener {

            var email = etEmail.text.toString()
            var password = etPassword.text.toString()

            RegisterCustomer(email, password)
        }

        //CLICK ON CUSTOME LOGIN BUTTON
        customerLoginTV.setOnClickListener {
            var email = etEmail.text.toString()
            var password = etPassword.text.toString()

            SignInCustomer(email, password)
        }
    }


    //FUNCTION FOR CUSTOME LOGIN
    private fun SignInCustomer(email: String, password: String) {


        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "please enter email or password !!", Toast.LENGTH_SHORT).show()
        }
        else{

            loadingBar.setTitle("Signing In...")
            loadingBar.setMessage("Please wait , verifying your data")
            loadingBar.show()

            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    if(it.isSuccessful){

                        startActivity(Intent(this, CustomerMapActivity::class.java))

                        Toast.makeText(this, "Book your CAB Now", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()


                    }
                    else{
                        Toast.makeText(this, "Sign IN Failed , Try again...", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()
                    }
                }
        }
    }


    //REGISTER CUSTOMER TO FIREBASE
    private fun RegisterCustomer(email: String, password: String) {

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "please enter email or password !!", Toast.LENGTH_SHORT).show()
        }
        else{

            loadingBar.setTitle("Registiring your Data")
            loadingBar.setMessage("Please wait , while processing your data")
            loadingBar.show()

            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    if(it.isSuccessful){

                        onLineCustomerID = mAuth.currentUser!!.uid
                        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(onLineCustomerID)

                        CustomerDatabaseRef.setValue(true)

                        //sent user to customer map activity
                        startActivity(Intent(this,CustomerMapActivity::class.java))

                        Toast.makeText(this, "Congratulations , Now you can search you CAB👍", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()
                    }
                    else{
                        Toast.makeText(this, "Registration Failed , Try again 🙂", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()
                    }
                }
        }
    }
}