package com.example.gridmaster.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gridmaster.GridMasterApplication
import com.example.gridmaster.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

// Data class for the list view
data class DutyEntry(
    val staffName: String,
    val role: String,
    val shift: ShiftType
)

class DutyViewModel(private val repository: FirestoreRepository) : ViewModel() {

    private val _dutyList = MutableStateFlow<List<DutyEntry>>(emptyList())
    val dutyList: StateFlow<List<DutyEntry>> = _dutyList.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Local cache of overrides from Cloud
    private var overrides: List<DutyOverride> = emptyList()

    init {
        // Start listening to cloud overrides
        viewModelScope.launch {
            repository.getDutyOverrides().collectLatest { cloudOverrides ->
                overrides = cloudOverrides
                // Recalculate whenever cloud data changes
                calculateDuty(_selectedDate.value)
            }
        }
    }

    fun calculateDuty(date: LocalDate) {
        val list = mutableListOf<DutyEntry>()

        // Helper to check for overrides
        fun getEffectiveShift(staff: StaffMember): ShiftType {
            val epoch = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val override = overrides.find { it.staffName == staff.name && it.dateEpoch == epoch }

            return if (override != null) {
                // Map DutyStatus to ShiftType for display
                when (override.status) {
                    DutyStatus.CL -> ShiftType.OFF // Or visual indicator for CL
                    DutyStatus.TR -> ShiftType.GEN
                    DutyStatus.EL -> ShiftType.OFF
                    DutyStatus.REST -> ShiftType.OFF
                    DutyStatus.NOL -> getShiftForDate(staff, date) // NOL shows as normal
                    DutyStatus.NORMAL -> getShiftForDate(staff, date)
                }
            } else {
                getShiftForDate(staff, date)
            }
        }

        // 1. Calculate for Guards
        securityGuards.forEach { staff ->
            list.add(DutyEntry(staff.name, "Security Guard", getEffectiveShift(staff)))
        }

        // 2. Calculate for Operators
        operators.forEach { staff ->
            list.add(DutyEntry(staff.name, "Operator", getEffectiveShift(staff)))
        }

        _dutyList.value = list
        _selectedDate.value = date
    }

    fun nextDay() = calculateDuty(_selectedDate.value.plusDays(1))
    fun prevDay() = calculateDuty(_selectedDate.value.minusDays(1))

    // --- CLOUD ACTIONS ---

    fun markLeave(staffName: String, date: LocalDate, status: DutyStatus) {
        viewModelScope.launch {
            val epoch = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.addDutyOverride(DutyOverride(staffName, epoch, status))
        }
    }

    fun revertToNormal(staffName: String, date: LocalDate) {
        viewModelScope.launch {
            val epoch = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.deleteDutyOverride(staffName, epoch)
        }
    }

    // --- EXPORT ---
    fun exportOperators(context: Context, start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            ExcelHelper.downloadDutyChart(context, start, end, operators, overrides)
        }
    }

    fun exportGuards(context: Context, start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            ExcelHelper.downloadDutyChart(context, start, end, securityGuards, overrides)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GridMasterApplication)
                // Use FirestoreRepository
                DutyViewModel(FirestoreRepository())
            }
        }
    }
}