package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

data class PasswordResetDTO(
    @SerializedName("newPassword")
    val newPassword: String
)
