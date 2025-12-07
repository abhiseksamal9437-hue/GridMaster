package com.example.gridmaster.ui
import android.net.Uri
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gridmaster.GridMasterApplication
import com.example.gridmaster.data.FaultLog
import com.example.gridmaster.data.FaultType
import com.example.gridmaster.data.Feeder
import com.example.gridmaster.data.FirestoreRepository
import com.example.gridmaster.data.VoltageLevel
import com.example.gridmaster.data.feederList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FaultViewModel(private val repository: FirestoreRepository) : ViewModel() {

    // 1. RAW DATA (Live from Cloud)
    private val rawFaults = repository.getFaults()

    val allFaultHistory: StateFlow<List<FaultLog>> = rawFaults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. ACTIVE FAULTS (Live Filter)
    // Logic: Only show items where isRestored == false
    val activeFaults: StateFlow<List<FaultLog>> = allFaultHistory.map { list ->
        list.filter { !it.isRestored }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. MONTH FILTER STATE
    val currentMonth = MutableStateFlow(System.currentTimeMillis())

    // 4. FILTERED HISTORY (For the List)
    val filteredHistory: StateFlow<List<FaultLog>> = combine(
        allFaultHistory,
        currentMonth
    ) { faults, monthMillis ->
        val cal = Calendar.getInstance()
        cal.timeInMillis = monthMillis
        val targetMonth = cal.get(Calendar.MONTH)
        val targetYear = cal.get(Calendar.YEAR)

        faults.filter { fault ->
            // Rule 1: Must be Restored (History)
            if (!fault.isRestored) return@filter false

            // Rule 2: Must match selected Month & Year
            val faultCal = Calendar.getInstance().apply { timeInMillis = fault.tripTime }
            faultCal.get(Calendar.MONTH) == targetMonth && faultCal.get(Calendar.YEAR) == targetYear
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMonthFilter(millis: Long) { currentMonth.value = millis }

    fun getFeedersByVoltage(voltage: VoltageLevel): List<Feeder> {
        return feederList.filter { it.voltage == voltage }
    }

    // SAVE (Create New or Update Existing)
// 3. LOG / UPDATE FAULT (With Phase Support)
    // UPDATED: Accepts Phases (A, B, C) and Restoration Data
    fun saveFault(
        id: String = "",
        feederName: String,
        voltage: String,
        type: FaultType,
        // NEW: Phase Booleans
        phaseA: Boolean,
        phaseB: Boolean,
        phaseC: Boolean,
        phaseG: Boolean, // NEW // Saving Ground
        // Existing Params
        ia: String,
        ib: String,
        ic: String,
        remarks: String,
        tripTime: Long,
        // NEW: Restore Details
        restoreTime: Long? = null,
        isRestored: Boolean = false,
        localImageUri: Uri? = null
    ) {
        viewModelScope.launch {
            var finalImageUrl: String? = null
            if (localImageUri != null) {
                finalImageUrl = repository.uploadFaultImage(localImageUri)
            }
            val fault = FaultLog(
                id = id,
                feederName = feederName,
                voltageLevel = voltage,
                faultType = type,
                // Save Phases
                phaseA = phaseA,
                phaseB = phaseB,
                phaseC = phaseC,
                // Save Readings
                currentIA = ia,
                currentIB = ib,
                currentIC = ic,
                tripTime = tripTime,
                restoreTime = restoreTime,
                isRestored = isRestored,
                remarks = remarks,
                imageUrl = finalImageUrl // Save the link!
            )

            if (id.isEmpty()) {
                repository.addFault(fault)
            } else {
                repository.updateFault(fault)
            }
        }
    }  // FIX: RESTORE FUNCTION
    // This explicitly sets isRestored = true so it moves to History
    fun restoreFault(fault: FaultLog, restoreTime: Long) {
        viewModelScope.launch {
            val restoredFault = fault.copy(
                isRestored = true, // <--- FORCE TRUE
                restoreTime = restoreTime
            )
            repository.updateFault(restoredFault)
        }
    }

    fun deleteFault(id: String) {
        viewModelScope.launch { repository.deleteFault(id) }
    }

    // EXPORT (Downloads Excel)
    fun exportFaults(context: Context, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            // Export raw list filtered by date range
            val list = allFaultHistory.value.filter { it.tripTime in startDate..endDate }
            ExcelHelper.downloadFaults(context, list)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GridMasterApplication)
                FaultViewModel(FirestoreRepository())
            }
        }
    }
}