package com.example.thetaskmanagerapp.viewmodel

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thetaskmanagerapp.data.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters

class TimetableViewModel : ViewModel() {
    var uiData by mutableStateOf<List<UIReservation>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var classCode by mutableStateOf("ICT24K-SW")
        private set

    var currentWeekStart by mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
        private set

    private val apiKey = "phFFkNRbJi48HZkVwoOX"

    private val api: TimetableApi = Retrofit.Builder()
        .baseUrl("https://opendata.metropolia.fi/")
        .client(OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor { Log.d("OkHttp", it) }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build())
        .addConverterFactory(GsonConverterFactory.create(com.google.gson.GsonBuilder()
            .registerTypeAdapter(TimetableResponse::class.java, TimetableResponseDeserializer())
            .create()))
        .build()
        .create(TimetableApi::class.java)

    fun updateClassCode(newCode: String) {
        classCode = newCode
    }

    fun changeWeek(weeks: Long) {
        currentWeekStart = currentWeekStart.plusWeeks(weeks)
        fetchTimetable()
    }

    fun fetchTimetable() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val authHeader = "Basic " + Base64.encodeToString("$apiKey:".toByteArray(), Base64.NO_WRAP)
                val requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val request = TimetableRequest(
                    startDate = currentWeekStart.format(requestFormatter),
                    endDate = currentWeekStart.plusDays(7).format(requestFormatter),
                    studentGroups = listOf(classCode)
                )

                val response = api.getTimetable(authHeader, request)
                val reservations = response.getReservations()

                val itemFormatter = DateTimeFormatterBuilder()
                    .append(DateTimeFormatter.ISO_LOCAL_DATE)
                    .appendLiteral('T')
                    .appendValue(ChronoField.HOUR_OF_DAY, 2)
                    .appendLiteral(':')
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                    .optionalStart().appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalEnd()
                    .toFormatter()

                uiData = reservations.mapNotNull { res ->
                    val resourceMatch = res.resources?.any { 
                        it.code?.equals(classCode, ignoreCase = true) == true 
                    } ?: false
                    
                    val textMatch = res.subject?.contains(classCode, ignoreCase = true) == true ||
                                   res.description?.contains(classCode, ignoreCase = true) == true

                    if (!resourceMatch && !textMatch) return@mapNotNull null

                    try {
                        fun clean(s: String) = s.trim().replace(" ", "T")
                            .substringBefore("+").substringBefore("Z").substringBefore("[")
                        
                        val startDt = LocalDateTime.parse(clean(res.startDate), itemFormatter)
                        val endDt = LocalDateTime.parse(clean(res.endDate), itemFormatter)
                        val loc = res.resources?.firstOrNull { it.type == "room" }?.code ?: ""

                        UIReservation(res.id, res.subject ?: "No Title", startDt.toLocalDate(),
                            startDt.toLocalTime(), endDt.toLocalTime(), res.description ?: "", loc)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedWith(compareBy({ it.date }, { it.startTime }))

                if (uiData.isEmpty()) {
                    errorMessage = "No lessons found for $classCode this week."
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
