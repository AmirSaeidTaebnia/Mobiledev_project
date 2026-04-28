package com.example.thetaskmanagerapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.thetaskmanagerapp.data.AppDatabase
import com.example.thetaskmanagerapp.data.Task
import com.example.thetaskmanagerapp.data.TaskDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        taskDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTask_andReadAllTasks() = runBlocking {

        val task = Task(
            title = "DAO Test",
            description = "Testing Flow",
            dueDate = "2026-04-21",
            status = "Pending",
            workload = 2
        )

        // insert
        taskDao.insertTask(task)

        // read (Flow → first())
        val tasks = taskDao.getAllTasks().first()

        assertEquals(1, tasks.size)
        assertEquals("DAO Test", tasks[0].title)
    }
}