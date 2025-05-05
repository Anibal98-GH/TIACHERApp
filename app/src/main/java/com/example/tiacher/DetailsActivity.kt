package com.example.tiacher

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tiacher.dataclass.CorreccionResponse

class DetailsActivity : AppCompatActivity() {


    private lateinit var tvAciertos: TextView
    private lateinit var tvFallos: TextView
    private lateinit var tvNC: TextView
    private lateinit var tvNota: TextView
    private lateinit var buttonBack: ImageButton
    val TEXTSIZE = 14f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalles)
        val correccion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("correccion", CorreccionResponse::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<CorreccionResponse>("correccion")
        }

        Log.d("correccion", correccion.toString())

        tvAciertos = findViewById(R.id.textViewPonerAciertoDetalles)
        tvFallos = findViewById(R.id.textViewPonerFalloDetalles)
        tvNC = findViewById(R.id.textViewPonerNCDetalles)
        tvNota = findViewById(R.id.textViewPonerNotaDetalles)


        tvAciertos.text = correccion!!.valid.toString()
        tvFallos.text = correccion.fail.toString()
        tvNota.apply {
            text = correccion?.grade.toString()
            setTextColor(
                when {
                    (correccion.grade ?: 0.0) > 4.9 -> Color.GREEN
                    else -> Color.RED
                }
            )
        }
        tvNC.text = correccion.nc.toString()

        buttonBack = findViewById(R.id.imageButtonBackDetalles)

        buttonBack.setOnClickListener {
            finish()
        }

        val linearContainer = findViewById<LinearLayout>(R.id.linearContainer)
        correccion.data.forEach { pregunta ->

            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val tvNumero = TextView(this).apply {
                layoutParams = params
                text = pregunta.question
                gravity = Gravity.CENTER
                textSize = TEXTSIZE
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
            }

            val tvCorrecta = TextView(this).apply {
                layoutParams = params
                text = pregunta.correct ?: "-"
                gravity = Gravity.CENTER
                textSize = TEXTSIZE
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
            }

            val tvContestada = TextView(this).apply {
                layoutParams = params
                text = pregunta.answered ?: "-"
                gravity = Gravity.CENTER
                textSize = TEXTSIZE
                setTextColor(Color.BLACK)
                setTypeface(null, Typeface.BOLD)
            }

            val tvResultado = TextView(this).apply {
                layoutParams = params
                text = pregunta.result ?: "-"
                gravity = Gravity.CENTER
                textSize = TEXTSIZE
                setTextColor(
                    when (pregunta.result) {
                        "Acierto" -> Color.GREEN
                        "Fallo" -> resources.getColor(R.color.red, theme)
                        else -> Color.BLACK
                    }
                )
                setTypeface(null, Typeface.BOLD)
            }

            fila.addView(tvNumero)
            fila.addView(tvCorrecta)
            fila.addView(tvContestada)
            fila.addView(tvResultado)

            linearContainer.addView(fila)

            val divisor = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2
                )
                setBackgroundColor(Color.BLACK)
            }
            linearContainer.addView(divisor)
        }
    }


}