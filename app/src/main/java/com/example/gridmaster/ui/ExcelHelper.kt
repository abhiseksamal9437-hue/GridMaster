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
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import java.util.Calendar // [FIX] Added this import


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
            "Sl No.",
            "Feeder Name", // [NEW COLUMN HERE]
            "Trip Date", "Trip Time", "Restore Date", "Restore Time",
            "Duration", "Fault Type", "Affected Phase",
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
            // [NEW] Col 1: Feeder Name
            row.createCell(1).setCellValue(fault.feederName)

            // Col 2 & 3: Trip Date & Time
            row.createCell(2).setCellValue(dateFormat.format(tripDate))
            row.createCell(3).setCellValue(timeFormat.format(tripDate))

            // Col 4, 5 & 6: Restoration Details
            if (fault.isRestored && fault.restoreTime != null) {
                val restoreDate = Date(fault.restoreTime)
                val diffMillis = fault.restoreTime - fault.tripTime

                // Format Duration nicely (e.g., "1h 30m")
                val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
                val durationStr = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                row.createCell(4).setCellValue(dateFormat.format(restoreDate))
                row.createCell(5).setCellValue(timeFormat.format(restoreDate))
                row.createCell(6).setCellValue(durationStr)
            } else {
                row.createCell(4).setCellValue("-")
                row.createCell(5).setCellValue("-")
                row.createCell(6).setCellValue("Active")
            }

            // Col 7: Fault Type
            row.createCell(7).setCellValue(fault.faultType.name)

            // Col 8: Affected Phase (Logic to build string)
            // --- UPDATED PHASE LOGIC ---
            val phaseList = mutableListOf<String>()
            if (fault.phaseA) phaseList.add("R")
            if (fault.phaseB) phaseList.add("Y")
            if (fault.phaseC) phaseList.add("B")
            if (fault.phaseG) phaseList.add("G") // Added Ground

            val phaseStr = if (phaseList.isEmpty()) "-" else phaseList.joinToString("-") // e.g., "R-G"
            row.createCell(8).setCellValue(phaseStr)

            // Col 9, 10, 11: Currents
            row.createCell(9).setCellValue(fault.currentIA)
            row.createCell(10).setCellValue(fault.currentIB)
            row.createCell(11).setCellValue(fault.currentIC)

            // Col 12: Reason
            row.createCell(12).setCellValue(fault.reason)

            // Col 13: Remarks
            row.createCell(13).setCellValue(fault.remarks)
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
    // ... inside ExcelHelper object ...

    // ==========================================
    // 3. MAS STORE REPORT GENERATOR
    // ==========================================
    fun generateMasReport(
        context: Context,
        items: List<com.example.gridmaster.data.StoreItem>,
        transactions: List<com.example.gridmaster.data.StoreTransaction>,
        reportMonthMillis: Long
    ) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("MAS Report")

        // --- STYLES ---
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            wrapText = true
        }
        val font = workbook.createFont().apply { bold = true }
        headerStyle.setFont(font)

        val dataStyle = workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            verticalAlignment = VerticalAlignment.CENTER
        }

        // --- HEADER ROW (Row 0) ---
        // We match your specific 14-column layout
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "S.N.", "NAME OF MATERIALS", "UNIT", "O/B",
            "REC DATE", "REC REF", "REC QTY", // Receipt Group
            "ISS DATE", "ISS REF", "ISS QTY", // Issue Group
            "C/B", "UNIT RATE", "TOTAL VALUE", "REMARKS", "SAP DESC"
        )

        headers.forEachIndexed { i, title ->
            val cell = headerRow.createCell(i)
            cell.setCellValue(title)
            cell.cellStyle = headerStyle
            // Set basic column widths
            sheet.setColumnWidth(i, if(i==1 || i==14) 8000 else 3000)
        }

        // --- DATA PROCESSING ---
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = reportMonthMillis
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

        // Sort items by SortIndex (Excel Row Order)
        val sortedItems = items.sortedBy { it.sortIndex }

        sortedItems.forEachIndexed { index, item ->
            val row = sheet.createRow(index + 1)
            row.heightInPoints = 40f // Give some breathing room

            // 1. Find Transactions for THIS item in THIS month
            val itemTxns = transactions.filter { txn ->
                txn.itemId == item.id && isSameMonth(txn.date, targetMonth, targetYear)
            }

            val totalReceived = itemTxns.filter { it.type == com.example.gridmaster.data.TransactionType.RECEIVE }.sumOf { it.quantity }
            val totalIssued = itemTxns.filter { it.type == com.example.gridmaster.data.TransactionType.ISSUE }.sumOf { it.quantity }

            // 2. Calculate Opening Balance (O/B = C/B + Issued - Received)
            // Note: item.quantity is the CURRENT live stock (Closing Balance)
            val closingBalance = item.quantity
            val openingBalance = closingBalance + totalIssued - totalReceived

            // 3. Prepare Receipt/Issue Strings (Aggregate if multiple)
            val recTxns = itemTxns.filter { it.type == com.example.gridmaster.data.TransactionType.RECEIVE }
            val issTxns = itemTxns.filter { it.type == com.example.gridmaster.data.TransactionType.ISSUE }

            val recDate = recTxns.joinToString("\n") { dateFormat.format(it.date) }
            val recRef = recTxns.joinToString("\n") { it.reference }
            val recQty = if(totalReceived > 0) totalReceived.toString() else "-"

            val issDate = issTxns.joinToString("\n") { dateFormat.format(it.date) }
            val issRef = issTxns.joinToString("\n") { it.reference }
            val issQty = if(totalIssued > 0) totalIssued.toString() else "-"

            // 4. FILL CELLS
            createCell(row, 0, item.masterSn.toString(), dataStyle)
            createCell(row, 1, item.legacyName, dataStyle)
            createCell(row, 2, item.unit, dataStyle)
            createCell(row, 3, String.format("%.2f", openingBalance), dataStyle) // O/B

            // Receipt
            createCell(row, 4, recDate, dataStyle)
            createCell(row, 5, recRef, dataStyle)
            createCell(row, 6, recQty, dataStyle)

            // Issue
            createCell(row, 7, issDate, dataStyle)
            createCell(row, 8, issRef, dataStyle)
            createCell(row, 9, issQty, dataStyle)

            // Closing & Financials
            createCell(row, 10, String.format("%.2f", closingBalance), dataStyle) // C/B
            createCell(row, 11, item.unitRate.toString(), dataStyle)
            createCell(row, 12, String.format("%.2f", closingBalance * item.unitRate), dataStyle) // Rate

            // Remarks & SAP
            createCell(row, 13, item.nickname, dataStyle)
            createCell(row, 14, item.sapName, dataStyle)
        }

        // Generate Filename: "MAS_Report_Oct_2025.xlsx"
        val fileMonth = SimpleDateFormat("MMM_yyyy", Locale.getDefault()).format(reportMonthMillis)
        saveToDownloads(context, workbook, "MAS_Report_$fileMonth.xlsx")
    }

    // Helper for Clean Cell Creation
    private fun createCell(row: org.apache.poi.ss.usermodel.Row, index: Int, value: String, style: org.apache.poi.ss.usermodel.CellStyle) {
        val cell = row.createCell(index)
        cell.setCellValue(value)
        cell.cellStyle = style
    }

    private fun isSameMonth(dateMillis: Long, targetMonth: Int, targetYear: Int): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        return cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
    }
}