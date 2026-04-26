package com.example.thetaskmanagerapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.thetaskmanagerapp.ui.MainActivity
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity> = 
        createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_startsCorrectly() {
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun navigation_toCalendarScreen() {
        // Find by text label "Calendar" in the NavigationBar
        composeTestRule
            .onNodeWithText("Calendar")
            .performClick()

        // Verify we are on the Calendar screen by checking its title
        // In CalendarScreen.kt, the title is "Calendar View"
        composeTestRule
            .onNodeWithText("Calendar View")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_toTasks() {
        // NavigationBarItem with label "Tasks"
        composeTestRule
            .onNodeWithText("Tasks")
            .performClick()

        // TaskListScreen has a header with text "Your Tasks"
        composeTestRule
            .onNodeWithText("Your Tasks")
            .assertIsDisplayed()
    }
}
