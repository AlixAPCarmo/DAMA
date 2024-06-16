package pt.ipt.DAMA.model.API

import com.google.gson.annotations.SerializedName

// DTO for Celestial Objects
data class CelestialObjectDTO(
    val id: Int,
    val name: String,
    val userId: Int
)


// DTO for creating a new Celestial Object
data class CelestialCreateRequestDTO(
    @SerializedName("name")
    val name: String
)

// DTO for updating an existing Celestial Object
data class CelestialUpdateRequestDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

// DTO for deleting a Celestial Object
data class CelestialDeleteRequestDTO(
    @SerializedName("id")
    val id: Int
)

data class CelestialFindRequestDTO(
    @SerializedName("name")
    val name: String
)
