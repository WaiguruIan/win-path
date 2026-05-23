package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import android.widget.Toast
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.content.Intent
import android.provider.MediaStore
import android.net.Uri
import android.app.Activity
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.model.MediaType
import com.example.data.model.MilestoneNode
import com.example.data.model.NodeMedia
import com.example.data.model.NodeWithMedia
import com.example.data.model.TodoItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.JourneyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import androidx.compose.runtime.key
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

fun getDayLabel(id: Int): String {
    val word = when (id) {
        1 -> "One"
        2 -> "Two"
        3 -> "Three"
        4 -> "Four"
        5 -> "Five"
        6 -> "Six"
        7 -> "Seven"
        8 -> "Eight"
        9 -> "Nine"
        10 -> "Ten"
        else -> id.toString()
    }
    return "Day $word"
}

@Composable
fun JourneyApp(
    viewModel: JourneyViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(1) } // Default of 1: Journey Map Screen

    val nodes by viewModel.allNodes.collectAsState()
    val nodesWithMedia by viewModel.allNodesWithMedia.collectAsState()
    val selectedNode by viewModel.selectedNodeWithMedia.collectAsState()
    val isCheckingProgress by viewModel.isCheckingProgress.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = if (isDark) Color(0xFF0A0818) else Color(0xFFFFFFFF),
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Left Tab: To-Do Calendar
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "To-Do List") },
                    label = { Text("To-Do") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ActiveGreen,
                        selectedTextColor = ActiveGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = ActiveGreen.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_todo")
                )

                // Middle Tab: Journey Map
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Journey") },
                    label = { Text("Journey") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ActiveGreen,
                        selectedTextColor = ActiveGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = ActiveGreen.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_journey")
                )

                // Right Tab: Settings
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ActiveGreen,
                        selectedTextColor = ActiveGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = ActiveGreen.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(SpaceDarkBg, Color(0xFF070515))
                        } else {
                            listOf(SpaceDarkBg, Color(0xFFF3EEFD))
                        }
                    )
                )
        ) {
            when (selectedTab) {
                0 -> TodoCalendarScreen(viewModel = viewModel)
                1 -> JourneyMapScreen(viewModel = viewModel, nodesWithMedia = nodesWithMedia)
                2 -> SettingsScreen(viewModel = viewModel, nodes = nodes)
            }

            // Dialog modal overlay for media updates
            selectedNode?.let { nodeWithMedia ->
                NodeDetailDialog(
                    nodeWithMedia = nodeWithMedia,
                    onDismiss = { viewModel.clearSelectedNode() },
                    onAddProof = { type, content ->
                        viewModel.addProof(nodeWithMedia.node.id, type, content)
                    },
                    onDeleteMedia = { mediaId ->
                        viewModel.deleteMedia(mediaId, nodeWithMedia.node.id)
                    },
                    onSimulateMidnight = {
                        viewModel.simulateMidnightAndCheck(nodeWithMedia.node.id)
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: TO-DO CALENDAR SCREEN IMPLEMENTATION
// -------------------------------------------------------------
@Composable
fun TodoCalendarScreen(viewModel: JourneyViewModel) {
    var weekOffset by remember { mutableStateOf(0) }
    
    val todayId = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    var selectedDateString by remember { mutableStateOf(todayId) }
    
    val daysOfWeek = remember(weekOffset) {
        getDaysOfWeek(weekOffset)
    }
    
    val monthTitle = remember(weekOffset) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(cal.time)
    }

    // Task Items for chosen date
    val todoItemsState = remember(selectedDateString) {
        viewModel.getTodoItems(selectedDateString)
    }
    val todoItems by todoItemsState.collectAsState()

    var newTaskTitle by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // App Title Headings
        Text(
            text = "CALENDAR TO-DO",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                color = ActiveGreen,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Manage your routine calendar tasks",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Week strip container
        Surface(
            color = SpaceCardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SpaceCardBorder),
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header month slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { weekOffset-- }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev Week", tint = ActiveGreen, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = monthTitle.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            letterSpacing = 0.5.sp
                        )
                    )
                    IconButton(onClick = { weekOffset++ }) {
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Week", tint = ActiveGreen, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Days grid row (7 Days)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEach { day ->
                        val isSelected = day.dateId == selectedDateString
                        val isToday = day.dateId == todayId
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(Brush.verticalGradient(listOf(ActiveGreen, AccentCyan)))
                                    } else if (isToday) {
                                        Modifier.background(ActiveGreen.copy(alpha = 0.08f))
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedDateString = day.dateId }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else if (isToday) ActiveGreen else SpaceCardBorder.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.dayName.substring(0, 3).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else LockedText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day.dateNumber,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = if (isSelected) Color.White else TextLight,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Format selected date
        val displayDateLabel = remember(selectedDateString) {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            try {
                val date = sdfInput.parse(selectedDateString)
                date?.let { sdfOutput.format(it) } ?: selectedDateString
            } catch (e: Exception) {
                selectedDateString
            }
        }

        Text(
            text = if (selectedDateString == todayId) "TODAY'S MISSION" else "DATE OBJECTIVES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = CompletedGold,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = displayDateLabel,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Create Task Input Bar
        Surface(
            color = SpaceCardBg,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, SpaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("What needs to be done?", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("todo_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addTodoItem(newTaskTitle.trim(), selectedDateString)
                            newTaskTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_todo_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Checklist Rendering
        if (todoItems.isEmpty()) {
            Surface(
                color = SpaceCardBg.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SpaceCardBorder.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = "No tasks",
                        tint = LockedText.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Slate is clear!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextLight.copy(alpha = 0.8f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add tasks above to organize work for this date calendar slot.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                todoItems.forEach { item ->
                    Surface(
                        color = SpaceCardBg,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("todo_item_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rounded Checkbox Matching Candy Progress Colors
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (item.isCompleted) CompletedGold else Color.Transparent)
                                    .border(
                                        width = 2.dp,
                                        color = if (item.isCompleted) CompletedGold else ActiveGreen,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.toggleTodoItem(item) }
                                    .testTag("todo_checkbox_${item.id}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Task Title Text
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isCompleted) TextMuted else TextLight,
                                    textDecoration = if (item.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Trash Delete Icon
                            IconButton(
                                onClick = { viewModel.deleteTodoItem(item) },
                                modifier = Modifier.testTag("delete_todo_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Helper models for simple calendar date calculation
data class CalendarDay(
    val dayName: String,
    val dateNumber: String,
    val dateId: String,
    val epochTime: Long
)

fun getDaysOfWeek(offsetWeek: Int): List<CalendarDay> {
    val days = mutableListOf<CalendarDay>()
    val cal = Calendar.getInstance()
    cal.add(Calendar.WEEK_OF_YEAR, offsetWeek)
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
    val sdfDate = SimpleDateFormat("d", Locale.getDefault())
    val sdfId = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    for (i in 0 until 7) {
        days.add(
            CalendarDay(
                dayName = sdfDay.format(cal.time),
                dateNumber = sdfDate.format(cal.time),
                dateId = sdfId.format(cal.time),
                epochTime = cal.timeInMillis
            )
        )
        cal.add(Calendar.DATE, 1)
    }
    return days
}

// -------------------------------------------------------------
// TAB 1: REDESIGNED JOURNEY MAP SCREEN (BOTTOM-TO-TOP)
// -------------------------------------------------------------
@Composable
fun JourneyMapScreen(
    viewModel: JourneyViewModel,
    nodesWithMedia: List<NodeWithMedia>
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkMode.collectAsState()
    val nodes = remember(nodesWithMedia) { nodesWithMedia.map { it.node } }
    val completedCount = nodes.count { it.isCompleted }
    val totalCount = nodes.size.coerceAtLeast(1)
    val progressPct = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)

    var currentStreak = 0
    for (node in nodes) {
        if (node.isCompleted) {
            currentStreak++
        } else {
            break
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0xFF0F0A1D), // Elegant deep midnight lilac
                            Color(0xFF1B122B), // Cosmic amethyst
                            Color(0xFF0F0A1D)
                        )
                    } else {
                        listOf(
                            Color(0xFFFFFFFF), // Pure white light mode base
                            Color(0xFFF3EEFD), // Very soft lilac glow accent
                            Color(0xFFFFFFFF)
                        )
                    }
                )
            )
    ) {
        // Redesigned Top Header Panel matching Figma Dark Purple vibe exactly
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LILAC JOURNEY",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = ActiveGreen,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Climb the 365 Days of Progress",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = TextMuted
                            )
                        )
                    }

                    // Scoreboard aligned perfectly to top-right layout in Figma mockup
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current Completed Days (Streak Column)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentStreak.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextLight,
                                    fontSize = 24.sp,
                                    lineHeight = 24.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "DAYS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ActiveGreen,
                                    fontSize = 8.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        // Rank Column (Figma: RANK on top, 1 below)
                        val activeNodeId = nodes.firstOrNull { it.isActive }?.id ?: 1
                        val calculatedRank = 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "RANK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ActiveGreen,
                                    fontSize = 8.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = calculatedRank.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextLight,
                                    fontSize = 24.sp,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Track Progress Percentage Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Days Completed: $completedCount/$totalCount",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "${(progressPct * 100).toInt()}% Done",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Black,
                            color = ActiveGreen,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progressPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ActiveGreen,
                    trackColor = LockedGray
                )
            }
        }

        // Active Ascending Scrolling board Map
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (nodes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ActiveGreen)
                }
            } else {
                val lazyListState = key(nodes.isNotEmpty()) {
                    rememberLazyListState(initialFirstVisibleItemIndex = if (nodes.isNotEmpty()) nodes.size else 0)
                }
                val hasScrolled = remember { mutableStateOf(false) }
                
                // Auto-scroll to the bottom (Day 1) on first launch so users climb up
                LaunchedEffect(nodes) {
                    if (nodes.isNotEmpty() && !hasScrolled.value) {
                        hasScrolled.value = true
                        try {
                            lazyListState.scrollToItem(nodes.size)
                        } catch (e: Exception) {}
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    item {
                        // Tips guidance box displayed on top with sleek dark glassmorphism
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            color = Color(0xFF1E1535).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF3C2C63).copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Instructions",
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Your journey starts at the bottom! Complete missions and watch yourself ascend to the peak. Tap a node to log your proofs.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFD6C5F0),
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    // IMPORTANT: Reverse list for visual climbing progression (Day 1 bottom -> Day 365 top)
                    val reversedNodesWithMedia = nodesWithMedia.reversed()

                    items(
                        items = reversedNodesWithMedia,
                        key = { it.node.id }
                    ) { nodeWithMedia ->
                        val node = nodeWithMedia.node
                        val mediaList = nodeWithMedia.mediaList

                        val pathAbove = remember { Path() }
                        val pathBelow = remember { Path() }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Canvas renders 100% mathematically continuous curves
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                
                                val currentX = getXCoordinateFraction(node.id) * w
                                val currentY = h / 2f

                                // Curved road to preceding rendering node (visually ABOVE in Column: larger node.id)
                                if (node.id < nodes.size) {
                                    val topX = (getXCoordinateFraction(node.id) + getXCoordinateFraction(node.id + 1)) / 2f * w

                                    // Reuse pathAbove structure
                                    pathAbove.reset()
                                    pathAbove.moveTo(topX, 0f)
                                    pathAbove.cubicTo(topX, h / 4f, currentX, h / 4f, currentX, currentY)

                                    // 1. Base shadow / track backing
                                    drawPath(
                                        path = pathAbove,
                                        color = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE5E5EA),
                                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                                    )
                                    // 2. Clear mid-track line
                                    drawPath(
                                        path = pathAbove,
                                        color = if (isDark) Color(0xFF2C2C2C) else Color(0xFFD1D1D6),
                                        style = Stroke(width = 16f, cap = StrokeCap.Round)
                                    )
                                    // 3. Highlight colored state path
                                    val targetCompleted = nodes.firstOrNull { it.id == node.id + 1 }?.isCompleted == true
                                    val isComp = node.isCompleted && targetCompleted
                                    val isAct = (node.isCompleted || node.isActive) && (targetCompleted || (nodes.firstOrNull { it.id == node.id + 1 }?.isActive == true))

                                    if (isComp || isAct) {
                                        drawPath(
                                            path = pathAbove,
                                            color = if (isComp) CompletedGold else ActiveGreen,
                                            style = Stroke(width = 10f, cap = StrokeCap.Round)
                                        )
                                    }
                                }

                                // Curved road to subsequent rendering node (visually BELOW in Column: smaller node.id)
                                if (node.id > 1) {
                                    val bottomX = (getXCoordinateFraction(node.id) + getXCoordinateFraction(node.id - 1)) / 2f * w

                                    // Reuse pathBelow structure
                                    pathBelow.reset()
                                    pathBelow.moveTo(currentX, currentY)
                                    pathBelow.cubicTo(currentX, 3f * h / 4f, bottomX, 3f * h / 4f, bottomX, h)

                                    // 1. Base shadow / track backing
                                    drawPath(
                                        path = pathBelow,
                                        color = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE5E5EA),
                                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                                    )
                                    // 2. Clear mid-track line
                                    drawPath(
                                        path = pathBelow,
                                        color = if (isDark) Color(0xFF2C2C2C) else Color(0xFFD1D1D6),
                                        style = Stroke(width = 16f, cap = StrokeCap.Round)
                                    )
                                    // 3. Highlight colored state path
                                    val prevCompleted = nodes.firstOrNull { it.id == node.id - 1 }?.isCompleted == true
                                    val isComp = node.isCompleted && prevCompleted
                                    val isAct = (node.isCompleted || node.isActive) && (prevCompleted || (nodes.firstOrNull { it.id == node.id - 1 }?.isActive == true))

                                    if (isComp || isAct) {
                                        drawPath(
                                            path = pathBelow,
                                            color = if (isComp) CompletedGold else ActiveGreen,
                                            style = Stroke(width = 10f, cap = StrokeCap.Round)
                                        )
                                    }
                                }
                            }

                            // Symmetric 3-column Layout to guarantee NO OVERLAP between notes and road
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val bias = getHorizontalBias(node.id)
                                val activeNode = nodes.find { it.isActive }
                                val activeId = activeNode?.id ?: 1
                                val isUnlocked = node.isActive || node.isCompleted || node.id == 1

                                val onClickNode = {
                                    if (isUnlocked) {
                                        viewModel.selectNode(node.id)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Day ${node.id} is LOCKED! Complete Day $activeId and wait until midnight to unlock.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                
                                // Left Column (takes Note if node is Right, or takes Node if node is Left)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(if (node.id == 1) Modifier.offset(x = 56.dp) else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bias == -0.35f) {
                                        NodeItem(
                                            node = node,
                                            index = node.id,
                                            onClick = onClickNode,
                                            achievementCount = mediaList.size
                                        )
                                    } else if (bias == 0.35f && mediaList.isNotEmpty()) {
                                        JournalSummaryMiniCard(mediaList = mediaList, onClick = onClickNode)
                                    }
                                }

                                // Center Column (Only ever takes Node if node is Center! Completely empty of notes to preserve the road!)
                                Box(
                                    modifier = Modifier.weight(1.2f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bias == 0.0f) {
                                        NodeItem(
                                            node = node,
                                            index = node.id,
                                            onClick = onClickNode,
                                            achievementCount = mediaList.size
                                        )
                                    }
                                }

                                // Right Column (takes Note if node is Left/Center, or takes Node if node is Right)
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bias == 0.35f) {
                                        NodeItem(
                                            node = node,
                                            index = node.id,
                                            onClick = onClickNode,
                                            achievementCount = mediaList.size
                                        )
                                    } else if ((bias == -0.35f || bias == 0.0f) && mediaList.isNotEmpty()) {
                                        JournalSummaryMiniCard(mediaList = mediaList, onClick = onClickNode)
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

@Composable
fun JournalSummaryMiniCard(
    mediaList: List<NodeMedia>,
    onClick: () -> Unit
) {
    val latestMedia = mediaList.lastOrNull() ?: return
    val parts = latestMedia.content.split("|||")
    val filePath = parts.getOrNull(0) ?: ""
    val descriptionText = parts.getOrNull(1) ?: ""

    val mediaIcon = when (latestMedia.type) {
        MediaType.TEXT -> Icons.Default.Edit
        MediaType.IMAGE -> Icons.Default.Image
        MediaType.AUDIO -> Icons.Default.Mic
        MediaType.VIDEO -> Icons.Default.Videocam
    }
    val mediaColor = when (latestMedia.type) {
        MediaType.TEXT -> AccentCyan
        MediaType.IMAGE -> CompletedGold
        MediaType.AUDIO -> ActiveGreen
        MediaType.VIDEO -> Color(0xFFEA580C)
    }
    val mediaLabel = when (latestMedia.type) {
        MediaType.TEXT -> "Note Added"
        MediaType.IMAGE -> "Photo Entry"
        MediaType.AUDIO -> "Voice Note"
        MediaType.VIDEO -> "Quick Video"
    }

    Surface(
        color = SpaceCardBg.copy(alpha = 0.95f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, SpaceCardBorder.copy(alpha = 0.5f)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            if (latestMedia.type == MediaType.IMAGE && filePath.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = filePath,
                        contentDescription = "Day Image Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (descriptionText.isNotBlank()) {
                    Text(
                        text = descriptionText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextLight,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Image logged",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            } else if (latestMedia.type == MediaType.AUDIO) {
                val context = LocalContext.current
                var isPlayState by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = mediaIcon,
                        contentDescription = null,
                        tint = mediaColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mediaLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = mediaColor,
                            fontSize = 9.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                var mediaPlayerLocal by remember { mutableStateOf<MediaPlayer?>(null) }
                DisposableEffect(filePath) {
                    onDispose {
                        mediaPlayerLocal?.apply {
                            if (isPlayState) stop()
                            release()
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ActiveGreen.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (filePath.isNotBlank() && File(filePath).exists()) {
                                if (isPlayState) {
                                    mediaPlayerLocal?.apply {
                                        pause()
                                        isPlayState = false
                                    }
                                } else {
                                    try {
                                        if (mediaPlayerLocal == null) {
                                            mediaPlayerLocal = MediaPlayer().apply {
                                                setDataSource(filePath)
                                                prepare()
                                                setOnCompletionListener {
                                                    isPlayState = false
                                                }
                                            }
                                        }
                                        mediaPlayerLocal?.start()
                                        isPlayState = true
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot play audio record.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "No audio note file found.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayState) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause Voice Note",
                            tint = ActiveGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlayState) "Playing..." else descriptionText.ifBlank { "Listen VN" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextLight,
                            fontSize = 8.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else if (latestMedia.type == MediaType.VIDEO && filePath.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = mediaIcon,
                        contentDescription = null,
                        tint = mediaColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mediaLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = mediaColor,
                            fontSize = 9.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Render silent video preview looped
                VideoPlayer(
                    filePath = filePath,
                    muted = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                )
                if (descriptionText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = descriptionText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextLight,
                            fontSize = 8.sp,
                            fontStyle = FontStyle.Italic
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = mediaIcon,
                        contentDescription = null,
                        tint = mediaColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mediaLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = mediaColor,
                            fontSize = 9.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                val displayText = if (latestMedia.type == MediaType.TEXT) {
                    latestMedia.content
                } else {
                    descriptionText.ifBlank { "Recorded memo" }
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextLight.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        lineHeight = 11.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: SETTINGS SCREEN IMPLEMENTATION WITH DEV SUITE
// -------------------------------------------------------------
@Composable
fun SettingsScreen(viewModel: JourneyViewModel, nodes: List<MilestoneNode>) {
    val completedCount = nodes.count { it.isCompleted }
    val totalCount = nodes.size.coerceAtLeast(1)

    // Collect dynamic visual settings
    val isDark by viewModel.isDarkMode.collectAsState()
    val themeColorHex by viewModel.themeColorHex.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val avatarPresetIndex by viewModel.avatarPresetIndex.collectAsState()
    val customAvatarUri by viewModel.customAvatarUri.collectAsState()

    var currentStreak = 0
    for (node in nodes) {
        if (node.isCompleted) {
            currentStreak++
        } else {
            break
        }
    }

    val colorPresets = listOf(
        "#C084FC" to "Lilac \uD83D\uDC9C",
        "#EC4899" to "Rose \uD83C\uDF38",
        "#3B82F6" to "Sapphire \uD83D\uDC99",
        "#10B981" to "Mint \uD83D\uDC9A",
        "#F59E0B" to "Gold \uD83D\uDC9B",
        "#FF5722" to "Tangerine \uD83D\uDC91",
        "#00BCD4" to "Cyan \uD83E\uDDE1"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // App Title
        Text(
            text = "PREFERENCES",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                color = ActiveGreen,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Profile and platform diagnostics",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Wanderer Traveler profile card
        Surface(
            color = SpaceCardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SpaceCardBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circle Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ActiveGreen, AccentCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JP",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Journey Pioneer \uD83C\uDF1F",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextLight
                    )
                )
                Text(
                    text = "Lvl 2 Pathfinder • Pro Member",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Metric Statistics card strip
        Text(
            text = "JOURNEY DISCIPLINE METRICS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = CompletedGold,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = SpaceCardBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "STREAK", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$currentStreak Days", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = ActiveGreen))
                }
            }
            Surface(
                color = SpaceCardBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "COMPLETED", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$completedCount Mapped", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = ActiveGreen))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Theme Configuration
        Text(
            text = "\uD83C\uDFA8 VISUAL THEME PREFERENCES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = CompletedGold,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            color = SpaceCardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SpaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Light vs Dark Mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme style mode",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextLight, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isDark) "Ambient cosmic midnight" else "Clean white-background light mode",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ActiveGreen,
                            checkedTrackColor = ActiveGreen.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("dark_mode_switch_setting")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SpaceCardBorder.copy(alpha = 0.5f))

                // Predefined Palette Taps
                Text(
                    text = "Select Primary Color Accent",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextLight, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorPresets.forEach { (presetHex, presetName) ->
                        val isSelected = themeColorHex.equals(presetHex, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.setThemeColorHex(presetHex) }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(presetHex)))
                                    .border(
                                        BorderStroke(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) (if (isDark) Color.White else Color.Black) else Color.Transparent
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (presetHex == "#C084FC" || presetHex == "#FFFD38") Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = presetName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = if (isSelected) ActiveGreen else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// -------------------------------------------------------------
// CORE REUSABLE SUB-COMPONENTS
// -------------------------------------------------------------
@Composable
fun PulsingGlow() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(80.dp * pulseScale)
            .clip(CircleShape)
            .background(ActiveGreen.copy(alpha = pulseAlpha))
    )
}

@Composable
fun NodeItem(
    node: MilestoneNode,
    index: Int,
    onClick: () -> Unit,
    achievementCount: Int = 0
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(130.dp)
    ) {
        val nodeSize = if (node.isActive) 76.dp else 60.dp
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse Glow on the Active Node
            if (node.isActive) {
                PulsingGlow()
            }

            // Central Node Core Circle Styled with dynamic borders
            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .clip(CircleShape)
                    .background(
                        when {
                            node.isActive -> Brush.verticalGradient(
                                listOf(
                                    ActiveGreen,
                                    ActiveGreen.copy(alpha = 0.75f)
                                )
                            )
                            node.isCompleted -> Brush.verticalGradient(
                                listOf(
                                    ActiveGreen.copy(alpha = 0.9f),
                                    ActiveGreen.copy(alpha = 0.6f)
                                )
                            )
                            else -> if (DynamicTheme.isDarkMode) {
                                Brush.verticalGradient(listOf(Color(0xFF281E43), Color(0xFF1E1635)))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFFE2DFEA), Color(0xFFE5E2F0)))
                            }
                        }
                    )
                    .then(
                        if (node.isActive) {
                            Modifier.border(4.dp, if (DynamicTheme.isDarkMode) Color.White else Color(0xFF333333), CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 32.dp),
                        onClick = onClick
                    )
                    .testTag("node_button_${node.id}"),
                contentAlignment = Alignment.Center
            ) {
                when {
                    node.isActive -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = index.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 18.sp,
                                    lineHeight = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 8.sp,
                                    letterSpacing = 0.5.sp,
                                    lineHeight = 8.sp
                                )
                            )
                        }
                    }
                    node.isCompleted -> {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }
                    else -> {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C6FA4),
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }
                }
            }

            // Complete Leaf Checkmark icon
            if (node.isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(22.dp)
                        .background(Color.White, CircleShape)
                        .padding(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ActiveGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Flag badge for current active position (Red flag)
            if (node.isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = (2).dp)
                        .background(Color(0xFFEF4444), CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Active Position",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Numeric badge of achievements opposite the red flag (TopStart)
            if (achievementCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 2.dp, y = (2).dp)
                        .size(22.dp)
                        .background(AccentCyan, CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = achievementCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Outer label detail text
        Text(
            text = getDayLabel(node.id),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (node.isActive) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (node.isActive) ActiveGreen else if (node.isCompleted) CompletedGold else TextMuted,
                fontFamily = if (node.isActive) FontFamily.Serif else FontFamily.Default
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// Interactive half-screen proof input sheet
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NodeDetailDialog(
    nodeWithMedia: NodeWithMedia,
    onDismiss: () -> Unit,
    onAddProof: (MediaType, String) -> Unit,
    onDeleteMedia: (Long) -> Unit,
    onSimulateMidnight: () -> Unit
) {
    val node = nodeWithMedia.node
    val mediaItems = nodeWithMedia.mediaList

    val context = LocalContext.current
    var proofText by remember { mutableStateOf("") }
    var selectedProofType by remember { mutableStateOf(MediaType.TEXT) }
    
    // File path and custom description fields
    var pendingFilePath by remember { mutableStateOf("") }
    var pendingDescription by remember { mutableStateOf("") }
    var isAddingNewProof by remember { mutableStateOf(false) }

    // Media states (audio & video recording)
    var isRecording by remember { mutableStateOf(false) }
    var isRecordingVideo by remember { mutableStateOf(false) }
    var voiceRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentRecordFile by remember { mutableStateOf<File?>(null) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var videoRecordingSeconds by remember { mutableStateOf(0) }

    // Sorted chronological entries
    val sortedMediaItems = remember(mediaItems) { mediaItems.sortedBy { it.timestamp } }

    val formattedUnlockDate = remember(node.unlockedAt) {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
        "${getDayLabel(node.id)}, ${sdf.format(Date(node.unlockedAt))}"
    }

    // Timer for voice recording UI feedback
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                recordingSeconds++
            }
        }
    }

    // Timer for video recording simulation
    LaunchedEffect(isRecordingVideo) {
        if (isRecordingVideo) {
            videoRecordingSeconds = 0
            while (isRecordingVideo) {
                kotlinx.coroutines.delay(1000)
                videoRecordingSeconds++
                if (videoRecordingSeconds >= 15) {
                    // Capped automatically at 15 seconds!
                    isRecordingVideo = false
                    val cacheFile = File(context.cacheDir, "simulated_video_${System.currentTimeMillis()}.mp4")
                    try {
                        FileOutputStream(cacheFile).use { out ->
                            out.write(ByteArray(1024)) // Dummy content to make it a real file
                        }
                    } catch (e: Exception) {}
                    pendingFilePath = cacheFile.absolutePath
                    Toast.makeText(context, "Video captured successfully (15s Limit hit!)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Clean-up on dimissal
    DisposableEffect(Unit) {
        onDispose {
            try {
                voiceRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {}
        }
    }

    // Launchers for system pickers & captures
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val cacheFile = File(context.cacheDir, "captured_img_${System.currentTimeMillis()}.png")
                FileOutputStream(cacheFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
                pendingFilePath = cacheFile.absolutePath
                Toast.makeText(context, "Captured Photo Loaded! Write a description below.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Camera capture error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val cacheFile = File(context.cacheDir, "gallery_img_${System.currentTimeMillis()}.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
                pendingFilePath = cacheFile.absolutePath
                Toast.makeText(context, "Photo Loaded! Write a description below.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Selection load failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val audioFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val cacheFile = File(context.cacheDir, "chosen_audio_${System.currentTimeMillis()}.mp3")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
                pendingFilePath = cacheFile.absolutePath
                Toast.makeText(context, "Audio file imported! You can describe it below.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Audio import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val videoFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val cacheFile = File(context.cacheDir, "chosen_video_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
                pendingFilePath = cacheFile.absolutePath
                Toast.makeText(context, "Video imported! Add details below (10-15s checked).", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Video import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUri = result.data?.data
            if (videoUri != null) {
                try {
                    val cacheFile = File(context.cacheDir, "camera_recorded_video_${System.currentTimeMillis()}.mp4")
                    context.contentResolver.openInputStream(videoUri)?.use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    pendingFilePath = cacheFile.absolutePath
                    Toast.makeText(context, "Video captured successfully! Add details below.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save recorded video: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Video recording returned no data.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cam = permissions[Manifest.permission.CAMERA] ?: false
        val mic = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        Toast.makeText(
            context,
            "Camera: ${if (cam) "Granted" else "Denied"} | Microphone: ${if (mic) "Granted" else "Denied"}",
            Toast.LENGTH_SHORT
        ).show()
    }

    val triggerAudioRecordingStart = {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionRequestLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            } else {
                val recFile = File(context.cacheDir, "achievement_voice_${System.currentTimeMillis()}.mp3")
                currentRecordFile = recFile

                @Suppress("DEPRECATION")
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    MediaRecorder()
                }
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setAudioSamplingRate(44100) // High quality sampling rate
                recorder.setAudioEncodingBitRate(128000) // 128 kbps high quality encoding
                recorder.setOutputFile(recFile.absolutePath)
                recorder.prepare()
                recorder.start()

                voiceRecorder = recorder
                isRecording = true
                Toast.makeText(context, "Recording your voice note...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to start recording: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    val triggerAudioRecordingStop = {
        try {
            voiceRecorder?.apply {
                stop()
                release()
            }
            voiceRecorder = null
            isRecording = false
            currentRecordFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    pendingFilePath = file.absolutePath
                    Toast.makeText(context, "Voice recording captured! You can describe it below.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            isRecording = false
            voiceRecorder = null
            Toast.makeText(context, "Recording stop error", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(top = 16.dp)
                .testTag("add_proof_dialog"),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = SpaceDarkBg,
            border = BorderStroke(1.dp, SpaceCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Dialog Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (node.isCompleted) CompletedGold.copy(alpha = 0.2f) else ActiveGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = getDayLabel(node.id).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (node.isCompleted) CompletedGold else ActiveGreen
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            if (node.isActive) {
                                Text(
                                    text = "ACTIVE TODAY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ActiveGreen,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isAddingNewProof) "Log New Achievement" else "${getDayLabel(node.id)} Achievements",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextLight,
                                fontFamily = FontFamily.Serif
                            )
                        )

                        Text(
                            text = "Unlocked: $formattedUnlockDate",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isAddingNewProof) {
                                isAddingNewProof = false
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.background(SpaceCardBg, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isAddingNewProof) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                            contentDescription = if (isAddingNewProof) "Back" else "Close",
                            tint = TextLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isAddingNewProof) {
                    // SCREEN 1: LIST VIEW OF ACHIEVEMENTS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "MY ACHIEVEMENTS LIST",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CompletedGold,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (sortedMediaItems.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudQueue,
                                        contentDescription = null,
                                        tint = TextMuted.copy(alpha = 0.5f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No achievements logged for this day yet.",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextLight, fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tap the plus button below to log your notes, photos, voice recordings or videos!",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(bottom = 72.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(sortedMediaItems) { media ->
                                        MediaRowItem(
                                            media = media,
                                            index = sortedMediaItems.indexOf(media),
                                            onDelete = { onDeleteMedia(media.id) }
                                        )
                                    }
                                }
                            }
                        }

                        // Floating PLUS Bar at bottom of screen
                        ExtendedFloatingActionButton(
                            onClick = {
                                isAddingNewProof = true
                                proofText = ""
                                pendingFilePath = ""
                                pendingDescription = ""
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .testTag("floating_add_proof_button"),
                            containerColor = AccentCyan,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Achievement")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Achievement", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                } else {
                    // SCREEN 2: ADD NEW LOG PANEL (Notes, Picture, Audio, Video)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "SELECT ACHIEVEMENT TYPE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = CompletedGold,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Mode Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val chips = listOf(
                                Triple(MediaType.TEXT, Icons.Default.Edit, "Note"),
                                Triple(MediaType.IMAGE, Icons.Default.Image, "Photo"),
                                Triple(MediaType.AUDIO, Icons.Default.Mic, "Audio"),
                                Triple(MediaType.VIDEO, Icons.Default.Videocam, "Video")
                            )

                            chips.forEach { (type, icon, name) ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ProofSelectorChip(
                                        type = type,
                                        icon = icon,
                                        text = name,
                                        selected = selectedProofType == type,
                                        onClick = {
                                            selectedProofType = type
                                            pendingFilePath = ""
                                            pendingDescription = ""
                                            proofText = ""
                                        },
                                        testTag = "tab_proof_${type.name.lowercase()}"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Inputs per selected media mode
                        when (selectedProofType) {
                            MediaType.TEXT -> {
                                OutlinedTextField(
                                    value = proofText,
                                    onValueChange = { proofText = it },
                                    label = { Text("Write details of today's progress...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .testTag("proof_text_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCyan,
                                        unfocusedBorderColor = SpaceCardBorder,
                                        focusedTextColor = TextLight,
                                        unfocusedTextColor = TextLight,
                                        focusedLabelColor = AccentCyan,
                                        unfocusedLabelColor = TextMuted
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            MediaType.IMAGE -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SpaceCardBg, RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, SpaceCardBorder), RoundedCornerShape(12.dp))
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Select Photo Proof",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextLight, fontSize = 12.sp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                    permissionRequestLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                                } else {
                                                    cameraLauncher.launch(null)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CompletedGold),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CAMERA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp))
                                        }

                                        Button(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("GALLERY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp))
                                        }
                                    }

                                    if (pendingFilePath.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(115.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = pendingFilePath,
                                                contentDescription = "Selected Picture",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Captured image ready", style = MaterialTheme.typography.labelSmall.copy(color = ActiveGreen))
                                    }
                                }
                            }

                            MediaType.AUDIO -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SpaceCardBg, RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, SpaceCardBorder), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isRecording) "RECORDING IN PROGRESS" else "Audio Log Device",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRecording) Color.Red else TextLight,
                                            fontSize = 12.sp
                                        )
                                    )

                                    if (isRecording) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Timer: $recordingSeconds seconds",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Red, fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (!isRecording) {
                                            Button(
                                                onClick = { triggerAudioRecordingStart() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1.1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("RECORD", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp))
                                            }
                                        } else {
                                            Button(
                                                onClick = { triggerAudioRecordingStop() },
                                                colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1.1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("STOP", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp))
                                            }
                                        }

                                        Button(
                                            onClick = { audioFileLauncher.launch("audio/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaceCardBorder),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(0.9f)
                                        ) {
                                            Icon(imageVector = Icons.Default.AudioFile, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CHOOSE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 9.sp))
                                        }
                                    }

                                    if (pendingFilePath.isNotBlank() && !isRecording) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = ActiveGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Audio file ready", style = MaterialTheme.typography.labelSmall.copy(color = ActiveGreen, fontSize = 10.sp))
                                            }
                                        }
                                    }
                                }
                            }

                            MediaType.VIDEO -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SpaceCardBg, RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, SpaceCardBorder), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isRecordingVideo) "RECORDING VIDEO MEMO" else "Capture Video Achievement (10-15s)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRecordingVideo) Color.Red else TextLight,
                                            fontSize = 12.sp
                                        )
                                    )

                                    if (isRecordingVideo) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Duration: $videoRecordingSeconds / 15 seconds (Will stop automatically)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (videoRecordingSeconds < 10) Color.Red else ActiveGreen,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (!isRecordingVideo) {
                                            Button(
                                                onClick = {
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                        permissionRequestLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                                    } else {
                                                        try {
                                                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                                                                putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15)
                                                            }
                                                            recordVideoLauncher.launch(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Cannot open camera: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1.1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Camera, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("CAMERA (15s)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp))
                                            }

                                            Button(
                                                onClick = {
                                                    isRecordingVideo = true
                                                    pendingFilePath = ""
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("VIRTUAL REC", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp))
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    if (videoRecordingSeconds < 3) {
                                                        Toast.makeText(context, "Maintain video recording at least 3s!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        isRecordingVideo = false
                                                        val cacheFile = File(context.cacheDir, "simulated_video_${System.currentTimeMillis()}.mp4")
                                                        try {
                                                            FileOutputStream(cacheFile).use { out ->
                                                                out.write(ByteArray(1024))
                                                            }
                                                        } catch (e: Exception) {}
                                                        pendingFilePath = cacheFile.absolutePath
                                                        Toast.makeText(context, "Virtual Video captured successfully!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(2.1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("STOP VIRTUAL REC", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp))
                                            }
                                        }

                                        Button(
                                            onClick = { videoFileLauncher.launch("video/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaceCardBorder),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(0.9f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("CHOOSE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 9.sp))
                                        }
                                    }

                                    if (pendingFilePath.isNotBlank() && !isRecordingVideo) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = ActiveGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Captured Video Preview (15s max):", style = MaterialTheme.typography.labelSmall.copy(color = ActiveGreen, fontWeight = FontWeight.Bold))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        VideoPlayerWidget(filePath = pendingFilePath)
                                    }
                                }
                            }
                        }

                        // Write Description caption field input (For Image, Audio, Video)
                        if (selectedProofType != MediaType.TEXT) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = pendingDescription,
                                onValueChange = { pendingDescription = it },
                                label = { 
                                    Text(
                                        if (selectedProofType == MediaType.IMAGE) "Describe what this picture tells (e.g., Python learned)..."
                                        else "Describe what this message holds (Optional)..."
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = SpaceCardBorder,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight,
                                    focusedLabelColor = AccentCyan,
                                    unfocusedLabelColor = TextMuted
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // LOCK IN ACHIEVEMENT SUBMIT BUTTON
                        val canSave = when (selectedProofType) {
                            MediaType.TEXT -> proofText.isNotBlank()
                            MediaType.IMAGE -> pendingFilePath.isNotBlank() && pendingDescription.isNotBlank()
                            MediaType.AUDIO -> pendingFilePath.isNotBlank()
                            MediaType.VIDEO -> pendingFilePath.isNotBlank()
                        }

                        val actionText = when (selectedProofType) {
                            MediaType.TEXT -> "PUBLISH NOTE ENTRY"
                            MediaType.IMAGE -> "PUBLISH PHOTO ENTRY"
                            MediaType.AUDIO -> "PUBLISH AUDIO VOICE"
                            MediaType.VIDEO -> "PUBLISH VIDEO ENTRY"
                        }

                        Button(
                            onClick = {
                                when (selectedProofType) {
                                    MediaType.TEXT -> {
                                        onAddProof(MediaType.TEXT, proofText)
                                        proofText = ""
                                        isAddingNewProof = false // Return back to list screen!
                                        Toast.makeText(context, "Note added to achievements!", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        // Store in combined format: filePath + "|||" + description
                                        val composite = "$pendingFilePath|||$pendingDescription"
                                        onAddProof(selectedProofType, composite)
                                        pendingFilePath = ""
                                        pendingDescription = ""
                                        isAddingNewProof = false // Return back to list screen!
                                        Toast.makeText(context, "Achievement added successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = canSave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_proof_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentCyan,
                                disabledContainerColor = LockedGray
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = actionText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
fun MediaRowItem(
    media: NodeMedia,
    index: Int,
    onDelete: () -> Unit
) {
    val parts = remember(media.content) { media.content.split("|||") }
    val filePath = parts.getOrNull(0) ?: ""
    val descriptionText = parts.getOrNull(1) ?: ""

    var showFullImageDialog by remember { mutableStateOf(false) }

    if (showFullImageDialog && media.type == MediaType.IMAGE) {
        Dialog(onDismissRequest = { showFullImageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SpaceDarkBg,
                border = BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Full Photo View",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextLight, fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showFullImageDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextLight)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    AsyncImage(
                        model = filePath,
                        contentDescription = "Full Image View",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    if (descriptionText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = descriptionText,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextLight, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    Surface(
        color = SpaceCardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SpaceCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            val icon = when (media.type) {
                MediaType.TEXT -> Icons.Default.Edit
                MediaType.IMAGE -> Icons.Default.Image
                MediaType.AUDIO -> Icons.Default.Mic
                MediaType.VIDEO -> Icons.Default.Videocam
            }

            val accent = when (media.type) {
                MediaType.TEXT -> AccentCyan
                MediaType.IMAGE -> CompletedGold
                MediaType.AUDIO -> ActiveGreen
                MediaType.VIDEO -> Color(0xFFEA580C)
            }

            Surface(
                color = accent.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val formattedTime = remember(media.timestamp) {
                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                    sdf.format(Date(media.timestamp))
                }

                Text(
                    text = "Achievement ${index + 1} for tonight ($formattedTime)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                when (media.type) {
                    MediaType.TEXT -> {
                        Text(
                            text = media.content,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextLight.copy(alpha = 0.95f))
                        )
                    }
                    MediaType.IMAGE -> {
                        if (filePath.isNotBlank()) {
                            AsyncImage(
                                model = filePath,
                                contentDescription = "Goal Image Entry Preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showFullImageDialog = true }
                                    .border(BorderStroke(1.dp, SpaceCardBorder), RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (descriptionText.isNotBlank()) {
                            Text(
                                text = "Caption: $descriptionText",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextLight, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                            )
                        } else {
                            Text(
                                text = "No description entered for image.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                    }
                    MediaType.AUDIO -> {
                        if (descriptionText.isNotBlank()) {
                            Text(
                                text = "Voice Memo: \"$descriptionText\"",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextLight, fontStyle = FontStyle.Italic)
                            )
                        } else {
                            Text(
                                text = "Voice Memo file logged.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Path: " + filePath.substringAfterLast("/"),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp)
                        )
                        AudioPlayerWidget(filePath = filePath)
                    }
                    MediaType.VIDEO -> {
                        if (descriptionText.isNotBlank()) {
                            Text(
                                text = "Video Memo: \"$descriptionText\"",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextLight, fontStyle = FontStyle.Italic)
                            )
                        } else {
                            Text(
                                text = "Quick Video logged.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Path: " + filePath.substringAfterLast("/"),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp)
                        )
                        VideoPlayerWidget(filePath = filePath)
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_media_button_${media.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AudioPlayerWidget(filePath: String) {
    if (filePath.isBlank()) {
        Text("Audio recording not found", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
        return
    }

    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(filePath) {
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying) {
                    try { stop() } catch(e: Exception) {}
                }
                release()
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(SpaceCardBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, SpaceCardBorder.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    mediaPlayer?.apply {
                        pause()
                        isPlaying = false
                    }
                } else {
                    try {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(filePath)
                                prepare()
                                setOnCompletionListener {
                                    isPlaying = false
                                }
                            }
                        }
                        mediaPlayer?.start()
                        isPlaying = true
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error starting voice playback", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .size(36.dp)
                .background(ActiveGreen.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause VN" else "Play VN",
                tint = ActiveGreen,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = if (isPlaying) "Playing VN Voice..." else "Play recorded Voice Note",
                style = MaterialTheme.typography.labelMedium.copy(color = TextLight, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Tap button to listen / relisten",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp)
            )
        }
    }
}

@Composable
fun VideoPlayer(
    filePath: String,
    muted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (filePath.isBlank() || !File(filePath).exists()) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No Video", color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
        return
    }

    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }

    DisposableEffect(filePath) {
        onDispose {
            try {
                videoViewInstance?.stopPlayback()
            } catch (e: Exception) {}
        }
    }

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                videoViewInstance = this
                setVideoPath(filePath)
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    if (muted) {
                        mp.setVolume(0f, 0f)
                    } else {
                        mp.setVolume(1f, 1f)
                    }
                    start()
                }
            }
        },
        update = { videoView ->
            videoViewInstance = videoView
            try {
                videoView.setVideoPath(filePath)
            } catch (e: Exception) {}
        },
        modifier = modifier
    )
}

@Composable
fun FullscreenIcon(tint: Color = Color.White) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val w = size.width
        val h = size.height
        val t = 2.dp.toPx()
        val l = 4.dp.toPx()
        
        // Top-left corner
        drawLine(color = tint, start = Offset(0f, 0f), end = Offset(l, 0f), strokeWidth = t)
        drawLine(color = tint, start = Offset(0f, 0f), end = Offset(0f, l), strokeWidth = t)
        
        // Top-right corner
        drawLine(color = tint, start = Offset(w, 0f), end = Offset(w - l, 0f), strokeWidth = t)
        drawLine(color = tint, start = Offset(w, 0f), end = Offset(w, l), strokeWidth = t)
        
        // Bottom-left corner
        drawLine(color = tint, start = Offset(0f, h), end = Offset(l, h), strokeWidth = t)
        drawLine(color = tint, start = Offset(0f, h), end = Offset(0f, h - l), strokeWidth = t)
        
        // Bottom-right corner
        drawLine(color = tint, start = Offset(w, h), end = Offset(w - l, h), strokeWidth = t)
        drawLine(color = tint, start = Offset(w, h), end = Offset(w, h - l), strokeWidth = t)
    }
}

@Composable
fun FullscreenExitIcon(tint: Color = Color.White) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val w = size.width
        val h = size.height
        val t = 2.dp.toPx()
        val l = 4.dp.toPx()
        
        // Middle references
        val mx = w / 3f
        val my = h / 3f
        
        // Top-left pointing inward
        drawLine(color = tint, start = Offset(mx, my), end = Offset(mx - l, my), strokeWidth = t)
        drawLine(color = tint, start = Offset(mx, my), end = Offset(mx, my - l), strokeWidth = t)
        
        // Top-right pointing inward
        drawLine(color = tint, start = Offset(w - mx, my), end = Offset(w - mx + l, my), strokeWidth = t)
        drawLine(color = tint, start = Offset(w - mx, my), end = Offset(w - mx, my - l), strokeWidth = t)
        
        // Bottom-left pointing inward
        drawLine(color = tint, start = Offset(mx, h - my), end = Offset(mx - l, h - my), strokeWidth = t)
        drawLine(color = tint, start = Offset(mx, h - my), end = Offset(mx, h - my + l), strokeWidth = t)
        
        // Bottom-right pointing inward
        drawLine(color = tint, start = Offset(w - mx, h - my), end = Offset(w - mx + l, h - my), strokeWidth = t)
        drawLine(color = tint, start = Offset(w - mx, h - my), end = Offset(w - mx, h - my + l), strokeWidth = t)
    }
}

fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun CustomVideoPlayerControlsContainer(
    filePath: String,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0) }
    var durationMs by remember { mutableStateOf(0) }
    var dragPositionMs by remember { mutableStateOf<Int?>(null) }
    
    // YouTube indicators for double tap
    var showForwardIndicator by remember { mutableStateOf(false) }
    var showBackwardIndicator by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    // Poll current position of the VideoView at interval
    LaunchedEffect(videoViewInstance, isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                videoViewInstance?.let { vv ->
                    currentPositionMs = vv.currentPosition
                    if (vv.duration > 0) {
                        durationMs = vv.duration
                    }
                    if (!vv.isPlaying) {
                        isPlaying = false
                    }
                }
                kotlinx.coroutines.delay(250)
            }
        }
    }

    // Controls visibility timer (auto-fade controls after 2.5s if video is playing)
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            kotlinx.coroutines.delay(2500)
            showControls = false
        }
    }

    // Simple delay handlers for high-fidelity skip feedback
    LaunchedEffect(showForwardIndicator) {
        if (showForwardIndicator) {
            kotlinx.coroutines.delay(650)
            showForwardIndicator = false
        }
    }
    LaunchedEffect(showBackwardIndicator) {
        if (showBackwardIndicator) {
            kotlinx.coroutines.delay(650)
            showBackwardIndicator = false
        }
    }

    DisposableEffect(filePath) {
        onDispose {
            try {
                videoViewInstance?.stopPlayback()
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = if (isFullscreen) {
            modifier.background(Color.Black)
        } else {
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
        },
        contentAlignment = Alignment.Center
    ) {
        // 1. Android VideoView
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoPath(filePath)
                    setOnPreparedListener { mp ->
                        mp.isLooping = false // Disable looping as requested
                        mp.setVolume(1f, 1f) // high quality sound with volume
                        durationMs = mp.duration
                        start()
                        isPlaying = true
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        currentPositionMs = 0
                        showControls = true // Bring controls back on completion
                    }
                    setOnErrorListener { _, _, _ ->
                        true // handle gracefully
                    }
                }
            },
            update = { view ->
                videoViewInstance = view
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Gesture Detector Layer (Translucent/Invisible wrapper over VideoView)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(videoViewInstance, durationMs) {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                        },
                        onDoubleTap = { offset ->
                            val isRightHalf = offset.x > size.width / 2f
                            videoViewInstance?.let { vv ->
                                val displacement = 10000 // 10 seconds skip as requested
                                val target = if (isRightHalf) {
                                    (vv.currentPosition + displacement).coerceAtMost(durationMs)
                                } else {
                                    (vv.currentPosition - displacement).coerceAtLeast(0)
                                }
                                vv.seekTo(target)
                                currentPositionMs = target
                                if (isRightHalf) {
                                    showForwardIndicator = true
                                    showBackwardIndicator = false
                                } else {
                                    showBackwardIndicator = true
                                    showForwardIndicator = false
                                }
                                // Make controls appear to reflect seek position immediately
                                showControls = true
                            }
                        }
                    )
                }
        )

        // 3. Skip indicators (Rewind on Left, Skip on Right)
        if (showBackwardIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "10s",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
            }
        }

        if (showForwardIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "10s",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
            }
        }

        // 4. Overlaid interactive controls (Fade out to fully highlight video)
        if (showControls || !isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Central Play/Pause button
                IconButton(
                    onClick = {
                        videoViewInstance?.let { vv ->
                            if (vv.isPlaying) {
                                vv.pause()
                                isPlaying = false
                            } else {
                                vv.start()
                                isPlaying = true
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = AccentCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Immersive bottom bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .then(
                            if (isFullscreen) {
                                Modifier
                                    .navigationBarsPadding()
                                    .padding(bottom = 54.dp, start = 16.dp, end = 16.dp)
                            } else {
                                Modifier.padding(bottom = 4.dp)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = formatTime(dragPositionMs ?: currentPositionMs),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 9.sp),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Start
                    )

                    Slider(
                        value = if (durationMs > 0) {
                            (dragPositionMs ?: currentPositionMs).toFloat() / durationMs.toFloat()
                        } else {
                            0f
                        },
                        onValueChange = { fraction ->
                            if (durationMs > 0) {
                                dragPositionMs = (fraction * durationMs).toInt()
                            }
                        },
                        onValueChangeFinished = {
                            dragPositionMs?.let { dragMs ->
                                videoViewInstance?.seekTo(dragMs)
                                currentPositionMs = dragMs
                            }
                            dragPositionMs = null
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .padding(horizontal = 4.dp)
                    )

                    Text(
                        text = formatTime(durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 9.sp),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(28.dp)
                            .background(ActiveGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    ) {
                        if (isFullscreen) {
                            FullscreenExitIcon(tint = Color.White)
                        } else {
                            FullscreenIcon(tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerWidget(filePath: String) {
    if (filePath.isBlank() || !File(filePath).exists()) {
        Text("Video record not found", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
        return
    }

    var isFullScreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(SpaceCardBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, SpaceCardBorder.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        if (!isFullScreen) {
            CustomVideoPlayerControlsContainer(
                filePath = filePath,
                isFullscreen = false,
                onToggleFullscreen = { isFullScreen = true }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Playing in full screen...",
                    color = AccentCyan,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (isFullScreen) {
        Dialog(
            onDismissRequest = { isFullScreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                CustomVideoPlayerControlsContainer(
                    filePath = filePath,
                    isFullscreen = true,
                    onToggleFullscreen = { isFullScreen = false },
                    modifier = Modifier.fillMaxSize()
                )

                // High visibility close/exit button on top-right corner
                IconButton(
                    onClick = { isFullScreen = false },
                    modifier = Modifier
                        .statusBarsPadding()
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.60f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Fullscreen Mode",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProofSelectorChip(
    type: MediaType,
    icon: ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (selected) AccentCyan.copy(alpha = 0.15f) else SpaceCardBg,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) AccentCyan else SpaceCardBorder
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) AccentCyan else TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selected) AccentCyan else TextMuted,
                    fontSize = 10.sp
                )
            )
        }
    }
}

// Helper methods to calculate continuous board-game path offsets and Canvas anchors alignment
private fun getHorizontalBias(id: Int): Float {
    return when (id % 3) {
        1 -> -0.35f // Left
        2 -> 0.35f  // Right
        0 -> 0.0f   // Center
        else -> 0.0f
    }
}

private fun getXCoordinateFraction(id: Int): Float {
    if (id == 1) return 0.44f // Shift Day 1 further to the right as requested
    return when (getHorizontalBias(id)) {
        -0.35f -> 0.28f // Left column center
        0.0f -> 0.5f    // Center column center
        0.35f -> 0.72f  // Right column center
        else -> 0.5f
    }
}
