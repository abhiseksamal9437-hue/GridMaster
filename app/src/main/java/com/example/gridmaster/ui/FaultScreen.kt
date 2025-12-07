package com.example.gridmaster.ui

import androidx.activity.result.contract.ActivityResultContracts
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gridmaster.data.FaultLog
import com.example.gridmaster.data.FaultType
import com.example.gridmaster.data.VoltageLevel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import android.net.Uri
import android.widget.Toast
import org.apache.poi.hpsf.Date
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaultScreen(
    viewModel: FaultViewModel = viewModel(factory = FaultViewModel.Factory),
    onOpenDrawer: () -> Unit // <--- NEW PARAMETER
) {
    val activeFaults by viewModel.activeFaults.collectAsState()
    val historyFaults by viewModel.filteredHistory.collectAsState()
    val currentMonthMillis by viewModel.currentMonth.collectAsState()

    var showLogDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    var faultToEdit by remember { mutableStateOf<FaultLog?>(null) }
    var faultToRestore by remember { mutableStateOf<FaultLog?>(null) }

    val context = LocalContext.current
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 24.dp, top = 48.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // --- NEW: DRAWER ICON ---
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Fault", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Detector", fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Light)
                        }
                    }

                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.Share, "Export", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    faultToEdit = null
                    showLogDialog = true
                },
                containerColor = Color(0xFFB71C1C),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Warning, null)
                Spacer(Modifier.width(8.dp))
                Text("LOG TRIP", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. STATUS
                item { SystemStatusCard(activeCount = activeFaults.size) }

                // 2. ACTIVE
                if (activeFaults.isNotEmpty()) {
                    item { SectionHeader("Active Alerts", Icons.Default.Notifications) }
                    items(activeFaults) { fault ->
                        ActiveFaultCard(
                            fault = fault,
                            onRestoreClick = { faultToRestore = fault },
                            onEdit = {
                                faultToEdit = fault
                                showLogDialog = true
                            }
                        )
                    }
                }

                // 3. HISTORY
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        SectionHeader("Event History", Icons.Default.List)
                        OutlinedButton(
                            onClick = { showMonthPicker = true },
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(monthFormat.format(currentMonthMillis), fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                if (historyFaults.isEmpty()) {
                    item { Text("No faults recorded this month.", modifier = Modifier.fillMaxWidth().padding(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.secondary) }
                }

                items(historyFaults) { fault ->
                    HistoryFaultCard(
                        fault = fault,
                        onDelete = { viewModel.deleteFault(fault.id) },
                        onEdit = {
                            faultToEdit = fault
                            showLogDialog = true
                        }
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // --- DIALOGS ---

    // --- DIALOGS ---

    if (showLogDialog) {
        LogFaultDialog(
            existingFault = faultToEdit,
            viewModel = viewModel,
            onDismiss = { showLogDialog = false },
            // [FIX] Added 'uri' at the end of the list
            onConfirm = { id, feeder, volt, type, ia, ib, ic, rem, time, restoreTime, isRestored, pA, pB, pC, pG, uri ->
                // [FIX] Passed 'uri' to the viewModel function
                viewModel.saveFault(id, feeder, volt, type, pA, pB, pC, pG, ia, ib, ic, rem, time, restoreTime, isRestored, uri)
                showLogDialog = false
            }
        )
    }
    // 2. QUICK RESTORE DIALOG (From "FIX" button)
    if (faultToRestore != null) {
        RestoreFaultDialog(
            onDismiss = { faultToRestore = null },
            onConfirm = { time ->
                viewModel.restoreFault(faultToRestore!!, time)
                faultToRestore = null
            }
        )
    }

    // 3. EXPORT DIALOG
    if (showExportDialog) {
        // Uses DateRangeExportDialog from ExportComponents.kt
        DateRangeExportDialog(
            title = "Download Fault Log",
            onDismiss = { showExportDialog = false },
            onExport = { start: Long, end: Long ->
                viewModel.exportFaults(context, start, end)
                showExportDialog = false
            }
        )
    }

    if (showMonthPicker) {
        FaultMonthPicker(currentMonthMillis, { showMonthPicker = false }) { viewModel.setMonthFilter(it); showMonthPicker = false }
    }
}

// ==========================================
// UPGRADED DIALOG WITH RESTORE FIELDS
// ==========================================
@Composable
fun LogFaultDialog(
    existingFault: FaultLog?,
    viewModel: FaultViewModel,
    onDismiss: () -> Unit,
    // Updated Signature
    onConfirm: (String, String, String, FaultType, String, String, String, String, Long, Long?, Boolean, Boolean, Boolean, Boolean, Boolean, Uri?) -> Unit
) {
    var step by remember(existingFault) { mutableIntStateOf(if(existingFault != null) 3 else 1) }
    var selectedVoltage by remember(existingFault) { mutableStateOf(if(existingFault?.voltageLevel == "132 kV") VoltageLevel.KV132 else VoltageLevel.KV33) }
    var selectedFeeder by remember(existingFault) { mutableStateOf(existingFault?.feederName ?: "") }

    // PHASE STATE
    var pA by remember(existingFault) { mutableStateOf(existingFault?.phaseA ?: false) }
    var pB by remember(existingFault) { mutableStateOf(existingFault?.phaseB ?: false) }
    var pC by remember(existingFault) { mutableStateOf(existingFault?.phaseC ?: false) }
    var pG by remember(existingFault) { mutableStateOf(existingFault?.phaseG ?: false) } // Ground

    var selectedFaultType by remember(existingFault) { mutableStateOf(existingFault?.faultType ?: FaultType.OC) }
    var ia by remember(existingFault) { mutableStateOf(existingFault?.currentIA ?: "") }
    var ib by remember(existingFault) { mutableStateOf(existingFault?.currentIB ?: "") }
    var ic by remember(existingFault) { mutableStateOf(existingFault?.currentIC ?: "") }
    var remarks by remember(existingFault) { mutableStateOf(existingFault?.remarks ?: "") }

    var selectedTime by remember(existingFault) { mutableLongStateOf(existingFault?.tripTime ?: System.currentTimeMillis()) }
    var isRestored by remember(existingFault) { mutableStateOf(existingFault?.isRestored ?: false) }
    var selectedRestoreTime by remember(existingFault) { mutableLongStateOf(existingFault?.restoreTime ?: System.currentTimeMillis()) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) } // For the camera to write to

    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun pickDateTime(initialTime: Long, onTimeSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = initialTime }
        DatePickerDialog(context, { _, y, m, d ->
            cal.set(y, m, d)
            TimePickerDialog(context, { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, min)
                onTimeSelected(cal.timeInMillis)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
    // [CAMERA LAUNCHER]
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedImageUri = tempPhotoUri
        }
    }

    // [NEW] Gallery Launcher: Opens the phone's image picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedImageUri = uri // Updates the preview and prepares for upload
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(existingFault == null) "Log New Trip" else "Edit Trip Details") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (step == 1) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = { selectedVoltage = VoltageLevel.KV33; step = 2 }) { Text("33 kV") }
                        Button(onClick = { selectedVoltage = VoltageLevel.KV132; step = 2 }) { Text("132 kV") }
                    }
                } else if (step == 2) {
                    val feeders = viewModel.getFeedersByVoltage(selectedVoltage)
                    LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(300.dp)) {
                        items(feeders) { feeder -> OutlinedButton(onClick = { selectedFeeder = feeder.name; step = 3 }) { Text(feeder.name, fontSize = 12.sp) } }
                    }
                } else {
                    Text("Feeder: $selectedFeeder", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    OutlinedTextField(value = dateFormat.format(selectedTime), onValueChange = {}, readOnly = true, label = { Text("Trip Time") }, trailingIcon = { IconButton(onClick = { pickDateTime(selectedTime) { selectedTime = it } }) { Icon(Icons.Default.DateRange, null) } }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))

                    // --- NEW: PHASE SELECTION (R, Y, B, G) ---
                    Text("Affected Phases:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = pA, onClick = { pA = !pA }, label = { Text("R") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFE53935), selectedLabelColor = Color.White))
                        FilterChip(selected = pB, onClick = { pB = !pB }, label = { Text("Y") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFB300), selectedLabelColor = Color.White))
                        FilterChip(selected = pC, onClick = { pC = !pC }, label = { Text("B") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF1E88E5), selectedLabelColor = Color.White))
                        FilterChip(selected = pG, onClick = { pG = !pG }, label = { Text("G") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF43A047), selectedLabelColor = Color.White))
                    }
                    Spacer(Modifier.height(8.dp))

                    // RESTORE
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isRestored, onCheckedChange = { isRestored = it }); Text("Restored?", fontWeight = FontWeight.Bold) }
                    if (isRestored) {
                        OutlinedTextField(value = dateFormat.format(selectedRestoreTime), onValueChange = {}, readOnly = true, label = { Text("Charging Time") }, trailingIcon = { IconButton(onClick = { pickDateTime(selectedRestoreTime) { selectedRestoreTime = it } }) { Icon(Icons.Default.DateRange, null) } }, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Fault Type:", fontSize = 12.sp)
                    ScrollableTabRow(selectedTabIndex = FaultType.values().indexOf(selectedFaultType), edgePadding = 0.dp) {
                        FaultType.values().forEach { type -> Tab(selected = selectedFaultType == type, onClick = { selectedFaultType = type }, text = { Text(type.name) }) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = ia, onValueChange = { ia = it }, label = { Text("Ia") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = ib, onValueChange = { ib = it }, label = { Text("Ib") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = ic, onValueChange = { ic = it }, label = { Text("Ic") }, modifier = Modifier.weight(1f))
                    }
                    // ... inside LogFaultDialog ...

                    Text("Photo Evidence:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // Gap between buttons
                    ) {
                        // BUTTON 1: CAMERA (Existing)
                        OutlinedButton(
                            onClick = {
                                try {
                                    val file = context.createImageFile()
                                    val authority = "${context.packageName}.provider"
                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Camera Error: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f) // Takes 50% width
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Camera")
                        }

                        // BUTTON 2: GALLERY (New)
                        OutlinedButton(
                            onClick = {
                                // Launch the gallery picker for images only
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f) // Takes 50% width
                        ) {
                            // You can use a generic icon like Search or List since we don't have a Gallery icon imported
                            Icon(Icons.Default.List, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Gallery")
                        }


// ... Image Preview code follows here ...

                    }

                    // 2. Image Preview (If taken)
                    if (capturedImageUri != null) {
                        Spacer(Modifier.height(8.dp))
                        AsyncImage(
                            model = capturedImageUri,
                            contentDescription = "Evidence",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            if (step == 3) {
                Button(
                    onClick = {
                        onConfirm(existingFault?.id ?: "", selectedFeeder, selectedVoltage.name, selectedFaultType, ia, ib, ic, remarks, selectedTime, if(isRestored) selectedRestoreTime else null, isRestored, pA, pB, pC, pG,capturedImageUri)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) { Text(if(existingFault == null) "LOG TRIP" else "UPDATE") }
            }
        },
        dismissButton = { TextButton(onClick = { if (step > 1 && existingFault == null) step-- else onDismiss() }) { Text(if (step > 1 && existingFault == null) "Back" else "Cancel") } }
    )
}// ... (KEEP RestoreFaultDialog, ActiveFaultCard, HistoryFaultCard, SystemStatusCard, FaultTag, SectionHeader, FaultMonthPicker UNCHANGED)
// They are perfect. I am omitting them for brevity but DO NOT DELETE THEM.

@Composable
fun RestoreFaultDialog(onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var selectedTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun pickDateTime() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedTime }
        DatePickerDialog(context, { _, y, m, d ->
            cal.set(y, m, d)
            TimePickerDialog(context, { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                selectedTime = cal.timeInMillis
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore Feeder") },
        text = {
            Column {
                Text("When was the feeder charged?", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateFormat.format(selectedTime),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Restore Time") },
                    trailingIcon = { IconButton(onClick = { pickDateTime() }) { Icon(Icons.Default.DateRange, null) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedTime) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                Text("CONFIRM RESTORE")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ActiveFaultCard(fault: FaultLog, onRestoreClick: () -> Unit, onEdit: () -> Unit) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.fillMaxHeight().width(8.dp).background(Color(0xFFD32F2F)))
            Column(Modifier.padding(16.dp).weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(fault.feederName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (fault.imageUrl != null) {
                        Icon(Icons.Default.CheckCircle, "Has Photo", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(timeFormat.format(fault.tripTime), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FaultTag(fault.voltageLevel, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    FaultTag(fault.faultType.name, Color(0xFFFFEBEE), Color(0xFFC62828))
                }
                Spacer(Modifier.height(12.dp))
                Text("Current: Ia=${fault.currentIA}  Ib=${fault.currentIB}  Ic=${fault.currentIC}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // RESTORE BUTTON
            Column(
                modifier = Modifier.fillMaxHeight().clickable { onRestoreClick() }.background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Refresh, null, tint = Color(0xFF2E7D32))
                Text("FIX", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun HistoryFaultCard(fault: FaultLog, onDelete: () -> Unit, onEdit: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(50.dp)) {
                Text(dateFormat.format(fault.tripTime).split(" ")[0], fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(dateFormat.format(fault.tripTime).split(" ")[1].uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Box(Modifier.height(40.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(fault.feederName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("${fault.voltageLevel} • ${fault.faultType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                if(fault.isRestored && fault.restoreTime != null) {
                    Text("Restored: ${timeFormat.format(fault.restoreTime)}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                }
            }
            // EDIT & DELETE
            Column(horizontalAlignment = Alignment.End) {
                Text(timeFormat.format(fault.tripTime), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(8.dp))
                Row {
                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemStatusCard(activeCount: Int) {
    val isSafe = activeCount == 0
    val bgColor = if (isSafe) Color(0xFF2E7D32) else Color(0xFFB71C1C)
    val statusText = if (isSafe) "GRID SECURE" else "ATTENTION REQ."
    val subText = if (isSafe) "All Feeders Normal" else "$activeCount Active Trip(s)"

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSafe) 1.05f else 1.1f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse)
    )

    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = bgColor), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth().height(120.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("SYSTEM STATUS", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text(statusText, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text(subText, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp).scale(scale).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
                Icon(if (isSafe) Icons.Default.Check else Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun FaultTag(text: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(4.dp)) {
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(title.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.sp)
    }
}

@Composable
fun FaultMonthPicker(initialMillis: Long, onDismiss: () -> Unit, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = initialMillis
    var displayYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { displayYear-- }) { Icon(Icons.Default.ArrowBack, "Prev") }
            Text(displayYear.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            IconButton(onClick = { displayYear++ }) { Icon(Icons.Default.ArrowForward, "Next") }
        }},
        text = { LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(12) { index -> Button(onClick = { calendar.set(Calendar.YEAR, displayYear); calendar.set(Calendar.MONTH, index); onDateSelected(calendar.timeInMillis) }, colors = ButtonDefaults.buttonColors(containerColor = if(index == calendar.get(Calendar.MONTH) && displayYear == calendar.get(Calendar.YEAR)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, contentColor = if(index == calendar.get(Calendar.MONTH) && displayYear == calendar.get(Calendar.YEAR)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)) { Text(months[index]) } } } },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Put this at the very bottom of FaultScreen.kt
