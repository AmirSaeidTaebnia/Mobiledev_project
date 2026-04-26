package com.example.thetaskmanagerapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.thetaskmanagerapp.data.Task
import com.example.thetaskmanagerapp.ui.TaskListScreen
import org.junit.Rule
import org.junit.Test

class TaskListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun taskListScreen_displaysHeaderAndTasks() {
        val testTasks = listOf(
            Task(1, "Buy Groceries", "Milk, Eggs, Bread", "2024-05-01", "Pending", 1.0),
            Task(2, "Gym", "Leg day", "2024-05-01", "Done", 1.5)
        )

        composeTestRule.setContent {
            TaskListScreen(
                tasks = testTasks,
                notificationCount = 2,
                onAddTask = {},
                onEditTask = {},
                onDeleteTask = {},
                onNavigateToCalendar = {},
                onNavigateToDone = {},
                onNavigateToTimetable = {},
                onNavigateToNotifications = {}
            )
        }

        // Verify header
        composeTestRule.onNodeWithText("Your Tasks").assertIsDisplayed()
        
        // Verify tasks are displayed
        composeTestRule.onNodeWithText("Buy Groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gym").assertIsDisplayed()
        
        // Verify notification badge
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun taskListScreen_addButtonClick_triggersCallback() {
        var addClicked = false
        composeTestRule.setContent {
            TaskListScreen(
                tasks = emptyList(),
                notificationCount = 0,
                onAddTask = { addClicked = true },
                onEditTask = {},
                onDeleteTask = {},
                onNavigateToCalendar = {},
                onNavigateToDone = {},
                onNavigateToTimetable = {},
                onNavigateToNotifications = {}
            )
        }

        // Click the "Add" button using its text label
        composeTestRule.onNodeWithText("Add").performClick()
        
        assert(addClicked)
    }

    @Test
    fun taskListScreen_navigationItems_areDisplayed() {
        composeTestRule.setContent {
            TaskListScreen(
                tasks = emptyList(),
                notificationCount = 0,
                onAddTask = {},
                onEditTask = {},
                onDeleteTask = {},
                onNavigateToCalendar = {},
                onNavigateToDone = {},
                onNavigateToTimetable = {},
                onNavigateToNotifications = {}
            )
        }

        // Verify bottom navigation items
        composeTestRule.onNodeWithText("Tasks").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
        composeTestRule.onNodeWithText("Schedule").assertIsDisplayed()
    }
}
