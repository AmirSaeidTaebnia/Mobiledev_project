package com.example.thetaskmanagerapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT SUM(notificationCount) FROM tasks")
    fun getTotalNotificationCount(): Flow<Int?>

    @Query("UPDATE tasks SET hasUnreadNotification = 0, notificationCount = 0 WHERE id = :taskId")
    suspend fun clearNotification(taskId: Int)

    @Query("UPDATE tasks SET hasUnreadNotification = 1, notificationCount = notificationCount + 1 WHERE id = :taskId")
    suspend fun increaseNotificationCount(taskId: Int)
}