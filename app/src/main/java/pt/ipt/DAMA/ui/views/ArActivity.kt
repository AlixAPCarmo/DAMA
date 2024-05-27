package pt.ipt.DAMA.ui.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.GpsManager
import pt.ipt.DAMA.model.astronomyAPI.AstronomyPositionResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyRequestDTO
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.utils.DateAndTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ArActivity: AppCompatActivity()  {
    private lateinit var gpsManager: GpsManager
    private lateinit var retrofit: RetrofitInitializer
    private lateinit var positions: List<PositionData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        //View components
        //inicialize vars
        gpsManager = GpsManager(this)
        retrofit = RetrofitInitializer(this)

        requestPositions()
    }

    private fun requestPositions(){
        val parameters = AstronomyRequestDTO(
            gpsManager.getCurrentLocation()!!.latitude,
            gpsManager.getCurrentLocation()!!.longitude,
            gpsManager.getCurrentLocation()!!.altitude,
            DateAndTime.getCurrentDate(),
            DateAndTime.getCurrentDate(),
            DateAndTime.getCurrentTime())
        val call = retrofit.AstronomyAPI().getBodyPositions(parameters.toMap())
        call.enqueue(object : Callback<AstronomyPositionResponseDTO?> {
            override fun onResponse(call: Call<AstronomyPositionResponseDTO?>, response: Response<AstronomyPositionResponseDTO?>) {
                response.body()?.let {
                    val positions = it.data.table.rows
                    Toast.makeText(this@ArActivity, this@ArActivity.getString(R.string.positions_received), Toast.LENGTH_SHORT).show()
                    transformData(positions)
                }
            }
            override fun onFailure(call: Call<AstronomyPositionResponseDTO?>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@ArActivity, this@ArActivity.getString(R.string.positions_error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun transformData(positions: List<AstronomyPositionResponseDTO.Data.Table.Row>){
        fun degreesToRadians(degrees: Double): Double {
            return degrees * (PI / 180.0)
        }

        positions.forEach {
            val aux = it.cells.first().position.horizonal
            val azimuthRadians = degreesToRadians(aux.azimuth.degrees)
            val altitudeRadians = degreesToRadians(aux.altitude.degrees)
            val position = PositionData(
                it.entry.name,
                cos(altitudeRadians) * sin(azimuthRadians),
                sin(altitudeRadians),
                cos(altitudeRadians) * cos(azimuthRadians)
            )
            this.positions += position
        }

    }


}

data class PositionData(
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double
)