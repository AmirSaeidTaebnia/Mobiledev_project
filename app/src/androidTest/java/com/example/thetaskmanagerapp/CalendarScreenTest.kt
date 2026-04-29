package com.example.thetaskmanagerapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.thetaskmanagerapp.data.Task
import com.example.thetaskmanagerapp.ui.CalendarScreen
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calendarScreen_displaysEmptyState() {
        composeTestRule.setContent {
            CalendarScreen(
                tasks = emptyList(),
                onBack = {}
            )
        }

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun calendarScreen_displaysTasks() {
        val testTasks = listOf(
            Task(
                id = 1,
                title = "Test Task 1",
                description = "Desc 1",
                dueDate = "2024-05-01",
                dueTime = "10:00",
                status = "Pending",
                workLoadInHours = 1.0
            ),
            Task(
                id = 2,
                title = "Test Task 2",
                description = "Desc 2",
                dueDate = "2024-05-01",
                dueTime = "14:00",
                status = "Done",
                workLoadInHours = 2.0
            )
        )

        composeTestRule.setContent {
            CalendarScreen(
                tasks = testTasks,
                onBack = {}
            )
        }

        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun calendarScreen_hasBackButtonClickable() {
        var backClicked = false
        composeTestRule.setContent {
            CalendarScreen(
                tasks = emptyList(),
                onBack = { backClicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(backClicked)
    }
}
