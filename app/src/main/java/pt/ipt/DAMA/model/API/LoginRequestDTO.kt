package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

/**
 * Data class to represent a login request
 */
data class LoginRequestDTO(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
