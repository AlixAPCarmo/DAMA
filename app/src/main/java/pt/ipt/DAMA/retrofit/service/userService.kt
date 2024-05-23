package pt.ipt.DAMA.retrofit.service

import pt.ipt.DAMA.DTO.UserLoginRequest
import pt.ipt.DAMA.DTO.UserLoginResponse
import pt.ipt.DAMA.DTO.UserRegister
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Description of functions that request data from API
 *
 * @constructor Create empty UserService
 */
interface UserService {

    /**
     * Function to log in a user
     *
     * @param userLoginRequest
     */
    @POST("api/login")
    fun loginUser(@Body userLoginRequest: UserLoginRequest): Call<UserLoginResponse>

    /**
     * Function to register a user
     *
     * @param userRegister
     */
    @POST("api/register")
    fun registerUser(@Body userRegister: UserRegister): Call<Void>
}
