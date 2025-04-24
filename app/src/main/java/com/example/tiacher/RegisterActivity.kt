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


class RegisterActivity : AppCompatActivity() {
    lateinit var etNombre: EditText
    lateinit var etMail: EditText
    lateinit var etPassword: EditText
    lateinit var etRepPassword: EditText
    lateinit var buttonRegister: Button
    lateinit var tvToLogin: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        etNombre = findViewById(R.id.etNombre)
        etMail = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPasswordReg)
        etRepPassword = findViewById(R.id.etRePasswordReg)
        buttonRegister = findViewById(R.id.buttonRegister)
        tvToLogin = findViewById(R.id.textViewToLogin)

        buttonRegister.setOnClickListener {
            val mail = etMail.text.toString()
            val password = etPassword.text.toString()
            val repPassword = etRepPassword.text.toString()
            val nombre = etNombre.text.toString()

            if (!repPassword.equals(password)) {
                Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (mail.isEmpty() || password.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!mail.endsWith("@educa.jcyl.es")) {
                Toast.makeText(this, "Introduce un email válido.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            AuthenticationHelper.register(nombre, mail, password) { isRegistered ->
                if (isRegistered) {
                    AuthenticationHelper.login(this, mail, password) { isLogged, accessRol ->
                        if (isLogged && accessRol > 0) {
                            val intent = Intent(this, qrActivity::class.java)
                            startActivity(intent)
                        } else if (accessRol <= 0) {
                            Toast.makeText(
                                this,
                                "Espera a que un administrador te active la cuenta.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Error registrando al usuario, inténtalo de nuevo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        tvToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}