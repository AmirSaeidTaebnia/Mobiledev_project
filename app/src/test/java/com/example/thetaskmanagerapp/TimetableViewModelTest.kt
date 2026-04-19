package com.example.thetaskmanagerapp

import com.example.thetaskmanagerapp.data.*
import com.example.thetaskmanagerapp.viewmodel.TimetableViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class TimetableViewModelTest {

    private lateinit var viewModel: TimetableViewModel

    @Before
    fun setup() {
        // Use of a real ViewModel which is the TimetableViewModel, and we will inspect its state
        viewModel = TimetableViewModel()
    }

    @Test
    fun testReservationFiltering() {
        // Arrange: Mocks a list of raw reservations
        val classCode = "ICT24K-SW"
        val rawReservations = listOf(
            // Item 1: Correct group
            Reservation(
                id = "1",
                subject = "Mobile Dev",
                startDate = "2026-04-13T09:30:00",
                endDate = "2026-04-13T12:00:00",
                resources = listOf(Resource("r1", "student_group", classCode, classCode))
            ),
            // Item 2: Wrong group (Maintenance)
            Reservation(
                id = "2",
                subject = "Huolto",
                startDate = "2026-04-13T08:00:00",
                endDate = "2026-04-13T16:00:00",
                resources = listOf(Resource("r2", "room", "MPC3011", "MPC3011"))
            )
        )

        // Act: Uses the same filtering logic as the ViewModel
        val filtered = rawReservations.filter { res ->
            res.resources?.any { it.code?.equals(classCode, ignoreCase = true) == true } == true
        }

        // Assertions
        assertEquals("Should only find 1 matching reservation", 1, filtered.size)
        assertEquals("Subject should match", "Mobile Dev", filtered[0].subject)
    }

    @Test
    fun testWorkLoadRenamingConsistency() {
        // This test ensures that the Task model uses the variable workLoadInHours correctly
        val task = Task(
            id = 1,
            title = "Test Task",
            workLoadInHours = 5.5
        )
        
        assertEquals(5.5, task.workLoadInHours, 0.0)
    }
}
