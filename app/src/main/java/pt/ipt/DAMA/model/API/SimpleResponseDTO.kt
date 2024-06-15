package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

/**
 * Data class to represent a simple response from the API
 */
data class SimpleResponseDTO(
    @SerializedName("ok")
    val ok: Boolean,

    @SerializedName("data")
    val data: Any,

    @SerializedName("status")
    val status: Int,

    @SerializedName("error")
    val error: String?,

    @SerializedName("message")
    val message: String
)
