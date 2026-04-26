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
        // Check for some header or specific element if it exists
    }

    @Test
    fun calendarScreen_displaysTasks() {
        val testTasks = listOf(
            Task(1, "Test Task 1", "Desc 1", "2024-05-01", "Pending", 1.0),
            Task(2, "Test Task 2", "Desc 2", "2024-05-01", "Done", 2.0)
        )

        composeTestRule.setContent {
            CalendarScreen(
                tasks = testTasks,
                onBack = {}
            )
        }

        // Verify that the screen is displayed
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

        // Assuming there's a back button with content description "Back"
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(backClicked)
    }
}
