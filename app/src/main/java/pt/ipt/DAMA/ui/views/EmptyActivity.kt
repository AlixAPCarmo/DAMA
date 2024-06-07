package pt.ipt.DAMA.ui.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import pt.ipt.DAMA.R
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.retrofit.MyCookieJar
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.retrofit.service.OurAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmptyActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var retrofit: RetrofitInitializer
    private lateinit var api: OurAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty_activity)

        // Initialize Retrofit and API
        retrofit = RetrofitInitializer(this)
        api = retrofit.API()

        // Initialize UI components
        logoutButton = findViewById(R.id.logout_button)

        // Set up logout button click listener
        logoutButton.setOnClickListener {
            logoutUser()
        }
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
                        MyCookieJar(this@EmptyActivity).clearCookies(getString(R.string.ourAPI).toHttpUrlOrNull()!!)

                        Toast.makeText(
                            this@EmptyActivity,
                            "Logged out successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@EmptyActivity, ArActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@EmptyActivity,
                            logoutResponse?.error ?: "Unknown error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                Log.e("EmptyActivity", "Network Failure: ${t.message}")
                Toast.makeText(
                    this@EmptyActivity,
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
                    this@EmptyActivity,
                    errorResponse.error ?: "Unknown error",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: JsonSyntaxException) {
                Toast.makeText(
                    this@EmptyActivity,
                    "Error parsing response: $errorBody",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IllegalStateException) {
                // Handle case where response is not a JSON object
                Toast.makeText(
                    this@EmptyActivity,
                    "Unexpected response format: $errorBody",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this@EmptyActivity, "Unknown error", Toast.LENGTH_SHORT).show()
        }
    }
}
