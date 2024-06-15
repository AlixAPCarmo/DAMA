package pt.ipt.DAMA.retrofit

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import pt.ipt.DAMA.R
import pt.ipt.DAMA.retrofit.service.AstronomyAPI
import pt.ipt.DAMA.retrofit.service.ImageAPI
import pt.ipt.DAMA.retrofit.service.WikipediaAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import pt.ipt.DAMA.retrofit.service.OurAPI

class RetrofitInitializer(context: Context) {
    // Initialize Gson with lenient setting to allow less strict JSON responses
    private val gson: Gson = GsonBuilder().setLenient().create()

    // Configuration for Astronomy API with basic authentication
    private val hostAstronomyapi = context.getString(R.string.astronomy_url)
    private val aplicationId = context.getString(R.string.astronomy_appId)
    private val aplicationSecret = context.getString(R.string.astronomy_appSecret)

    // Interceptor to add authentication header to requests
    private val authInterceptorAstronomy = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader(
                "Authorization",
                "Basic " + encodeStringToBase64("$aplicationId:$aplicationSecret")
            )
            .build()
        chain.proceed(newRequest)
    }

    // OkHttpClient with interceptor for Astronomy API
    private val clientAstronomy = OkHttpClient.Builder()
        .addInterceptor(authInterceptorAstronomy)
        .build()

    // Retrofit instance for Astronomy API
    private val retrofitAstronomy =
        Retrofit.Builder()
            .baseUrl(hostAstronomyapi)
            .client(clientAstronomy)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    // Retrofit instance for Image API
    private val hostImageapi = context.getString(R.string.image_url)
    private val retrofitImage =
        Retrofit.Builder()
            .baseUrl(hostImageapi)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    // Retrofit instance for Wikipedia API in English
    private val hostWikiapiEn = context.getString(R.string.wiki_url_en)
    private val retrofitwikiEn =
        Retrofit.Builder()
            .baseUrl(hostWikiapiEn)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    // OkHttpClient with cookie management for our custom API
    private val hostApi = context.getString(R.string.ourAPI)
    private val myCookieJar = MyCookieJar(context)

    val client = OkHttpClient.Builder()
        .cookieJar(myCookieJar)
        .build()

    // Retrofit instance for our custom API
    private val retrofitAPI = Retrofit.Builder()
        .baseUrl(hostApi)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()

    // Public API accessors
    fun API(): OurAPI = retrofitAPI.create(OurAPI::class.java)
    fun AstronomyAPI(): AstronomyAPI = retrofitAstronomy.create(AstronomyAPI::class.java)
    fun ImageAPI(): ImageAPI = retrofitImage.create(ImageAPI::class.java)
    fun WikiAPI(): WikipediaAPI = retrofitwikiEn.create(WikipediaAPI::class.java)

    // Helper function to encode application credentials to Base64 for HTTP Basic Authentication
    private fun encodeStringToBase64(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
