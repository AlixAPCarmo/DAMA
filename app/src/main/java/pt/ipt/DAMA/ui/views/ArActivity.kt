package pt.ipt.DAMA.ui.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.ux.ArFragment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.GpsManager
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyPositionResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyRequestDTO
import pt.ipt.DAMA.retrofit.MyCookieJar
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.utils.ArUtils
import pt.ipt.DAMA.utils.DateAndTime
import pt.ipt.DAMA.utils.PermissionsManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ArActivity : AppCompatActivity() {
    // Declare necessary variables and components
    private lateinit var gpsManager: GpsManager
    private lateinit var arUtils: ArUtils
    private lateinit var permissionManager: PermissionsManager
    private lateinit var retrofit: RetrofitInitializer
    private var positions: List<PositionData> = emptyList()

    private lateinit var arFragment: ArFragment
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnList: ImageButton
    private lateinit var btnLogout: ImageButton
    private lateinit var btnAbout: ImageButton

    private val handler = Handler(Looper.getMainLooper())
    private val arScale = 4.0 // Scale factor for positioning AR objects

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_ar)

        // Initialize permission manager
        permissionManager = PermissionsManager(this, registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionManager.handlePermissionsResult(permissions)
        })

        if (!permissionManager.allPermissionsGranted()) {
            permissionManager.requestPermissions()
        }

        // Inicializar UI componentes
        arFragment = supportFragmentManager.findFragmentById(R.id.fragment) as ArFragment
        btnRefresh = findViewById(R.id.refreshButton)
        btnList = findViewById(R.id.listButton)
        btnLogout = findViewById(R.id.logoutButton)
        btnAbout = findViewById(R.id.aboutButton)

        // Inicializar services
        gpsManager = GpsManager(this)
        gpsManager.getLocation()
        retrofit = RetrofitInitializer(this)
        arUtils = ArUtils(this, arFragment)

        // Login checks and UI updates based on login status
        if (!MyCookieJar(this).isUserLoggedIn()){
            Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
            btnList.visibility = View.GONE
            btnLogout.setImageResource(R.drawable.login_icon)
        }

        // Button actions setup
        btnRefresh.setOnClickListener {
            arUtils.removeAllNodes()
            requestPositions()
            if (!MyCookieJar(this).isUserLoggedIn()){
                Toast.makeText(this, getString(R.string.user_not_logged_in), Toast.LENGTH_SHORT).show()
                btnList.visibility = View.GONE
                btnLogout.setImageResource(R.drawable.login_icon)
            }else{
                btnList.visibility = View.VISIBLE
                btnLogout.setImageResource(R.drawable.logout_icon)
            }
        }
        btnList.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            if (MyCookieJar(this).isUserLoggedIn()){
                logoutUser()
            }else{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

        }

        btnAbout.setOnClickListener {
            val intent = Intent(this, AboutUsActivity::class.java)
            startActivity(intent)
        }

        requestPositions()
    }

    /**
     * Function to request the positions of celestial bodies from the Astronomy API
     */
    private fun requestPositions() {
        val pos = gpsManager.getCurrentLocation()
        if (pos != null) {
            val parameters = AstronomyRequestDTO(
                pos.latitude,
                pos.longitude,
                pos.altitude,
                DateAndTime.getCurrentDate(),
                DateAndTime.getCurrentDate(),
                DateAndTime.getCurrentTime()
            )
            val call = retrofit.AstronomyAPI().getBodyPositions(parameters.toMap())
            call.enqueue(object : Callback<AstronomyPositionResponseDTO?> {
                override fun onResponse(call: Call<AstronomyPositionResponseDTO?>, response: Response<AstronomyPositionResponseDTO?>) {
                    response.body()?.let {
                        val positions = it.data.table.rows
                        Toast.makeText(this@ArActivity, getString(R.string.positions_received), Toast.LENGTH_SHORT).show()
                        transformData(positions)
                    }
                }

                override fun onFailure(call: Call<AstronomyPositionResponseDTO?>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@ArActivity, getString(R.string.positions_error), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * Function to transform the data received from the Astronomy API into AR positions
     */
    private fun transformData(positions: List<AstronomyPositionResponseDTO.Data.Table.Row>) {
        fun degreesToRadians(degrees: Double): Double {
            return degrees * (PI / 180.0)
        }
        this.positions = emptyList()
        positions.forEach {
            val aux = it.cells.first().position.horizonal
            val azimuthRadians = degreesToRadians(aux.azimuth.degrees)
            val altitudeRadians = degreesToRadians(aux.altitude.degrees)
            val position = PositionData(
                it.entry.name,
                (cos(altitudeRadians) * sin(azimuthRadians) * arScale).toFloat(),
                (sin(altitudeRadians) * arScale).toFloat(),
                (cos(altitudeRadians) * cos(azimuthRadians)* arScale).toFloat()
            )
            this.positions += position
        }
        positionNodes()
    }

    /**
     * Function to position the AR nodes in the scene
     */
    private fun positionNodes(){
        if (positions.isEmpty() && !arUtils.checkARCoreAvailability(this)){
            handler.postDelayed({positionNodes()} , 1000)
        }else {
            this.positions.forEach {
                arUtils.addSphereToPosition(
                    it.name,
                    it.x,
                    it.y,
                    it.z
                )
            }
        }

    }

    /**
     * Function to logout the user from the application
     */
    private fun logoutUser() {
        retrofit.API().logoutUser().enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(
                call: Call<SimpleResponseDTO>,
                response: Response<SimpleResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val logoutResponse = response.body()
                    if (logoutResponse != null && logoutResponse.ok) {
                        // Clear cookies
                        MyCookieJar(this@ArActivity).clearCookies(getString(R.string.ourAPI).toHttpUrlOrNull()!!)

                        Toast.makeText(
                            this@ArActivity,
                            getString(R.string.logged_out_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@ArActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@ArActivity,
                            logoutResponse?.error ?: getString(R.string.unknown_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                Toast.makeText(
                    this@ArActivity,
                    getString(R.string.network_error)+": ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Function to handle error responses from the API
     */
    private fun handleErrorResponse(response: Response<SimpleResponseDTO>) {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                val gson = Gson()
                val errorResponse: SimpleResponseDTO =
                    gson.fromJson(errorBody, SimpleResponseDTO::class.java)
                Toast.makeText(
                    this@ArActivity,
                    errorResponse.error ?: getString(R.string.unknown_error),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: JsonSyntaxException) {
                Toast.makeText(
                    this@ArActivity,
                    getString(R.string.error_parsing_response)+": $errorBody",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IllegalStateException) {
                // Handle case where response is not a JSON object
                Toast.makeText(
                    this@ArActivity,
                    getString(R.string.unexpected_response_format)+": $errorBody",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this@ArActivity, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Data class to represent the position of a celestial body in AR
 */
data class PositionData(
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float
)
