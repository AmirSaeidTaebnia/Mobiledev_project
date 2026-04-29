package com.example.thetaskmanagerapp

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.thetaskmanagerapp.data.Task
import com.example.thetaskmanagerapp.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class TaskViewModelTest {

    private lateinit var viewModel: TaskViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = TaskViewModel(app)
    }

    @Test
    fun testViewModelInitialization() {
        assertNotNull(viewModel)
    }

    @Test
    fun insertAndGetTask() = runBlocking {
        val task = Task(
            id = 0,
            title = "Test Task",
            description = "Description",
            dueDate = "2024-05-01",
            dueTime = "12:00",
            status = "Pending",
            workLoadInHours = 1.0
        )
        viewModel.insertTask(task)
        
        val tasks = viewModel.tasks.first()
        assert(tasks.any { it.title == "Test Task" })
    }
}
