package com.example.thetaskmanagerapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val status: String = "Pending",

    // 🔔 NEW FIELDS (notification system)
    val hasUnreadNotification: Boolean = false,
    val notificationCount: Int = 0,
    val reminderEnabled: Boolean = true
)