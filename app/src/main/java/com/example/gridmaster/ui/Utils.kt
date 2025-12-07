package com.example.gridmaster.ui

import android.content.Context
import java.io.File

// This helper is now shared. Both FaultScreen and MaintenanceScreen can see it.
fun Context.createImageFile(): File {
    // Use raw timestamp to avoid Date formatting crashes
    val timeStamp = System.currentTimeMillis().toString()

    // Uses the internal cache, which is safe and requires no permissions
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        cacheDir
    )
}