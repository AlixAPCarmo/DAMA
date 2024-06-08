package pt.ipt.DAMA.ui.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import pt.ipt.DAMA.R
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.model.wikipédia.WikipediaResponseDTO
import pt.ipt.DAMA.retrofit.MyCookieJar
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.retrofit.service.OurAPI
import pt.ipt.DAMA.retrofit.service.WikipediaAPI
import pt.ipt.DAMA.ui.adapter.ImageCarouselAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CelestialActivity : AppCompatActivity() {

    private lateinit var logoutButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var api: OurAPI
    private lateinit var wikipediaAPI: WikipediaAPI
    private lateinit var imageCarousel: ViewPager2
    private lateinit var imageCarouselAdapter: ImageCarouselAdapter
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_celestial)

        // Initialize Retrofit and APIs
        val retrofitInitializer = RetrofitInitializer(this)
        api = retrofitInitializer.API()
        wikipediaAPI = retrofitInitializer.WikiAPI()

        // Initialize UI components
        logoutButton = findViewById(R.id.logout_button)
        titleTextView = findViewById(R.id.title)
        contentTextView = findViewById(R.id.content)
        imageCarousel = findViewById(R.id.image_carousel)

        // Set up logout button click listener
        logoutButton.setOnClickListener {
            logoutUser()
        }

        // Set up back button click listener
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

        // Call Wikipedia API
        val searchQuery = "Saturn"
        getWikipediaContent(searchQuery)
    }

    private fun getWikipediaContent(query: String) {
        wikipediaAPI.getSearch(titles = query).enqueue(object : Callback<WikipediaResponseDTO> {
            override fun onResponse(
                call: Call<WikipediaResponseDTO>,
                response: Response<WikipediaResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val wikiResponse = response.body()
                    if (wikiResponse != null && wikiResponse.query.pages.isNotEmpty()) {
                        val page = wikiResponse.query.pages[0]
                        titleTextView.text = page.title
                        contentTextView.text = page.extract

                        val imageUrls = mutableListOf<String>()
                        page.thumbnail?.let { imageUrls.add(it.source) }
                        page.original?.let { imageUrls.add(it.source) }

                        imageCarouselAdapter = ImageCarouselAdapter(imageUrls)
                        imageCarousel.adapter = imageCarouselAdapter
                    } else {
                        Toast.makeText(
                            this@CelestialActivity,
                            "No content found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@CelestialActivity,
                        "Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<WikipediaResponseDTO>, t: Throwable) {
                Log.e("CelestialActivity", "Network Failure: ${t.message}")
                Toast.makeText(
                    this@CelestialActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun logoutUser() {
        api.logoutUser().enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(
                call: Call<SimpleResponseDTO>,
                response: Response<SimpleResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val logoutResponse = response.body()
                    if (logoutResponse != null && logoutResponse.ok) {
                        // Clear cookies
                        MyCookieJar(this@CelestialActivity).clearCookies(getString(R.string.ourAPI).toHttpUrlOrNull()!!)

                        Toast.makeText(
                            this@CelestialActivity,
                            "Logged out successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@CelestialActivity, ArActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@CelestialActivity,
                            logoutResponse?.error ?: "Unknown error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                Log.e("CelestialActivity", "Network Failure: ${t.message}")
                Toast.makeText(
                    this@CelestialActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun handleErrorResponse(response: Response<SimpleResponseDTO>) {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                val gson = Gson()
                val errorResponse: SimpleResponseDTO = gson.fromJson(errorBody, SimpleResponseDTO::class.java)
                Toast.makeText(
                    this@CelestialActivity,
                    errorResponse.error ?: "Unknown error",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: JsonSyntaxException) {
                Toast.makeText(
                    this@CelestialActivity,
                    "Error parsing response: $errorBody",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IllegalStateException) {
                // Handle case where response is not a JSON object
                Toast.makeText(
                    this@CelestialActivity,
                    "Unexpected response format: $errorBody",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this@CelestialActivity, "Unknown error", Toast.LENGTH_SHORT).show()
        }
    }
}
