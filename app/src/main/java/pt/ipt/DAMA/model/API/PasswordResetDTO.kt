package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

/**
 * Data class to represent a password reset request
 */
data class PasswordResetDTO(
    @SerializedName("newPassword")
    val newPassword: String
)
