package com.example.gridmaster.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gridmaster.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoreViewModel(private val repository: StoreRepository) : ViewModel() {

    // ... inside StoreViewModel ...

    // --- GLOBAL HISTORY STATES ---
    private val _selectedMonth = MutableStateFlow(System.currentTimeMillis())
    val selectedMonth = _selectedMonth.asStateFlow()

    // Filtered Global History
    val globalHistory: StateFlow<List<StoreTransaction>> = combine(
        repository.getAllTransactions(),
        _selectedMonth
    ) { transactions, monthMillis ->
        // logic to filter by selected month
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = monthMillis
        val targetMonth = calendar.get(java.util.Calendar.MONTH)
        val targetYear = calendar.get(java.util.Calendar.YEAR)

        transactions.filter { txn ->
            calendar.timeInMillis = txn.date
            val txnMonth = calendar.get(java.util.Calendar.MONTH)
            val txnYear = calendar.get(java.util.Calendar.YEAR)

            txnMonth == targetMonth && txnYear == targetYear
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHistoryMonth(millis: Long) {
        _selectedMonth.value = millis
    }

    // --- 1. SEARCH STATE ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        // [CRITICAL] This runs once when you open the Store tab
        checkAndSeedInitialData()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- 2. LIVE INVENTORY (Smart Filtered) ---
    val inventory: StateFlow<List<StoreItem>> = combine(
        repository.getStoreItems(),
        _searchQuery
    ) { items, query ->
        if (query.isBlank()) {
            items
        } else {
            val q = query.trim().lowercase()
            items.filter { item ->
                // "Universal Search": Checks Legacy, SAP, Nickname, and Serial No
                item.legacyName.lowercase().contains(q) ||
                        item.sapName.lowercase().contains(q) ||
                        item.nickname.lowercase().contains(q) ||
                        item.masterSn.toString() == q
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- 3. SEEDING LOGIC (The "One-Time" Setup) ---
// --- 3. SEEDING LOGIC ---
    private fun checkAndSeedInitialData() {
        viewModelScope.launch {
            val currentItems = repository.getStoreItems().first()

            if (currentItems.isEmpty()) {
                println("⚠️ Store is empty. Seeding initial data...")

                // [FIX] Use 'forEachIndexed' to capture the exact position
                initialStoreInventory.forEachIndexed { index, item ->
                    // Copy the item and add the sortIndex (0, 1, 2...)
                    val orderedItem = item.copy(sortIndex = index)
                    repository.addNewItem(orderedItem)
                }
            }
        }
    }

    // --- 4. TRANSACTION ACTIONS (Issue / Receive) ---
    fun executeTransaction(
        item: StoreItem,
        type: TransactionType,
        qtyStr: String,
        ref: String,
        remarks: String,
        date: Long, // [NEW PARAMETER]
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val qty = qtyStr.toDoubleOrNull()
            if (qty == null || qty <= 0) {
                onError("Please enter a valid quantity")
                return@launch
            }

            try {
                // Pass the date to the repository
                repository.executeTransaction(item, type, qty, ref, remarks, date)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Transaction Failed")
            }
        }
    }

    // --- 5. MASTER DATA MANAGEMENT ---

    // Edit Details (Fixing typos, setting nicknames)
    fun updateItemDetails(item: StoreItem) {
        viewModelScope.launch {
            repository.updateItemDetails(item)
        }
    }

    // Add New Item (For totally new spares arriving at the grid)
    fun addNewItem(
        legacyName: String,
        sapName: String,
        nickname: String,
        serialNumber: String,
        unit: String,
        rate: String,
        initialQty: String
    ) {
        viewModelScope.launch {
            val sn = serialNumber.toIntOrNull() ?: 0
            val r = rate.toDoubleOrNull() ?: 0.0
            val q = initialQty.toDoubleOrNull() ?: 0.0

            val newItem = StoreItem(
                legacyName = legacyName,
                sapName = sapName,
                nickname = nickname,
                masterSn = sn,
                unit = unit,
                unitRate = r,
                quantity = q,
                lastUpdated = System.currentTimeMillis()
            )
            repository.addNewItem(newItem)
        }
    }

    // --- 6. EXPORT LOGIC (Placeholder for Future Phase) ---


    // --- FACTORY ---
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StoreViewModel(StoreRepository())
            }
        }
    }
    // ... inside StoreViewModel ...
    fun getItemHistory(itemId: String): Flow<List<StoreTransaction>> {
        return repository.getItemHistory(itemId)
    }
    // ... inside StoreViewModel ...

    fun exportMasReport(context: Context) {
        viewModelScope.launch {
            // 1. Get All Items
            val items = repository.getStoreItems().first()

            // 2. Get All Transactions
            val allTransactions = repository.getAllTransactions().first()

            // 3. Generate Report for the CURRENT month
            // (You could pass a specific month, but usually you export the current status)
            ExcelHelper.generateMasReport(context, items, allTransactions, System.currentTimeMillis())

            android.widget.Toast.makeText(context, "MAS Report Downloaded!", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}