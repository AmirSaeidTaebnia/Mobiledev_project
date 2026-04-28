package com.example.thetaskmanagerapp

import com.example.thetaskmanagerapp.data.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskTest {

    @Test
    fun createTask_checkValues() {
        val task = Task(
            id = 1,
            title = "Study",
            description = "Read notes",
            dueDate = "2026-04-21",
            status = "Pending",
            hasUnreadNotification = true,
            notificationCount = 2,
            reminderEnabled = true,
            workload = 3
        )

        assertEquals(1, task.id)
        assertEquals("Study", task.title)
        assertEquals("Read notes", task.description)
        assertEquals("2026-04-21", task.dueDate)
        assertEquals("Pending", task.status)
        assertEquals(true, task.hasUnreadNotification)
        assertEquals(2, task.notificationCount)
        assertEquals(true, task.reminderEnabled)
        assertEquals(3, task.workload)
    }
}