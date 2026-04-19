package com.example.thetaskmanagerapp

import com.example.thetaskmanagerapp.data.*
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.Base64

class TimetableApiTest {

    private val apiKey = ""
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(TimetableResponse::class.java, TimetableResponseDeserializer())
        .create()

    private val api = Retrofit.Builder()
        .baseUrl("https://opendata.metropolia.fi/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(TimetableApi::class.java)

    @Test
    fun debugApiFiltering() = runBlocking {
        val classCode = "ICT24K-SW"
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        val authHeader = "Basic " + Base64.getEncoder().encodeToString("$apiKey:".toByteArray())

        // Tests with the plural fields which is currently in your models
        val request = TimetableRequest(
            startDate = monday.format(formatter),
            endDate = monday.plusDays(7).format(formatter),
            studentGroups = listOf(classCode)
        )

        val response = api.getTimetable(authHeader, request)
        val reservations = response.getReservations()

        println("--- API DEBUG INFO ---")
        println("Total items returned: ${reservations.size}")
        
        if (reservations.size >= 1000) {
            println("WARNING: API returned 1000 items. This means the 'studentGroups' filter was likely IGNORED.")
        }

        // Prints the first 5 items to see what kind of data we are getting
        println("\nSample of first 5 items:")
        reservations.take(5).forEach { res ->
            val resources = res.resources?.joinToString { "${it.type}:${it.code}" }
            println("ID: ${res.id} | Subject: ${res.subject} | Resources: [$resources]")
        }

        // Checks if any of the items actually match the class code
        val matchingItems = reservations.filter { res ->
            res.resources?.any { it.code?.equals(classCode, ignoreCase = true) == true } == true
        }

        println("\n--- FILTERING RESULTS ---")
        println("Items matching $classCode: ${matchingItems.size}")
        
        matchingItems.forEach { 
            println("MATCH FOUND: ${it.startDate} - ${it.subject}")
        }

        // Assertions
        assertNotNull(response)
        // If this fails, it proves that the api is returning the wrong data
        assertTrue("API should return at least one item for $classCode", matchingItems.isNotEmpty())
    }
}
