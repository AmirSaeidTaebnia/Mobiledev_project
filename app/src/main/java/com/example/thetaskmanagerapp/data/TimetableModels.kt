package com.example.thetaskmanagerapp.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime

// UI Model
data class UIReservation(
    val id: String,
    val subject: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String,
    val location: String
)

// Navigation Model
sealed class Screen {
    object TaskList : Screen()
    data class AddEditTask(val task: Task? = null) : Screen()
    object DoneTasks : Screen()
    object Calendar : Screen()
    object Timetable : Screen()
    object Notifications : Screen() // 🔔 New notifications screen
}

// API Models
data class TimetableResponse(
    @SerializedName("reservations")
    val reservationsList: List<Reservation> = emptyList(),
    @SerializedName("data")
    val data: List<Reservation>? = null
) {
    fun getReservations(): List<Reservation> = reservationsList.ifEmpty { data ?: emptyList() }
}

data class Reservation(
    @SerializedName("id") val id: String = "",
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("startDate") val startDate: String = "",
    @SerializedName("endDate") val endDate: String = "",
    @SerializedName("resources") val resources: List<Resource>? = null
)

data class Resource(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("code") val code: String?,
    @SerializedName("name") val name: String?
)

data class TimetableRequest(
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("studentGroups") val studentGroups: List<String>? = null,
    @SerializedName("studentGroup") val studentGroup: List<String>? = null,
    @SerializedName("realization") val realization: List<String>? = null
)

class TimetableResponseDeserializer : JsonDeserializer<TimetableResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TimetableResponse {
        val reservations = mutableListOf<Reservation>()
        try {
            if (json.isJsonObject) {
                val obj = json.asJsonObject
                val arr = when {
                    obj.has("reservations") -> obj.getAsJsonArray("reservations")
                    obj.has("data") -> obj.getAsJsonArray("data")
                    else -> null
                }
                arr?.forEach { reservations.add(context.deserialize(it, Reservation::class.java)) }
            } else if (json.isJsonArray) {
                json.asJsonArray.forEach { reservations.add(context.deserialize(it, Reservation::class.java)) }
            }
        } catch (e: Exception) {}
        return TimetableResponse(reservationsList = reservations)
    }
}
