package pt.ipt.DAMA.ui.views

import android.content.Intent
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

class LoginActivity : AppCompatActivity() {

    // UI components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var backButton: ImageView
    private lateinit var skipButton: Button

    // Retrofit initializer
    private lateinit var retrofit: RetrofitInitializer

    /*
     * Initializes the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val intent = Intent(this, EmptyActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    /*
     * Function to login a user
     */
    private fun loginUser(email: String, password: String) {
        // Create a Retrofit instance
        val callOurAPI = retrofit.API().login(LoginRequestDTO(email, password))

        // Make the network request
        callOurAPI.enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(
                call: Call<SimpleResponseDTO>,
                response: Response<SimpleResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    Log.d("LoginActivity", "Response: ${response.body()}")
                    if (loginResponse != null && loginResponse.ok) {
                        Toast.makeText(
                            this@LoginActivity,
                            loginResponse.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        // Handle successful login
                        val intent = Intent(this@LoginActivity, EmptyActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            loginResponse?.error ?: "Unknown error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("LoginActivity", "Error Response: $errorBody")
                    if (errorBody != null) {
                        try {
                            val gson = Gson()
                            val loginResponse: SimpleResponseDTO =
                                gson.fromJson(errorBody, SimpleResponseDTO::class.java)
                            Toast.makeText(
                                this@LoginActivity,
                                loginResponse.error ?: "Unknown error",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: JsonSyntaxException) {
                            // Handle the case where the error body is a plain string
                            Toast.makeText(this@LoginActivity, errorBody, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Unknown error", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                Log.e("LoginActivity", "Network Failure: ${t.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
