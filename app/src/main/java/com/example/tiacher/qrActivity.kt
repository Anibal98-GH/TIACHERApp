package com.example.tiacher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tiacher.helper.AuthenticationHelper
import com.example.tiacher.helper.DatabaseHelper
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class qrActivity : AppCompatActivity() {

    private lateinit var scannerIcon: ImageView
    private lateinit var etUUID: EditText
    private lateinit var buttonLogout: Button
    private lateinit var buttonNext: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qr)

        scannerIcon = findViewById(R.id.imageViewQR)
        etUUID = findViewById(R.id.etQR)
        buttonLogout = findViewById(R.id.buttonLogout)
        buttonNext = findViewById(R.id.buttonSiguiente)


        scannerIcon.setOnClickListener {
            scannerLauncher.launch(
                ScanOptions().setPrompt("Escanea el codigo QR")
                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            )
        }

        buttonLogout.setOnClickListener {
            AuthenticationHelper.closeSesion(this)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        buttonNext.setOnClickListener {
            //Comparar si existe UUID del examen, si existe enviar titulo y uuid al intent siguiente
            if (etUUID.text.isEmpty()) {
                Toast.makeText(this, "Rellena el campo del identificador", Toast.LENGTH_SHORT)
                    .show()
            } else {
                DatabaseHelper.checkExamExist(
                    this,
                    etUUID.text.toString()
                ) { exist, titulo, message ->

                    if (exist) {
                        val intent = Intent(this, ExamActivity::class.java)
                        intent.putExtra("uuid", etUUID.text.toString())
                        intent.putExtra("titulo", titulo)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }


                }

            }


        }


    }

    private val scannerLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
        } else {
            Log.e("QR", "Código qr escaneado: " + result.contents)
            etUUID.text.clear()
            DatabaseHelper.identificador = result.contents
            etUUID.setText(result.contents)
        }


    }
}