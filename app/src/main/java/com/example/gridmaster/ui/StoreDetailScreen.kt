package com.example.gridmaster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gridmaster.data.StoreItem
import com.example.gridmaster.data.StoreTransaction
import com.example.gridmaster.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    item: StoreItem,
    viewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory),
    onBack: () -> Unit,
    onTransactionClick: () -> Unit // [NEW] Callback for the FAB
) {
    val history by viewModel.getItemHistory(item.id).collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(item.legacyName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("SN: ${item.masterSn}", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        },
        // [NEW] The FAB is now implemented here
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onTransactionClick, // Trigger the callback
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- SECTION 1: LIVE STATUS ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Current Stock", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "${item.quantity} ${item.unit}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Unit Rate: ₹${item.unitRate}", fontSize = 14.sp)
                    Text("Total Value: ₹${String.format("%.0f", item.quantity * item.unitRate)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Divider()

            Text(
                "Transaction History",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // --- SECTION 2: TIMELINE LIST ---
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp) // Extra padding for FAB
            ) {
                if (history.isEmpty()) {
                    item {
                        Text(
                            "No movement recorded yet.",
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }

                items(history) { txn ->
                    TransactionRow(txn, dateFormat, item.unit)
                }

                item {
                    OpeningBalanceRow(item)
                }
            }
        }
    }
}

// ... (Keep TransactionRow and OpeningBalanceRow functions as they were) ...
// (I am not repeating them here to save space, but make sure they are still in the file!)
@Composable
fun TransactionRow(txn: StoreTransaction, formatter: SimpleDateFormat, unit: String) {
    val isIssue = txn.type == TransactionType.ISSUE
    val color = if (isIssue) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val icon = if (isIssue) Icons.Default.ArrowForward else Icons.Default.ArrowBack

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if (isIssue) "Issued to ${txn.reference}" else "Received from ${txn.reference}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(formatter.format(txn.date), fontSize = 12.sp, color = Color.Gray)
            if (txn.remarks.isNotEmpty()) Text("Note: ${txn.remarks}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
        Text("${if(isIssue) "-" else "+"}${txn.quantity} $unit", fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
    }
}

@Composable
fun OpeningBalanceRow(item: StoreItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).background(Color.LightGray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(Color.Gray, CircleShape))
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Opening Balance (October)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
            Text("Imported from Master Sheet", fontSize = 10.sp, color = Color.Gray)
        }
    }
}