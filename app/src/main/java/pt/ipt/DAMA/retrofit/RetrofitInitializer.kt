package pt.ipt.DAMA.retrofit

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import pt.ipt.DAMA.retrofit.service.AstronomyAPI
import pt.ipt.DAMA.retrofit.service.ImageAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInitializer {
    private val gson: Gson = GsonBuilder().setLenient().create()

    // ASTRONOMY API
    // location of our API
    private val host_AstronomyAPI = "https://api.astronomyapi.com/api/v2/"
    private val aplicationId = "f57f77f5-f9de-45d7-827b-02ec9e08628c"
    private val aplicationSecret = "4e46df0069cdb2e4afd9e6516494d12e294a1b925eb1c926eae03b114fb76d7dab4d82cb5287afd0f01a6d2b123ef2e7255da48565d774a9df1c5aa9a8e6e90e0e623608b3f3d7850a2c9dc1b29cf9bf59360f0205fbd2a5a08e2d2c8806e5bd5b1456705ff230bba254a0692f56df94"

    val authInterceptorAstronomy = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Basic "+encodeStringToBase64("$aplicationId:$aplicationSecret"))
            .build()
        chain.proceed(newRequest)
    }

    // Configura o cliente OkHttpClient com o interceptor
    val clientAstronomy = OkHttpClient.Builder()
        .addInterceptor(authInterceptorAstronomy)
        .build()

    // 'opens' the connection to API
    private val retrofitAstronomy =
        Retrofit.Builder()
            .baseUrl(host_AstronomyAPI)
            .client(clientAstronomy)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    //////////////////////////////////////////////////////////////////////////
    private val host_ImageAPI = "https://api.pexels.com/v1/"
    private val retrofitImage =
        Retrofit.Builder()
            .baseUrl(host_ImageAPI)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    fun AstronomyAPI(): AstronomyAPI = retrofitAstronomy.create(AstronomyAPI::class.java)
    fun ImageAPI(): ImageAPI = retrofitImage.create(ImageAPI::class.java)

    //função auxiliar para codificar string em base64
    private fun encodeStringToBase64(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}