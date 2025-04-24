package com.example.tiacher.helper

import android.content.Context
import android.util.Log
import com.example.tiacher.dataclass.CorreccionResponse
import com.example.tiacher.dataclass.ExamExistResponse
import com.example.tiacher.helper.AuthenticationHelper.loginResponse
import com.example.tiacher.helper.AuthenticationHelper.refreshToken
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import org.json.JSONObject

object DatabaseHelper {

    private const val tagDB = "RegisterDebug"
    val HOST = "http://192.168.1.137:5000"
    var identificador = ""

    fun checkExamExist(
        context: Context,
        uuid: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        refreshToken(context)
        val url = HOST + "/api/exam/${uuid}"
        val headers = mapOf(
            "Authorization" to "Bearer ${loginResponse?.token}"
        )

        url.httpGet()
            .header(headers)
            .responseString { _, response, result ->
                if (response.isSuccessful) {
                    val responseBody = result.get()
                    val gson = Gson()
                    val respuestaExamen =
                        gson.fromJson(responseBody, ExamExistResponse::class.java)
                    Log.d("ExisteExamen", respuestaExamen.toString())
                    callback(true, respuestaExamen.titulo!!, "El examen existe")
                } else {
                    callback(false, null.toString(), "El examen no existe")
                }

            }
    }

    fun sendPictureToServer(
        context: Context,
        base64Image: String,
        callback: (Boolean, CorreccionResponse) -> Unit
    ) {
        refreshToken(context)
        val url = HOST + "/api/exam/result"
        val headers = mapOf(
            "Authorization" to "Bearer ${loginResponse?.token}"
        )
        val json = JSONObject().apply {
            put("image", base64Image)
        }
        val json_data = json.toString()
        url.httpPost()
            .header(headers)
            .jsonBody(json_data)
            .responseString { _, response, result ->
                when (result) {
                    is Result.Success -> {
                        val responseBody = result.get()
                        val gson = Gson()
                        val correccionExamen =
                            gson.fromJson(responseBody, CorreccionResponse::class.java)
                        Log.d("ExisteExamen", correccionExamen.toString())
                        callback(true, correccionExamen)
                    }

                    is Result.Failure -> {
                        val errorBody = result.error.response.body().asString("application/json")
                        try {
                            val gson = Gson()
                            val correccionExamen =
                                gson.fromJson(errorBody, CorreccionResponse::class.java)
                            Log.e(
                                "Correccion",
                                "Error: ${result.error.message}, Response: $correccionExamen"
                            )
                            callback(false, correccionExamen)
                        } catch (e: Exception) {
                            Log.e("Correccion", "Error parsing response: ${e.message}")
                            callback(
                                false,
                                CorreccionResponse(valid = 0, fail = 0, nc = 0, grade = 0)
                            ) // Send empty response or create an error response
                        }
                    }
                }
            }
    }
}