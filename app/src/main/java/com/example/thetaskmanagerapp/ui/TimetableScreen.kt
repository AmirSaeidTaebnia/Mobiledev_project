package com.example.thetaskmanagerapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thetaskmanagerapp.data.UIReservation
import com.example.thetaskmanagerapp.viewmodel.TimetableViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(onBack: () -> Unit, viewModel: TimetableViewModel = viewModel()) {
    // Initial fetch if data is empty
    LaunchedEffect(Unit) { if (viewModel.uiData.isEmpty()) viewModel.fetchTimetable() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Weekly Schedule", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface) 
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Class search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), 
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.classCode,
                    onValueChange = { viewModel.updateClassCode(it) },
                    label = { Text("Class Code") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Class, null, tint = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                Button(
                    onClick = { viewModel.fetchTimetable() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Search, "Search")
                }
            }

            // Week Selector
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.changeWeek(-1) }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev Week", tint = MaterialTheme.colorScheme.primary) 
                    }
                    Text(
                        "Week: ${viewModel.currentWeekStart.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${viewModel.currentWeekStart.plusDays(6).format(DateTimeFormatter.ofPattern("MMM dd"))}", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { viewModel.changeWeek(1) }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Week", tint = MaterialTheme.colorScheme.primary) 
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
            } else if (viewModel.errorMessage != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.fetchTimetable() }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Retry")
                    }
                }
            } else {
                ScheduleGrid(viewModel.uiData, viewModel.currentWeekStart)
            }
        }
    }
}

@Composable
fun ScheduleGrid(reservations: List<UIReservation>, weekStart: LocalDate) {
    val startHour = 7
    val endHour = 21
    val hours = (startHour..endHour).toList()
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }

    Row(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Hour Labels
        Column(modifier = Modifier.width(48.dp).background(MaterialTheme.colorScheme.surface)) {
            Spacer(modifier = Modifier.height(40.dp))
            hours.forEach { hour ->
                Box(modifier = Modifier.height(60.dp).fillMaxWidth().padding(end = 4.dp), contentAlignment = Alignment.TopEnd) {
                    Text(
                        "$hour:00", 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        days.forEach { day ->
            val isToday = day == LocalDate.now()
            Column(modifier = Modifier.weight(1f).border(0.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))) {
                // Day Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 12.sp,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        day.dayOfMonth.toString(),
                        fontSize = 10.sp,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().height(((endHour - startHour + 1) * 60).dp)) {
                    // Grid Lines
                    hours.forEachIndexed { index, _ ->
                        HorizontalDivider(
                            modifier = Modifier.offset(y = (index * 60).dp), 
                            thickness = 0.5.dp, 
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }

                    reservations.filter { it.date == day }.forEach { res ->
                        val startMin = (res.startTime.hour - startHour) * 60 + res.startTime.minute
                        val duration = (res.endTime.hour * 60 + res.endTime.minute) - (res.startTime.hour * 60 + res.startTime.minute)

                        if (duration > 0 && startMin >= 0) {
                            LessonCard(res, startMin, duration)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCard(res: UIReservation, startOffset: Int, duration: Int) {
    Card(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxWidth()
            .offset(y = startOffset.dp)
            .height(duration.dp - 1.dp)
            .clip(RoundedCornerShape(4.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                res.subject, 
                fontSize = 9.sp, 
                fontWeight = FontWeight.Bold, 
                lineHeight = 10.sp, 
                maxLines = 3, 
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            if (res.location.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        res.location, 
                        fontSize = 8.sp, 
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
