package com.example.tiacher.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.auth0.android.jwt.JWT
import com.example.tiacher.dataclass.LoginResponse
import com.example.tiacher.dataclass.MantenerSesionResponse
import com.example.tiacher.dataclass.RefreshTokenResponse
import com.example.tiacher.dataclass.RegisterResponse
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.isSuccessful
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import org.json.JSONObject
import java.util.Date

//Ya que no puedo hacer herencia multiple pues hago un singletone de auth
object AuthenticationHelper {
    /**
     * diferentes tags para debug
     */
    private const val tagReg = "RegisterDebug"
    private const val tagLog = "LoginDebug"
    private const val tagRef = "RefreshDebug"
    private const val KEY_RESPONSE = "loginResponse"
    private const val PREF_NAME = "UserSession"

    //loginResponse mas accesible desde otras clases
    var registerResponse: RegisterResponse? = null
    var loginResponse: LoginResponse? = null

    //Hay que cambiar host cuando se hagan las pruebas
    val HOST = "https://9aca-2a01-4f8-1c1c-7c0e-00-1.ngrok-free.app"

    /**
     * Registro del usuario
     */
    fun register(nombre: String, mail: String, password: String, callback: (Boolean) -> Unit) {
        val url = HOST + "/api/register"
        val json = JSONObject().apply {
            put("nombre", nombre)
            put("mail", mail)
            put("password", password)
        }
        val json_data = json.toString()
        url.httpPost()
            .jsonBody(json_data)
            .responseString { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        Log.e(tagReg, "Error de red: ${result.getException().message}")
                        callback(false)
                    }

                    is Result.Success -> {
                        val response = result.get()
                        val gson = Gson()
                        registerResponse = gson.fromJson(response, RegisterResponse::class.java)
                        Log.d(tagReg, "¿Registrado?: ${registerResponse?.register}")
                        callback(registerResponse?.register == true)
                    }
                }
            }
    }

    /**
     * Login del usuario
     */
    fun login(
        context: Context,
        mail: String,
        password: String,
        callback: (Boolean, Int) -> Unit
    ) {
        val url = HOST + "/api/login"
        val json = JSONObject().apply {
            put("mail", mail)
            put("password", password)
        }
        val json_data = json.toString()
        url.httpPost()
            .jsonBody(json_data)
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        Log.e(tagLog, "Error de red: ${result.getException().message}")
                        callback(false, 0)
                    }

                    is Result.Success -> {
                        val responseLogin = result.get()
                        val gson = Gson()
                        loginResponse = gson.fromJson(responseLogin, LoginResponse::class.java)
                        saveLoginResponse(context, loginResponse)
                        Log.d(tagLog, "¿Logged in?: ${loginResponse?.logged}")
                        Log.d(tagLog, "ID Usuario: ${loginResponse?.access}")
                        Log.d(tagLog, "Token: ${loginResponse?.token}")
                        callback(loginResponse?.logged!!, loginResponse?.access!!)

                    }
                }
            }
    }

    /**
     * Refrescar token si falta x tiempo para que expire
     */
    fun refreshToken(context: Context) {
        if (almostExpiredToken()) {
            val url = HOST + "/api/sesion"
            val headers = mapOf(
                "Authorization" to "Bearer ${loginResponse?.token}"
            )
            url.httpGet()
                .header(headers)
                .responseString { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            Log.e(tagRef, "Error de red: ${result.getException().message}")
                        }

                        is Result.Success -> {
                            val response = result.get()
                            val gson = Gson()
                            val refreshTokenResponse =
                                gson.fromJson(response, RefreshTokenResponse::class.java)
                            Log.d(
                                tagRef,
                                "Token antiguo: ${loginResponse?.token} \n Token nuevo ${refreshTokenResponse.token}"
                            )
                            loginResponse?.token = refreshTokenResponse.token
                            saveLoginResponse(context, loginResponse)

                        }
                    }
                }
        }
    }

    /**
     * True si expira en 5 minutos
     */
    private fun almostExpiredToken(): Boolean {
        val jwt = JWT(loginResponse?.token.toString())
        val expirationTime = jwt.expiresAt
        if (expirationTime != null) {
            val currentTime = Date()
            val fiveMinutesInMillis = 5 * 60 * 1000
            val marginTime = Date(currentTime.time + fiveMinutesInMillis)
            return expirationTime.before(marginTime)
        }
        return false
    }

    /**
     *
     * Almacenamiento de sesión
     *
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }


    //Funcion para almacenar la data class login response
    fun saveLoginResponse(context: Context, loginResponse: LoginResponse?) {
        val gson = Gson()
        val json = gson.toJson(loginResponse)
        val preferences = getPreferences(context).edit()
        Log.d(tagLog, "Guardado $json")
        preferences.putString(KEY_RESPONSE, json)
        preferences.apply()
    }

    //Función para conseguir el login response almacenado
    fun getLoginDataClass(context: Context): LoginResponse? {
        val gson = Gson()
        //null por defecto, si no hay nada devuelve null
        val json = getPreferences(context).getString(KEY_RESPONSE, null)
        if (json.isNullOrEmpty()) {
            return null
        } else {
            return gson.fromJson(json, LoginResponse::class.java)
        }
    }

    /**
     * Función al iniciar la app, revisa si hay una sesión almacenada
     */
    fun startApp(context: Context, callback: (Boolean, String) -> Unit) {
        loginResponse = getLoginDataClass(context)
        if (loginResponse == null) {
            callback(false, "Vuelve a iniciar sesión")
        } else {
            val url = HOST + "/api/user/sesion/mantener"
            val headers = mapOf(
                "Authorization" to "Bearer ${loginResponse?.token}"
            )
            url.httpGet()
                .header(headers)
                .responseString() { requests, response, result ->
                    if (response.isSuccessful) {
                        val responseBody = result.get()
                        val gson = Gson()
                        val respuestaSesion =
                            gson.fromJson(responseBody, MantenerSesionResponse::class.java)
                        Log.d("MantenerSesion", respuestaSesion.toString())
                        callback(true, "Sesión válida")
                    } else {
                        callback(false, "Vuelve a iniciar sesión")
                    }

                }


        }


    }

    /**
     * Cierra la sesión
     */
    fun closeSesion(context: Context) {
        val preferences = getPreferences(context).edit()
        preferences.clear()
        preferences.apply()

        registerResponse = null
        loginResponse = null

    }

}