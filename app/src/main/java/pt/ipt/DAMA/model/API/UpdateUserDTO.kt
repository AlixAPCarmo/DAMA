package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

data class UpdateUserDTO(
    @SerializedName("firstname")
    val firstName: String?,
    @SerializedName("lastname")
    val lastName: String?,
    @SerializedName("role")
    val role: String?
)
