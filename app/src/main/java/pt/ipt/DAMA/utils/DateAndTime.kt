package pt.ipt.DAMA.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DateAndTime {
    companion object {

        /**
         * Get the current date in the format "yyyy-MM-dd"
         */
        fun getCurrentDate(): String {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return currentDate.format(formatter)
        }

        /**
         * Get the current time in the format "HH:mm:ss"
         */
        fun getCurrentTime(): String {
            val currentTime = LocalTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            return currentTime.format(formatter)
        }
    }
}