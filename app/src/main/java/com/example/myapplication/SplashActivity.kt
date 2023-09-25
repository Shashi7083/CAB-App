package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val runnable = Runnable(){
            try{
                Thread.sleep(2000)
            }catch (e: Exception){

            }finally {
                val intent : Intent = Intent(this,WelcomeActivity::class.java)
                startActivity(intent)
            }
        }

        val thread = Thread(runnable)
        thread.name = "Splash Thread"
        thread.start()



    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}