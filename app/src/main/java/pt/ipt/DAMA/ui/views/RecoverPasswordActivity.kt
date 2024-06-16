package pt.ipt.DAMA.ui.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.DAMA.R
import pt.ipt.DAMA.model.API.EmailDTO
import pt.ipt.DAMA.model.API.PasswordResetDTO
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.retrofit.service.OurAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecoverPasswordActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var tokenEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var requestResetButton: Button
    private lateinit var resetPasswordButton: Button
    private lateinit var retrofitAPI: RetrofitInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recovery_password_activity)

        emailEditText = findViewById(R.id.email)
        tokenEditText = findViewById(R.id.token)
        newPasswordEditText = findViewById(R.id.new_password)
        requestResetButton = findViewById(R.id.request_reset_button)
        resetPasswordButton = findViewById(R.id.reset_password_button)

        // Retrofit initializer
        retrofitAPI = RetrofitInitializer(this)

        requestResetButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {
                requestPasswordReset(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        resetPasswordButton.setOnClickListener {
            val token = tokenEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            if (token.isNotEmpty() && newPassword.isNotEmpty()) {
                resetPassword(token, newPassword)
            } else {
                Toast.makeText(this, "Please enter the token and new password", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun requestPasswordReset(email: String) {
        val emailInfo = EmailDTO(email)
        retrofitAPI.API().requestPasswordReset(emailInfo)
            .enqueue(object : Callback<SimpleResponseDTO> {
                override fun onResponse(
                    call: Call<SimpleResponseDTO>,
                    response: Response<SimpleResponseDTO>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RecoverPasswordActivity,
                            "Password reset email sent successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RecoverPasswordActivity,
                            "Error sending password reset email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                    Toast.makeText(
                        this@RecoverPasswordActivity,
                        "Request failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun resetPassword(token: String, newPassword: String) {
        val resetInfo = PasswordResetDTO(newPassword, token)
        retrofitAPI.API().resetPassword(resetInfo).enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(
                call: Call<SimpleResponseDTO>,
                response: Response<SimpleResponseDTO>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@RecoverPasswordActivity,
                        "Password reset successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@RecoverPasswordActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@RecoverPasswordActivity,
                        "Error resetting password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                Toast.makeText(
                    this@RecoverPasswordActivity,
                    "Request failed: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
