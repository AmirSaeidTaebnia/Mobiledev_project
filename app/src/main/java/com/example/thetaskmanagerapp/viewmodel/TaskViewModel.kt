package com.example.thetaskmanagerapp.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thetaskmanagerapp.NotificationReceiver
import com.example.thetaskmanagerapp.data.AppDatabase
import com.example.thetaskmanagerapp.data.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).taskDao()

    val tasks: Flow<List<Task>> = dao.getAllTasks()

    fun insertTask(task: Task) {
        viewModelScope.launch {
            val id = dao.insertTask(task).toInt()
            scheduleNotification(task.copy(id = id))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task)
            if (task.status != "Done") {
                scheduleNotification(task)
            } else {
                cancelNotification(task.id)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            cancelNotification(task.id)
        }
    }

    // START NOTIFICATION LOGIC / INICIO LÓGICA DE AVISOS
    private fun scheduleNotification(task: Task) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(getApplication(), NotificationReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_description", "Reminder: ${task.title}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val dueDate = LocalDate.parse(task.dueDate)
            val dueMillis = dueDate.atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            if (System.currentTimeMillis() < dueMillis) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Exact alarm permission / Permiso alarma exacta
                    if (alarmManager.canScheduleExactAlarms()) {
                        // Precision guaranteed / Precisión garantizada
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 10000,
                            pendingIntent
                        )
                    } else {
                        // Battery optimized delay / Retraso por ahorro de batería
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + 10000,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Android 6-11: Exact in Doze / Exacto en Doze
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 10000,
                        pendingIntent
                    )
                } else {
                    // Legacy: Simple exact / Legado: Exacto simple
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + 10000,
                        pendingIntent
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelNotification(taskId: Int) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(getApplication(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

}
