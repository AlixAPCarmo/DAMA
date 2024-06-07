package pt.ipt.DAMA.retrofit

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import pt.ipt.DAMA.R

class MyCookieJar(private val context: Context) : CookieJar {
    var cookies: List<Cookie> = ArrayList()
    private val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)

    init {
        val host = context.getString(R.string.ourAPI).toHttpUrlOrNull()?.host
        val cookiesString = sharedPreferences.getString(host, null)
        Log.d("MyCookieJar", "Loading cookies: $cookiesString")
        if (cookiesString != null) {
            cookies = Gson().fromJson(cookiesString, Array<Cookie>::class.java).toList()
            Log.d("MyCookieJar", "Parsed cookies: $cookies")
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isNotEmpty()) {
            val jsonString = Gson().toJson(cookies)
            sharedPreferences.edit().putString(url.host, jsonString).apply()
            this.cookies = cookies
            Log.d("MyCookieJar", "Cookies saved: $jsonString")
        }else{
            this.cookies = emptyList()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies
    }

    fun clearCookies(url: HttpUrl) {
        sharedPreferences.edit().remove(url.host).apply()
        cookies = emptyList()
    }

    fun isUserLoggedIn(): Boolean {
        val baseUrlString = context.getString(R.string.ourAPI).split("://")[1].split("/")[0]
        val url = HttpUrl.Builder()
            .host(baseUrlString)
            .scheme("https")
            .addPathSegment("api")
            .build()
        val cookies = loadForRequest(url)
        val isLoggedIn = cookies.any { cookie ->
            cookie.name == "connect.sid" && !cookie.hasExpired()
        }
        Log.d("MyCookieJar", "isUserLoggedIn: $isLoggedIn")
        return isLoggedIn
    }

    private fun Cookie.hasExpired(): Boolean {
        return expiresAt < System.currentTimeMillis()
    }
}
