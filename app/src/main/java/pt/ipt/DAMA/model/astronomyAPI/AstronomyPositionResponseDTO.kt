package pt.ipt.DAMA.model.astronomyAPI

import com.google.gson.annotations.SerializedName

data class AstronomyPositionResponseDTO(
    @SerializedName("data") val data: Data
){
    data class Data(
        @SerializedName("dates") val dates: Dates,
        @SerializedName("observer") val observer: Observer,
        @SerializedName("table") val table: Table
    ) {
        data class Dates(
            @SerializedName("from") val from: String,
            @SerializedName("to") val to: String
        )

        data class Observer(
            @SerializedName("location") val location: Location
        ) {
            data class Location(
                @SerializedName("longitude") val longitude: Double,
                @SerializedName("latitude") val latitude: Double,
                @SerializedName("elevation") val elevation: Double
            )
        }

        data class Table(
            @SerializedName("header") val header: List<String>,
            @SerializedName("rows") val rows: List<Row>
        ) {
            data class Row(
                @SerializedName("entry") val entry: Entry,
                @SerializedName("cells") val cells: List<Cell>
            ) {
                data class Entry(
                    @SerializedName("id") val id: String,
                    @SerializedName("name") val name: String
                )

                data class Cell(
                    @SerializedName("date") val date: String,
                    @SerializedName("id") val id: String,
                    @SerializedName("name") val name: String,
                    @SerializedName("distance") val distance: Distance,
                    @SerializedName("position") val position: Position,
                    @SerializedName("extraInfo") val extraInfo: ExtraInfo
                ) {
                    data class Distance(
                        @SerializedName("fromEarth") val fromEarth: FromEarth
                    ) {
                        data class FromEarth(
                            @SerializedName("au") val au: String,
                            @SerializedName("km") val km: String
                        )
                    }

                    data class Position(
                        @SerializedName("horizontal") val horizontal: Horizontal,
                        @SerializedName("horizonal") val horizonal: Horizontal,
                        @SerializedName("equatorial") val equatorial: Equatorial,
                        @SerializedName("constellation") val constellation: Constellation
                    ) {
                        data class Horizontal(
                            @SerializedName("altitude") val altitude: Altitude,
                            @SerializedName("azimuth") val azimuth: Azimuth
                        ) {
                            data class Altitude(
                                @SerializedName("degrees") val degrees: Double,
                                @SerializedName("string") val string: String
                            )

                            data class Azimuth(
                                @SerializedName("degrees") val degrees: Double,
                                @SerializedName("string") val string: String
                            )
                        }

                        data class Equatorial(
                            @SerializedName("rightAscension") val rightAscension: RightAscension,
                            @SerializedName("declination") val declination: Declination
                        ) {
                            data class RightAscension(
                                @SerializedName("hours") val hours: Double,
                                @SerializedName("string") val string: String
                            )

                            data class Declination(
                                @SerializedName("degrees") val degrees: Double,
                                @SerializedName("string") val string: String
                            )
                        }

                        data class Constellation(
                            @SerializedName("id") val id: String,
                            @SerializedName("short") val short: String,
                            @SerializedName("name") val name: String
                        )
                    }

                    data class ExtraInfo(
                        @SerializedName("elongation") val elongation: Double?,
                        @SerializedName("magnitude") val magnitude: Double?,
                        @SerializedName("phase") val phase: Phase?
                    ) {
                        data class Phase(
                            @SerializedName("angel") val angel: Double,
                            @SerializedName("fraction") val fraction: String,
                            @SerializedName("string") val string: String
                        )
                    }
                }
            }
        }
    }
}
