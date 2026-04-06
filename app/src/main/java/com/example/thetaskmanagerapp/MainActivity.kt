package com.example.thetaskmanagerapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class Screen {
    object TaskList : Screen()
    object Calendar : Screen()
    object DoneTasks : Screen()
    object Notifications : Screen()
    data class AddEditTask(val task: Task? = null) : Screen()
}

data class AppNotification(
    val id: Int,
    val title: String,
    val message: String
)

class MainActivity : ComponentActivity() {

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

    val notifications = remember(tasks) {
        buildNotificationList(tasks)
    }

    var readNotificationIds by rememberSaveable { mutableStateOf(setOf<Int>()) }

    val unreadNotifications = notifications.filter { it.id !in readNotificationIds }
    val notificationCount = unreadNotifications.size

    when (val screen = currentScreen) {
        is Screen.TaskList -> {
            TaskListScreen(
                tasks = tasks,
                notificationCount = notificationCount,
                onAddTask = { currentScreen = Screen.AddEditTask() },
                onEditTask = { task -> currentScreen = Screen.AddEditTask(task) },
                onDeleteTask = { task ->
                    scope.launch { dao.deleteTask(task) }
                },
                onCalendarClick = { currentScreen = Screen.Calendar },
                onDoneClick = { currentScreen = Screen.DoneTasks },
                onNotificationClick = { currentScreen = Screen.Notifications }
            )
        }

        is Screen.Calendar -> {
            CalendarScreen(
                tasks = tasks,
                onBack = { currentScreen = Screen.TaskList }
            )
        }

        is Screen.DoneTasks -> {
            DoneTasksScreen(
                tasks = tasks.filter { it.status.equals("Done", ignoreCase = true) },
                onBack = { currentScreen = Screen.TaskList }
            )
        }

        is Screen.Notifications -> {
            NotificationScreen(
                notifications = unreadNotifications,
                onNotificationRead = { notification ->
                    readNotificationIds = readNotificationIds + notification.id
                },
                onBack = { currentScreen = Screen.TaskList }
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

fun buildNotificationList(tasks: List<Task>): List<AppNotification> {
    val notificationList = mutableListOf<AppNotification>()
    val today = LocalDate.now()

    tasks.forEach { task ->
        val taskDate = parseTaskDate(task.dueDate)

        if (task.status.equals("Done", ignoreCase = true)) {
            notificationList.add(
                AppNotification(
                    id = task.id * 10 + 1,
                    title = "Task Completed",
                    message = "\"${task.title}\" is marked as Done"
                )
            )
        } else if (taskDate != null && taskDate.isEqual(today.minusDays(1))) {
            notificationList.add(
                AppNotification(
                    id = task.id * 10 + 2,
                    title = "Task Overdue",
                    message = "\"${task.title}\" is overdue"
                )
            )
        } else if (taskDate != null && taskDate.isEqual(today)) {
            notificationList.add(
                AppNotification(
                    id = task.id * 10 + 3,
                    title = "Task Due Today",
                    message = "\"${task.title}\" is due today"
                )
            )
        } else if (taskDate != null && taskDate.isEqual(today.plusDays(1))) {
            notificationList.add(
                AppNotification(
                    id = task.id * 10 + 4,
                    title = "Reminder",
                    message = "\"${task.title}\" is due tomorrow"
                )
            )
        }
    }

    return notificationList
}

fun parseTaskDate(dateString: String): LocalDate? {
    val patterns = listOf(
        "dd-MM-yyyy",
        "dd-MM-yy",
        "yyyy-MM-dd"
    )

    for (pattern in patterns) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern))
        } catch (_: Exception) {
        }
    }

    return null
}

