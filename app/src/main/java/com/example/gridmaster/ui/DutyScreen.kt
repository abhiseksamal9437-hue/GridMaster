package com.example.gridmaster.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gridmaster.data.DutyStatus
import com.example.gridmaster.data.ShiftType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyScreen(
    viewModel: DutyViewModel = viewModel(factory = DutyViewModel.Factory),
    onOpenDrawer: () -> Unit // <--- NEW
) {
    val dutyList by viewModel.dutyList.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }

    // --- NEW: Track which staff member is clicked ---
    var selectedStaffForEdit by remember { mutableStateOf<DutyEntry?>(null) }

    val context = LocalContext.current

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        cal.set(selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)
        DatePickerDialog(context, { _, y, m, d ->
            viewModel.calculateDuty(LocalDate.of(y, m + 1, d))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Duty Command", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    // --- DRAWER ICON ---
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    // DATE PICKER BUTTON
                    IconButton(onClick = { openDatePicker() }) {
                        Icon(Icons.Default.DateRange, "Date", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    // EXPORT BUTTON
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.background(Color(0xFFE3F2FD), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, "Export", tint = Color(0xFF1976D2))
                    }
                }
            )
        }
    ) { padding ->
        // ... (Keep existing content: DateStrip, LazyColumn, Dialogs) ...
        // Same logic as previous DutyScreen, just inside this new Scaffold structure.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            DateStrip(selectedDate) { newDate -> viewModel.calculateDuty(newDate) }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { DutySummaryCard(dutyList) }
                item { SectionTitleModern("Operators", Icons.Default.Settings) }
                items(dutyList.filter { it.role == "Operator" }) { entry -> ModernDutyCard(entry, onClick = { selectedStaffForEdit = entry }) }
                item { SectionTitleModern("Security Team", Icons.Default.Lock) }
                items(dutyList.filter { it.role == "Security Guard" }) { entry -> ModernDutyCard(entry, onClick = { selectedStaffForEdit = entry }) }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // --- MANAGE DUTY DIALOG (Revert / CL / Training) ---
    if (selectedStaffForEdit != null) {
        ManageDutyDialog(
            staffName = selectedStaffForEdit!!.staffName,
            currentDate = selectedDate,
            onDismiss = { selectedStaffForEdit = null },
            onMarkCL = {
                viewModel.markLeave(selectedStaffForEdit!!.staffName, selectedDate, DutyStatus.CL)
                selectedStaffForEdit = null
            },
            onMarkTraining = {
                viewModel.markLeave(selectedStaffForEdit!!.staffName, selectedDate, DutyStatus.TR)
                selectedStaffForEdit = null
            },
            onRevert = {
                viewModel.revertToNormal(selectedStaffForEdit!!.staffName, selectedDate)
                selectedStaffForEdit = null
            }
        )
    }

    // --- EXPORT DIALOG ---
    if (showExportDialog) {
        DutyExportDialog(
            onDismiss = { showExportDialog = false },
            onExportOperators = { start, end -> viewModel.exportOperators(context, start, end); showExportDialog = false },
            onExportGuards = { start, end -> viewModel.exportGuards(context, start, end); showExportDialog = false }
        )
    }
}

// ==========================================
// UI COMPONENTS
// ==========================================

@Composable
fun ManageDutyDialog(
    staffName: String,
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onMarkCL: () -> Unit,
    onMarkTraining: () -> Unit,
    onRevert: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Duty: $staffName") },
        text = { Text("Change status for $currentDate:") },
        confirmButton = {},
        dismissButton = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMarkCL, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) {
                    Text("Mark Casual Leave (CL)")
                }
                Button(onClick = onMarkTraining, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) {
                    Text("Mark Training (TR)")
                }
                OutlinedButton(onClick = onRevert, modifier = Modifier.fillMaxWidth()) {
                    Text("Revert to Normal Duty")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun ModernDutyCard(entry: DutyEntry, onClick: () -> Unit) {
    val (bgColor, icon) = when (entry.shift) {
        ShiftType.A -> Pair(Color(0xFFFFF8E1), Icons.Default.Star)
        ShiftType.B -> Pair(Color(0xFFFFF3E0), Icons.Default.Warning)
        ShiftType.C -> Pair(Color(0xFFE3F2FD), Icons.Default.Notifications)
        ShiftType.GEN -> Pair(Color(0xFFE0F2F1), Icons.Default.Settings)
        ShiftType.OFF -> Pair(Color(0xFFEEEEEE), Icons.Default.Home)
    }
    val textColor = if(entry.shift == ShiftType.OFF) Color.Gray else MaterialTheme.colorScheme.onSurface

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() } // CLICK ACTION
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                Text(text = entry.staffName.take(1), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.staffName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                Text(text = entry.role, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Surface(color = bgColor, shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                    Spacer(Modifier.width(6.dp))
                    Text(text = entry.shift.label, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DateStrip(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val dates = (-3..3).map { selectedDate.plusDays(it.toLong()) }
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(color)
                    .clickable { onDateSelected(date) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(date.format(DateTimeFormatter.ofPattern("EEE")).uppercase(), fontSize = 10.sp, color = textColor.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text(date.dayOfMonth.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
fun DutySummaryCard(list: List<DutyEntry>) {
    val shiftA = list.filter { it.shift == ShiftType.A }.joinToString { it.staffName.split(" ").first() }
    val shiftB = list.filter { it.shift == ShiftType.B }.joinToString { it.staffName.split(" ").first() }
    val shiftC = list.filter { it.shift == ShiftType.C }.joinToString { it.staffName.split(" ").first() }

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            Text("SHIFT OVERVIEW", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ShiftSummaryItem("Morning (A)", shiftA.ifEmpty { "-" }, Color(0xFFFFC107))
                ShiftSummaryItem("Evening (B)", shiftB.ifEmpty { "-" }, Color(0xFFFF9800))
                ShiftSummaryItem("Night (C)", shiftC.ifEmpty { "-" }, Color(0xFF90CAF9))
            }
        }
    }
}

@Composable
fun ShiftSummaryItem(label: String, names: String, dotColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(dotColor, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
        }
        Text(text = names, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
    }
}

@Composable
fun SectionTitleModern(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun DutyExportDialog(onDismiss: () -> Unit, onExportOperators: (LocalDate, LocalDate) -> Unit, onExportGuards: (LocalDate, LocalDate) -> Unit) {
    var startDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())) }
    val context = LocalContext.current
    fun showPicker(initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.set(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)
        DatePickerDialog(context, { _, y, m, d -> onDateSelected(LocalDate.of(y, m + 1, d)) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download Duty Roster") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select Date Range:", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showPicker(startDate) { startDate = it } }, modifier = Modifier.weight(1f)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("FROM", fontSize = 10.sp, color = Color.Gray); Text(startDate.toString(), fontWeight = FontWeight.Bold) } }
                    OutlinedButton(onClick = { showPicker(endDate) { endDate = it } }, modifier = Modifier.weight(1f)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("TO", fontSize = 10.sp, color = Color.Gray); Text(endDate.toString(), fontWeight = FontWeight.Bold) } }
                }
                Divider()
                Text("Select Team to Download:", fontWeight = FontWeight.Bold)
                Button(onClick = { onExportOperators(startDate, endDate) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) { Text("Download Operators Chart") }
                Button(onClick = { onExportGuards(startDate, endDate) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) { Text("Download Guards Chart") }
            }
        },
        confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}