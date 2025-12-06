package com.example.gridmaster.ui

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas  // ← THIS WAS MISSING (fixes Canvas, drawCircle, drawArc, toPx)
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset  // ← Also add this for completeness (Offset in drawCircle)
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gridmaster.data.MaintenanceFreq
import com.example.gridmaster.data.ShiftType
import com.example.gridmaster.ui.components.AppDrawer
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: androidx.navigation.NavController,
    maintViewModel: MaintenanceViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val faultViewModel: FaultViewModel = viewModel(factory = FaultViewModel.Factory)
    val dutyViewModel: DutyViewModel = viewModel(factory = DutyViewModel.Factory)

    val activeFaults by faultViewModel.activeFaults.collectAsState()
    val tasks by maintViewModel.filteredTasks.collectAsState()
    val dutyList by dutyViewModel.dutyList.collectAsState()

    val dailyTasks = tasks.filter { it.frequency == MaintenanceFreq.DAILY }
    val completed = dailyTasks.count { it.isCompleted }
    val total = dailyTasks.size
    val progress = if (total > 0) completed.toFloat() / total else 0f

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val (currentShift, shiftLabel) = when (currentHour) {
        in 6..13 -> Pair(ShiftType.A, "Morning Shift (A)")
        in 14..21 -> Pair(ShiftType.B, "Evening Shift (B)")
        else -> Pair(ShiftType.C, "Night Shift (C)")
    }
    val staffOnDuty = dutyList.filter { it.shift == currentShift }

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val email = user?.email ?: "Engineer"
    val initial = if (email.isNotEmpty()) email.first().toString().uppercase() else "U"

    var showProfileDialog by remember { mutableStateOf(false) }
    var showChangePassDialog by remember { mutableStateOf(false) }
    val dateStr = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date())
    val context = LocalContext.current

    val headerGradient = if (isDarkTheme) {
        Brush.verticalGradient(colors = listOf(Color(0xFF1A237E), Color(0xFF121212)))
    } else {
        Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(headerGradient)
        ) {
            Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 56.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT: DRAWER MENU
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }

                    // RIGHT: THEME & PROFILE
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = Color.White
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { showProfileDialog = true }
                        ) {
                            Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text("Welcome back,", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Text(
                    text = email.split("@")[0].replaceFirstChar { it.uppercase() },
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(dateStr, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }

        // --- CONTENT ---
        Column(
            modifier = Modifier
                .offset(y = (-50).dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. GRID STATUS
            val isSafe = activeFaults.isEmpty()
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isSafe) Color(0xFF2E7D32) else Color(0xFFB71C1C)),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("faults") }
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("GRID STATUS", color = Color.White.copy(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(if (isSafe) "SECURE" else "ALERT", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Text(if (isSafe) "No Active Trips" else "${activeFaults.size} Feeder(s) Tripped", color = Color.White.copy(alpha = 0.9f))
                    }
                    Icon(if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }

            // 2. LIVE SHIFT
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("duty") }
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ON DUTY NOW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(shiftLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    if (staffOnDuty.isEmpty()) {
                        Text("No staff assigned", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                    } else {
                        staffOnDuty.forEach { staff ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (staff.role == "Operator") Color(0xFF1E88E5) else Color(0xFF43A047)))
                                Spacer(Modifier.width(8.dp))
                                Text(staff.staffName, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.weight(1f))
                                Text(if (staff.role == "Operator") "OP" else "SEC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // 3. DAILY PROGRESS
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("maintenance") }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DAILY TASKS", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("${(progress * 100).toInt()}% Done", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("$completed / $total Completed", color = MaterialTheme.colorScheme.secondary)
                    }
                    CircularProgressDash(progress, 64.dp, MaterialTheme.colorScheme.primary)
                }
            }

            // 4. ACTIONS
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardActionCard(
                    title = "Log Trip",
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFD32F2F),
                    cardColor = MaterialTheme.colorScheme.surface,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("faults") }
                )
                DashboardActionCard(
                    title = "Tools",
                    icon = Icons.Default.Build,
                    iconColor = Color(0xFF1976D2),
                    cardColor = MaterialTheme.colorScheme.surface,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("diagnostics") }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    // --- DIALOGS ---
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("User Profile", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text("Logged in as:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(email, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            showProfileDialog = false
                            showChangePassDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Password")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showProfileDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Out")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) { Text("Close") }
            }
        )
    }

    if (showChangePassDialog) {
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var isUpdating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangePassDialog = false },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPass == confirmPass && newPass.length >= 6) {
                            isUpdating = true
                            user?.updatePassword(newPass)?.addOnCompleteListener { task ->
                                isUpdating = false
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Password Updated!", Toast.LENGTH_SHORT).show()
                                    showChangePassDialog = false
                                } else {
                                    Toast.makeText(context, "Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    if (task.exception?.message?.contains("sensitive") == true) {
                                        Toast.makeText(context, "Please Log Out and Log In again to change password.", Toast.LENGTH_LONG).show()
                                        onLogout()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Passwords must match & be >6 chars", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isUpdating
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePassDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun CircularProgressDash(progress: Float, size: androidx.compose.ui.unit.Dp, color: Color) {
    val animatedProgress by animateFloatAsState(targetValue = progress)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = color.copy(alpha = 0.1f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}