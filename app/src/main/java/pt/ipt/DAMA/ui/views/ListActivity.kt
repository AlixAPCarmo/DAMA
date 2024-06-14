package pt.ipt.DAMA.ui.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import pt.ipt.DAMA.R
import pt.ipt.DAMA.hardware.GpsManager
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyPositionResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyRequestDTO
import pt.ipt.DAMA.model.pexelsImageAPI.ImageResponseDTO
import pt.ipt.DAMA.model.wikipédia.WikipediaResponseDTO
import pt.ipt.DAMA.retrofit.MyCookieJar
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.ui.adapter.ItemList
import pt.ipt.DAMA.ui.adapter.ListAdapter
import pt.ipt.DAMA.utils.DateAndTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListAdapter
    private lateinit var gpsManager: GpsManager

    private lateinit var retrofit: RetrofitInitializer
    private var listItens = emptyArray<ItemList>()

    private lateinit var btnAR: Button
    private lateinit var btnLogout: Button

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        retrofit = RetrofitInitializer(this)
        gpsManager = GpsManager(this)

        btnAR = findViewById(R.id.arButton)
        btnAR.setOnClickListener {
            val intent = Intent(this, ArActivity::class.java)
            startActivity(intent)
        }
        btnLogout = findViewById(R.id.logoutButton)
        btnLogout.setOnClickListener {
            logoutUser()
        }
        loadDataName()
    }

    private fun loadDataName(){
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
                    response.body()?.let { res ->
                        val size = res.data.table.rows.size
                        var count = 0
                        res.data.table.rows.forEach {
                            count++
                            loadDataImage(it.entry.name, count >= size)
                            Thread.sleep(100)
                        }

                    }
                }

                override fun onFailure(call: Call<AstronomyPositionResponseDTO?>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@ListActivity, getString(R.string.positions_error), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun loadDataImage(query: String, end: Boolean){
        retrofit.WikiAPI().getSearch(titles = query).enqueue(object : Callback<WikipediaResponseDTO> {
            override fun onResponse(
                call: Call<WikipediaResponseDTO>,
                response: Response<WikipediaResponseDTO>
            ) {
                if (response.isSuccessful) {
                    val wikiResponse = response.body()
                    if (wikiResponse != null && wikiResponse.query.pages.isNotEmpty()) {
                        val page = wikiResponse.query.pages[0]
                        listItens += ItemList(
                            query,
                            (page.thumbnail?.source ?: page.original?.source).toString()
                        ){
                            val intent = Intent(this@ListActivity, MainActivity::class.java)
                            intent.putExtra("planet", query)
                            startActivity(intent)
                            TODO("Alterar para a pagina de more info")
                        }
                        if(end){
                            adapter = ListAdapter(listItens.toList(), this@ListActivity)
                            recyclerView.adapter = adapter
                        }
                    } else {
                        Toast.makeText(
                            this@ListActivity,
                            getString(R.string.no_content_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ListActivity,
                        getString(R.string.error)+": ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<WikipediaResponseDTO>, t: Throwable) {
                Toast.makeText(
                    this@ListActivity,
                    getString(R.string.network_error)+": ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

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
                        MyCookieJar(this@ListActivity).clearCookies(getString(R.string.ourAPI).toHttpUrlOrNull()!!)

                        Toast.makeText(
                            this@ListActivity,
                            getString(R.string.logged_out_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@ListActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@ListActivity,
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
                    this@ListActivity,
                    getString(R.string.network_error)+": ${t.message}",
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
                    this@ListActivity,
                    errorResponse.error ?: getString(R.string.unknown_error),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: JsonSyntaxException) {
                Toast.makeText(
                    this@ListActivity,
                    getString(R.string.error_parsing_response)+": $errorBody",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IllegalStateException) {
                // Handle case where response is not a JSON object
                Toast.makeText(
                    this@ListActivity,
                    getString(R.string.unexpected_response_format)+": $errorBody",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this@ListActivity, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
        }
    }
}

