package com.example.thetaskmanagerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate


sealed class Screen {
    object TaskList : Screen()
    data class AddEditTask(val task: Task? = null) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TaskApp()
                }
            }
        }
    }
}

@Composable
fun TaskApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val dao = database.taskDao()
    val tasks by dao.getAllTasks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.TaskList) }

    when (val screen = currentScreen) {
        is Screen.TaskList -> {
            TaskListScreen(
                tasks = tasks,
                onAddTask = { currentScreen = Screen.AddEditTask() },
                onEditTask = { task -> currentScreen = Screen.AddEditTask(task) },
                onDeleteTask = { task -> 
                    scope.launch { dao.deleteTask(task) }
                }
            )
        }
        is Screen.AddEditTask -> {
            AddEditTaskScreen(
                task = screen.task,
                onSave = { newTask ->
                    scope.launch {
                        if (screen.task == null) {
                            dao.insertTask(newTask)
                        } else {
                            dao.updateTask(newTask)
                        }
                    }
                    currentScreen = Screen.TaskList
                },
                onCancel = { currentScreen = Screen.TaskList }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Manager") },
                actions = {
                    Button(onClick = onAddTask) { Text("Add Task") }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { /* Calendar logic */ }) { Text("Calendar") }
                    TextButton(onClick = { /* Done tasks logic */ }) { Text("Done") }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Title", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelLarge)
                Text("Due", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelLarge)
                Text("Status", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.width(96.dp))
            }
            HorizontalDivider()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskRow(task, onEditTask, onDeleteTask)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun TaskRow(task: Task, onEditTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onEditTask(task) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(task.title, modifier = Modifier.weight(1.5f))
        Text(task.dueDate, modifier = Modifier.weight(1.2f))
        Text(task.status, modifier = Modifier.weight(1f))
        Row {
            IconButton(onClick = { onEditTask(task) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDeleteTask(task) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(task: Task?, onSave: (Task) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: LocalDate.now().toString()) }
    var status by remember { mutableStateOf(task?.status ?: "Pending") }
    
    val statuses = listOf("Pending", "In Progress", "Done")
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (task == null) "Add New Task" else "Edit Task") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            TextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    statuses.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                status = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onSave(Task(task?.id ?: 0, title, description, dueDate, status)) }) {
                    Text("Save Task")
                }
            }
        }
    }
}
