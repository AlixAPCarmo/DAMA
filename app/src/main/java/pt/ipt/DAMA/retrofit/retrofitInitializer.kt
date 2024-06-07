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
    private val gson: Gson = GsonBuilder().setLenient().create()

    // ASTRONOMY API
    // location of our API
    private val host_AstronomyAPI = context.getString(R.string.astronomy_url)
    private val aplicationId = context.getString(R.string.astronomy_appId)
    private val aplicationSecret = context.getString(R.string.astronomy_appSecret)

    val authInterceptorAstronomy = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader(
                "Authorization",
                "Basic " + encodeStringToBase64("$aplicationId:$aplicationSecret")
            )
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
    private val host_ImageAPI = context.getString(R.string.image_url)
    private val retrofitImage =
        Retrofit.Builder()
            .baseUrl(host_ImageAPI)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    //////////////////////////////////////////////////////////////////////////
    private val host_WikiAPI_en = context.getString(R.string.wiki_url_en)
    private val retrofitWiki_en =
        Retrofit.Builder()
            .baseUrl(host_WikiAPI_en)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    //////////////////////////////////////////////////////////////////////////
    private val host_API = context.getString(R.string.ourAPI)
    private val myCookieJar = MyCookieJar(context)

    val client = OkHttpClient.Builder()
        .cookieJar(myCookieJar)
        .build()

    private val retrofitAPI = Retrofit.Builder()
        .baseUrl(host_API)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()


    fun API(): OurAPI = retrofitAPI.create(OurAPI::class.java)
    fun AstronomyAPI(): AstronomyAPI = retrofitAstronomy.create(AstronomyAPI::class.java)
    fun ImageAPI(): ImageAPI = retrofitImage.create(ImageAPI::class.java)
    fun WikiAPI(): WikipediaAPI = retrofitWiki_en.create(WikipediaAPI::class.java)


    //função auxiliar para codificar string em base64
    private fun encodeStringToBase64(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}