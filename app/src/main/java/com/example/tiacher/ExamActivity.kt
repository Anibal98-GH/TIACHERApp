package com.example.tiacher

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.tiacher.helper.AuthenticationHelper
import com.example.tiacher.helper.DatabaseHelper
import java.io.ByteArrayOutputStream
import java.io.File

class ExamActivity : AppCompatActivity() {


    private lateinit var buttonLogout: Button
    private lateinit var tvTitulo: TextView
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonCamera: ImageView
    private lateinit var buttonCorregir: Button
    private lateinit var uuid: String
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    private lateinit var photoUri: Uri
    private var imageB64: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_plantilla)


        buttonLogout = findViewById(R.id.buttonLogout2)
        tvTitulo = findViewById(R.id.textViewTitulo)
        buttonBack = findViewById(R.id.imageButtonBack)
        buttonCamera = findViewById(R.id.imageViewCamera)
        buttonCorregir = findViewById(R.id.buttonCorregir)


        tvTitulo.text = intent.getStringExtra("titulo")
        uuid = intent.getStringExtra("uuid").toString()

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    try {
                        val inputStream = contentResolver.openInputStream(photoUri)
                        val original = BitmapFactory.decodeStream(inputStream)

                        val matrix = Matrix().apply { postRotate(90f) }

                        val rotated = Bitmap.createBitmap(
                            original, 0, 0,
                            original.width, original.height,
                            matrix, true
                        )
                        buttonCamera.setImageBitmap(rotated)
                        imageB64 = bitmapToBase64(rotated)

                    } catch (e: Exception) {
                        Log.e("Camera", "Error loading image: ${e.message}")
                        Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Foto cancelada", Toast.LENGTH_SHORT).show()
                }
            }


        buttonLogout.setOnClickListener {
            AuthenticationHelper.closeSesion(this)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, qrActivity::class.java)
            startActivity(intent)
        }

        buttonCamera.setOnClickListener {
            takePicture()
        }

        buttonCorregir.setOnClickListener {
            if (!imageB64.isNullOrEmpty()) {
                DatabaseHelper.sendPictureToServer(
                    this,
                    imageB64!!,
                    uuid
                ) { _, correccionResponse ->
                    val dialogCorreccion = Dialog(this)
                    dialogCorreccion.setContentView(R.layout.dialog_results)
                    dialogCorreccion.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    dialogCorreccion.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    dialogCorreccion.setCancelable(false)
                    dialogCorreccion.show()

                    val validAnswer =
                        dialogCorreccion.findViewById<TextView>(R.id.TextViewCorrectas)
                    val failedAnswer = dialogCorreccion.findViewById<TextView>(R.id.TextViewFallos)
                    val notAnswered = dialogCorreccion.findViewById<TextView>(R.id.TextViewNC)
                    val gradeTV = dialogCorreccion.findViewById<TextView>(R.id.TextViewNota)
                    val buttonCloseDialog =
                        dialogCorreccion.findViewById<Button>(R.id.buttonAceptar)
                    val buttonSeeDetails =
                        dialogCorreccion.findViewById<Button>(R.id.buttonVerDetalles)
                    validAnswer.text =
                        getString(R.string.correctas_dialog, correccionResponse.valid.toString())
                    failedAnswer.text =
                        getString(R.string.fallos_dialog, correccionResponse.fail.toString())
                    notAnswered.text =
                        getString(R.string.sin_contestar_dialog, correccionResponse.nc.toString())
                    gradeTV.text =
                        getString(R.string.nota_dialog, correccionResponse.grade.toString())

                    buttonCloseDialog.setOnClickListener {
                        dialogCorreccion.dismiss()
                    }

                    buttonSeeDetails.setOnClickListener {
                        val intent = Intent(this, DetailsActivity::class.java)
                        intent.putExtra("correccion", correccionResponse)
                        Log.d("Intent", correccionResponse.toString())
                        startActivity(intent)
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Saca una foto a la plantilla antes de intentar corregir el examen.",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }
    }

    private fun takePicture() {
        val imagesDir = File(cacheDir, "images")
        imagesDir.mkdirs()

        val photoFile = File(imagesDir, "exam_photo_${System.currentTimeMillis()}.jpg")

        photoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        cameraLauncher.launch(intent)
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

}