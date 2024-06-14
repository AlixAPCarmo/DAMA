package pt.ipt.DAMA.ui.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.model.API.UserRegisterDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import pt.ipt.DAMA.R
import pt.ipt.DAMA.retrofit.RetrofitInitializer

class RegisterActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var skipButton: TextView
    private lateinit var backButton: ImageView
    private lateinit var retrofit: RetrofitInitializer

    @SuppressLint("MissingInflatedId", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_register)

        // Initialize UI components
        firstNameEditText = findViewById(R.id.first_name)
        lastNameEditText = findViewById(R.id.last_name)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        signUpButton = findViewById(R.id.sign_up_button)
        backButton = findViewById(R.id.back_button)
        skipButton = findViewById(R.id.skip)

        // Initialize the retrofit variable with context
        retrofit = RetrofitInitializer(this)

        // Set up sign-up button click listener
        signUpButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Call register function
            registerUser(firstName, lastName, email, password)
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

    /*
    * Function to register a user
     */
    private fun registerUser(firstName: String, lastName: String, email: String, password: String) {
        // Create a Retrofit instance
        val callOurAPI =
            retrofit.API().registerUser(UserRegisterDTO(email, password, firstName, lastName))

        // Make the network request
        callOurAPI.enqueue(object : Callback<SimpleResponseDTO> {
            /*
            * Handle the response from the server
             */
            override fun onResponse(
                call: Call<SimpleResponseDTO>,
                response: Response<SimpleResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    Log.d("RegisterActivity", "Response: ${response.body()}")
                    if (registerResponse != null && registerResponse.ok) {
                        Toast.makeText(
                            this@RegisterActivity,
                            registerResponse.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        // navigate to the login activity
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            registerResponse?.error ?: getString(R.string.unknown_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RegisterActivity", "Error Response: $errorBody")
                    if (errorBody != null) {
                        try {
                            val gson = Gson()
                            val registerResponse: SimpleResponseDTO =
                                gson.fromJson(errorBody, SimpleResponseDTO::class.java)
                            Toast.makeText(
                                this@RegisterActivity,
                                registerResponse.error ?:getString(R.string.unknown_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: JsonSyntaxException) {
                            // Handle the case where the error body is a plain string
                            Toast.makeText(this@RegisterActivity, errorBody, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, getString(R.string.unknown_error), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                Log.e("RegisterActivity", "Network Failure: ${t.message}")
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.network_error) +": ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
