package pt.ipt.DAMA.model.astronomyAPI

import com.google.gson.annotations.SerializedName
import java.sql.Time
import java.util.Date

data class AstronomyRequestDTO(
    @SerializedName("latitude") var latitude : Double,
    @SerializedName("longitude") var longitude : Double,
    @SerializedName("elevation") val elevation : Double,
    @SerializedName("from_date") val fromDate : String,
    @SerializedName("to_date") val toDate : String,
    @SerializedName("time") val time : String
){
    fun toMap(): Map<String, String> {
        return mapOf(
            "latitude" to latitude.toString(),
            "longitude" to longitude.toString(),
            "elevation" to elevation.toString(),
            "from_date" to fromDate,
            "to_date" to toDate,
            "time" to time
        )
    }
}