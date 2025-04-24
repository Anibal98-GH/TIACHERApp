package com.example.tiacher

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tiacher.helper.AuthenticationHelper

class LoginActivity : AppCompatActivity() {

    lateinit var etMail: EditText
    lateinit var etPassword: EditText
    lateinit var buttonLogin: Button
    lateinit var tvToReg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        etMail = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        tvToReg = findViewById(R.id.textViewToRegister)

        tvToReg.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            if (etMail.text.isNotEmpty() && etPassword.text.isNotEmpty() && etMail.text.endsWith("educa.jcyl.es")) {
                val mail = etMail.text.toString()
                val password = etPassword.text.toString()
                AuthenticationHelper.login(this, mail, password) { isLogged, accessRol ->
                    if (isLogged && accessRol > 0) {
                        val intent = Intent(this, qrActivity::class.java)
                        startActivity(intent)
                    } else if (accessRol <= 0) {
                        Toast.makeText(
                            this,
                            "No tienes permiso para acceder a la aplicación, contacta con un administrador para que te proporcione acceso.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } else {
                Toast.makeText(this, "Credenciales incorrectas.", Toast.LENGTH_LONG).show()
            }

        }
    }
}