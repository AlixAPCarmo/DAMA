package pt.ipt.DAMA.retrofit

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import pt.ipt.DAMA.R

class MyCookieJar(private val context: Context) : CookieJar {
    private var cookies: List<Cookie> = ArrayList()
    private val sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
    init {
        val cookiesString = sharedPreferences.getString("cookies", null)
        val baseUrlString = context.getString(R.string.ourAPI)

        if (cookiesString != null) {
            val httpUrl = baseUrlString.toHttpUrlOrNull()
            val headers = headersOf("Cookie", cookiesString)
            cookies = httpUrl?.let { Cookie.parseAll(it, headers) }!!
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookiesHeaderValue = cookies.joinToString("; ") { cookie ->
            "${cookie.name}=${cookie.value}"
        }
        sharedPreferences.edit().putString("cookies", cookiesHeaderValue).apply()
        this.cookies = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies
    }

    fun isUserLoggedIn(): Boolean {
        val baseUrlString = context.getString(R.string.ourAPI)
        val url = HttpUrl.Builder().host(baseUrlString).build()
        val cookies = loadForRequest(url)
        return cookies.any { cookie ->
            cookie.name == "session_id" && !cookie.hasExpired()
        }

    }

    private fun Cookie.hasExpired(): Boolean {
        return expiresAt < System.currentTimeMillis()
    }
}