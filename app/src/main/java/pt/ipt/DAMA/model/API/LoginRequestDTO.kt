package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

data class LoginRequestDTO(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
