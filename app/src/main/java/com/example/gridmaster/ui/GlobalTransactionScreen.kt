package com.example.gridmaster.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalTransactionScreen(
    viewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory),
    onBack: () -> Unit
) {
    val transactions by viewModel.globalHistory.collectAsState()
    val currentMonth by viewModel.selectedMonth.collectAsState()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    // Month Picker Logic
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            viewModel.setHistoryMonth(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        1
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Store Register", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    // MONTH PICKER BUTTON
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(monthFormat.format(currentMonth))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Summary Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Transactions:", fontWeight = FontWeight.Bold)
                    Text("${transactions.size} Records", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // The List
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                if (transactions.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No transactions found for this month.", color = Color.Gray)
                        }
                    }
                }

                items(transactions) { txn ->
                    // Reusing the TransactionRow from StoreComponents/DetailScreen
                    // We need to pass the Item Name because this is a GLOBAL list
                    GlobalTransactionCard(txn, dateFormat)
                }
            }
        }
    }
}

@Composable
fun GlobalTransactionCard(txn: com.example.gridmaster.data.StoreTransaction, formatter: SimpleDateFormat) {
    val isIssue = txn.type == com.example.gridmaster.data.TransactionType.ISSUE
    val color = if (isIssue) Color(0xFFD32F2F) else Color(0xFF2E7D32)

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Item Name (Bold)
                Text(
                    text = txn.itemName, // [CRITICAL] Shows WHICH item was moved
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                // Qty Badge
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${if(isIssue) "-" else "+"}${txn.quantity}",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if(isIssue) "Issued to: ${txn.reference}" else "Received: ${txn.reference}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatter.format(txn.date),
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}