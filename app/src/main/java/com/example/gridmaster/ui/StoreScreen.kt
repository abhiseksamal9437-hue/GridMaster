package com.example.gridmaster.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gridmaster.data.StoreItem
import com.example.gridmaster.data.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory),
    onOpenDrawer: () -> Unit
) {
    // --- 1. STATE MANAGEMENT ---
    val inventory by viewModel.inventory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<StoreItem?>(null) } // The item being acted upon

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Store Inventory",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Export Button (For MAS Excel)
                    IconButton(
                        onClick = { viewModel.exportMasReport(context) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.Share, "Export MAS", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- SEARCH BAR ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by Name, Nickname, or SN...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedItem = null // Null means "Add New"
                    showEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Material")
            }
        }
    ) { padding ->
        // --- 2. INVENTORY LIST ---
        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp), // Space for FAB
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (inventory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No items in store.\nTap + to add." else "No matches found.",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            items(inventory) { item ->
                StoreItemCard(
                    item = item,
                    onClick = {
                        // Tapping the card opens "Issue / Receive"
                        selectedItem = item
                        showTransactionDialog = true
                    },
                    onEditClick = {
                        // [NEW] Clicking the Pencil opens "Master Edit"
                        selectedItem = item
                        showEditDialog = true
                    }
                )
            }
        }
    }

    // --- 3. DIALOGS ---

    // A. TRANSACTION DIALOG (The Daily Driver)
    if (showTransactionDialog && selectedItem != null) {
        StoreTransactionDialog(
            item = selectedItem!!,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { type, qty, ref, remark ->
                viewModel.executeTransaction(
                    item = selectedItem!!,
                    type = type,
                    qtyStr = qty,
                    ref = ref,
                    remarks = remark,
                    onSuccess = {
                        Toast.makeText(context, "Transaction Logged!", Toast.LENGTH_SHORT).show()
                        showTransactionDialog = false
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // B. MASTER EDIT DIALOG (The Admin Tool)
    if (showEditDialog) {
        StoreEditDialog(
            item = selectedItem, // If null, dialog enters "Add New" mode
            onDismiss = { showEditDialog = false },
            onSave = { updatedItem ->
                if (selectedItem == null) {
                    // Creating New
                    viewModel.addNewItem(
                        legacyName = updatedItem.legacyName,
                        sapName = updatedItem.sapName,
                        nickname = updatedItem.nickname,
                        serialNumber = updatedItem.masterSn.toString(),
                        unit = updatedItem.unit,
                        rate = updatedItem.unitRate.toString(),
                        initialQty = updatedItem.quantity.toString()
                    )
                } else {
                    // Updating Existing
                    viewModel.updateItemDetails(updatedItem)
                }
                showEditDialog = false
                Toast.makeText(context, "Master Data Updated", Toast.LENGTH_SHORT).show()
            }
        )
    }
}