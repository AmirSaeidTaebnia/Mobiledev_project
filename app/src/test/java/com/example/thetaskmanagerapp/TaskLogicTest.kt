package com.example.thetaskmanagerapp

import com.example.thetaskmanagerapp.data.Task
import org.junit.Test
import org.junit.Assert.*

class TaskLogicTest {

    @Test
    fun test_notification_counter_logic() {
        // Given: a fixed date and a list of tasks
        val today = "2024-11-20"
        val tasks = listOf(
            Task(id = 1, title = "Task 1", dueDate = today, status = "Pending"),
            Task(id = 2, title = "Task 2", dueDate = today, status = "Done"),
            Task(id = 3, title = "Task 3", dueDate = "2024-12-01", status = "Pending")
        )

        // When: we calculate how many tasks are due today and not finished
        val count = tasks.count { it.dueDate == today && it.status != "Done" }

        // Then: the result should be exactly 1 (only Task 1 fits)
        assertEquals("The counter should only include pending tasks for today", 1, count)
    }
}
