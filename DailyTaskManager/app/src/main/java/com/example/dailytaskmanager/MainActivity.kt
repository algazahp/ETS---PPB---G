package com.example.dailytaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyTaskManagerApp()
        }
    }
}

// Model untuk Task
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val deadline: Date,
    var isCompleted: Boolean = false,
    val color: Int = generateRandomColor()
)

// Fungsi untuk menghasilkan warna pastel secara acak
fun generateRandomColor(): Int {
    val baseColor = 180 + Random.nextInt(0, 75)
    val red = baseColor - Random.nextInt(0, 30)
    val green = baseColor - Random.nextInt(0, 30)
    val blue = baseColor - Random.nextInt(0, 30)
    return Color(red, green, blue, 255).toArgb()
}

enum class SortType {
    DEADLINE, STATUS, NONE
}

@Composable
fun DailyTaskManagerApp() {
    val tasks = remember { mutableStateListOf<Task>() }
    var showDialog by remember { mutableStateOf(false) }
    var sortType by remember { mutableStateOf(SortType.NONE) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Preload some sample tasks
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()

        // Tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        calendar.set(Calendar.MINUTE, 0)
        val tomorrow = calendar.time

        // Day after tomorrow
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 14)
        calendar.set(Calendar.MINUTE, 30)
        val dayAfterTomorrow = calendar.time

        // Next week
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        val nextWeek = calendar.time

        tasks.addAll(
            listOf(
                Task(
                    id = 1,
                    title = "Mempersiapkan presentasi proyek",
                    description = "Buat slide untuk presentasi aplikasi Daily Task Manager",
                    deadline = tomorrow
                ),
                Task(
                    id = 2,
                    title = "Pertemuan tim developer",
                    description = "Diskusi tentang fitur baru yang akan diimplementasikan",
                    deadline = dayAfterTomorrow
                ),
                Task(
                    id = 3,
                    title = "Testing aplikasi",
                    description = "Melakukan testing UI dan fungsionalitas",
                    deadline = nextWeek,
                    isCompleted = true
                )
            )
        )
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopBar(
                        sortType = sortType,
                        onSortTypeChanged = { sortType = it },
                        showSortMenu = showSortMenu,
                        onShowSortMenuChanged = { showSortMenu = it }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    // Header Section
                    HeaderSection()

                    // Tasks List
                    TaskList(
                        tasks = when (sortType) {
                            SortType.DEADLINE -> tasks.sortedBy { it.deadline }
                            SortType.STATUS -> tasks.sortedBy { it.isCompleted }
                            SortType.NONE -> tasks
                        },
                        onTaskComplete = { task ->
                            val index = tasks.indexOfFirst { it.id == task.id }
                            if (index != -1) {
                                tasks[index] = tasks[index].copy(isCompleted = !tasks[index].isCompleted)
                            }
                        },
                        onTaskDelete = { task ->
                            tasks.removeAll { it.id == task.id }
                        }
                    )
                }
            }

            // Add Task Dialog
            if (showDialog) {
                AddTaskDialog(
                    onDismiss = { showDialog = false },
                    onTaskAdded = { title, description, deadline ->
                        val newTask = Task(
                            id = if (tasks.isEmpty()) 1 else tasks.maxOf { it.id } + 1,
                            title = title,
                            description = description,
                            deadline = deadline
                        )
                        tasks.add(newTask)
                        showDialog = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    sortType: SortType,
    onSortTypeChanged: (SortType) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChanged: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Daily Task Manager",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            Box {
                IconButton(onClick = { onShowSortMenuChanged(true) }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { onShowSortMenuChanged(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Urutkan berdasarkan Deadline") },
                        onClick = {
                            onSortTypeChanged(SortType.DEADLINE)
                            onShowSortMenuChanged(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Urutkan berdasarkan Status") },
                        onClick = {
                            onSortTypeChanged(SortType.STATUS)
                            onShowSortMenuChanged(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tidak diurutkan") },
                        onClick = {
                            onSortTypeChanged(SortType.NONE)
                            onShowSortMenuChanged(false)
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun HeaderSection() {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    val todayDate = dateFormat.format(calendar.time)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = todayDate,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Text(
            text = "Tugas Anda",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskComplete: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tidak ada tugas untuk saat ini",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onTaskComplete = onTaskComplete,
                    onTaskDelete = onTaskDelete,
                    modifier = Modifier.animateItemPlacement()
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskComplete: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
    val deadlineText = dateFormat.format(task.deadline)
    val isOverdue = task.deadline.before(Date()) && !task.isCompleted

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(task.color))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (isOverdue) Color.Red else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = deadlineText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (isOverdue) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terlambat",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions
            IconButton(
                onClick = { onTaskDelete(task) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onTaskComplete(task) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (String, String, Date) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah Tugas Baru",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // Date and time selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = dateFormatter.format(Date(selectedDate)),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text(
                            text = String.format("%02d:%02d", selectedHour, selectedMinute),
                            fontSize = 14.sp
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                calendar.timeInMillis = selectedDate
                                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                                calendar.set(Calendar.MINUTE, selectedMinute)

                                onTaskAdded(title, description, calendar.time)
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val timePickerState = rememberTimePickerState(
                        initialHour = selectedHour,
                        initialMinute = selectedMinute
                    )

                    Text(
                        text = "Pilih Waktu",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TimePicker(state = timePickerState)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Batal")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                selectedHour = timePickerState.hour
                                selectedMinute = timePickerState.minute
                                showTimePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}