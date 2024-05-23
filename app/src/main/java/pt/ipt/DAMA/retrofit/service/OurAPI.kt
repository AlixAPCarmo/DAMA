package pt.ipt.DAMA.retrofit.service

import pt.ipt.DAMA.model.API.LoginRequestDTO
import pt.ipt.DAMA.model.API.PasswordResetDTO
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.model.API.UpdateUserDTO
import pt.ipt.DAMA.model.API.UserRegisterDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface OurAPI {
    @POST("login")
    fun login(@Body credentials: LoginRequestDTO): Call<SimpleResponseDTO>

    @POST("users/register")
    fun registerUser(@Body userInfo: UserRegisterDTO): Call<SimpleResponseDTO>

    @GET("users/logout")
    fun logoutUser(): Call<SimpleResponseDTO>

    @PUT("users/update")
    fun updateUser(@Body updateInfo: UpdateUserDTO): Call<SimpleResponseDTO>

    @POST("users/reset-password")
    fun resetPassword(@Body resetInfo: PasswordResetDTO): Call<SimpleResponseDTO>
}