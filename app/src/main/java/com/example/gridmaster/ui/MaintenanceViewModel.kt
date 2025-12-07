package com.example.gridmaster.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gridmaster.GridMasterApplication
import com.example.gridmaster.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.net.Uri

class MaintenanceViewModel(
    application: Application,
    private val repository: FirestoreRepository // Uses Cloud Repository
) : AndroidViewModel(application) {

    // --- SESSION STATE ---
    // Remembers if the "Morning Briefing" popup has been shown this session
    var isBriefingSeen = false

    // 1. Install Date Logic (Smart Unlock)
    private val prefs = application.getSharedPreferences(GridMasterApplication.PREFS_NAME, Context.MODE_PRIVATE)
    private val installDate = prefs.getLong(GridMasterApplication.KEY_INSTALL_DATE, System.currentTimeMillis())
    private val daysSinceInstall = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - installDate)

    // Logic: Is this task "Due" based on app installation time?
    private fun isTaskDue(freq: MaintenanceFreq): Boolean {
        return when (freq) {
            MaintenanceFreq.DAILY -> true
            MaintenanceFreq.WEEKLY -> daysSinceInstall >= 7
            MaintenanceFreq.MONTHLY -> daysSinceInstall >= 30
            MaintenanceFreq.QUARTERLY -> daysSinceInstall >= 90
            MaintenanceFreq.YEARLY -> daysSinceInstall >= 365
            else -> true
        }
    }

    // --- FILTERS ---
    val selectedFreq = MutableStateFlow<MaintenanceFreq?>(MaintenanceFreq.DAILY)
    val selectedEquipment = MutableStateFlow<EquipmentType?>(null)

    // Month Filter (Defaults to Current Month) - Google Pay Style
    val currentMonth = MutableStateFlow(System.currentTimeMillis())

    // --- CLOUD DATA STREAMS ---

    // 1. Routine Tasks (Live from Cloud)
    val filteredTasks: StateFlow<List<MaintenanceTask>> = combine(
        repository.getTasks(),
        selectedFreq,
        selectedEquipment
    ) { tasks, freq, equip ->
        // SEEDING: If cloud is empty, upload the manual automatically
        if (tasks.isEmpty()) { seedCloudDatabase() }

        tasks.filter { task ->
            (freq == null || task.frequency == freq) &&
                    (equip == null || task.equipment == equip)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Work Orders (Live from Cloud -> Filtered by Month)
    val plannedNotes: StateFlow<List<PlannedWork>> = combine(
        repository.getNotes(),
        currentMonth
    ) { notes, monthMillis ->
        // Calculate target Month/Year from the picker
        val calSelected = Calendar.getInstance().apply { timeInMillis = monthMillis }
        val selMonth = calSelected.get(Calendar.MONTH)
        val selYear = calSelected.get(Calendar.YEAR)

        // Filter the notes
        notes.filter { note ->
            val calNote = Calendar.getInstance().apply { timeInMillis = note.scheduledDate }
            calNote.get(Calendar.MONTH) == selMonth && calNote.get(Calendar.YEAR) == selYear
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- POPUP LOGIC (Counts Pending items for Briefing) ---
    val pendingCount: StateFlow<Int> = combine(repository.getTasks(), repository.getNotes()) { tasks, notes ->
        val pendingTasks = tasks.count { !it.isCompleted && isTaskDue(it.frequency) }
        val pendingNotes = notes.count { !it.isCompleted }
        pendingTasks + pendingNotes
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- ACTIONS (All go through Repository for Cloud Sync) ---

    private fun seedCloudDatabase() {
        viewModelScope.launch {
            // detailedMaintenanceList comes from MaintenanceSeedData.kt
            repository.seedInitialTasks(detailedMaintenanceList)
        }
    }

    fun toggleTask(task: MaintenanceTask) {
        viewModelScope.launch {
            val date = if (!task.isCompleted) System.currentTimeMillis() else null
            // Repository will automatically stamp 'completedBy' and 'userId'
            repository.updateTask(task.copy(isCompleted = !task.isCompleted, completedDate = date))
        }
    }

    // SMART SAVE: Handles ADD (New) and EDIT (Update)
    fun saveNote(id: String, title: String, description: String, equipment: EquipmentType, priority: Priority, date: Long,imageUri: Uri? = null) {
        viewModelScope.launch {
            // 1. Upload Image (Reuse the repository function)
            var finalImageUrl: String? = null
            if (imageUri != null) {
                finalImageUrl = repository.uploadFaultImage(imageUri) // We can reuse the same upload function
            }
            val note = PlannedWork(
                id = id, // If empty string, Repository creates new. If valid string, it updates.
                title = title,
                description = description,
                equipmentType = equipment,
                priority = priority,
                scheduledDate = date,
                isCompleted = false,
                imageUrl = finalImageUrl
                // createdBy/userId is handled inside Repository

            )
            repository.saveNote(note)
        }
    }

    fun toggleNote(note: PlannedWork) {
        viewModelScope.launch { repository.saveNote(note.copy(isCompleted = !note.isCompleted)) }
    }

    fun deleteNote(note: PlannedWork) {
        viewModelScope.launch { repository.deleteNote(note.id) }
    }

    // Filter Helpers
    fun setMonthFilter(timeInMillis: Long) { currentMonth.value = timeInMillis }
    fun setFrequencyFilter(freq: MaintenanceFreq?) { selectedFreq.value = freq }
    fun setEquipmentFilter(equip: EquipmentType?) { selectedEquipment.value = equip }

    // --- EXPORT (Downloads Excel) ---
    fun exportReport(context: Context, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            // Get latest data snapshot
            val allTasks = repository.getTasks().first()
            val allNotes = repository.getNotes().first()

            // Filter completed items within date range
            val completedTasks = allTasks.filter {
                it.isCompleted && (it.completedDate ?: 0L) in startDate..endDate
            }
            val completedNotes = allNotes.filter {
                it.isCompleted && it.scheduledDate in startDate..endDate
            }

            // Generate File
            ExcelHelper.downloadMaintenance(context, completedTasks, completedNotes)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GridMasterApplication)
                // Inject FirestoreRepository instead of local DAO
                MaintenanceViewModel(app, FirestoreRepository())
            }
        }
    }
}