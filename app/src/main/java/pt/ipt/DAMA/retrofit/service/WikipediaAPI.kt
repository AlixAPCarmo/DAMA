package pt.ipt.DAMA.retrofit.service

import pt.ipt.DAMA.model.wikipédia.WikipediaResponseDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaAPI {
    @GET("api.php")
    fun getSearch(
        @Query("titles") titles: String,
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "extracts|pageimages",
        @Query("exsentences") exsentences: Int = 10,
        @Query("exlimit") exlimit: Int = 1,
        @Query("explaintext") explaintext: Int = 1,
        @Query("piprop") piprop: String = "original",
        @Query("pithumbsize") pithumbsize: Int = 500,
        @Query("pilimit") pilimit: Int = 10,
        @Query("formatversion") formatversion: Int = 2,
        @Query("format") format: String = "json"
    ): Call<WikipediaResponseDTO>
}
