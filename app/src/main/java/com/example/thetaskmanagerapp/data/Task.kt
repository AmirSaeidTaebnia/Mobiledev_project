package com.example.thetaskmanagerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",
    val dueTime: String = "", // 🕒 NEW FIELD
    val status: String = "Pending",
    val workLoadInHours: Double = 0.0,

    // 🔔 NEW FIELDS (notification system)
    val hasUnreadNotification: Boolean = false,
    val notificationCount: Int = 0,
    val reminderEnabled: Boolean = true
)
