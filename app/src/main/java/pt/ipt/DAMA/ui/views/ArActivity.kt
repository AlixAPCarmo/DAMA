package pt.ipt.DAMA.ui.views

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.sceneform.ux.ArFragment
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.GpsManager
import pt.ipt.DAMA.model.astronomyAPI.AstronomyPositionResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyRequestDTO
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.utils.ArUtils
import pt.ipt.DAMA.utils.DateAndTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ArActivity : AppCompatActivity() {
    private lateinit var gpsManager: GpsManager
    private lateinit var arUtils: ArUtils

    private lateinit var retrofit: RetrofitInitializer
    private var positions: List<PositionData> = emptyList()
    private lateinit var arFragment: ArFragment
    private val cameraPermissionCode = 101
    private val handler = Handler(Looper.getMainLooper())
    private val arScale = 4.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_ar)

        // Inicializar componentes de visualização
        arFragment = supportFragmentManager.findFragmentById(R.id.fragment) as ArFragment

        // Inicializar variáveis
        gpsManager = GpsManager(this)
        gpsManager.getLocation()
        retrofit = RetrofitInitializer(this)
        arUtils = ArUtils(this, arFragment)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
        } else {
            requestPositions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission is granted", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show()
        }
    }

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
}

data class PositionData(
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float
)
