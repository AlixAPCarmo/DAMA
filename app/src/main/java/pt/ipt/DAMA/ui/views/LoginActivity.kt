package pt.ipt.DAMA.ui.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.DAMA.R
import pt.ipt.DAMA.model.API.LoginRequestDTO
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import pt.ipt.DAMA.retrofit.MyCookieJar

class LoginActivity : AppCompatActivity() {

    // UI components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var backButton: ImageView
    private lateinit var skipButton: Button

    // Retrofit initializer
    private lateinit var retrofit: RetrofitInitializer

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_login)

        // Initialize UI components
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        signInButton = findViewById(R.id.sign_in_button)
        backButton = findViewById(R.id.back_button)
        skipButton = findViewById(R.id.skip)

        // Initialize the retrofit variable with context
        retrofit = RetrofitInitializer(this)

        // Set up sign-in button click listener
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Call login function
            loginUser(email, password)
        }

        // Set up back button click listener
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Set up skip button click listener
        skipButton.setOnClickListener {
            val intent = Intent(this, ArActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        val callOurAPI = retrofit.API().login(LoginRequestDTO(email, password))

        callOurAPI.enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(call: Call<SimpleResponseDTO>, response: Response<SimpleResponseDTO>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.ok) {
                        // Verify cookies saved
                        val cookieJar = retrofit.client.cookieJar as MyCookieJar
                        Log.d("LoginActivity", "Cookies after login: ${cookieJar.cookies}")

                        Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, ArActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.error ?: getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                Log.e("LoginActivity", "Network Failure: ${t.message}")
                Toast.makeText(this@LoginActivity, getString(R.string.network_error)+": ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleErrorResponse(response: Response<SimpleResponseDTO>) {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                val gson = Gson()
                val errorResponse: SimpleResponseDTO = gson.fromJson(errorBody, SimpleResponseDTO::class.java)
                Toast.makeText(this@LoginActivity, errorResponse.error ?: getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
            } catch (e: JsonSyntaxException) {
                Toast.makeText(this@LoginActivity, getString(R.string.error_parsing_response)+": $errorBody", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalStateException) {
                // Handle case where response is not a JSON object
                Toast.makeText(this@LoginActivity, getString(R.string.unexpected_response_format)+": $errorBody", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@LoginActivity, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
        }
    }
}
