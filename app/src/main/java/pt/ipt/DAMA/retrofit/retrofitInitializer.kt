package pt.ipt.DAMA.api.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import pt.ipt.DAMA.retrofit.service.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This class is responsible for communication with the API
 */
class RetrofitInitializer {

    // This object will translate the JSON content to Android app
    private val gson: Gson = GsonBuilder().setLenient().create()

    // Location of our API
    private val host = "https://adamastor.ipt.pt/DAM-API/"

    // 'Opens' the connection to API
    private val retrofit =
        Retrofit.Builder().baseUrl(host)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    fun userService(): UserService = retrofit.create(UserService::class.java)
}
