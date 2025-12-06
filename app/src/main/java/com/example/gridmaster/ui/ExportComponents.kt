package com.example.gridmaster.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// --- REUSABLE DATE RANGE EXPORT DIALOG ---
@Composable
fun DateRangeExportDialog(
    title: String = "Export Data",
    onDismiss: () -> Unit,
    onExport: (Long, Long) -> Unit
) {
    // Default: 1st of Month to Today
    val calendar = Calendar.getInstance()
    val today = calendar.timeInMillis
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = calendar.timeInMillis

    var startDate by remember { mutableStateOf(startOfMonth) }
    var endDate by remember { mutableStateOf(today) }

    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Date Picker Logic
    fun pickDate(current: Long, onSet: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = current }
        DatePickerDialog(context, { _, y, m, d ->
            cal.set(y, m, d)
            onSet(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Select Period:", fontSize = 14.sp, color = Color.Gray)

                // ROW FOR DATES
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // FROM BUTTON
                    OutlinedButton(
                        onClick = { pickDate(startDate) { startDate = it } },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FROM", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            Text(dateFormat.format(startDate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // TO BUTTON
                    OutlinedButton(
                        onClick = { pickDate(endDate) { endDate = it } },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TO", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            Text(dateFormat.format(endDate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onExport(startDate, endDate) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("DOWNLOAD EXCEL")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}