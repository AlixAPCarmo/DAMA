package pt.ipt.DAMA.model.pexelsImageAPI

import com.google.gson.annotations.SerializedName

data class ImageResponseDTO(
    @SerializedName("page") val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("photos") val photos: List<PhotoDetails>,
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("next_page") val nextPage: String
) {
    data class PhotoDetails(
        @SerializedName("id") val id: Int,
        @SerializedName("width") val width: Int,
        @SerializedName("height") val height: Int,
        @SerializedName("url") val url: String,
        @SerializedName("photographer") val photographer: String,
        @SerializedName("photographer_url") val photographerUrl: String,
        @SerializedName("photographer_id") val photographerId: Int,
        @SerializedName("avg_color") val avgColor: String,
        @SerializedName("src") val src: Source,
        @SerializedName("liked") val liked: Boolean,
        @SerializedName("alt") val alt: String
    ) {
        data class Source(
            @SerializedName("original") val original: String,
            @SerializedName("large2x") val large2x: String,
            @SerializedName("large") val large: String,
            @SerializedName("medium") val medium: String,
            @SerializedName("small") val small: String,
            @SerializedName("portrait") val portrait: String,
            @SerializedName("landscape") val landscape: String,
            @SerializedName("tiny") val tiny: String
        )
    }
}
