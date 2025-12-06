package com.example.gridmaster.data

import androidx.room.Entity // <--- THIS WAS MISSING
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// 1. Shift Types
enum class ShiftType(val label: String, val colorHex: Long) {
    A("A", 0xFFFFC107),      // Morning - Amber
    B("B", 0xFFFF9800),      // Evening - Orange
    C("C", 0xFF3F51B5),      // Night - Indigo
    GEN("G", 0xFF009688),    // General - Teal
    OFF("O", 0xFF9E9E9E)     // Off - Grey
}

// 2. Duty Status
enum class DutyStatus {
    NORMAL, CL, TR, EL, NOL, REST
}

// 3. Database Entity
@Entity(tableName = "duty_overrides", primaryKeys = ["staffName", "dateEpoch"])
data class DutyOverride(
    val staffName: String,
    val dateEpoch: Long,
    val status: DutyStatus
)

// 4. Staff Data Model
data class StaffMember(
    val name: String,
    val role: String,
    val patternStartDate: String,
    val pattern: List<ShiftType>
)

// 5. STAFF LISTS
val securityGuards = listOf(
    StaffMember("B. Sahoo", "Security Guard", "2025-09-01", listOf(ShiftType.B, ShiftType.B, ShiftType.A, ShiftType.A, ShiftType.OFF, ShiftType.C, ShiftType.C)),
    StaffMember("K.C. Swain", "Security Guard", "2025-09-01", listOf(ShiftType.A, ShiftType.A, ShiftType.OFF, ShiftType.C, ShiftType.C, ShiftType.B, ShiftType.B)),
    StaffMember("S.C. Mishra", "Security Guard", "2025-09-01", listOf(ShiftType.OFF, ShiftType.C, ShiftType.C, ShiftType.B, ShiftType.B, ShiftType.A, ShiftType.A)),
    StaffMember("P.K. Nayak", "Security Guard", "2025-09-01", listOf(ShiftType.C, ShiftType.C, ShiftType.B, ShiftType.C, ShiftType.A, ShiftType.OFF, ShiftType.C))
)

val operators = listOf(
    StaffMember("D. Malik", "Operator", "2025-08-21", listOf(ShiftType.A, ShiftType.OFF, ShiftType.GEN, ShiftType.C, ShiftType.GEN, ShiftType.B, ShiftType.GEN)),
    StaffMember("S. Bardhan", "Operator", "2025-08-21", listOf(ShiftType.C, ShiftType.B, ShiftType.B, ShiftType.A, ShiftType.A, ShiftType.OFF, ShiftType.C)),
    StaffMember("P.K. Das", "Operator", "2025-08-21", listOf(ShiftType.OFF, ShiftType.C, ShiftType.C, ShiftType.B, ShiftType.B, ShiftType.A, ShiftType.A)),
    StaffMember("H. Patra", "Operator", "2025-08-21", listOf(ShiftType.B, ShiftType.A, ShiftType.A, ShiftType.OFF, ShiftType.C, ShiftType.C, ShiftType.B))
)

// 6. LOGIC
fun getShiftForDate(staff: StaffMember, targetDate: LocalDate): ShiftType {
    val startDate = LocalDate.parse(staff.patternStartDate)
    val daysBetween = ChronoUnit.DAYS.between(startDate, targetDate)

    if (daysBetween < 0) {
        val cycleLength = staff.pattern.size
        val reverseIndex = (cycleLength + (daysBetween % cycleLength)) % cycleLength
        return staff.pattern[reverseIndex.toInt()]
    }

    val cycleLength = staff.pattern.size
    val index = (daysBetween % cycleLength).toInt()
    return staff.pattern[index]
}