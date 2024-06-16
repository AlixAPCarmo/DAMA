package pt.ipt.DAMA.retrofit

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import pt.ipt.DAMA.R

/**
 * Custom CookieJar implementation to manage cookies using SharedPreferences.
 */
class MyCookieJar(private val context: Context) : CookieJar {
    // List to store current cookies
    var cookies: List<Cookie> = ArrayList()
    // Access SharedPreferences to store cookies persistently
    private val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)

    // Initialization block to load cookies from SharedPreferences
    init {
        // Retrieve the host name from a resource string to use as a key for storing cookies
        val host = context.getString(R.string.ourAPI).toHttpUrlOrNull()?.host
        val cookiesString = sharedPreferences.getString(host, null)
        Log.d("MyCookieJar", "Loading cookies: $cookiesString")
        // Deserialize cookies from JSON string and load them into the list
        if (cookiesString != null) {
            cookies = Gson().fromJson(cookiesString, Array<Cookie>::class.java).toList()
            Log.d("MyCookieJar", "Parsed cookies: $cookies")
        }
    }

    /**
     * Save received cookies from server responses into SharedPreferences
     */
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isNotEmpty()) {
            val jsonString = Gson().toJson(cookies)
            sharedPreferences.edit().putString(url.host, jsonString).apply()
            this.cookies = cookies
            Log.d("MyCookieJar", "Cookies saved: $jsonString")
        } else {
            this.cookies = emptyList()
        }
    }

    /**
     * Load cookies for outgoing requests
     */
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies
    }

    /**
     * Clear cookies from SharedPreferences
     */
    fun clearCookies(url: HttpUrl) {
        sharedPreferences.edit().remove(url.host).apply()
        cookies = emptyList()
    }

    /**
     * Check if user is logged in based on the presence and validity of a specific cookie
     */
    fun isUserLoggedIn(): Boolean {
        val baseUrlString = context.getString(R.string.ourAPI).split("://")[1].split("/")[0]
        val url = HttpUrl.Builder()
            .scheme("http")
            .host(baseUrlString)
            .addPathSegment("api")
            .build()
        val cookies = loadForRequest(url)
        val isLoggedIn = cookies.any { cookie ->
            cookie.name == "connect.sid" && !cookie.hasExpired()
        }
        Log.d("MyCookieJar", "isUserLoggedIn: $isLoggedIn")
        return isLoggedIn
    }

    /**
     * Helper function to check if a cookie has expired
     */
    private fun Cookie.hasExpired(): Boolean {
        return expiresAt < System.currentTimeMillis()
    }
}