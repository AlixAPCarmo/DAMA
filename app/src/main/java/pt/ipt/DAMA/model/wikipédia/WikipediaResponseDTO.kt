package pt.ipt.DAMA.model.wikipédia

import com.google.gson.annotations.SerializedName

data class WikipediaResponseDTO(
    @SerializedName("batchcomplete") val batchComplete: Boolean,
    @SerializedName("query") val query: WikiQuery
)

data class WikiQuery(
    @SerializedName("pages") val pages: List<WikiPage>
)

data class WikiPage(
    @SerializedName("pageid") val pageId: Int,
    @SerializedName("ns") val ns: Int,
    @SerializedName("title") val title: String,
    @SerializedName("extract") val extract: String
)
