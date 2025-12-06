package com.example.gridmaster.ui.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ==========================================
// 1. BREAKER WIZARD
// ==========================================
@Composable
fun ToolBreakerWizard(onBack: () -> Unit) {
    val problems = mapOf(
        "breaker_local_close" to "Not Closing (Local)",
        "breaker_remote_close" to "Not Closing (Remote)",
        "breaker_trip_fail" to "Fails to Trip (Stuck)",
        "breaker_ghost_trip" to "Trips without Fault",
        "breaker_coil_burn" to "Trip/Close Coil Burnt",
        "breaker_immediate_trip" to "Trips immediately on Close",
        "breaker_fuse_blow" to "Fuse Blown after Trip",
        "breaker_pd" to "Pole Discrepancy Trip"
    )

    var selectedKey by remember { mutableStateOf(problems.keys.first()) }

    ToolScreen(selectedKey, onBack) {
        Text(
            "Select Symptom:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface // <--- EXPLICIT DARK MODE COLOR
        )

        Column(Modifier.fillMaxWidth()) {
            problems.forEach { (key, name) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { selectedKey = key }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedKey == key),
                        onClick = { selectedKey = key },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface // <--- EXPLICIT DARK MODE COLOR
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        val info = toolKnowledgeMap[selectedKey]
        if (info != null) {
            Text("DIAGNOSIS & CHECKS:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(info.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(8.dp))

            val checklist = info.explanation.substringAfter("CHECKLIST:")
            Text(
                checklist.trim(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface // <--- EXPLICIT DARK MODE COLOR
            )
        }
    }
}

// ==========================================
// 2. ANNUNCIATION WIZARD
// ==========================================
@Composable
fun ToolAnnunciation(onBack: () -> Unit) {
    val problems = mapOf(
        "annun_fuse" to "Annunciation Fuse Blown",
        "annun_no_flash" to "Facia Not Flashing/Glowing",
        "annun_wrong_glow" to "Wrong Window Glowing",
        "annun_intermittent" to "Intermittent Glowing/Reset",
        "annun_buzzer_fuse" to "Buzzer Fuse Blown",
        "annun_bell_fail" to "Bell/Hooter/Buttons Fail"
    )
    var selectedKey by remember { mutableStateOf(problems.keys.first()) }

    ToolScreen(selectedKey, onBack) {
        Text("Select Alarm Issue:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

        Column(Modifier.fillMaxWidth()) {
            problems.forEach { (key, name) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { selectedKey = key }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (selectedKey == key), onClick = { selectedKey = key })
                    Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        val info = toolKnowledgeMap[selectedKey]
        if (info != null) {
            Text("TROUBLESHOOTING STEPS:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            val checklist = if (info.explanation.contains("CHECKLIST:"))
                info.explanation.substringAfter("CHECKLIST:")
            else info.explanation
            Text(checklist.trim(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ==========================================
// 3. METERING WIZARD
// ==========================================
@Composable
fun ToolMeteringWizard(onBack: () -> Unit) {
    val problems = mapOf(
        "meter_normal" to "Reference: Normal Condition",
        "meter_r_rev" to "R-Phase CT Reverse (Neg Power)",
        "meter_y_rev" to "Y-Phase CT Reverse",
        "meter_b_rev" to "B-Phase CT Reverse",
        "meter_ry_swap" to "R & Y CTs Swapped",
        "meter_rb_swap" to "R & B CTs Swapped",
        "meter_yb_swap" to "Y & B CTs Swapped"
    )
    var selectedKey by remember { mutableStateOf(problems.keys.first()) }

    ToolScreen(selectedKey, onBack) {
        Text("Select Terminal Readings:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

        Column(Modifier.fillMaxWidth()) {
            problems.forEach { (key, name) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { selectedKey = key }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (selectedKey == key), onClick = { selectedKey = key })
                    Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        val info = toolKnowledgeMap[selectedKey]
        if (info != null) {
            Text("ANALYSIS & CORRECTION:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            val checklist = if (info.explanation.contains("DIAGNOSIS:"))
                info.explanation.substringAfter("DIAGNOSIS:")
            else info.explanation
            Text(checklist.trim(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}