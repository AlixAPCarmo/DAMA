package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

data class UserRegisterDTO(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String
)
