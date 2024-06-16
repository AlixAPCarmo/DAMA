package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

data class EmailDTO(
    @SerializedName("email")
    val email: String
)
