package com.example.gridmaster.ui
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.core.content.FileProvider
import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gridmaster.data.EquipmentType
import com.example.gridmaster.data.MaintenanceFreq
import com.example.gridmaster.data.MaintenanceTask
import com.example.gridmaster.data.PlannedWork
import com.example.gridmaster.data.Priority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceViewModel,
    onOpenDrawer: () -> Unit // <--- NEW
) {
    // --- DATA STREAMS ---
    val tasks by viewModel.filteredTasks.collectAsState()
    val notes by viewModel.plannedNotes.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val selectedFreq by viewModel.selectedFreq.collectAsState()
    val selectedEquip by viewModel.selectedEquipment.collectAsState()
    val currentMonthMillis by viewModel.currentMonth.collectAsState()

    // --- UI STATE ---
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<PlannedWork?>(null) }
    var showDailyBriefing by remember { mutableStateOf(!viewModel.isBriefingSeen) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    // --- BRIEFING POPUP ---
    if (showDailyBriefing && pendingCount > 0) {
        AlertDialog(
            onDismissRequest = {
                showDailyBriefing = false
                viewModel.isBriefingSeen = true
            },
            icon = { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Morning Briefing") },
            text = { Text("You have $pendingCount tasks/notes pending for today.") },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                Button(onClick = {
                    showDailyBriefing = false
                    viewModel.isBriefingSeen = true
                }) { Text("Let's Start") }
            }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 24.dp, top = 48.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f) // <--- THIS SAVES THE LAYOUT
                    ) {
                        // --- DRAWER ICON ---
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Maintenance", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Operations", fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Light)
                        }
                    }
                    Spacer(Modifier.width(8.dp)) // Safe gap

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Kept Theme/Export for convenience, but you can remove Theme if you want to force Drawer usage

                        IconButton(onClick = { showExportDialog = true }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                            Icon(Icons.Default.DateRange, "Download", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.background).padding(4.dp)) {
                    TabButton("Routine Checks", selectedTab == 0) { selectedTab = 0 }
                    TabButton("Work Orders", selectedTab == 1) { selectedTab = 1 }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        noteToEdit = null
                        showAddNoteDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(Icons.Default.Add, "Add Note") }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {

            if (selectedTab == 0) {
                // --- ROUTINE TAB ---
                val groupedTasks = tasks.groupBy { it.equipment }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        RoutineDashboard(
                            tasks,
                            selectedFreq,
                            selectedEquip,
                            onFilterFreq = { viewModel.setFrequencyFilter(it) },
                            onFilterEquip = { viewModel.setEquipmentFilter(it) }
                        )
                    }
                    groupedTasks.forEach { (equip, equipmentTasks) ->
                        item {
                            Box(Modifier.padding(horizontal = 16.dp)) {
                                ExpandableEquipmentGroup(equip, equipmentTasks) { task -> viewModel.toggleTask(task) }
                            }
                        }
                    }
                }

            } else {
                // --- WORK ORDERS TAB (Split View) ---

                // 1. Split the list into Active and Completed
                val activeNotes = notes.filter { !it.isCompleted }
                val historyNotes = notes.filter { it.isCompleted }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month Picker (Keep this at top)
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            OutlinedButton(
                                onClick = { showMonthPicker = true },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(monthFormat.format(currentMonthMillis))
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    }

                    // 2. Empty State
                    if (notes.isEmpty()) {
                        item {
                            Text(
                                "No work orders found for this month.",
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // 3. ACTIVE SECTION (No Header needed, just show them)
                    items(activeNotes) { note ->
                        ModernWorkOrderCard(
                            note = note,
                            onToggle = { viewModel.toggleNote(note) },
                            onEdit = {
                                noteToEdit = note
                                showAddNoteDialog = true
                            },
                            onDelete = { viewModel.deleteNote(note) }
                        )
                    }

                    // 4. HISTORY SECTION (Only show if there are completed items)
                    if (historyNotes.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Divider(Modifier.weight(1f))
                                Text(
                                    "COMPLETED HISTORY",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Divider(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        items(historyNotes) { note ->
                            // Render completed items with a slightly faded look (alpha)
                            Box(modifier = Modifier.alpha(0.6f)) {
                                ModernWorkOrderCard(
                                    note = note,
                                    onToggle = { viewModel.toggleNote(note) }, // Unchecking moves it back to Active!
                                    onEdit = {
                                        noteToEdit = note
                                        showAddNoteDialog = true
                                    },
                                    onDelete = { viewModel.deleteNote(note) }
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // --- DIALOGS ---

    if (showAddNoteDialog) {
        MaintAddNoteDialog(
            existingNote = noteToEdit,
            onDismiss = { showAddNoteDialog = false },
            // [UPDATE] Handle the new 'uri' parameter
            onConfirm = { t, d, e, p, date, uri ->
                val id = noteToEdit?.id ?: ""
                viewModel.saveNote(id, t, d, e, p, date, uri)
                showAddNoteDialog = false
            }
        )
    }

    if (showExportDialog) {
        // FIX: Explicitly specify types (start: Long, end: Long) to solve inference error
        DateRangeExportDialog(
            title = "Download Maintenance Report",
            onDismiss = { showExportDialog = false },
            onExport = { start: Long, end: Long ->
                viewModel.exportReport(context, start, end)
                showExportDialog = false
            }
        )
    }

    if (showMonthPicker) {
        MaintMonthPicker(
            initialMillis = currentMonthMillis,
            onDismiss = { showMonthPicker = false },
            onDateSelected = {
                viewModel.setMonthFilter(it)
                showMonthPicker = false
            }
        )
    }
}

// ==========================================
// UI COMPONENTS
// ==========================================

@Composable
fun RoutineDashboard(
    tasks: List<MaintenanceTask>,
    selectedFreq: MaintenanceFreq?,
    selectedEquip: EquipmentType?,
    onFilterFreq: (MaintenanceFreq) -> Unit,
    onFilterEquip: (EquipmentType) -> Unit
) {
    val total = tasks.size
    val done = tasks.count { it.isCompleted }
    val progress = if (total > 0) done.toFloat() / total else 0f

    // Explicit Lists to help compiler
    val frequencies = MaintenanceFreq.values().toList()
    val equipments = EquipmentType.values().toList()

    Column(Modifier.padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Completion Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    Text("$done / $total Done", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text(if(progress == 1f) "Excellent Work!" else "Keep Going", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                CircularProgress(progress = progress, size = 80.dp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("FREQUENCY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = frequencies) { freq ->
                val isSelected = selectedFreq == freq
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bgColor).clickable { onFilterFreq(freq) }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(freq.name, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("EQUIPMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = equipments) { equip ->
                val isSelected = selectedEquip == equip
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                val displayName = equip.name.replace("_", " ")
                Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bgColor).clickable { onFilterEquip(equip) }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(displayName, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ExpandableEquipmentGroup(equipment: EquipmentType, tasks: List<MaintenanceTask>, onToggle: (MaintenanceTask) -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    val doneCount = tasks.count { it.isCompleted }
    val icon = when(equipment) {
        EquipmentType.TRANSFORMER -> Icons.Default.Build
        EquipmentType.BATTERY -> Icons.Default.Info
        EquipmentType.SWITCHYARD -> Icons.Default.Menu
        EquipmentType.FIRE_SYSTEM -> Icons.Default.Warning
        else -> Icons.Default.Settings
    }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(equipment.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("$doneCount / ${tasks.size} Complete", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.secondary)
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    tasks.forEach { task ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onToggle(task) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle(task) }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = task.taskDescription,
                                fontSize = 14.sp,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernWorkOrderCard(
    note: PlannedWork,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val isCritical = note.priority == Priority.CRITICAL

    // Card Colors
    val cardColor = MaterialTheme.colorScheme.surface
    val priorityColor = if (isCritical) Color(0xFFD32F2F) else Color(0xFF1976D2)
    val priorityLabel = if (isCritical) "CRITICAL" else "NORMAL"

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onEdit() } // Click card to edit
    ) {
        Column {
            // --- HEADER: IMAGE & STATUS ---
            Box(Modifier.height(140.dp).fillMaxWidth()) {
                if (note.imageUrl != null) {
                    // Show the attached photo
                    AsyncImage(
                        model = note.imageUrl,
                        contentDescription = "Defect Photo",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay for text readability
                    Box(Modifier.fillMaxSize().background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    ))
                } else {
                    // Fallback: Show a nice colored background with Icon
                    Box(Modifier.fillMaxSize().background(priorityColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Build, // Or specific equipment icon
                            contentDescription = null,
                            tint = priorityColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Priority Badge (Top Right)
                Surface(
                    color = priorityColor,
                    shape = RoundedCornerShape(bottomStart = 16.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = priorityLabel,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // --- BODY: DETAILS ---
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Checkbox for "Done"
                    Checkbox(
                        checked = note.isCompleted,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(checkedColor = priorityColor)
                    )
                    Spacer(Modifier.width(8.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null
                        )
                        Text(
                            text = note.equipmentType.name.replace("_", " "),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = note.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(8.dp))

                // --- FOOTER: META INFO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text(dateFormat.format(note.scheduledDate), fontSize = 12.sp, color = Color.Gray)

                        Spacer(Modifier.width(16.dp))

                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        // Assuming you might want to show who created it
                        Text(if(note.createdBy.isNotEmpty()) note.createdBy else "Me", fontSize = 12.sp, color = Color.Gray)
                    }

                    // Delete Button
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
    }
}

@Composable
fun CircularProgress(progress: Float, size: Dp, color: Color) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 1000))
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = color.copy(alpha = 0.1f), style = Stroke(width = 8.dp.toPx()))
            drawArc(color = color, startAngle = -90f, sweepAngle = 360 * animatedProgress, useCenter = false, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
        }
        Text(text = "${(animatedProgress * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintAddNoteDialog(
    existingNote: PlannedWork?,
    onDismiss: () -> Unit,
    // The signature expects 6 parameters now
    onConfirm: (String, String, EquipmentType, Priority, Long, Uri?) -> Unit
) {
    // 1. FORM STATE
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var description by remember { mutableStateOf(existingNote?.description ?: "") }
    var priority by remember { mutableStateOf(existingNote?.priority ?: Priority.NORMAL) }
    var selectedDate by remember { mutableLongStateOf(existingNote?.scheduledDate ?: System.currentTimeMillis()) }
    var equipmentExpanded by remember { mutableStateOf(false) }
    var selectedEquipment by remember { mutableStateOf(existingNote?.equipmentType ?: EquipmentType.GENERAL) }

    // 2. IMAGE STATE
    // Initialize with existing URL if editing, or null if new
    // Note: We only track *new* URIs here. If null, the ViewModel keeps the old one.
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // 3. SYSTEM HELPERS
    val context = LocalContext.current // [FIX] Defined only once
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // 4. LAUNCHERS
    // [FIX] Explicitly added 'success: Boolean' type
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            capturedImageUri = tempPhotoUri
        }
    }

    // [FIX] Explicitly added 'uri: Uri?' type
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedImageUri = uri
        }
    }

    // 5. DATE PICKER
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            selectedDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingNote == null) "New Work Order" else "Edit Work Order") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // --- FORM FIELDS ---
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedEquipment.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipment") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth().clickable { equipmentExpanded = true }
                    )
                    DropdownMenu(
                        expanded = equipmentExpanded,
                        onDismissRequest = { equipmentExpanded = false }
                    ) {
                        EquipmentType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedEquipment = type
                                    equipmentExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = dateFormat.format(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = priority == Priority.NORMAL,
                        onClick = { priority = Priority.NORMAL }
                    )
                    Text("Normal")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = priority == Priority.CRITICAL,
                        onClick = { priority = Priority.CRITICAL }
                    )
                    Text("Critical")
                }

                // --- PHOTO SECTION ---
                Spacer(Modifier.height(16.dp))
                Text("Attach Photo:", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val file = context.createImageFile()
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                tempPhotoUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Error: ${e.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Text("Camera")
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.List, null)
                        Text("Gallery")
                    }
                }

                // PREVIEW
                if (capturedImageUri != null) {
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = capturedImageUri,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // [FIX] Passing 'capturedImageUri' as the 6th argument
                onConfirm(title, description, selectedEquipment, priority, selectedDate, capturedImageUri)
            }) {
                Text(if (existingNote == null) "Create Ticket" else "Update Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
@Composable
fun MaintMonthPicker(initialMillis: Long, onDismiss: () -> Unit, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = initialMillis
    var displayYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { displayYear-- }) { Icon(Icons.Default.ArrowBack, "Prev") }
                Text(displayYear.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = { displayYear++ }) { Icon(Icons.Default.ArrowForward, "Next") }
            }
        },
        text = {
            LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(12) { index ->
                    Button(
                        onClick = { calendar.set(Calendar.YEAR, displayYear); calendar.set(Calendar.MONTH, index); onDateSelected(calendar.timeInMillis) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(index == calendar.get(Calendar.MONTH) && displayYear == calendar.get(Calendar.YEAR)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if(index == calendar.get(Calendar.MONTH) && displayYear == calendar.get(Calendar.YEAR)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text(months[index]) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}