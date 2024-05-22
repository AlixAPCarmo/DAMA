package pt.ipt.DAMA.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.CameraManager
import pt.ipt.DAMA.hardware.GpsManager
import pt.ipt.DAMA.model.astronomyAPI.AstronomyPositionResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyRequestDTO
import pt.ipt.DAMA.model.pexelsImageAPI.ImageResponseDTO
import pt.ipt.DAMA.model.wikipédia.WikipediaResponseDTO
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    private lateinit var gpsManager: GpsManager
    private lateinit var cameraManager: CameraManager
    private lateinit var btnLocation: Button
    private lateinit var txt: TextView
    private lateinit var btnpositions : Button
    private lateinit var img : ImageView
    private lateinit var btnImage : Button
    private lateinit var txt2 : TextView
    private lateinit var btnWiki : Button
    private lateinit var astroResponse : AstronomyPositionResponseDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gpsManager = GpsManager(this)
        gpsManager.getLocation()
        cameraManager = CameraManager(this)
        btnLocation = findViewById(R.id.getLocation)
        txt = findViewById(R.id.text)
        btnpositions = findViewById(R.id.getPositions)
        img = findViewById(R.id.imageView)
        btnImage = findViewById(R.id.getImage)
        txt2 = findViewById(R.id.text2)
        btnWiki = findViewById(R.id.getWiki)

        btnLocation.setOnClickListener {
            var currentLocation = gpsManager.getCurrentLocation()

            if (currentLocation != null) {
                txt.text = "Latitude: ${currentLocation.latitude}\n " +
                        "Longitude: ${currentLocation.longitude}\n"+
                        "Altitude: ${currentLocation.altitude}"
            }else{
                txt.text = "Location not found"
            }
        }

        btnpositions.setOnClickListener {
            val parameters = gpsManager.getCurrentLocation()?.let { it1 ->
                AstronomyRequestDTO(
                    it1.latitude,
                    it1.longitude,
                    it1.altitude,
                    getCurrentDate(),
                    getCurrentDate(),
                    getCurrentTime()
                )
            }
            val call = parameters?.let { it1 ->
                RetrofitInitializer().AstronomyAPI().getBodyPositions(
                    it1.toMap())
            }
            if (call != null) {
                processResponse(call)


            }
        }

        btnImage.setOnClickListener {
            val callImage = RetrofitInitializer().ImageAPI()
                .searchPhotos(astroResponse.data.table.rows[0].entry.name)
            processResponseImg(callImage)
        }

        btnWiki.setOnClickListener {
            val callWiki = RetrofitInitializer().WikiAPI().getSearch(astroResponse.data.table.rows[0].entry.name)
            callWiki.enqueue(object : Callback<WikipediaResponseDTO?> {
                override fun onResponse(
                    call: Call<WikipediaResponseDTO?>,
                    response: Response<WikipediaResponseDTO?>
                ) {
                    response.body()?.let { res ->
                        txt2.text = res.query.pages[0].extract
                    } ?: run {
                        Log.e("Response Error", "Received null body in successful response")
                    }
                }
                override fun onFailure(call: Call<WikipediaResponseDTO?>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
        }

    }

    private fun processResponseImg(call: Call<ImageResponseDTO>) {
        call.enqueue(object : Callback<ImageResponseDTO?> {
            override fun onResponse(
                call: Call<ImageResponseDTO?>,
                response: Response<ImageResponseDTO?>
            ) {
                response.body()?.let { res ->
                    Glide.with(this@MainActivity)
                        .load(res.photos[0].src.small)
                        .into(img)
                } ?: run {
                    Log.e("Response Error", "Received null body in successful response")
                }
            }

            override fun onFailure(call: Call<ImageResponseDTO?>, t: Throwable) {
                TODO("Not yet implemented")
            }


        })

    }

    private fun processResponse(call: Call<AstronomyPositionResponseDTO>){
        call.enqueue(object : Callback<AstronomyPositionResponseDTO?> {
            override fun onResponse(
                call: Call<AstronomyPositionResponseDTO?>,
                response: Response<AstronomyPositionResponseDTO?>
            ) {
                response.body()?.let { posits ->
                    astroResponse = posits
                    putTextInView(posits)
                } ?: run {
                    Log.e("Response Error", "Received null body in successful response")
                }

            }

            override fun onFailure(call: Call<AstronomyPositionResponseDTO?>, t: Throwable) {
                t.printStackTrace()
                t.message?.let { Log.e("onFailure error", it) }
            }

        })
    }

    fun putTextInView(dto: AstronomyPositionResponseDTO){
        """${dto.data.table.rows[0].entry.name} 
                |${dto.data.table.rows[0].cells[0].date} 
                |${dto.data.table.rows[0].cells[0].position.horizontal}""".trimMargin()
            .also { txt.text = it }

    }

    private fun getCurrentTime(): String {
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return currentTime.format(formatter)
    }

    private fun getCurrentDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
    }
}

