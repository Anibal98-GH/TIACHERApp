package com.example.tiacher.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginResponse(
    val logged: Boolean,
    var mail: String? = null,
    var access: Int? = null,
    var token: String? = null,
    val error: String? = null
) : Parcelable

@Parcelize
data class RegisterResponse(
    val register: Boolean,
    val error: String? = null
) : Parcelable

@Parcelize
data class RefreshTokenResponse(
    val token: String? = null,
    val message: String? = null
) : Parcelable

@Parcelize
data class MantenerSesionResponse(
    val valid: Boolean,
    val message: String? = null
) : Parcelable

@Parcelize
data class MensajeResponse(
    val message: String
) : Parcelable

@Parcelize
data class ExamExistResponse(
    val exist: Boolean,
    val titulo: String? = null,
    val message: String? = null
) : Parcelable

@Parcelize
data class PreguntaDetalle(
    val question: String,
    val correct: String?,
    val answered: String?,
    val result: String?
) : Parcelable

@Parcelize
data class CorreccionResponse(
    val valid: Int,
    val fail: Int,
    val nc: Int,
    val grade: Double,
    val data: List<PreguntaDetalle>
) : Parcelable