package pt.ipt.DAMA.retrofit

import android.content.Context
import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import pt.ipt.DAMA.R

class MyCookieJar(private val context: Context) : CookieJar {
    var cookies: List<Cookie> = ArrayList()
    private val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)

    init {
        val cookiesString = sharedPreferences.getString("cookies", null)
        val baseUrlString = context.getString(R.string.ourAPI)
        Log.d("MyCookieJar", "Loading cookies: $cookiesString")

        if (cookiesString != null) {
            val httpUrl = baseUrlString.toHttpUrlOrNull()
            val headers = Headers.headersOf("Cookie", cookiesString)
            cookies = httpUrl?.let { url ->
                headers.values("Cookie").flatMap { header ->
                    header.split(";").mapNotNull { Cookie.parse(url, it.trim()) }
                }
            } ?: emptyList()
            Log.d("MyCookieJar", "Parsed cookies: $cookies")
        }
    }

    override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<Cookie>) {
        val cookiesHeaderValue = cookies.joinToString("; ") { cookie ->
            "${cookie.name}=${cookie.value}"
        }
        sharedPreferences.edit().putString("cookies", cookiesHeaderValue).apply()
        this.cookies = cookies
        Log.d("MyCookieJar", "Cookies saved: $cookiesHeaderValue")
    }

    override fun loadForRequest(url: okhttp3.HttpUrl): List<Cookie> {
        return cookies
    }

    fun clearCookies() {
        sharedPreferences.edit().remove("cookies").apply()
        cookies = emptyList()
    }

    fun isUserLoggedIn(): Boolean {
        val baseUrlString = context.getString(R.string.ourAPI)
        val url = baseUrlString.toHttpUrlOrNull()
        if (url != null) {
            val cookies = loadForRequest(url)
            val isLoggedIn = cookies.any { cookie ->
                cookie.name == "connect.sid" && !cookie.hasExpired()
            }
            Log.d("MyCookieJar", "isUserLoggedIn: $isLoggedIn")
            return isLoggedIn
        }
        return false
    }

    private fun Cookie.hasExpired(): Boolean {
        return expiresAt < System.currentTimeMillis()
    }
}
