package com.example.gridmaster.data

import com.google.firebase.firestore.PropertyName

// ==========================================
// 1. GRID SUBSTATION MODELS
// ==========================================
enum class VoltageLevel { KV33, KV132 }

data class Feeder(
    val id: String = "",
    val name: String = "",
    val voltage: VoltageLevel = VoltageLevel.KV33
)

val feederList = listOf(
    Feeder("f1", "Medical", VoltageLevel.KV33),
    Feeder("f2", "Adhanga", VoltageLevel.KV33),
    Feeder("f3", "SunduriMuhin", VoltageLevel.KV33),
    Feeder("f4", "Megalift", VoltageLevel.KV33),
    Feeder("f5", "Bari", VoltageLevel.KV33),
    Feeder("f6", "Mangalpur", VoltageLevel.KV33),
    Feeder("f7", "Jajpur Town", VoltageLevel.KV33),
    Feeder("f8", "Binjharpur", VoltageLevel.KV33),
    Feeder("f9", "33kV TRF-3", VoltageLevel.KV33),
    Feeder("f10", "33kV TRF-2", VoltageLevel.KV33),
    Feeder("f11", "33kV TRF-1", VoltageLevel.KV33),
    Feeder("f12", "Jajpur Road", VoltageLevel.KV132),
    Feeder("f13", "Bhadrak", VoltageLevel.KV132),
    Feeder("f14", "132kV TRF-1", VoltageLevel.KV132),
    Feeder("f15", "132kV TRF-2", VoltageLevel.KV132),
    Feeder("f16", "132kV TRF-3", VoltageLevel.KV132)
)

enum class FaultType {
    OC, EF, OC_EF, SOURCE_FAILURE,
    AR_YOUR_SIDE, AR_THEIR_SIDE, DIFFERENTIAL,
    LC, HT
}

data class FaultLog(
    val id: String = "",
    val feederName: String = "",
    val voltageLevel: String = "",
    val faultType: FaultType = FaultType.OC,

// UPDATED: Added Phase G
    val phaseA: Boolean = false, // R
    val phaseB: Boolean = false, // Y
    val phaseC: Boolean = false, // B
    val phaseG: Boolean = false, // Ground (NEW)
    val currentIA: String = "",
    val currentIB: String = "",
    val currentIC: String = "",

    val tripTime: Long = 0L,
    val restoreTime: Long? = null,

    // FIX: Force Firebase to map this correctly
    @get:PropertyName("isRestored")
    val isRestored: Boolean = false,

    val remarks: String = "",
    val reason: String = "",
    val userId: String = "",
    val userName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 2. MAINTENANCE MODELS
// ==========================================
enum class MaintenanceFreq { DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, SPECIAL }

enum class EquipmentType {
    TRANSFORMER, BATTERY, SWITCHYARD, FIRE_SYSTEM, EARTHING, GENERAL,
    TRANSMISSION_LINE, REACTOR, CIVIL_WORKS
}

data class MaintenanceTask(
    val id: String = "",
    val equipment: EquipmentType = EquipmentType.GENERAL,
    val frequency: MaintenanceFreq = MaintenanceFreq.DAILY,
    val taskDescription: String = "",

    // FIX: Force Firebase mapping
    @get:PropertyName("isCompleted")
    val isCompleted: Boolean = false,

    val completedDate: Long? = null,
    val userNotes: String? = null,
    val completedBy: String = "",
    val completedById: String = ""
)

// ==========================================
// 3. PLANNED WORK / NOTES
// ==========================================
enum class Priority { NORMAL, CRITICAL }

data class PlannedWork(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val equipmentType: EquipmentType = EquipmentType.GENERAL,
    val priority: Priority = Priority.NORMAL,
    val scheduledDate: Long = 0L,

    // FIX: Force Firebase mapping
    @get:PropertyName("isCompleted")
    val isCompleted: Boolean = false,

    val createdBy: String = "",
    val createdById: String = ""
)