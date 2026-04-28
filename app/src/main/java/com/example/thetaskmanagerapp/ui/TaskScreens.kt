package com.example.thetaskmanagerapp.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thetaskmanagerapp.data.Task
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    tasks: List<Task>,
    notificationCount: Int,
    onAddTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToDone: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    // 🔽 Sort tasks by date then time
    val sortedTasks = remember(tasks) {
        tasks.sortedWith(compareBy({ it.dueDate }, { it.dueTime }))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Task Manager",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    NotificationIcon(notificationCount, onNavigateToNotifications)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = "Tasks") },
                    label = { Text("Tasks", fontSize = 10.sp) },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add") },
                    label = { Text("Add", fontSize = 10.sp) },
                    selected = false,
                    onClick = onAddTask
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToCalendar
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done") },
                    label = { Text("Done", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToDone
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Schedule") },
                    label = { Text("Schedule", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToTimetable
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TaskHeader()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(sortedTasks) { task ->
                    TaskCard(task, onEditTask, onDeleteTask)
                }
            }
        }
    }
}

@Composable
fun NotificationIcon(notificationCount: Int, onNavigateToNotifications: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(48.dp)
            .clickable { onNavigateToNotifications() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
        if (notificationCount > 0) {
            Badge(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(notificationCount.toString())
            }
        }
    }
}

@Composable
fun TaskHeader() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TaskCard(task: Task, onEditTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    // 🎨 Logic-based workload coloring (Slightly more saturated for better distinction)
    val cardBackgroundColor = if (task.status == "Done") {
        Color(0xFFD1FFB3) // Green 100
    } else {
        when {
            task.workLoadInHours < 15.0 -> Color(0xFFFFFED6) // Yellow 100
            task.workLoadInHours < 30.0 -> Color(0xFFF7E2B6) // Orange 100
            else -> Color(0xFFFFD6D6) // Red 100
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${task.dueDate} ${task.dueTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${task.workLoadInHours}h",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                color = when(task.status) {
                    "Done" -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                    "In Progress" -> Color(0xFF1565C0).copy(alpha = 0.2f)
                    else -> Color.Black.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = task.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when(task.status) {
                        "Done" -> Color(0xFF1B5E20)
                        "In Progress" -> Color(0xFF0D47A1)
                        else -> Color.DarkGray
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Dedicated Edit Button (Pen Icon)
            IconButton(onClick = { onEditTask(task) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Task",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }

            // Delete Button (Trash Icon)
            IconButton(onClick = { onDeleteTask(task) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(tasks: List<Task>, onBack: () -> Unit, onClearAllNotifications: () -> Unit) {
    val today = LocalDate.now().toString()
    val dueTodayTasks = tasks.filter { it.dueDate == today }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = {
                        onClearAllNotifications()
                        onBack()
                    }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            items(dueTodayTasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Task Due Today", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("'${task.title}' is due today", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (dueTodayTasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notifications for today", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoneTasksScreen(tasks: List<Task>, onBack: () -> Unit, onEditTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    // 🔽 Sort tasks by date then time
    val sortedTasks = remember(tasks) {
        tasks.sortedWith(compareBy({ it.dueDate }, { it.dueTime }))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Done Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(sortedTasks) { task ->
                    TaskCard(task, onEditTask, onDeleteTask)
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
    var dueTime by remember { mutableStateOf(task?.dueTime ?: "12:00") }
    var status by remember { mutableStateOf(task?.status ?: "Pending") }
    var workLoadInHours by remember { mutableStateOf(task?.workLoadInHours?.toString() ?: "0.0") }

    val context = LocalContext.current
    val statuses = listOf("Pending", "In Progress", "Done")
    var expanded by remember { mutableStateOf(false) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            dueTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        },
        12, 0, true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (task == null) "New Task" else "Edit Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("What needs to be done?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Add more details...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                minLines = 3
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("Due Time") },
                    modifier = Modifier.weight(1f).clickable { timePickerDialog.show() },
                    enabled = false, // Disable typing, force click
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(imageVector = Icons.Default.Place, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }

            OutlinedTextField(
                value = workLoadInHours,
                onValueChange = { workLoadInHours = it },
                label = { Text("Hours") },
                placeholder = { Text("0.0") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Build, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = when(status) {
                            "Done" -> Color(0xFFC8E6C9)
                            "In Progress" -> Color(0xFFBBDEFB)
                            else -> Color.Transparent
                        },
                        unfocusedContainerColor = when(status) {
                            "Done" -> Color(0xFFC8E6C9)
                            "In Progress" -> Color(0xFFBBDEFB)
                            else -> Color.Transparent
                        }
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                ) {
                    statuses.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    item,
                                    color = when(item) {
                                        "Done" -> Color(0xFF2E7D32)
                                        "In Progress" -> Color(0xFF1565C0)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            },
                            onClick = {
                                status = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val hours = workLoadInHours.toDoubleOrNull() ?: 0.0
                    onSave(Task(
                        id = task?.id ?: 0,
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        status = status,
                        workLoadInHours = hours
                    ))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Task", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
