package com.example.thetaskmanagerapp

import com.example.thetaskmanagerapp.data.TimetableRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TimetableApiTest {

    @Test
    fun getTimetable_returnsFakeData() = runBlocking {

        val api = FakeTimetableApi()

        val request = TimetableRequest(
            startDate = "2026-04-21",
            endDate = "2026-04-22",
            studentGroups = listOf("Group5")
        )

        val response = api.getTimetable(
            authHeader = "Bearer test",
            request = request
        )

        val reservations = response.getReservations()

        assertNotNull(reservations)
        assertEquals(1, reservations.size)
        assertEquals("Android Development", reservations[0].subject)
    }
}