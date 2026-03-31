package com.example.thetaskmanagerapp

import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


sealed class Screen {
    object TaskList : Screen()
    data class AddEditTask(val task: Task? = null) : Screen()
    object DoneTasks : Screen()
    object Calendar : Screen()
    object Timetable : Screen()
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
    val doneTasks by dao.getDoneTasks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.TaskList) }

    when (val screen = currentScreen) {
        is Screen.TaskList -> {
            TaskListScreen(
                tasks = tasks,
                onAddTask = { currentScreen = Screen.AddEditTask() },
                onEditTask = { task -> currentScreen = Screen.AddEditTask(task) },
                onDeleteTask = { task -> scope.launch { dao.deleteTask(task) } },
                onNavigateToCalendar = { currentScreen = Screen.Calendar },
                onNavigateToDone = { currentScreen = Screen.DoneTasks },
                onNavigateToTimetable = { currentScreen = Screen.Timetable }
            )
        }
        is Screen.AddEditTask -> {
            AddEditTaskScreen(
                task = screen.task,
                onSave = { newTask ->
                    scope.launch {
                        if (screen.task == null) dao.insertTask(newTask)
                        else dao.updateTask(newTask)
                    }
                    currentScreen = Screen.TaskList
                },
                onCancel = { currentScreen = Screen.TaskList }
            )
        }
        is Screen.DoneTasks -> {
            DoneTasksScreen(
                tasks = doneTasks,
                onBack = { currentScreen = Screen.TaskList },
                onEditTask = { task -> currentScreen = Screen.AddEditTask(task) },
                onDeleteTask = { task -> scope.launch { dao.deleteTask(task) } }
            )
        }
        is Screen.Calendar -> {
            CalendarScreen(
                tasks = tasks,
                onBack = { currentScreen = Screen.TaskList }
            )
        }
        is Screen.Timetable -> {
            TimetableScreen(onBack = { currentScreen = Screen.TaskList })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToDone: () -> Unit,
    onNavigateToTimetable: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Manager") },
                actions = {
                    Button(onClick = onAddTask) { Text("Add Task") }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = onNavigateToCalendar) { Text("Calendar") }
                    TextButton(onClick = onNavigateToDone) { Text("Done") }
                    TextButton(onClick = onNavigateToTimetable) { Text("School") }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TaskHeader()
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
fun TaskHeader() {
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
fun DoneTasksScreen(
    tasks: List<Task>,
    onBack: () -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Done Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TaskHeader()
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskRow(task, onEditTask, onDeleteTask)
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(tasks: List<Task>, onBack: () -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar View") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month")
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            val totalCells = ((daysInMonth + firstDayOfMonth + 6) / 7) * 7
            Column {
                for (row in 0 until totalCells / 7) {
                    Row(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                        for (col in 0 until 7) {
                            val dayIndex = row * 7 + col - firstDayOfMonth + 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(0.5.dp, Color.LightGray)
                                    .padding(2.dp)
                            ) {
                                if (dayIndex in 1..daysInMonth) {
                                    val dateStr = currentMonth.atDay(dayIndex).toString()
                                    val tasksForDay = tasks.filter { it.dueDate == dateStr }
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = dayIndex.toString(), fontSize = 12.sp)
                                        if (tasksForDay.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            )
                                            Text(
                                                text = "${tasksForDay.size} tasks",
                                                fontSize = 8.sp,
                                                lineHeight = 10.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(onBack: () -> Unit) {
    var classCode by remember { mutableStateOf("ICT24K-SW") }
    var timetableData by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val MY_API_KEY = ""

    val retrofit = remember {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://opendata.metropolia.fi/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = remember { retrofit.create(TimetableApi::class.java) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("School Timetable") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = classCode,
                    onValueChange = { classCode = it },
                    label = { Text("Class Code") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {

                            val authData = "$MY_API_KEY:".toByteArray()
                            val authHeader = "Basic " + Base64.encodeToString(authData, Base64.NO_WRAP)
                            
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                            val now = LocalDateTime.now().format(formatter)
                            val nextMonth = LocalDateTime.now().plusMonths(1).format(formatter)

                            val response = api.getTimetable(
                                authHeader = authHeader,
                                request = TimetableRequest(
                                    rangeStart = now,
                                    rangeEnd = nextMonth,
                                    studentGroup = listOf(classCode)
                                )
                            )
                            timetableData = response.reservations
                            if (timetableData.isEmpty()) {
                                errorMessage = "No lessons found for this class code."
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(timetableData) { reservation ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = reservation.subject ?: "No Subject",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${reservation.startDate} - ${reservation.endDate}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                reservation.description?.let {
                                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
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
