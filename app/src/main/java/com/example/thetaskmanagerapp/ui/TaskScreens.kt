package com.example.thetaskmanagerapp.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thetaskmanagerapp.data.Task
import java.time.LocalDate

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
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Task Manager", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                actions = { NotificationIcon(notificationCount, onNavigateToNotifications) }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, "Tasks") },
                    label = { Text("Tasks", fontSize = 10.sp) },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, "Add") },
                    label = { Text("Add", fontSize = 10.sp) },
                    selected = false,
                    onClick = onAddTask
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, "Calendar") },
                    label = { Text("Calendar", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToCalendar
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CheckCircle, "Done") },
                    label = { Text("Done", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToDone
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, "Schedule") },
                    label = { Text("Schedule", fontSize = 10.sp) },
                    selected = false,
                    onClick = onNavigateToTimetable
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TaskHeader()
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(tasks) { task -> TaskCard(task, onEditTask, onDeleteTask) }
            }
        }
    }
}

@Composable
fun NotificationIcon(notificationCount: Int, onNavigateToNotifications: () -> Unit) {
    Box(modifier = Modifier.padding(end = 8.dp).size(48.dp).clickable { onNavigateToNotifications() }, contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Notifications, "Notifications", tint = MaterialTheme.colorScheme.primary)
        if (notificationCount > 0) {
            Badge(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp), containerColor = MaterialTheme.colorScheme.error) {
                Text(notificationCount.toString(), color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

@Composable
fun TaskHeader() {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                "Your Tasks", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TaskCard(task: Task, onEditTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    val cardColor = when (task.priority) {
        "High" -> Color(0xFFFFEBEE)   // Soft red for the entire card
        "Medium" -> Color(0xFFFFF9C4) // Soft yellow for the entire card
        "Low" -> Color(0xFFE3F2FD)    // Soft blue for the entire card
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(task.dueDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Speed, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${task.workload}h", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                }
            }
            
            StatusBadge(task.status)
            
            Spacer(modifier = Modifier.width(4.dp))
            
            AnimatedIconButton(
                onClick = { onEditTask(task) },
                icon = Icons.Default.Edit,
                tint = MaterialTheme.colorScheme.primary
            )
            
            AnimatedIconButton(
                onClick = { onDeleteTask(task) },
                icon = Icons.Default.Delete,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AnimatedIconButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 1.3f else 1f, label = "scale")

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.scale(scale)
    ) { 
        Icon(icon, null, tint = tint)
    }
}

@Composable
fun StatusBadge(status: String) {
    val contentColor = when(status) {
        "Done" -> MaterialTheme.colorScheme.tertiary
        "In Progress" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        status.uppercase(),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = contentColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(task: Task?, onSave: (Task) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: LocalDate.now().toString()) }
    var status by remember { mutableStateOf(task?.status ?: "Pending") }
    var priority by remember { mutableStateOf(task?.priority ?: "Medium") }
    var workloadText by remember { mutableStateOf(task?.workload?.toString() ?: "1.0") }

    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (task == null) "New Task" else "Edit Task", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = { 
                    IconButton(onClick = onCancel) { 
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            "Cancel",
                            tint = MaterialTheme.colorScheme.onSurface
                        ) 
                    } 
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(24.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title, 
                onValueChange = { title = it }, 
                label = { Text("Title") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(20.dp),
                colors = textFieldColors,
                leadingIcon = { Icon(Icons.Default.Title, null, tint = MaterialTheme.colorScheme.primary) }
            )
            OutlinedTextField(
                value = description, 
                onValueChange = { description = it }, 
                label = { Text("Description") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(20.dp), 
                minLines = 2,
                colors = textFieldColors,
                leadingIcon = { Icon(Icons.Default.EditNote, null, tint = MaterialTheme.colorScheme.secondary) }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = dueDate, 
                    onValueChange = { dueDate = it }, 
                    label = { Text("Due Date") }, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(20.dp),
                    colors = textFieldColors,
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.secondary) }
                )
                OutlinedTextField(
                    value = workloadText, 
                    onValueChange = { workloadText = it }, 
                    label = { Text("Hours") }, 
                    modifier = Modifier.weight(0.7f), 
                    shape = RoundedCornerShape(20.dp), 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = textFieldColors,
                    leadingIcon = { Icon(Icons.Default.HourglassEmpty, null, tint = MaterialTheme.colorScheme.secondary) }
                )
            }

            // Visual Effort Selector (Priority) as a Dropdown
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = !priorityExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = priority,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Effort Degree") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(priorityExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = textFieldColors,
                    leadingIcon = {
                        val color = when(priority) {
                            "High" -> MaterialTheme.colorScheme.error
                            "Medium" -> MaterialTheme.colorScheme.secondary
                            "Low" -> MaterialTheme.colorScheme.primary
                            else -> Color.Gray
                        }
                        Box(modifier = Modifier.size(12.dp).background(color, androidx.compose.foundation.shape.CircleShape))
                    }
                )
                ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false }
                ) {
                    listOf("Low", "Medium", "High").forEach { level ->
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val color = when(level) {
                                        "High" -> MaterialTheme.colorScheme.error
                                        "Medium" -> MaterialTheme.colorScheme.secondary
                                        "Low" -> MaterialTheme.colorScheme.primary
                                        else -> Color.Gray
                                    }
                                    Box(modifier = Modifier.size(10.dp).background(color, androidx.compose.foundation.shape.CircleShape))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(level)
                                }
                            },
                            onClick = { 
                                priority = level
                                priorityExpanded = false 
                            }
                        )
                    }
                }
            }

            // Status Selector
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = status, onValueChange = {}, readOnly = true, label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = textFieldColors,
                    leadingIcon = {
                        val iconColor = when(status) {
                            "Done" -> MaterialTheme.colorScheme.tertiary
                            "In Progress" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        }
                        Icon(Icons.Default.Circle, null, modifier = Modifier.size(12.dp), tint = iconColor)
                    }
                )
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    listOf("Pending", "In Progress", "Done").forEach { s ->
                        DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusExpanded = false })
                    }
                }
            }

            Button(
                onClick = { onSave(Task(task?.id ?: 0, title, description, dueDate, status, priority, workload = workloadText.toDoubleOrNull() ?: 1.0)) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("SAVE TASK", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(tasks: List<Task>, onBack: () -> Unit) {
    val today = LocalDate.now().toString()
    val dueTodayTasks = tasks.filter { it.dueDate == today }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Notifications",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ) 
                }, 
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        ) 
                    } 
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
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Due Today", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("'${task.title}' is due today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoneTasksScreen(tasks: List<Task>, onBack: () -> Unit, onEditTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Done Tasks",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ) 
                }, 
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        ) 
                    } 
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(tasks) { task -> TaskCard(task, onEditTask, onDeleteTask) }
        }
    }
}