fun formatTaskDateForDisplay(dateString: String): String {
    val parsedDate = parseTaskDate(dateString)
    return if (parsedDate != null) {
        parsedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    } else {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    tasks: List<Task>,
    notificationCount: Int,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onCalendarClick: () -> Unit,
    onDoneClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val context = LocalContext.current

    val bluetoothAdapter: BluetoothAdapter? = remember {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (bluetoothAdapter?.isEnabled == true) {
            Toast.makeText(context, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Bluetooth was not enabled", Toast.LENGTH_SHORT).show()
        }
    }

    val requestBluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (bluetoothAdapter == null) {
                Toast.makeText(
                    context,
                    "Bluetooth not supported on this device",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            } else {
                Toast.makeText(context, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun enableBluetooth() {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported on this device", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return
            }
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            Toast.makeText(context, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Manager") },
                actions = {
                    Button(onClick = onAddTask) { Text("Add Task") }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = onCalendarClick) { Text("Calendar") }
                    TextButton(onClick = onDoneClick) { Text("Done") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { enableBluetooth() }
            ) {
                Text(
                    text = "ᛒ",
                    fontSize = 28.sp
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Title",
                    modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "Due",
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "Status",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge
                )

                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge {
                                Text(notificationCount.toString())
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { onNotificationClick() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskRow(
                        task = task,
                        onEditTask = onEditTask,
                        onDeleteTask = onDeleteTask
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onEditTask(task) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(task.title, modifier = Modifier.weight(1.5f))
        Text(formatTaskDateForDisplay(task.dueDate), modifier = Modifier.weight(1.2f))
        Text(task.status, modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.width(24.dp))

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
fun AddEditTaskScreen(
    task: Task?,
    onSave: (Task) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueDate by remember {
        mutableStateOf(
            task?.dueDate ?: LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        )
    }
    var status by remember { mutableStateOf(task?.status ?: "Pending") }

    val statuses = listOf("Pending", "In Progress", "Done")
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (task == null) "Add New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                label = { Text("Due Date (DD-MM-YYYY)") },
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
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
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
                Button(
                    onClick = {
                        onSave(Task(task?.id ?: 0, title, description, dueDate, status))
                    }
                ) {
                    Text("Save Task")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    tasks: List<Task>,
    onBack: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.dayOfWeek.value % 7
    val monthTitle = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    val selectedTasks = tasks.filter { task ->
        val taskDate = parseTaskDate(task.dueDate)
        taskDate == selectedDate
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar View") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        currentMonth = currentMonth.minusMonths(1).withDayOfMonth(1)
                        selectedDate = null
                    }
                ) {
                    Text(
                        text = "←",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = monthTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        currentMonth = currentMonth.plusMonths(1).withDayOfMonth(1)
                        selectedDate = null
                    }
                ) {
                    Text(
                        text = "→",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                daysOfWeek.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            val totalCells = firstDayOfWeek + daysInMonth
            val rows = if (totalCells % 7 == 0) totalCells / 7 else (totalCells / 7) + 1

            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeek + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDate = currentMonth.withDayOfMonth(dayNumber)
                                val tasksForDate = tasks.filter { task ->
                                    parseTaskDate(task.dueDate) == cellDate
                                }
                                val hasTask = tasksForDate.isNotEmpty()
                                val isSelected = selectedDate == cellDate
                                val firstTaskTitle = tasksForDate.firstOrNull()?.title ?: ""

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.9f)
                                        .border(
                                            width = 0.7.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                            }
                                        )
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            } else {
                                                MaterialTheme.colorScheme.background
                                            }
                                        )
                                        .clickable {
                                            selectedDate = cellDate
                                        }
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        if (hasTask) {
                                            Text(
                                                text = "●",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Spacer(modifier = Modifier.height(1.dp))

                                            Text(
                                                text = firstTaskTitle,
                                                fontSize = 9.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 10.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.9f)
                                        .border(
                                            width = 0.7.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedDate != null) {
                Text(
                    text = "Tasks on ${selectedDate!!.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTasks.isEmpty()) {
                    Text("No tasks for this date")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(selectedTasks) { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Description: ${task.description}")
                                    Text("Due Date: ${formatTaskDateForDisplay(task.dueDate)}")
                                    Text("Status: ${task.status}")
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
fun DoneTasksScreen(
    tasks: List<Task>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Done Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No completed tasks")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Description: ${task.description}")
                            Text("Due Date: ${formatTaskDateForDisplay(task.dueDate)}")
                            Text("Status: ${task.status}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    notifications: List<AppNotification>,
    onNotificationRead: (AppNotification) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                items(notifications) { notification ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                onNotificationRead(notification)
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = notification.message)
                        }
                    }
                }
            }
        }
    }
}