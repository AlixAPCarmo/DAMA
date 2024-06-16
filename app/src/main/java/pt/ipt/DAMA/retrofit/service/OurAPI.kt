package pt.ipt.DAMA.retrofit.service

import pt.ipt.DAMA.model.API.CelestialCreateRequestDTO
import pt.ipt.DAMA.model.API.CelestialDeleteRequestDTO
import pt.ipt.DAMA.model.API.CelestialFindRequestDTO
import pt.ipt.DAMA.model.API.CelestialUpdateRequestDTO
import pt.ipt.DAMA.model.API.EmailDTO
import pt.ipt.DAMA.model.API.LoginRequestDTO
import pt.ipt.DAMA.model.API.PasswordResetDTO
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.model.API.UpdateUserDTO
import pt.ipt.DAMA.model.API.UserRegisterDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Interface to represent the API
 */
interface OurAPI {
    @POST("login")
    fun login(@Body credentials: LoginRequestDTO): Call<SimpleResponseDTO>

    @POST("register")
    fun registerUser(@Body userInfo: UserRegisterDTO): Call<SimpleResponseDTO>

    @POST("logout")
    fun logoutUser(): Call<SimpleResponseDTO>

    @PUT("update")
    fun updateUser(@Body updateInfo: UpdateUserDTO): Call<SimpleResponseDTO>


    @POST("save-new-password")
    fun resetPassword(@Body resetInfo: PasswordResetDTO): Call<SimpleResponseDTO>

    @POST("request-password-reset")
    fun requestPasswordReset(@Body emailInfo: EmailDTO): Call<SimpleResponseDTO>

    @GET("celestial")
    fun getAllCelestialObjects(): Call<SimpleResponseDTO>

    @POST("celestial")
    fun createCelestialObject(@Body celestialData: CelestialCreateRequestDTO): Call<SimpleResponseDTO>

    @PUT("celestial")
    fun updateCelestialObject(@Body celestialData: CelestialUpdateRequestDTO): Call<SimpleResponseDTO>

    @HTTP(method = "DELETE", path = "celestial", hasBody = true)
    fun deleteCelestialObject(@Body deleteRequest: CelestialDeleteRequestDTO): Call<Void>

    @POST("celestial/name")
    fun findCelestialObject(@Body findRequest: CelestialFindRequestDTO): Call<SimpleResponseDTO>

}