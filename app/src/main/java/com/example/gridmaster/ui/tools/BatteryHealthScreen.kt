package com.example.gridmaster.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryHealthScreen(onBack: () -> Unit) {
    var pilot1 by remember { mutableStateOf("") }
    var pilot2 by remember { mutableStateOf("") }
    var pilot3 by remember { mutableStateOf("") }
    var avgSg by remember { mutableStateOf("1.200") } // Default standard

    var resultMessage by remember { mutableStateOf("") }
    var resultColor by remember { mutableStateOf(Color.Gray) }

    fun analyzeBattery() {
        val p1 = pilot1.toDoubleOrNull()
        val p2 = pilot2.toDoubleOrNull()
        val p3 = pilot3.toDoubleOrNull()
        val average = avgSg.toDoubleOrNull()

        if (p1 == null || p2 == null || p3 == null || average == null) {
            resultMessage = "Please enter valid readings."
            resultColor = Color.Red
            return
        }

        // Logic from Vol-05: Check deviation > 0.030
        val maxDeviation = listOf(
            kotlin.math.abs(p1 - average),
            kotlin.math.abs(p2 - average),
            kotlin.math.abs(p3 - average)
        ).maxOrNull() ?: 0.0

        if (maxDeviation > 0.030) {
            resultMessage = "CRITICAL: Specific Gravity variation is ${String.format("%.3f", maxDeviation)}.\n\n" +
                    "Action Required: Start EQUALIZING CHARGE immediately as per Vol-05 Manual."
            resultColor = Color(0xFFB71C1C) // Red
        } else {
            resultMessage = "HEALTHY: Variation is within limits (${String.format("%.3f", maxDeviation)}).\n\n" +
                    "Continue Normal Float Charging."
            resultColor = Color(0xFF2E7D32) // Green
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Health Doctor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1565C0))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Enter Pilot Cell Specific Gravity (SG) to check if Equalizing Charge is needed.",
                        fontSize = 14.sp,
                        color = Color(0xFF1565C0)
                    )
                }
            }

            OutlinedTextField(
                value = avgSg,
                onValueChange = { avgSg = it },
                label = { Text("Target Average SG (e.g. 1.200)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Pilot Cell Readings:", fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = pilot1, onValueChange = { pilot1 = it },
                    label = { Text("Cell #1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pilot2, onValueChange = { pilot2 = it },
                    label = { Text("Cell #2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pilot3, onValueChange = { pilot3 = it },
                    label = { Text("Cell #3") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = { analyzeBattery() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ANALYZE HEALTH")
            }

            if (resultMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = resultColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = resultMessage,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

