package com.example.tiacher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tiacher.helper.AuthenticationHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        AuthenticationHelper.startApp(this) { valid, message ->
            if (valid) {
                val intent = Intent(this, qrActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            Log.d("LoginDebug", message)
        }

    }

}