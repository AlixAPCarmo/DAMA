package pt.ipt.DAMA.retrofit

import android.content.Context
import android.util.Base64
import pt.ipt.DAMA.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pt.ipt.DAMA.R
import pt.ipt.DAMA.retrofit.service.AstronomyAPI
import pt.ipt.DAMA.retrofit.service.ImageAPI
import pt.ipt.DAMA.retrofit.service.OpenAiApiService
import pt.ipt.DAMA.retrofit.service.WikipediaAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import pt.ipt.DAMA.retrofit.service.OurAPI
import java.util.concurrent.TimeUnit

class RetrofitInitializer(context: Context) {
    private val gson: Gson = GsonBuilder().setLenient().create()

    // ASTRONOMY API
    // location of our API
    private val host_AstronomyAPI = context.getString(R.string.astronomy_url)
    private val aplicationId = context.getString(R.string.astronomy_appId)
    private val aplicationSecret = context.getString(R.string.astronomy_appSecret)
    private val openAI = context.getString(R.string.openAIAPI)

    val authInterceptorAstronomy = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader(
                "Authorization",
                "Basic " + encodeStringToBase64("$aplicationId:$aplicationSecret")
            )
            .build()
        chain.proceed(newRequest)
    }

    // Configure the client to use the interceptor and the base URL
    val clientAstronomy = OkHttpClient.Builder()
        .addInterceptor(authInterceptorAstronomy)
        .build()

    // Create the Retrofit instance for Astronomy API
    private val retrofitAstronomy =
        Retrofit.Builder()
            .baseUrl(host_AstronomyAPI)
            .client(clientAstronomy)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    //////////////////////////////////////////////////////////////////////////
    private val host_ImageAPI = context.getString(R.string.image_url)
    // Create the Retrofit instance for Image API
    private val retrofitImage =
        Retrofit.Builder()
            .baseUrl(host_ImageAPI)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    //////////////////////////////////////////////////////////////////////////
    private val host_WikiAPI_en = context.getString(R.string.wiki_url_en)
    // Create the Retrofit instance for Wikipedia API
    private val retrofitWiki_en =
        Retrofit.Builder()
            .baseUrl(host_WikiAPI_en)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    //////////////////////////////////////////////////////////////////////////
    private val host_API = context.getString(R.string.ourAPI)
    private val myCookieJar = MyCookieJar(context)

    // Configure the client to use the cookie jar
    val client = OkHttpClient.Builder()
        .cookieJar(myCookieJar)
        .build()
    // Create the Retrofit instance for our API
    private val retrofitAPI = Retrofit.Builder()
        .baseUrl(host_API)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()

    //////////////////////////////////////////////////////////////////////////
    // open ai API7
    private val host_OpenAI = openAI

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configure the client to use the interceptor and the base URL
    private val clientOpenAi = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.GOOGLE_AI_API_KEY}")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(60, TimeUnit.SECONDS) // Increase connect timeout
        .writeTimeout(60, TimeUnit.SECONDS) // Increase write timeout
        .readTimeout(60, TimeUnit.SECONDS) // Increase read timeout
        .build()

    // Create the Retrofit instance for OpenAI API
    private val retrofitOpenAI = Retrofit.Builder()
        .baseUrl(host_OpenAI)
        .client(clientOpenAi)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


    fun API(): OurAPI = retrofitAPI.create(OurAPI::class.java)
    fun AstronomyAPI(): AstronomyAPI = retrofitAstronomy.create(AstronomyAPI::class.java)
    fun ImageAPI(): ImageAPI = retrofitImage.create(ImageAPI::class.java)
    fun WikiAPI(): WikipediaAPI = retrofitWiki_en.create(WikipediaAPI::class.java)
    fun OpenAiAPI() = retrofitOpenAI.create(OpenAiApiService::class.java)

    // Function to encode a string to Base64
    private fun encodeStringToBase64(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}