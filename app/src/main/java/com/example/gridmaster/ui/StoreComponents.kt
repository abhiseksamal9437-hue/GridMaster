package com.example.gridmaster.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // [FIX] This was missing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search // [FIX] Added Search Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridmaster.data.StoreItem
import com.example.gridmaster.data.TransactionType

// ==========================================
// 1. THE "SMART ASSET" CARD
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StoreItemCard(
    item: StoreItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val mainName = if (item.nickname.isNotEmpty()) item.nickname else item.legacyName
    val subName = if (item.nickname.isNotEmpty()) item.legacyName else ""
    val isLowStock = item.quantity <= (item.minStock)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "SN: ${item.masterSn}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.Black
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = mainName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subName.isNotEmpty()) {
                        Text(
                            text = "($subName)",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (item.sapName.isNotEmpty()) {
                        Text(
                            text = "SAP: ${item.sapName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Stock:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%.2f", item.quantity)} ${item.unit}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLowStock && item.quantity < 5) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹ ${item.unitRate} / ${item.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Val: ₹ ${String.format("%.0f", item.quantity * item.unitRate)}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. TRANSACTION DIALOG
// ==========================================
@Composable
fun StoreTransactionDialog(
    item: StoreItem,
    onDismiss: () -> Unit,
    onConfirm: (TransactionType, String, String, String) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.ISSUE) }
    var qty by remember { mutableStateOf("") }
    var ref by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(if (type == TransactionType.ISSUE) "Issue Material" else "Receive Material")
                Text(item.legacyName, fontSize = 12.sp, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { type = TransactionType.ISSUE },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.ISSUE) Color(0xFFD32F2F) else Color.LightGray)
                    ) { Text("ISSUE (-)") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { type = TransactionType.RECEIVE },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.RECEIVE) Color(0xFF2E7D32) else Color.LightGray)
                    ) { Text("RECEIVE (+)") }
                }
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity (${item.unit})") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ref, onValueChange = { ref = it }, label = { Text(if (type == TransactionType.ISSUE) "Site / Indent No" else "Supplier / Challan No") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks (Optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(type, qty, ref, remarks) },
                colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.ISSUE) Color(0xFFD32F2F) else Color(0xFF2E7D32))
            ) { Text("CONFIRM") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ==========================================
// 3. MASTER EDIT DIALOG
// ==========================================
@Composable
fun StoreEditDialog(
    item: StoreItem?,
    onDismiss: () -> Unit,
    onSave: (StoreItem) -> Unit
) {
    var legacyName by remember { mutableStateOf(item?.legacyName ?: "") }
    var sapName by remember { mutableStateOf(item?.sapName ?: "") }
    var nickname by remember { mutableStateOf(item?.nickname ?: "") }
    var sn by remember { mutableStateOf(item?.masterSn?.toString() ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "No.") }
    var rate by remember { mutableStateOf(item?.unitRate?.toString() ?: "") }
    var qty by remember { mutableStateOf(item?.quantity?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add New Inventory" else "Edit Master Details") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = legacyName, onValueChange = { legacyName = it }, label = { Text("Legacy Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sapName, onValueChange = { sapName = it }, label = { Text("SAP Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Nickname") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = sn, onValueChange = { sn = it }, label = { Text("Master S.N.") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Rate") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    if (item == null) OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Initial Qty") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newItem = StoreItem(
                    id = item?.id ?: "",
                    legacyName = legacyName, sapName = sapName, nickname = nickname,
                    masterSn = sn.toIntOrNull() ?: 0, unit = unit,
                    unitRate = rate.toDoubleOrNull() ?: 0.0,
                    quantity = if (item == null) qty.toDoubleOrNull() ?: 0.0 else item.quantity
                )
                onSave(newItem)
            }) { Text("SAVE DATA") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ==========================================
// 4. MATERIAL PICKER DIALOG (Fixed)
// ==========================================
@Composable
fun MaterialPickerDialog(
    storeItems: List<StoreItem>,
    onDismiss: () -> Unit,
    onItemSelected: (StoreItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = if (searchQuery.isBlank()) storeItems else storeItems.filter {
        it.legacyName.contains(searchQuery, ignoreCase = true) ||
                it.nickname.contains(searchQuery, ignoreCase = true) ||
                it.sapName.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Material") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Store...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // [FIX] Explicitly type 'item' in the lambda
                    items(filteredItems) { item ->
                        Card(
                            onClick = { onItemSelected(item) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(text = if (item.nickname.isNotEmpty()) item.nickname else item.legacyName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Stock: ${item.quantity} ${item.unit}", fontSize = 12.sp, color = if (item.quantity < 5) Color.Red else Color.Gray)
                                }
                                Icon(Icons.Default.Add, null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}