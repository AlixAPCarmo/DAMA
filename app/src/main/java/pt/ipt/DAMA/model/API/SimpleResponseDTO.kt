package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName
data class SimpleResponseDTO(
    @SerializedName("ok")
    val ok: Boolean,

    @SerializedName("data")
    val data: UserData,

    @SerializedName("status")
    val status: Int,

    @SerializedName("error")
    val error: String?,

    @SerializedName("message")
    val message: String
) {
    data class UserData(
        @SerializedName("user_id")
        val userId: Int,

        @SerializedName("email")
        val email: String,

        @SerializedName("password")
        val password: String,

        @SerializedName("first_name")
        val firstName: String,

        @SerializedName("last_name")
        val lastName: String,

        @SerializedName("email_verification_token")
        val emailVerificationToken: String?,

        @SerializedName("email_verified")
        val emailVerified: Int,

        @SerializedName("role")
        val role: String,

        @SerializedName("created_at")
        val createdAt: String,

        @SerializedName("updated_at")
        val updatedAt: String
    )
}