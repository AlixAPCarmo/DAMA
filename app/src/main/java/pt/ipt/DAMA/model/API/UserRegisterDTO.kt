package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

/**
 * Data class to represent a user registration request
 */
data class UserRegisterDTO(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    val role: String = "user"
)
