package com.example.gridmaster.ui

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.gridmaster.data.DutyOverride
import com.example.gridmaster.data.DutyStatus
import com.example.gridmaster.data.FaultLog
import com.example.gridmaster.data.MaintenanceTask
import com.example.gridmaster.data.PlannedWork
import com.example.gridmaster.data.StaffMember
import com.example.gridmaster.data.getShiftForDate
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ExcelHelper {

    // ==========================================
    // 1. FAULT LOG EXPORT (UPDATED COLUMNS)
    // ==========================================
    fun downloadFaults(context: Context, faults: List<FaultLog>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Fault Log")

        // 1. HEADERS (Your Exact Requirement)
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "Sl No.", "Trip Date", "Trip Time", "Restore Date", "Restore Time",
            "Duration", "Fault Type", "Affected Phase", // Phase col
            "Ia (A)", "Ib (A)", "Ic (A)", "Reason", "Remarks"
        )

        // Style headers for better visibility (Optional polish)
        val boldStyle = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        boldStyle.setFont(font)

        headers.forEachIndexed { i, title ->
            val cell = headerRow.createCell(i)
            cell.setCellValue(title)
            cell.cellStyle = boldStyle
            sheet.setColumnWidth(i, 4000) // Make columns wider
        }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // 2. DATA ROWS
        faults.forEachIndexed { index, fault ->
            val row = sheet.createRow(index + 1)
            val tripDate = Date(fault.tripTime)

            // Col 1: Sl No.
            row.createCell(0).setCellValue((index + 1).toDouble())

            // Col 2 & 3: Trip Date & Time
            row.createCell(1).setCellValue(dateFormat.format(tripDate))
            row.createCell(2).setCellValue(timeFormat.format(tripDate))

            // Col 4, 5 & 6: Restoration Details
            if (fault.isRestored && fault.restoreTime != null) {
                val restoreDate = Date(fault.restoreTime)
                val diffMillis = fault.restoreTime - fault.tripTime

                // Format Duration nicely (e.g., "1h 30m")
                val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
                val durationStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                row.createCell(3).setCellValue(dateFormat.format(restoreDate))
                row.createCell(4).setCellValue(timeFormat.format(restoreDate))
                row.createCell(5).setCellValue(durationStr)
            } else {
                row.createCell(3).setCellValue("-")
                row.createCell(4).setCellValue("-")
                row.createCell(5).setCellValue("Active")
            }

            // Col 7: Fault Type
            row.createCell(6).setCellValue(fault.faultType.name)

            // Col 8: Affected Phase (Logic to build string)
            // --- UPDATED PHASE LOGIC ---
            val phaseList = mutableListOf<String>()
            if (fault.phaseA) phaseList.add("R")
            if (fault.phaseB) phaseList.add("Y")
            if (fault.phaseC) phaseList.add("B")
            if (fault.phaseG) phaseList.add("G") // Added Ground

            val phaseStr = if (phaseList.isEmpty()) "-" else phaseList.joinToString("-") // e.g., "R-G"
            row.createCell(7).setCellValue(phaseStr)

            // Col 9, 10, 11: Currents
            row.createCell(8).setCellValue(fault.currentIA)
            row.createCell(9).setCellValue(fault.currentIB)
            row.createCell(10).setCellValue(fault.currentIC)

            // Col 12: Reason
            row.createCell(11).setCellValue(fault.reason)

            // Col 13: Remarks
            row.createCell(12).setCellValue(fault.remarks)
        }

        saveToDownloads(context, workbook, "GridMaster_Faults_${System.currentTimeMillis()}.xlsx")
    }

    // ==========================================
    // 2. MAINTENANCE REPORT EXPORT
    // ==========================================
    fun downloadMaintenance(context: Context, tasks: List<MaintenanceTask>, notes: List<PlannedWork>) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Maintenance Report")

        val headerRow = sheet.createRow(0)
        val headers = listOf("Date", "Category", "Equipment", "Task / Description", "Status", "Notes")
        headers.forEachIndexed { i, title -> headerRow.createCell(i).setCellValue(title) }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        var rowIndex = 1

        // Part A: Routine Tasks
        tasks.forEach { task ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(if(task.completedDate != null) dateFormat.format(Date(task.completedDate!!)) else "-")
            row.createCell(1).setCellValue("Routine Check")
            row.createCell(2).setCellValue(task.equipment.name)
            row.createCell(3).setCellValue(task.taskDescription)
            row.createCell(4).setCellValue("COMPLETED")
            row.createCell(5).setCellValue(task.userNotes ?: "")
        }

        // Part B: Work Orders
        notes.forEach { note ->
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(dateFormat.format(Date(note.scheduledDate)))
            row.createCell(1).setCellValue("Work Order")
            row.createCell(2).setCellValue(note.equipmentType.name)
            row.createCell(3).setCellValue("${note.title}: ${note.description}")
            row.createCell(4).setCellValue(if (note.isCompleted) "COMPLETED" else "PENDING")
            row.createCell(5).setCellValue(note.priority.name)
        }
        saveToDownloads(context, workbook, "GridMaster_Maintenance_${System.currentTimeMillis()}.xlsx")
    }

    // ==========================================
    // 3. DUTY CHART EXPORT
    // ==========================================
    fun downloadDutyChart(
        context: Context,
        startDate: LocalDate,
        endDate: LocalDate,
        staffList: List<StaffMember>,
        overrides: List<DutyOverride>
    ) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Duty Roster")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("STAFF NAME")

        var date = startDate
        var colIndex = 1
        while (!date.isAfter(endDate)) {
            headerRow.createCell(colIndex++).setCellValue(date.toString())
            date = date.plusDays(1)
        }

        staffList.forEachIndexed { index, staff ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue("${staff.name} (${staff.role})")

            var currentDate = startDate
            var currentCol = 1
            while (!currentDate.isAfter(endDate)) {
                val epoch = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val override = overrides.find { it.staffName == staff.name && it.dateEpoch == epoch }
                val cellValue = if (override != null) {
                    when (override.status) {
                        DutyStatus.CL -> "CL"
                        DutyStatus.TR -> "TR"
                        DutyStatus.EL -> "EL"
                        DutyStatus.NOL -> getShiftForDate(staff, currentDate).label
                        else -> getShiftForDate(staff, currentDate).label
                    }
                } else {
                    getShiftForDate(staff, currentDate).label
                }
                row.createCell(currentCol++).setCellValue(cellValue)
                currentDate = currentDate.plusDays(1)
            }
        }
        saveToDownloads(context, workbook, "GridMaster_Duty_${System.currentTimeMillis()}.xlsx")
    }

    // --- INTERNAL SAVE FUNCTION ---
    private fun saveToDownloads(context: Context, workbook: XSSFWorkbook, fileName: String) {
        try {
            val out: OutputStream?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                out = uri?.let { resolver.openOutputStream(it) }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                out = FileOutputStream(file)
            }

            out?.use {
                workbook.write(it)
                workbook.close()
            }
            Toast.makeText(context, "Downloaded: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}