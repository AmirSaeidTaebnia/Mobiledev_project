package com.example.thetaskmanagerapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thetaskmanagerapp.data.Task
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(tasks: List<Task>, onBack: () -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar View") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            // Header con Mes y Año
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev") }
                Text("${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next") }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Días de la semana
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }
            }
            
            // Cuadrícula del calendario
            val totalCells = ((daysInMonth + firstDayOfMonth + 6) / 7) * 7
            Column {
                for (row in 0 until totalCells / 7) {
                    Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                        for (col in 0 until 7) {
                            val dayIndex = row * 7 + col - firstDayOfMonth + 1
                            val isSelected = dayIndex > 0 && dayIndex <= daysInMonth && currentMonth.atDay(dayIndex) == selectedDate
                            
                            Box(modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(0.5.dp, Color.LightGray.copy(0.3f))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
                                .clickable(enabled = dayIndex in 1..daysInMonth) {
                                    if (dayIndex in 1..daysInMonth) selectedDate = currentMonth.atDay(dayIndex)
                                }
                                .padding(2.dp)
                            ) {
                                if (dayIndex in 1..daysInMonth) {
                                    val dateStr = currentMonth.atDay(dayIndex).toString()
                                    val tasksForDay = tasks.filter { it.dueDate == dateStr }
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                                        Text(dayIndex.toString(), fontSize = 12.sp)
                                        
                                        // 🎨 Unified coloring for the balls
                                        if (tasksForDay.isNotEmpty()) {
                                            Row(horizontalArrangement = Arrangement.Center) {
                                                tasksForDay.take(3).forEach { task ->
                                                    val dotColor = when {
                                                        task.status == "Done" -> Color(0xFFD1FFB3) // Green
                                                        task.workLoadInHours < 15.0 -> Color(0xFFFFFED6) // Yellow
                                                        task.workLoadInHours < 30.0 -> Color(0xFFF7E2B6) // Orange
                                                        else -> Color(0xFFFFD6D6) // Red
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(horizontal = 1.dp)
                                                            .size(6.dp)
                                                            .background(dotColor, CircleShape)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de tareas debajo del calendario
            Text("Tasks on ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))}", fontWeight = FontWeight.Bold)
            
            val selectedDateStr = selectedDate.toString()
            val tasksForSelectedDay = tasks.filter { it.dueDate == selectedDateStr }
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasksForSelectedDay) { task ->
                    TaskCard(task, onEditTask = {}, onDeleteTask = {}) // Reuse TaskCard for consistency
                }
                if (tasksForSelectedDay.isEmpty()) {
                    item {
                        Text("No tasks for this day", modifier = Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
