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
import pt.ipt.DAMA.model.API.ChatGptMessage
import pt.ipt.DAMA.model.API.ChatGptRequest
import pt.ipt.DAMA.model.API.ChatGptResponse
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.model.wikipédia.WikipediaResponseDTO
import pt.ipt.DAMA.retrofit.MyCookieJar
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.retrofit.service.OpenAiApiService
import pt.ipt.DAMA.retrofit.service.OurAPI
import pt.ipt.DAMA.retrofit.service.WikipediaAPI
import pt.ipt.DAMA.ui.adapter.ImageCarouselAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class CelestialActivity : AppCompatActivity() {

    private lateinit var logoutButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var api: OurAPI
    private lateinit var wikipediaAPI: WikipediaAPI
    private lateinit var imageCarousel: ViewPager2
    private lateinit var imageCarouselAdapter: ImageCarouselAdapter
    private lateinit var backButton: ImageButton
    private lateinit var openAI: OpenAiApiService
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_celestial)

        name = intent.getStringExtra("name").toString()

        // get the system language
        val systemLanguage = Locale.getDefault().language
        Log.e("CelestialActivity", "System Language: $systemLanguage")

        // Initialize Retrofit and APIs
        val retrofitInitializer = RetrofitInitializer(this)
        api = retrofitInitializer.API()
        wikipediaAPI = retrofitInitializer.WikiAPI()
        openAI = retrofitInitializer.OpenAiAPI()

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

        // Translate the celestial object name to English
        translateName(name)
    }

    private fun translateName(name: String) {
        val messages = listOf(
            ChatGptMessage(
                role = "user",
                content = "Translate the following celestial object name to English. Return only the name in English. Name: $name"
            )
        )

        val request = ChatGptRequest(
            model = "gpt-3.5-turbo",
            messages = messages
        )

        openAI.generateCompletion(request).enqueue(object : Callback<ChatGptResponse> {
            override fun onResponse(
                call: Call<ChatGptResponse>,
                response: Response<ChatGptResponse>
            ) {
                if (response.isSuccessful) {
                    val completion = response.body()
                    if (completion != null && completion.choices.isNotEmpty()) {
                        val translatedName = completion.choices[0].message.content.trim()
                        handleContentBasedOnLanguage(translatedName)
                    } else {
                        Toast.makeText(
                            this@CelestialActivity,
                            "No translation found",
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

            override fun onFailure(call: Call<ChatGptResponse>, t: Throwable) {
                Log.e("CelestialActivity", "Network Failure: ${t.message}")
                Toast.makeText(
                    this@CelestialActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun handleContentBasedOnLanguage(translatedName: String) {
        val systemLanguage = Locale.getDefault().language
        if (systemLanguage != "en") {
            generateContentInSystemLanguage(translatedName, systemLanguage)
        }
        // Fetch images from Wikipedia regardless of the language
        getWikipediaContent(translatedName)
    }

    private fun generateContentInSystemLanguage(name: String, language: String) {
        val messages = listOf(
            ChatGptMessage(
                role = "user",
                content = "Generate information about the celestial object named $name in $language."
            )
        )

        val request = ChatGptRequest(
            model = "gpt-3.5-turbo",
            messages = messages
        )

        openAI.generateCompletion(request).enqueue(object : Callback<ChatGptResponse> {
            override fun onResponse(
                call: Call<ChatGptResponse>,
                response: Response<ChatGptResponse>
            ) {
                if (response.isSuccessful) {
                    val completion = response.body()
                    if (completion != null && completion.choices.isNotEmpty()) {
                        val generatedContent = completion.choices[0].message.content.trim()
                        titleTextView.text = name
                        contentTextView.text = generatedContent
                    } else {
                        Toast.makeText(
                            this@CelestialActivity,
                            "No content generated",
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

            override fun onFailure(call: Call<ChatGptResponse>, t: Throwable) {
                Log.e("CelestialActivity", "Network Failure: ${t.message}")
                Toast.makeText(
                    this@CelestialActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getWikipediaContent(query: String) {
        Log.e("CelestialActivity", "Query: $query")
        wikipediaAPI.getSearch(titles = query).enqueue(object : Callback<WikipediaResponseDTO> {
            override fun onResponse(
                call: Call<WikipediaResponseDTO>,
                response: Response<WikipediaResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val wikiResponse = response.body()
                    Log.e("CelestialActivity", "Response: $wikiResponse")
                    if (wikiResponse != null && wikiResponse.query.pages.isNotEmpty()) {
                        val page = wikiResponse.query.pages[0]
                        val title = page.title
                        val extract = page.extract

                        val imageUrls = mutableListOf<String>()
                        page.thumbnail?.let { imageUrls.add(it.source) }
                        page.original?.let { imageUrls.add(it.source) }

                        imageCarouselAdapter = ImageCarouselAdapter(imageUrls)
                        imageCarousel.adapter = imageCarouselAdapter

                        if (imageUrls.isEmpty()) {
                            // Fallback to fetching images from Wikipedia again
                            getWikipediaImages(query)
                        }

                        val systemLanguage = Locale.getDefault().language
                        if (systemLanguage == "en") {
                            titleTextView.text = title
                            contentTextView.text = extract
                        }
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

    private fun getWikipediaImages(query: String) {
        wikipediaAPI.getSearch(titles = query).enqueue(object : Callback<WikipediaResponseDTO> {
            override fun onResponse(
                call: Call<WikipediaResponseDTO>,
                response: Response<WikipediaResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val wikiResponse = response.body()
                    Log.e("CelestialActivity", "Response: $wikiResponse")
                    if (wikiResponse != null && wikiResponse.query.pages.isNotEmpty()) {
                        val page = wikiResponse.query.pages[0]

                        val imageUrls = mutableListOf<String>()
                        page.thumbnail?.let { imageUrls.add(it.source) }
                        page.original?.let { imageUrls.add(it.source) }

                        imageCarouselAdapter = ImageCarouselAdapter(imageUrls)
                        imageCarousel.adapter = imageCarouselAdapter
                    } else {
                        Toast.makeText(
                            this@CelestialActivity,
                            "No images found",
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
                val errorResponse: SimpleResponseDTO =
                    gson.fromJson(errorBody, SimpleResponseDTO::class.java)
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
