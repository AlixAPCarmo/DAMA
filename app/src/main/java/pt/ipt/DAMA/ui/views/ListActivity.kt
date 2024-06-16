package pt.ipt.DAMA.ui.views

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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
import pt.ipt.DAMA.model.API.CelestialCreateRequestDTO
import pt.ipt.DAMA.model.API.CelestialDeleteRequestDTO
import pt.ipt.DAMA.model.API.CelestialFindRequestDTO
import pt.ipt.DAMA.model.API.SimpleResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyPositionResponseDTO
import pt.ipt.DAMA.model.astronomyAPI.AstronomyRequestDTO
import pt.ipt.DAMA.model.wikipédia.WikipediaResponseDTO
import pt.ipt.DAMA.retrofit.MyCookieJar
import pt.ipt.DAMA.retrofit.RetrofitInitializer
import pt.ipt.DAMA.ui.adapter.ItemList
import pt.ipt.DAMA.ui.adapter.ListAdapter
import pt.ipt.DAMA.utils.DateAndTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListAdapter
    private lateinit var gpsManager: GpsManager

    private lateinit var retrofit: RetrofitInitializer
    private var listItems = mutableListOf<ItemList>()

    private lateinit var btnAR: LinearLayout
    private lateinit var btnLogout: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var searchBarLayout: LinearLayout

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_list)

        // Setup RecyclerView with a LinearLayoutManager
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListAdapter(listItems, this) { item -> findByNameandUser(item) }
        recyclerView.adapter = adapter

        retrofit = RetrofitInitializer(this)
        gpsManager = GpsManager(this)

        btnAR = findViewById(R.id.ar_button)
        btnAR.setOnClickListener {
            val intent = Intent(this, ArActivity::class.java)
            startActivity(intent)
        }
        // Setup Logout button to logout the user
        btnLogout = findViewById(R.id.logout_button)
        btnLogout.setOnClickListener { logoutUser() }

        btnBack = findViewById(R.id.back_button)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // if user not logged in, search button is hidden
        if (!MyCookieJar(this).isUserLoggedIn()) {
            searchBarLayout = findViewById(R.id.search_bar_layout)
            searchBarLayout.visibility = LinearLayout.GONE
        } else {
            searchInput = findViewById(R.id.search_input)
            searchButton = findViewById(R.id.search_button)
            searchButton.setOnClickListener {
                val searchTerm = searchInput.text.toString()
                if (searchTerm.isNotEmpty()) {
                    addSearchResult(searchTerm)
                } else {
                    logMessage("Please enter a search term")
                }
            }
            loadAllCelestialObjects()
        }

        loadDataName()
    }

    /**
     * Load data for celestial objects from the Astronomy API
     */
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
                        // Load data from the Astronomy API
                        res.data.table.rows.forEach {
                            count++
                            val name = it.entry.name
                            listItems.add(ItemList(name, "", false, 0, Runnable {
                                val intent = Intent(this@ListActivity, CelestialActivity::class.java)
                                intent.putExtra("name", name)
                                startActivity(intent)
                            }))
                            loadDataImage(name, count >= size)
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

    private fun loadAllCelestialObjects() {
        val call = retrofit.API().getAllCelestialObjects()
        call.enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(call: Call<SimpleResponseDTO>, response: Response<SimpleResponseDTO>) {
                if (response.isSuccessful) {
                    val celestialObjects = response.body()?.data as? List<Map<String, Any>>
                    Log.e("ListActivity", "Celestial objects: $celestialObjects")
                    celestialObjects?.forEach { item ->
                        val name = item["name"] as? String ?: ""
                        val id = item["id"] as? Int ?: 0
                        listItems.add(ItemList(name, "", true, id, Runnable {
                            val intent = Intent(this@ListActivity, CelestialActivity::class.java)
                            intent.putExtra("name", name)
                            startActivity(intent)
                        }))
                    }
                    adapter.notifyDataSetChanged()
                    listItems.forEach { item ->
                        if (item.isFromApi) {
                            loadDataImage(item.name, false)
                        }
                    }
                } else {
                    logMessage("Failed to fetch celestial objects")
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                t.printStackTrace()
                logMessage("Error fetching celestial objects")
            }
        })
    }

    private fun loadDataImage(query: String, end: Boolean) {
        retrofit.ImageAPI().searchPhotos("planet $query").enqueue(object : Callback<ImageResponseDTO> {
            override fun onResponse(call: Call<ImageResponseDTO>, response: Response<ImageResponseDTO>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.photos.isNotEmpty()) {
                        val photoUrl = data.photos[0].src.medium
                        val existingItem = listItems.find { it.name == query }
                        if (existingItem != null) {
                            existingItem.imageResource = photoUrl
                            logMessage("Image loaded for $query: $photoUrl")
                            adapter.notifyDataSetChanged()
                        } else {
                            logMessage("Existing item not found for $query")
                        }
                    } else {
                        logMessage("No images found for $query")
                    }
                } else {
                    logMessage("Failed to fetch image for $query")
                }
            }

            override fun onFailure(call: Call<ImageResponseDTO>, t: Throwable) {
                t.printStackTrace()
                logMessage("Error fetching image for $query")
            }
        })
    }

    private fun addSearchResult(query: String) {
        val newCelestial = CelestialCreateRequestDTO(name = query)
        retrofit.API().createCelestialObject(newCelestial).enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(call: Call<SimpleResponseDTO>, response: Response<SimpleResponseDTO>) {
                if (response.isSuccessful) {
                    loadAllCelestialObjects()
                } else {
                    logMessage("Failed to add celestial object")
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                t.printStackTrace()
                logMessage("Error adding celestial object")
            }
        })
    }

    private fun deleteCelestialObject(id: Int) {
        val deleteRequest = CelestialDeleteRequestDTO(id = id)
        Log.d("ListActivity", "Sending delete request for item: ${id}")

        retrofit.API().deleteCelestialObject(deleteRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("ListActivity", "Delete response code: ${response.code()}")
                if (response.isSuccessful) {
                    logMessage("Celestial object deleted successfully")
                } else {
                    Log.e("ListActivity", "Failed to delete celestial object, response code: ${response.code()}, response message: ${response.message()}")
                    logMessage("Failed to delete celestial object")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ListActivity", "Error deleting celestial object", t)
                logMessage("Error deleting celestial object")
            }
        })
    }

    private fun findByNameandUser(item: ItemList) {
        // remove "" from item.name
        val name = item.name.replace("\"", "")

        val findRequest = CelestialFindRequestDTO(name = name)
        val call = retrofit.API().findCelestialObject(findRequest)
        call.enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(call: Call<SimpleResponseDTO>, response: Response<SimpleResponseDTO>) {
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    logMessage("Celestial object found: $data")
                    val celestialId = (data as? Double)?.toInt()
                    logMessage("Celestial object ID: $celestialId")
                    if (celestialId != null) {
                        listItems.remove(item)
                        adapter.notifyDataSetChanged()
                        deleteCelestialObject(celestialId)
                    } else {
                        logMessage("Celestial object ID is null")
                    }
                } else {
                    logMessage("Failed to fetch celestial objects")
                    logMessage(response.errorBody()?.string() ?: "No error message")
                }
            }

            override fun onFailure(call: Call<SimpleResponseDTO>, t: Throwable) {
                t.printStackTrace()
                logMessage("Error fetching celestial objects")
            }
        })
    }

    /**
     * Logout the user
     */
    private fun logoutUser() {
        retrofit.API().logoutUser().enqueue(object : Callback<SimpleResponseDTO> {
            override fun onResponse(call: Call<SimpleResponseDTO>, response: Response<SimpleResponseDTO>) {
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

    private fun logMessage(message: String) {
        Log.d("ListActivity", message)
    }
}
